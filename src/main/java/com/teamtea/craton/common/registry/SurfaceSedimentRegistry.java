package com.teamtea.craton.common.registry;

import com.teamtea.craton.Craton;
import com.teamtea.craton.api.geology.surface.SurfaceSediment;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import java.util.List;
import java.util.Optional;

public final class SurfaceSedimentRegistry {
    public static final ResourceKey<SurfaceSediment> RIVER_SAND = key("river_sand");
    public static final ResourceKey<SurfaceSediment> RIVER_CLAY = key("river_clay");
    public static final ResourceKey<SurfaceSediment> RIVER_RED_SAND_PLAINS = key("river_red_sand_plains");
    public static final ResourceKey<SurfaceSediment> RIVER_RED_SAND_HILLS = key("river_red_sand_hills");
    public static final ResourceKey<SurfaceSediment> RIVER_ARKOSE_SAND = key("river_arkose_sand");
    public static final ResourceKey<SurfaceSediment> RIVER_SHALE = key("river_shale");
    public static final ResourceKey<SurfaceSediment> RIVER_CARBONATE = key("river_carbonate");
    public static final ResourceKey<SurfaceSediment> RIVER_VOLCANIC = key("river_volcanic");
    public static final ResourceKey<SurfaceSediment> RIVER_METAMORPHIC = key("river_metamorphic");
    public static final ResourceKey<SurfaceSediment> RIVER_IGNEOUS = key("river_igneous");
    public static final ResourceKey<SurfaceSediment> RIVER_GRAVEL = key("river_gravel");
    public static final ResourceKey<SurfaceSediment> RIVER_DEFAULT = key("river_default");
    public static final ResourceKey<SurfaceSediment> OCEAN = key("ocean");
    public static final ResourceKey<SurfaceSediment> LAKE_SILT = key("lake_silt");
    public static final ResourceKey<SurfaceSediment> RIVER_COLD_CLIMATE = key("river_cold_climate");
    public static final ResourceKey<SurfaceSediment> RIVER_SHALLOW_COLD = key("river_shallow_cold");
    public static final ResourceKey<SurfaceSediment> RIVER_TEMPERATE_CLIMATE = key("river_temperate_climate");
    public static final ResourceKey<SurfaceSediment> RIVER_SHALLOW_TEMPERATE = key("river_shallow_temperate");
    public static final ResourceKey<SurfaceSediment> RIVER_TROPICAL_CLIMATE = key("river_tropical_climate");
    public static final ResourceKey<SurfaceSediment> RIVER_SHALLOW_TROPICAL = key("river_shallow_tropical");
    public static final ResourceKey<SurfaceSediment> RIVER_DRY_RED_SAND = key("river_dry_red_sand");
    public static final ResourceKey<SurfaceSediment> RIVER_SHALLOW_DRY_RED_SAND = key("river_shallow_dry_red_sand");
    private SurfaceSedimentRegistry() {}

    private static ResourceKey<SurfaceSediment> key(String name) {
        return ResourceKey.create(CratonRegistries.SURFACE_SEDIMENT, Craton.rl(name));
    }

