package com.teamtea.craton.common.registry;

import com.teamtea.craton.api.geology.GeologyLayer;
import com.teamtea.craton.api.geology.GeologyProfile;
import com.teamtea.craton.api.geology.deposit.Deposit;
import net.minecraft.resources.Identifier;
import com.teamtea.craton.api.geology.ore.OreType;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import java.util.*;
import java.util.stream.Stream;

@EventBusSubscriber
public class CratonContents {

    @SubscribeEvent
    public static void onNewRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(
                CratonRegistries.GEOLOGY_LAYER,
                GeologyLayer.CODEC,
                GeologyLayer.CODEC
        );

        event.dataPackRegistry(
                CratonRegistries.GEOLOGY_PROFILE,
                GeologyProfile.CODEC,
                GeologyProfile.CODEC
        );

        event.dataPackRegistry(
                CratonRegistries.DEPOSIT,
                Deposit.CODEC,
                Deposit.CODEC
        );

        event.dataPackRegistry(
                CratonRegistries.ORE_TYPE,
                OreType.CODEC,
                OreType.CODEC
        );
    }

    private static final List<Holder<GeologyProfile>> geologyProfileRegistry =
            new ArrayList<>();

    private static final List<Holder<Deposit>> depositRegistry =
            new ArrayList<>();

    private static final Map<ResourceKey<OreType>, Holder<OreType>> oreTypeRegistry =
            new HashMap<>();

    private static final Map<Holder<Biome>, Optional<Holder<GeologyProfile>>> bi =
            new HashMap<>();

    public static List<Holder<GeologyProfile>> getGeologyProfiles() {
        return geologyProfileRegistry;
    }

    private static volatile Map<Identifier,List<Holder<Deposit>>> depositsByType=Map.of();

    public static Map<Identifier,List<Holder<Deposit>>> getDepositsByType() { return depositsByType; }

    public static List<Holder<Deposit>> getDeposits() {
        return depositRegistry;
    }

    public static Optional<Holder<OreType>> getOreType(ResourceKey<OreType> key) {
        return Optional.ofNullable(oreTypeRegistry.get(key));
    }

    public static Optional<Holder<GeologyProfile>> getGeologyProfile(
            Holder<Biome> surface
    ) {
        var geologyProfileHolder = bi.get(surface);

        return geologyProfileHolder != null
                ? geologyProfileHolder
                : Optional.empty();
    }

    @SubscribeEvent
    public static void onNewRegistry(TagsUpdatedEvent.ServerDataLoad event) {
        geologyProfileRegistry.clear();
        depositRegistry.clear();
        depositsByType=Map.of();
        oreTypeRegistry.clear();
        bi.clear();

        geologyProfileRegistry.addAll(
                event.getRegistries()
                        .lookup(CratonRegistries.GEOLOGY_PROFILE)
                        .map(Registry::listElements)
                        .map(Stream::toList)
                        .orElse(List.of())
        );

        depositRegistry.addAll(
                event.getRegistries()
                        .lookup(CratonRegistries.DEPOSIT)
                        .map(Registry::listElements)
                        .map(Stream::toList)
                        .orElse(List.of())
        );

        Map<Identifier,List<Holder<Deposit>>> grouped=new HashMap<>();
        depositRegistry.sort(Comparator.comparing(entry -> entry.unwrapKey().orElseThrow().identifier().toString()));
        for(Holder<Deposit> entry:depositRegistry)
            grouped.computeIfAbsent(entry.value().getType(),key -> new ArrayList<>()).add(entry);
        grouped.replaceAll((type,entries) -> List.copyOf(entries));
        depositsByType=Map.copyOf(grouped);

        event.getRegistries()
                .lookup(CratonRegistries.ORE_TYPE)
                .ifPresent(registry -> registry.listElements().forEach(holder ->
                        holder.unwrapKey().ifPresent(key -> oreTypeRegistry.put(key, holder))
                ));

        for (Holder<GeologyProfile> geologyProfile : geologyProfileRegistry) {
            for (Holder<Biome> biome : geologyProfile.value().biomes()) {
                bi.put(
                        biome,
                        Optional.of(geologyProfile)
                );
            }
        }
    }

    @SubscribeEvent
    public static void onNewRegistry(ServerStoppedEvent event) {
        geologyProfileRegistry.clear();
        depositRegistry.clear();
        depositsByType=Map.of();
        oreTypeRegistry.clear();
        bi.clear();
    }
}
