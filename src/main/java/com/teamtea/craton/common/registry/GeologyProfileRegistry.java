package com.teamtea.craton.common.registry;

import com.teamtea.craton.Craton;
import com.teamtea.craton.api.geology.GeologyLayer;
import com.teamtea.craton.api.geology.GeologyProfile;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

import java.util.List;


import net.minecraft.core.Holder;
import net.neoforged.neoforge.common.Tags;

import java.util.stream.Stream;

public final class GeologyProfileRegistry {
    public static final ResourceKey<GeologyProfile> PLAINS = createKey("plains");
    public static final ResourceKey<GeologyProfile> SNOWY_PLAINS = createKey("snowy_plains");
    public static final ResourceKey<GeologyProfile> SWAMP = createKey("swamp");
    public static final ResourceKey<GeologyProfile> RIVER = createKey("river");
    public static final ResourceKey<GeologyProfile> BEACH = createKey("beach");

    public static final ResourceKey<GeologyProfile> JUNGLE = createKey("jungle");
    public static final ResourceKey<GeologyProfile> FOREST = createKey("forest");
    public static final ResourceKey<GeologyProfile> TAIGA = createKey("taiga");

    public static final ResourceKey<GeologyProfile> HILLS = createKey("hills");
    public static final ResourceKey<GeologyProfile> MOUNTAINS = createKey("mountains");

    public static final ResourceKey<GeologyProfile> DESERT = createKey("desert");
    public static final ResourceKey<GeologyProfile> SAVANNA = createKey("savanna");

    public static final ResourceKey<GeologyProfile> OCEAN = createKey("ocean");

    private GeologyProfileRegistry() {
    }

    private static ResourceKey<GeologyProfile> createKey(String name) {
        return ResourceKey.create(CratonRegistries.GEOLOGY_PROFILE, Craton.rl(name));
    }

    public static void bootstrap(BootstrapContext<GeologyProfile> context) {
        var biomeGetter = context.lookup(Registries.BIOME);
        var layerGetter = context.lookup(CratonRegistries.GEOLOGY_LAYER);

        List<Holder<GeologyLayer>> common = layers(layerGetter,
                GeologyLayerRegistry.LIMESTONE,
                GeologyLayerRegistry.STONE,
                GeologyLayerRegistry.TUFF,
                GeologyLayerRegistry.ANDESITE,
                GeologyLayerRegistry.GNEISS,
                GeologyLayerRegistry.DEEPSLATE
        );

        context.register(PLAINS, new GeologyProfile(
                biomeGetter.getOrThrow(Tags.Biomes.IS_PLAINS),
                common
        ));

        context.register(RIVER, new GeologyProfile(
                biomeGetter.getOrThrow(Tags.Biomes.IS_RIVER),
                common
        ));

        context.register(SWAMP, new GeologyProfile(
                biomeGetter.getOrThrow(Tags.Biomes.IS_SWAMP),
                common
        ));

        context.register(BEACH, new GeologyProfile(
                biomeGetter.getOrThrow(Tags.Biomes.IS_BEACH),
                common
        ));

        context.register(SNOWY_PLAINS, new GeologyProfile(
                biomeGetter.getOrThrow(Tags.Biomes.IS_SNOWY_PLAINS),
                common
        ));

        List<Holder<GeologyLayer>> foresetGroup = layers(layerGetter,
                GeologyLayerRegistry.STONE,
                GeologyLayerRegistry.LIMESTONE,
                GeologyLayerRegistry.ANDESITE,
                GeologyLayerRegistry.GNEISS,
                GeologyLayerRegistry.GRANITE,
                GeologyLayerRegistry.DEEPSLATE
        );

        context.register(FOREST, new GeologyProfile(
                biomeGetter.getOrThrow(Tags.Biomes.IS_FOREST),
                foresetGroup
        ));
        context.register(TAIGA, new GeologyProfile(
                biomeGetter.getOrThrow(Tags.Biomes.IS_TAIGA),
                foresetGroup
        ));
        context.register(JUNGLE, new GeologyProfile(
                biomeGetter.getOrThrow(Tags.Biomes.IS_JUNGLE),
                foresetGroup
        ));

        context.register(HILLS, new GeologyProfile(
                biomeGetter.getOrThrow(Tags.Biomes.IS_HILL),
                layers(layerGetter,
                        GeologyLayerRegistry.ANDESITE,
                        GeologyLayerRegistry.STONE,
                        GeologyLayerRegistry.GRANITE,
                        GeologyLayerRegistry.GNEISS,
                        GeologyLayerRegistry.GABBRO,
                        GeologyLayerRegistry.DEEPSLATE
                )
        ));

        context.register(MOUNTAINS, new GeologyProfile(
                biomeGetter.getOrThrow(Tags.Biomes.IS_MOUNTAIN),
                layers(layerGetter,
                        GeologyLayerRegistry.GNEISS,
                        GeologyLayerRegistry.GRANITE,
                        GeologyLayerRegistry.PEGMATITE,
                        GeologyLayerRegistry.GABBRO,
                        GeologyLayerRegistry.RHYOLITE,
                        GeologyLayerRegistry.DEEPSLATE
                )
        ));

        context.register(DESERT, new GeologyProfile(
                biomeGetter.getOrThrow(Tags.Biomes.IS_DESERT),
                layers(layerGetter,
                        GeologyLayerRegistry.LIMESTONE,
                        GeologyLayerRegistry.GRANITE,
                        GeologyLayerRegistry.STONE,
                        GeologyLayerRegistry.GABBRO,
                        GeologyLayerRegistry.GNEISS,
                        GeologyLayerRegistry.DEEPSLATE
                )
        ));

        context.register(SAVANNA, new GeologyProfile(
                biomeGetter.getOrThrow(Tags.Biomes.IS_SAVANNA),
                layers(layerGetter,
                        GeologyLayerRegistry.GRANITE,
                        GeologyLayerRegistry.RHYOLITE,
                        GeologyLayerRegistry.ANDESITE,
                        GeologyLayerRegistry.GABBRO,
                        GeologyLayerRegistry.BASALT,
                        GeologyLayerRegistry.DEEPSLATE
                )
        ));

        context.register(OCEAN, new GeologyProfile(
                biomeGetter.getOrThrow(Tags.Biomes.IS_OCEAN),
                layers(layerGetter,
                        GeologyLayerRegistry.BASALT,
                        GeologyLayerRegistry.TUFF,
                        GeologyLayerRegistry.GABBRO,
                        GeologyLayerRegistry.STONE,
                        GeologyLayerRegistry.ANDESITE,
                        GeologyLayerRegistry.DEEPSLATE
                )
        ));
    }

    @SafeVarargs
    private static List<Holder<GeologyLayer>> layers(
            net.minecraft.core.HolderGetter<GeologyLayer> getter,
            ResourceKey<GeologyLayer>... keys
    ) {
        return List.copyOf(Stream.of(keys)
                .map(getter::getOrThrow)
                .toList());
    }

}