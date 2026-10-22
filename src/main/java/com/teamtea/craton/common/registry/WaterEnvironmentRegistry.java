package com.teamtea.craton.common.registry;

import com.teamtea.craton.Craton;
import com.teamtea.craton.api.geology.GeologyRegion.Range;
import com.teamtea.craton.api.geology.surface.WaterEnvironment;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.material.Fluids;
import java.util.Optional;

/** Bootstrap defaults mirror the water_environments datapack JSON. */
public final class WaterEnvironmentRegistry {
    public static final ResourceKey<WaterEnvironment> RIVER = key("river");
    public static final ResourceKey<WaterEnvironment> OCEAN = key("ocean");
    public static final ResourceKey<WaterEnvironment> LAKE = key("lake");
    public static final ResourceKey<WaterEnvironment> RIVER_COLD = key("river_cold");
    public static final ResourceKey<WaterEnvironment> RIVER_SHALLOW_COLD = key("river_shallow_cold");
    public static final ResourceKey<WaterEnvironment> RIVER_TEMPERATE = key("river_temperate");
    public static final ResourceKey<WaterEnvironment> RIVER_SHALLOW_TEMPERATE = key("river_shallow_temperate");
    public static final ResourceKey<WaterEnvironment> RIVER_TROPICAL = key("river_tropical");
    public static final ResourceKey<WaterEnvironment> RIVER_SHALLOW_TROPICAL = key("river_shallow_tropical");
    public static final ResourceKey<WaterEnvironment> RIVER_DRY = key("river_dry");
    public static final ResourceKey<WaterEnvironment> RIVER_SHALLOW_DRY = key("river_shallow_dry");
    private WaterEnvironmentRegistry() {}
    private static ResourceKey<WaterEnvironment> key(String id) {
        return ResourceKey.create(CratonRegistries.WATER_ENVIRONMENT, Craton.rl(id));
    }
    public static void bootstrap(BootstrapContext<WaterEnvironment> context) {
        context.register(RIVER, new WaterEnvironment("", Optional.of(Fluids.WATER),
                Optional.of(Range.atLeast(-0.19F)), Optional.of(Range.below(-0.65F)), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(8)));
        context.register(OCEAN, new WaterEnvironment("", Optional.of(Fluids.WATER),
                Optional.of(Range.below(-0.19F)), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
        context.register(LAKE, new WaterEnvironment("", Optional.of(Fluids.WATER),
                Optional.of(Range.atLeast(-0.11F)), Optional.of(Range.atLeast(-0.65F)), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));

        // Temperature is a generic environmental range, not a geological region or enum.
        context.register(RIVER_COLD, new WaterEnvironment("", Optional.of(Fluids.WATER),
                Optional.of(Range.atLeast(-0.19F)), Optional.of(Range.below(-0.65F)),
                Optional.of(Range.below(-0.35F)), Optional.empty(), Optional.empty(), Optional.of(8)));
        context.register(RIVER_SHALLOW_COLD, new WaterEnvironment("", Optional.of(Fluids.WATER),
                Optional.of(Range.atLeast(-0.19F)), Optional.of(Range.below(-0.65F)),
                Optional.of(Range.below(-0.35F)), Optional.empty(), Optional.empty(), Optional.of(2)));
        context.register(RIVER_TEMPERATE, new WaterEnvironment("", Optional.of(Fluids.WATER),
                Optional.of(Range.atLeast(-0.19F)), Optional.of(Range.below(-0.65F)),
                Optional.of(new Range(Optional.of(-0.35F), Optional.of(0.35F), true, false)), Optional.empty(), Optional.empty(), Optional.of(8)));
        context.register(RIVER_SHALLOW_TEMPERATE, new WaterEnvironment("", Optional.of(Fluids.WATER),
                Optional.of(Range.atLeast(-0.19F)), Optional.of(Range.below(-0.65F)),
                Optional.of(new Range(Optional.of(-0.35F), Optional.of(0.35F), true, false)), Optional.empty(), Optional.empty(), Optional.of(2)));
        context.register(RIVER_TROPICAL, new WaterEnvironment("", Optional.of(Fluids.WATER),
                Optional.of(Range.atLeast(-0.19F)), Optional.of(Range.below(-0.65F)),
                Optional.of(Range.atLeast(0.35F)), Optional.empty(), Optional.empty(), Optional.of(8)));
        context.register(RIVER_SHALLOW_TROPICAL, new WaterEnvironment("", Optional.of(Fluids.WATER),
                Optional.of(Range.atLeast(-0.19F)), Optional.of(Range.below(-0.65F)),
                Optional.of(Range.atLeast(0.35F)), Optional.empty(), Optional.empty(), Optional.of(2)));
        // Hot, dry river beds: cover badlands river floors even where sampled host rock
        // is not arkose/sandstone. Not linked to the GeologyRegion registry.
        context.register(RIVER_DRY, new WaterEnvironment("", Optional.of(Fluids.WATER),
                Optional.of(Range.atLeast(-0.19F)), Optional.of(Range.below(-0.65F)),
                Optional.of(Range.atLeast(0.55F)), Optional.of(Range.below(0.30F)), Optional.empty(), Optional.of(8)));
        context.register(RIVER_SHALLOW_DRY, new WaterEnvironment("", Optional.of(Fluids.WATER),
                Optional.of(Range.atLeast(-0.19F)), Optional.of(Range.below(-0.65F)),
                Optional.of(Range.atLeast(0.55F)), Optional.of(Range.below(0.30F)), Optional.empty(), Optional.of(2)));
    }
}
