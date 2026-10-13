package com.teamtea.craton.common.registry;

import com.teamtea.craton.Craton;
import com.teamtea.craton.api.geology.GeologyRegion;
import com.teamtea.craton.api.geology.GeologyRegion.Range;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

/** Default regions; datapacks can replace these or add their own registry entries. */
public class GeologyRegionRegistry {
    public static final ResourceKey<GeologyRegion> OCEAN = createKey("ocean");
    public static final ResourceKey<GeologyRegion> COASTAL = createKey("coastal");
    public static final ResourceKey<GeologyRegion> MOUNTAINS = createKey("mountains");
    public static final ResourceKey<GeologyRegion> ARID_HILLS = createKey("arid_hills");
    public static final ResourceKey<GeologyRegion> HILLS = createKey("hills");
    public static final ResourceKey<GeologyRegion> COLD_PLAINS = createKey("cold_plains");
    public static final ResourceKey<GeologyRegion> ARID_PLAINS = createKey("arid_plains");
    public static final ResourceKey<GeologyRegion> WARM_HUMID = createKey("warm_humid");
    public static final ResourceKey<GeologyRegion> WARM_SEMIARID = createKey("warm_semiarid");
    public static final ResourceKey<GeologyRegion> PLAINS = createKey("plains");

    private GeologyRegionRegistry() {}

    private static ResourceKey<GeologyRegion> createKey(String name) {
        return ResourceKey.create(CratonRegistries.GEOLOGY_REGION, Craton.rl(name));
    }

    public static void bootstrap(BootstrapContext<GeologyRegion> context) {
        context.register(OCEAN, new GeologyRegion(1000,
                Range.below(-0.19F), Range.ANY, Range.ANY, Range.ANY, Range.ANY));
        context.register(COASTAL, new GeologyRegion(900,
                Range.below(-0.11F), Range.ANY, Range.ANY, Range.ANY, Range.ANY));
        context.register(MOUNTAINS, new GeologyRegion(800,
                Range.atLeast(0.03F), Range.below(-0.375F), Range.ANY, Range.ANY, Range.greaterThan(-0.05F)));
        context.register(ARID_HILLS, new GeologyRegion(710,
                Range.ANY, Range.below(0.05F), Range.ANY, Range.below(-0.35F), Range.greaterThan(-0.6F)));
        context.register(HILLS, new GeologyRegion(700,
                Range.ANY, Range.below(0.05F), Range.ANY, Range.ANY, Range.greaterThan(-0.6F)));
        context.register(COLD_PLAINS, new GeologyRegion(600,
                Range.ANY, Range.ANY, Range.below(-0.45F), Range.ANY, Range.ANY));
        context.register(ARID_PLAINS, new GeologyRegion(500,
                Range.ANY, Range.ANY, Range.ANY, Range.below(-0.35F), Range.ANY));
        context.register(WARM_HUMID, new GeologyRegion(400,
                Range.ANY, Range.ANY, Range.atLeast(0.55F), Range.atLeast(0.3F), Range.ANY));
        context.register(WARM_SEMIARID, new GeologyRegion(300,
                Range.ANY, Range.ANY, Range.atLeast(0.55F), Range.below(0.1F), Range.ANY));
        context.register(PLAINS, new GeologyRegion(-1000,
                Range.ANY, Range.ANY, Range.ANY, Range.ANY, Range.ANY));
    }
}