    public static void bootstrap(BootstrapContext<SurfaceSediment> context) {
        var environments = context.lookup(CratonRegistries.WATER_ENVIRONMENT);
        var types = context.lookup(CratonRegistries.SURFACE_SEDIMENT_TYPE);
        var rocks = context.lookup(CratonRegistries.GEOLOGY_LAYER);
        var climates = context.lookup(CratonRegistries.GEOLOGY_REGION);
        var riverbed = types.getOrThrow(SurfaceSedimentTypeRegistry.RIVERBED);
        var alluvial = types.getOrThrow(SurfaceSedimentTypeRegistry.ALLUVIAL);
        var patch = types.getOrThrow(SurfaceSedimentTypeRegistry.DEPOSIT_PATCH);
        context.register(RIVER_SAND, new SurfaceSediment(riverbed,
                environments.getOrThrow(WaterEnvironmentRegistry.RIVER), Blocks.SAND,
                List.of(new SurfaceSediment.Secondary(Blocks.GRAVEL, alluvial)),
                List.of(rocks.getOrThrow(GeologyLayerRegistry.SANDSTONE), rocks.getOrThrow(GeologyLayerRegistry.GRANITE)),
                Optional.empty()));
        // Iron-bearing feldspathic and sandstone source rocks in genuinely arid regions.
        // Two records are used because climate is a single existing GeologyRegion reference.
        List<net.minecraft.core.Holder<com.teamtea.craton.api.geology.GeologyLayer>> redSandRocks = List.of(
                rocks.getOrThrow(GeologyLayerRegistry.ARKOSE_SANDSTONE),
                rocks.getOrThrow(GeologyLayerRegistry.SANDSTONE));
        context.register(RIVER_RED_SAND_PLAINS, new SurfaceSediment(riverbed,
                environments.getOrThrow(WaterEnvironmentRegistry.RIVER), Blocks.RED_SAND,
                List.of(new SurfaceSediment.Secondary(Blocks.GRAVEL, alluvial)),
                redSandRocks, Optional.of(climates.getOrThrow(GeologyRegionRegistry.ARID_PLAINS))));
        context.register(RIVER_RED_SAND_HILLS, new SurfaceSediment(riverbed,
                environments.getOrThrow(WaterEnvironmentRegistry.RIVER), Blocks.RED_SAND,
                List.of(new SurfaceSediment.Secondary(Blocks.GRAVEL, alluvial)),
                redSandRocks, Optional.of(climates.getOrThrow(GeologyRegionRegistry.ARID_HILLS))));
        context.register(RIVER_CLAY, new SurfaceSediment(riverbed,
                environments.getOrThrow(WaterEnvironmentRegistry.RIVER), Blocks.DIRT,
                List.of(new SurfaceSediment.Secondary(Blocks.CLAY, patch)),
                List.of(rocks.getOrThrow(GeologyLayerRegistry.SHALE)),
                Optional.of(climates.getOrThrow(GeologyRegionRegistry.WARM_HUMID))));
        context.register(RIVER_ARKOSE_SAND, new SurfaceSediment(riverbed,
                environments.getOrThrow(WaterEnvironmentRegistry.RIVER), Blocks.SAND,
                List.of(new SurfaceSediment.Secondary(Blocks.GRAVEL, alluvial)),
                List.of(rocks.getOrThrow(GeologyLayerRegistry.ARKOSE_SANDSTONE)), Optional.empty()));
        context.register(RIVER_SHALE, new SurfaceSediment(riverbed,
                environments.getOrThrow(WaterEnvironmentRegistry.RIVER), Blocks.DIRT,
                List.of(new SurfaceSediment.Secondary(Blocks.CLAY, patch)),
                List.of(rocks.getOrThrow(GeologyLayerRegistry.SHALE)), Optional.empty()));
        context.register(RIVER_CARBONATE, new SurfaceSediment(riverbed,
                environments.getOrThrow(WaterEnvironmentRegistry.RIVER), Blocks.GRAVEL,
                List.of(new SurfaceSediment.Secondary(Blocks.SAND, alluvial)),
                List.of(rocks.getOrThrow(GeologyLayerRegistry.LIMESTONE), rocks.getOrThrow(GeologyLayerRegistry.MARBLE)), Optional.empty()));
        context.register(RIVER_VOLCANIC, new SurfaceSediment(riverbed,
                environments.getOrThrow(WaterEnvironmentRegistry.RIVER), Blocks.GRAVEL,
                List.of(new SurfaceSediment.Secondary(Blocks.SAND, alluvial)),
                List.of(rocks.getOrThrow(GeologyLayerRegistry.BASALT), rocks.getOrThrow(GeologyLayerRegistry.GABBRO), rocks.getOrThrow(GeologyLayerRegistry.ANDESITE), rocks.getOrThrow(GeologyLayerRegistry.TUFF)), Optional.empty()));
        context.register(RIVER_METAMORPHIC, new SurfaceSediment(riverbed,
                environments.getOrThrow(WaterEnvironmentRegistry.RIVER), Blocks.GRAVEL,
                List.of(new SurfaceSediment.Secondary(Blocks.SAND, alluvial)),
                List.of(rocks.getOrThrow(GeologyLayerRegistry.GNEISS), rocks.getOrThrow(GeologyLayerRegistry.SCHIST), rocks.getOrThrow(GeologyLayerRegistry.ECLOGITE)), Optional.empty()));
        context.register(RIVER_IGNEOUS, new SurfaceSediment(riverbed,
                environments.getOrThrow(WaterEnvironmentRegistry.RIVER), Blocks.SAND,
                List.of(new SurfaceSediment.Secondary(Blocks.GRAVEL, alluvial)),
                List.of(rocks.getOrThrow(GeologyLayerRegistry.DIORITE), rocks.getOrThrow(GeologyLayerRegistry.RHYOLITE), rocks.getOrThrow(GeologyLayerRegistry.PEGMATITE)), Optional.empty()));
        context.register(RIVER_GRAVEL, new SurfaceSediment(riverbed,
                environments.getOrThrow(WaterEnvironmentRegistry.RIVER), Blocks.GRAVEL,
                List.of(),
                List.of(rocks.getOrThrow(GeologyLayerRegistry.GRAVEL)), Optional.empty()));
        context.register(RIVER_DEFAULT, new SurfaceSediment(riverbed,
                environments.getOrThrow(WaterEnvironmentRegistry.RIVER), Blocks.SAND,
                List.of(new SurfaceSediment.Secondary(Blocks.GRAVEL, alluvial)),
                List.of(), Optional.empty()));
        context.register(LAKE_SILT, new SurfaceSediment(riverbed,
                environments.getOrThrow(WaterEnvironmentRegistry.LAKE), Blocks.CLAY,
                List.of(new SurfaceSediment.Secondary(Blocks.GRAVEL, patch)),
                List.of(), Optional.empty()));
        context.register(OCEAN, new SurfaceSediment(types.getOrThrow(SurfaceSedimentTypeRegistry.SEABED),
                environments.getOrThrow(WaterEnvironmentRegistry.OCEAN), Blocks.GRAVEL,
                List.of(new SurfaceSediment.Secondary(Blocks.CLAY, patch),
                        new SurfaceSediment.Secondary(Blocks.SAND, alluvial)),
                List.of(), Optional.empty()));

        // Broad hot/dry river sediment is not gated on two specific sandstone layers.
        context.register(RIVER_DRY_RED_SAND, new SurfaceSediment(riverbed,
                environments.getOrThrow(WaterEnvironmentRegistry.RIVER_DRY), Blocks.RED_SAND,
                List.of(new SurfaceSediment.Secondary(Blocks.GRAVEL, patch)), List.of(), Optional.empty()));
        context.register(RIVER_SHALLOW_DRY_RED_SAND, new SurfaceSediment(alluvial,
                environments.getOrThrow(WaterEnvironmentRegistry.RIVER_SHALLOW_DRY), Blocks.RED_SAND,
                List.of(new SurfaceSediment.Secondary(Blocks.GRAVEL, patch)), List.of(), Optional.empty()));
        // Climate-dependent defaults apply when no more specific rock/climate profile wins.
        context.register(RIVER_COLD_CLIMATE, new SurfaceSediment(riverbed,
                environments.getOrThrow(WaterEnvironmentRegistry.RIVER_COLD), Blocks.GRAVEL,
                List.of(new SurfaceSediment.Secondary(Blocks.CLAY, patch)), List.of(), Optional.empty()));
        context.register(RIVER_SHALLOW_COLD, new SurfaceSediment(alluvial,
                environments.getOrThrow(WaterEnvironmentRegistry.RIVER_SHALLOW_COLD), Blocks.GRAVEL,
                List.of(new SurfaceSediment.Secondary(Blocks.SAND, patch)), List.of(), Optional.empty()));
        context.register(RIVER_TEMPERATE_CLIMATE, new SurfaceSediment(riverbed,
                environments.getOrThrow(WaterEnvironmentRegistry.RIVER_TEMPERATE), Blocks.SAND,
                List.of(new SurfaceSediment.Secondary(Blocks.GRAVEL, patch)), List.of(), Optional.empty()));
        context.register(RIVER_SHALLOW_TEMPERATE, new SurfaceSediment(alluvial,
                environments.getOrThrow(WaterEnvironmentRegistry.RIVER_SHALLOW_TEMPERATE), Blocks.SAND,
                List.of(new SurfaceSediment.Secondary(Blocks.CLAY, patch)), List.of(), Optional.empty()));
        context.register(RIVER_TROPICAL_CLIMATE, new SurfaceSediment(riverbed,
                environments.getOrThrow(WaterEnvironmentRegistry.RIVER_TROPICAL), Blocks.DIRT,
                List.of(new SurfaceSediment.Secondary(Blocks.CLAY, patch)), List.of(), Optional.empty()));
        context.register(RIVER_SHALLOW_TROPICAL, new SurfaceSediment(alluvial,
                environments.getOrThrow(WaterEnvironmentRegistry.RIVER_SHALLOW_TROPICAL), Blocks.SAND,
                List.of(new SurfaceSediment.Secondary(Blocks.CLAY, patch)), List.of(), Optional.empty()));
    }
}
