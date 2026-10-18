package com.teamtea.craton.common.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamtea.craton.Craton;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import java.util.List;
import java.util.Optional;
import net.minecraft.tags.TagKey;

public class CratonDataMaps {
    /** Main products use weight; bonus products each use an independent chance. */
    public record Product(Item item, int weight, int minCount, int maxCount, double chance, boolean enabled, Optional<TagKey<Item>> requiredIngotTag) {
        public static final Codec<Product> CODEC = RecordCodecBuilder.<Product>create(instance -> instance.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(Product::item),
                Codec.intRange(1, 10000).optionalFieldOf("weight", 1).forGetter(Product::weight),
                Codec.intRange(1, 64).optionalFieldOf("min_count", 1).forGetter(Product::minCount),
                Codec.intRange(1, 64).optionalFieldOf("max_count", 1).forGetter(Product::maxCount),
                Codec.doubleRange(0, 1).optionalFieldOf("chance", 1.0).forGetter(Product::chance),
                Codec.BOOL.optionalFieldOf("enabled", true).forGetter(Product::enabled),
                TagKey.codec(Registries.ITEM).optionalFieldOf("required_ingot_tag").forGetter(Product::requiredIngotTag)
        ).apply(instance, Product::new)).validate(product -> product.minCount() <= product.maxCount()
                && Double.isFinite(product.chance())
                ? com.mojang.serialization.DataResult.success(product)
                : com.mojang.serialization.DataResult.error(() -> "Invalid ore product count or chance"));
    }

    /** Exactly one enabled main product, followed by any successful bonus rolls. */
    public record OreDrops(List<Product> drops, List<Product> bonusDrops) {
        public OreDrops { drops = List.copyOf(drops); bonusDrops = List.copyOf(bonusDrops); }
        public static final Codec<OreDrops> CODEC = RecordCodecBuilder.<OreDrops>create(instance -> instance.group(
                Product.CODEC.listOf().fieldOf("drops").forGetter(OreDrops::drops),
                Product.CODEC.listOf().optionalFieldOf("bonus_drops", List.of()).forGetter(OreDrops::bonusDrops)
        ).apply(instance, OreDrops::new)).validate(data -> data.drops().stream().mapToLong(Product::weight).sum() <= Integer.MAX_VALUE
                ? com.mojang.serialization.DataResult.success(data)
                : com.mojang.serialization.DataResult.error(() -> "Ore product total weight exceeds integer range"));
    }

    public static final DataMapType<Block, OreDrops> ORE_DROPS =
            DataMapType.builder(Craton.rl("ore_drops"), Registries.BLOCK, OreDrops.CODEC).synced(OreDrops.CODEC, false).build();
    public static void register(RegisterDataMapTypesEvent event) { event.register(ORE_DROPS); }
    private CratonDataMaps() {}
}
