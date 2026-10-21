package com.teamtea.craton.common.registry;

import com.teamtea.craton.Craton;
import com.teamtea.craton.api.geology.surface.SurfaceSedimentType;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

public final class SurfaceSedimentTypeRegistry {
    public static final ResourceKey<SurfaceSedimentType> RIVERBED = key("riverbed");
    public static final ResourceKey<SurfaceSedimentType> ALLUVIAL = key("alluvial");
    public static final ResourceKey<SurfaceSedimentType> DEPOSIT_PATCH = key("deposit_patch");
    public static final ResourceKey<SurfaceSedimentType> SEABED = key("seabed");

    private SurfaceSedimentTypeRegistry() {}

    private static ResourceKey<SurfaceSedimentType> key(String name) {
        return ResourceKey.create(CratonRegistries.SURFACE_SEDIMENT_TYPE, Craton.rl(name));
    }

    public static void bootstrap(BootstrapContext<SurfaceSedimentType> context) {
        context.register(RIVERBED, new SurfaceSedimentType(SurfaceSedimentType.Pattern.BED, 3));
        context.register(ALLUVIAL, new SurfaceSedimentType(SurfaceSedimentType.Pattern.PATCH, 2));
        context.register(DEPOSIT_PATCH, new SurfaceSedimentType(SurfaceSedimentType.Pattern.PATCH, 2));
        context.register(SEABED, new SurfaceSedimentType(SurfaceSedimentType.Pattern.BED, 5));
    }
}
