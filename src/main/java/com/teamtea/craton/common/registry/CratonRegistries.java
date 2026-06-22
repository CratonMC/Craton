package com.teamtea.craton.common.registry;

import com.teamtea.craton.Craton;
import com.teamtea.craton.api.geology.GeologyLayer;
import com.teamtea.craton.api.geology.GeologyProfile;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public final class CratonRegistries {
    public static final ResourceKey<Registry<GeologyLayer>> GEOLOGY_LAYER =
            ResourceKey.createRegistryKey(Craton.rl("geology_layer"));

    public static final ResourceKey<Registry<GeologyProfile>> GEOLOGY_PROFILE =
            ResourceKey.createRegistryKey(Craton.rl("geology_profile"));
}
