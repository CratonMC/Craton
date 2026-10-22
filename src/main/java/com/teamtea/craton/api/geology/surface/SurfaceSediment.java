package com.teamtea.craton.api.geology.surface;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamtea.craton.api.geology.GeologyLayer;
import com.teamtea.craton.api.geology.GeologyRegion;
import com.teamtea.craton.common.registry.CratonRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.codec.RegistryFixedCodec;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Optional;

/** Main sediment cover and localized secondary deposits, linked to existing geology/climate registries. */
public record SurfaceSediment(
        Holder<SurfaceSedimentType> type,
        Holder<WaterEnvironment> environment,
        Block mainBlock,
        List<Secondary> secondary,
        List<Holder<GeologyLayer>> rocks,
        Optional<Holder<GeologyRegion>> climate
) {
    public SurfaceSediment {
        secondary = List.copyOf(secondary);
        rocks = List.copyOf(rocks);
    }

    public record Secondary(Block block, Holder<SurfaceSedimentType> type) {
        public static final Codec<Secondary> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(Secondary::block),
                RegistryFixedCodec.create(CratonRegistries.SURFACE_SEDIMENT_TYPE)
                        .fieldOf("type").forGetter(Secondary::type)
        ).apply(instance, Secondary::new));
    }

    public static final Codec<SurfaceSediment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryFixedCodec.create(CratonRegistries.SURFACE_SEDIMENT_TYPE)
                    .fieldOf("type").forGetter(SurfaceSediment::type),
            RegistryFixedCodec.create(CratonRegistries.WATER_ENVIRONMENT)
                    .fieldOf("environment").forGetter(SurfaceSediment::environment),
            BuiltInRegistries.BLOCK.byNameCodec()
                    .fieldOf("main_block").forGetter(SurfaceSediment::mainBlock),
            Secondary.CODEC.listOf().optionalFieldOf("secondary", List.of()).forGetter(SurfaceSediment::secondary),
            RegistryFixedCodec.create(CratonRegistries.GEOLOGY_LAYER)
                    .listOf().optionalFieldOf("rocks", List.of()).forGetter(SurfaceSediment::rocks),
            RegistryFixedCodec.create(CratonRegistries.GEOLOGY_REGION)
                    .optionalFieldOf("climate").forGetter(SurfaceSediment::climate)
    ).apply(instance, SurfaceSediment::new));
}
