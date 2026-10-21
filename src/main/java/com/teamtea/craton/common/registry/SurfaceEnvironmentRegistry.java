package com.teamtea.craton.common.registry;

import com.teamtea.craton.Craton;
import com.teamtea.craton.api.geology.surface.SurfaceEnvironment;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.material.Fluids;
import java.util.Optional;

public final class SurfaceEnvironmentRegistry {
    public static final ResourceKey<SurfaceEnvironment> RIVER = key("river");
    public static final ResourceKey<SurfaceEnvironment> OCEAN = key("ocean");
    // Lake intentionally has no implicit vanilla classification.
    private SurfaceEnvironmentRegistry() {}
    private static ResourceKey<SurfaceEnvironment> key(String id) {
        return ResourceKey.create(CratonRegistries.SURFACE_ENVIRONMENT, Craton.rl(id));
    }
    public static void bootstrap(BootstrapContext<SurfaceEnvironment> context) {
        context.register(RIVER, new SurfaceEnvironment("#c:is_river", Optional.of(Fluids.WATER)));
        context.register(OCEAN, new SurfaceEnvironment("#c:is_ocean", Optional.of(Fluids.WATER)));
    }
}
