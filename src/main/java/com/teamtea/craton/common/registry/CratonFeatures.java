package com.teamtea.craton.common.registry;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.Craton;
import com.teamtea.craton.common.worldgen.MeteoriteFeature;
import com.teamtea.craton.common.worldgen.BadlandsRedSandPlacerFeature;
import com.teamtea.craton.common.worldgen.PlacerFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CratonFeatures {
    public static final DeferredRegister<MapCodec<? extends Feature>> DEFERRED_REGISTER =
            DeferredRegister.create(Registries.FEATURE_TYPE,Craton.MODID);

    public static final  DeferredHolder<MapCodec<? extends Feature>, MapCodec<MeteoriteFeature>> meteorite = DEFERRED_REGISTER.register("meteorite", () -> MeteoriteFeature.CODEC);

    public static final DeferredHolder<MapCodec<? extends Feature>,MapCodec<PlacerFeature>> placer = DEFERRED_REGISTER.register("placer", () -> PlacerFeature.CODEC);

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<BadlandsRedSandPlacerFeature>> badlands_red_sand_placer =
            DEFERRED_REGISTER.register("badlands_red_sand_placer", () -> BadlandsRedSandPlacerFeature.CODEC);
}

