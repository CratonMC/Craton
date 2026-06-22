package com.teamtea.craton.common.registry;

import com.teamtea.craton.api.geology.GeologyLayer;
import com.teamtea.craton.api.geology.GeologyProfile;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;
import java.util.stream.Stream;

@EventBusSubscriber
public class CratonContents {

    @SubscribeEvent
    public static void onNewRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(CratonRegistries.GEOLOGY_LAYER, GeologyLayer.CODEC, GeologyLayer.CODEC);
        event.dataPackRegistry(CratonRegistries.GEOLOGY_PROFILE, GeologyProfile.CODEC, GeologyProfile.CODEC);
    }

    private static final List<Holder<GeologyProfile>> geologyProfileRegistry = new ArrayList<>();
    private static final Map<Holder<Biome>, Optional<Holder<GeologyProfile>>> bi = new HashMap<>();

    public static List<Holder<GeologyProfile>> getGeologyProfiles() {
        return geologyProfileRegistry;
    }

    public static Optional<Holder<GeologyProfile>> getGeologyProfile(Holder<Biome> surface) {
        var geologyProfileHolder = bi.get(surface);
        return geologyProfileHolder != null ? geologyProfileHolder : Optional.empty();
    }

    @SubscribeEvent
    public static void onNewRegistry(TagsUpdatedEvent.ServerDataLoad event) {
        geologyProfileRegistry.addAll(event.getRegistries()
                .lookup(CratonRegistries.GEOLOGY_PROFILE)
                .map(Registry::listElements)
                .map(Stream::toList)
                .orElse(List.of()));
        for (Holder<GeologyProfile> geologyProfile : getGeologyProfiles()) {
            for (Holder<Biome> biome : geologyProfile.value().biomes()) {
                bi.put(biome, Optional.of(geologyProfile));
            }
        }
    }

    @SubscribeEvent
    public static void onNewRegistry(ServerStoppedEvent event) {
        geologyProfileRegistry.clear();
        bi.clear();
    }

}
