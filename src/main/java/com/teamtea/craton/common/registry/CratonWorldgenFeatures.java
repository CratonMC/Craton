package com.teamtea.craton.common.registry;

import com.teamtea.craton.Craton;
import com.teamtea.craton.common.worldgen.MeteoriteFeature;
import com.teamtea.craton.common.worldgen.BadlandsRedSandPlacerFeature;
import com.teamtea.craton.common.worldgen.PlacerFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;

public class CratonWorldgenFeatures {
    public static final ResourceKey<Feature> METEORITE = ResourceKey.create(Registries.FEATURE, Craton.rl("meteorite"));
    public static final ResourceKey<PlacedFeature> PLACED_METEORITE = ResourceKey.create(Registries.PLACED_FEATURE, Craton.rl("meteorite"));

    public static final ResourceKey<Feature> PLACER = ResourceKey.create(Registries.FEATURE, Craton.rl("placer"));
    public static final ResourceKey<PlacedFeature> PLACED_PLACER = ResourceKey.create(Registries.PLACED_FEATURE, Craton.rl("placer"));

    public static final ResourceKey<Feature> BADLANDS_RED_SAND_PLACER = ResourceKey.create(Registries.FEATURE, Craton.rl("badlands_red_sand_placer"));
    public static final ResourceKey<PlacedFeature> PLACED_BADLANDS_RED_SAND_PLACER = ResourceKey.create(Registries.PLACED_FEATURE, Craton.rl("badlands_red_sand_placer"));

    public static void bootstrap(BootstrapContext<Feature> context) {
        context.register(BADLANDS_RED_SAND_PLACER, new BadlandsRedSandPlacerFeature());
        context.register(PLACER,new PlacerFeature(context.lookup(CratonRegistries.DEPOSIT).getOrThrow(DepositRegistry.PLACER)));
        context.register(METEORITE, new MeteoriteFeature(context.lookup(CratonRegistries.DEPOSIT).getOrThrow(DepositRegistry.METEORITE)));
    }

    public static void bootstrapPlaced(BootstrapContext<PlacedFeature> context) {
        context.register(PLACED_BADLANDS_RED_SAND_PLACER, new PlacedFeature(
                context.lookup(Registries.FEATURE).getOrThrow(BADLANDS_RED_SAND_PLACER), List.of()));
        // A mixed-biome chunk may contain a river away from its placement origin.
        context.register(PLACED_PLACER,new PlacedFeature(context.lookup(Registries.FEATURE).getOrThrow(PLACER),List.of()));
        context.register(PLACED_METEORITE, new PlacedFeature(context.lookup(Registries.FEATURE).getOrThrow(METEORITE),
                List.of(InSquarePlacement.spread(), BiomeFilter.biome())));
    }
}
