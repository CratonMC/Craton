package com.teamtea.craton.api.geology.ore;

import com.mojang.serialization.Codec;
import com.teamtea.craton.common.registry.CratonRegistries;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Mineralization requirements belong to its data-pack definition, independently of loot. */
@EventBusSubscriber
public record OreType(List<Variant> variants, List<TagKey<Item>> requiredIngotTags) {
    public OreType {
        variants = List.copyOf(variants);
        requiredIngotTags = List.copyOf(requiredIngotTags);
    }

    public static final Codec<OreType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Variant.CODEC.listOf().fieldOf("variants").forGetter(OreType::variants),
            TagKey.codec(Registries.ITEM).listOf().optionalFieldOf("required_ingot_tags", List.of())
                    .forGetter(OreType::requiredIngotTags)
    ).apply(instance, OreType::new));

    private static volatile ConcurrentMap<TagKey<Item>, Boolean> ingotTags = new ConcurrentHashMap<>();

    private static volatile Map<Block, List<OreType>> byBlock = Map.of();

    public static boolean available(Block block) {
        var types = byBlock.get(block);
        if (types == null) return true;
        for (var type : types) if (type.available()) return true;
        return false;
    }

    public static boolean hasIngot(TagKey<Item> tag) {
        return ingotTags.computeIfAbsent(tag, key ->
                BuiltInRegistries.ITEM.listElements().anyMatch(holder -> holder.is(key)));
    }

    /** Empty requirements are unconditional; otherwise at least one tag must be nonempty. */
    public boolean available() {
        if (requiredIngotTags.isEmpty()) return true;
        for (var tag : requiredIngotTags) if (hasIngot(tag)) return true;
        return false;
    }

    public BlockState getOreState(BlockState host) {
        if (!available()) return host;
        Holder<Block> hostHolder = host.getBlock().builtInRegistryHolder();
        for (Variant variant : variants) {
            if (variant.hosts().contains(hostHolder)) return variant.result();
        }
        return host;
    }

    @SubscribeEvent public static void tagsUpdated(TagsUpdatedEvent event) {
        ingotTags = new ConcurrentHashMap<>();
        Map<Block, List<OreType>> index = new HashMap<>();
        event.getRegistries().lookup(CratonRegistries.ORE_TYPE).ifPresent(registry ->
                registry.listElements().forEach(holder -> {
                    OreType type = holder.value();
                    for (var variant : type.variants())
                        index.computeIfAbsent(variant.result().getBlock(), ignored -> new ArrayList<>()).add(type);
                }));
        index.replaceAll((block, types) -> List.copyOf(types));
        byBlock = Map.copyOf(index);
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) {
        ingotTags = new ConcurrentHashMap<>();
        byBlock = Map.of();
    }

    public record Variant(HolderSet<Block> hosts, BlockState result) {
        public static final Codec<Variant> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                RegistryCodecs.holderSet(Registries.BLOCK).fieldOf("hosts").forGetter(Variant::hosts),
                BlockState.CODEC.fieldOf("result").forGetter(Variant::result)
        ).apply(instance, Variant::new));
    }
}
