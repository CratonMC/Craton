package com.teamtea.craton.common.registry;

import com.teamtea.craton.Craton;
import com.teamtea.craton.api.geology.GeologyLayer;
import com.teamtea.craton.api.geology.surface.SurfaceEnvironment;
import com.teamtea.craton.api.geology.surface.SurfaceSediment;
import com.teamtea.craton.api.geology.surface.SurfaceSedimentType;
import com.teamtea.craton.api.geology.GeologyProfile;
import com.teamtea.craton.api.geology.GeologyRegion;
import com.teamtea.craton.api.geology.deposit.Deposit;
import com.teamtea.craton.api.geology.ore.OreType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class CratonRegistries {
    public static final ResourceKey<Registry<GeologyRegion>> GEOLOGY_REGION =
            ResourceKey.createRegistryKey(Craton.rl("geology_region"));

    public static final ResourceKey<Registry<GeologyLayer>> GEOLOGY_LAYER =
            ResourceKey.createRegistryKey(Craton.rl("geology_layer"));

    public static final ResourceKey<Registry<GeologyProfile>> GEOLOGY_PROFILE =
            ResourceKey.createRegistryKey(Craton.rl("geology_profile"));

    public static final ResourceKey<Registry<Deposit>> DEPOSIT =
            ResourceKey.createRegistryKey(Craton.rl("deposit"));

    public static final ResourceKey<Registry<OreType>> ORE_TYPE =
            ResourceKey.createRegistryKey(Craton.rl("ore_type"));
    public static final ResourceKey<Registry<SurfaceEnvironment>> SURFACE_ENVIRONMENT =
            ResourceKey.createRegistryKey(Craton.rl("surface_environments"));

    public static final ResourceKey<Registry<SurfaceSedimentType>> SURFACE_SEDIMENT_TYPE =
            ResourceKey.createRegistryKey(Craton.rl("surface_sediment_type"));

    public static final ResourceKey<Registry<SurfaceSediment>> SURFACE_SEDIMENT =
            ResourceKey.createRegistryKey(Craton.rl("surface_sediments"));
}
