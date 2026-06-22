package com.teamtea.craton.api.geology;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamtea.craton.common.registry.CratonRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.world.level.biome.Biome;

import java.util.List;

public record GeologyProfile(
        HolderSet<Biome> biomes,
        List<Holder<GeologyLayer>> layers
) {
    public static final Codec<GeologyProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.BIOME)
                    .fieldOf("biomes")
                    .forGetter(GeologyProfile::biomes),

            RegistryFixedCodec.create(CratonRegistries.GEOLOGY_LAYER)
                    .listOf()
                    .fieldOf("layers")
                    .forGetter(GeologyProfile::layers)
    ).apply(instance, GeologyProfile::new));
}