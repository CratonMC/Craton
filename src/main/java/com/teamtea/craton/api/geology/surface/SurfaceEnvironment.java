package com.teamtea.craton.api.geology.surface;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import java.util.Optional;

/** Datapack-defined location predicate. biome is a biome id or a #biome-tag id. */
public record SurfaceEnvironment(String biomes, Optional<Fluid> fluid) {
    public static final Codec<SurfaceEnvironment> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("biomes").forGetter(SurfaceEnvironment::biomes),
            BuiltInRegistries.FLUID.byNameCodec().optionalFieldOf("fluid").forGetter(SurfaceEnvironment::fluid)
    ).apply(i, SurfaceEnvironment::new)).validate(value -> {
        String id = value.biomes().startsWith("#") ? value.biomes().substring(1) : value.biomes();
        try {
            Identifier.parse(id);
            return DataResult.success(value);
        } catch (IllegalArgumentException ex) {
            return DataResult.error(() -> "Invalid biome id or tag: " + value.biomes());
        }
    });
}
