package com.teamtea.craton.common.registry;

import com.teamtea.craton.Craton;
import com.teamtea.craton.common.worldgen.MeteoriteFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

import java.util.List;

public final class MeteoriteFeatures {
    public static final ResourceKey<Feature> METEORITE = ResourceKey.create(Registries.FEATURE, Craton.rl("meteorite"));
    public static final ResourceKey<PlacedFeature> PLACED_METEORITE = ResourceKey.create(Registries.PLACED_FEATURE, Craton.rl("meteorite"));

    public static void bootstrap(BootstrapContext<Feature> context) {
        context.register(METEORITE, new MeteoriteFeature(context.lookup(CratonRegistries.ORE_TYPE).getOrThrow(OreTypeRegistry.METEORITIC_IRON),
                Blocks.BASALT.defaultBlockState(), 3, 6, .13f));
    }

    public static void bootstrapPlaced(BootstrapContext<PlacedFeature> context) {
        context.register(PLACED_METEORITE, new PlacedFeature(context.lookup(Registries.FEATURE).getOrThrow(METEORITE),
                List.of(RarityFilter.onAverageOnceEvery(128), InSquarePlacement.spread(), BiomeFilter.biome())));
    }
}
