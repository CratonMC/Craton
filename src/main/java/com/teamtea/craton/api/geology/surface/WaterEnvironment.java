package com.teamtea.craton.api.geology.surface;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamtea.craton.api.geology.GeologyRegion;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import java.util.Optional;

/** A water environment is independent of geology-region registration and biome identity. */
public record WaterEnvironment(String biomes, Optional<Fluid> fluid,
                               Optional<GeologyRegion.Range> continentalness,
                               Optional<GeologyRegion.Range> peaksAndValleys,
                               Optional<GeologyRegion.Range> temperature,
                               Optional<GeologyRegion.Range> humidity,
                               Optional<Integer> minDepth, Optional<Integer> maxDepth) {
    public static final Codec<WaterEnvironment> CODEC = RecordCodecBuilder.<WaterEnvironment>create(i -> i.group(
            Codec.STRING.optionalFieldOf("biomes", "").forGetter(WaterEnvironment::biomes),
            BuiltInRegistries.FLUID.byNameCodec().optionalFieldOf("fluid").forGetter(WaterEnvironment::fluid),
            GeologyRegion.Range.CODEC.optionalFieldOf("continentalness").forGetter(WaterEnvironment::continentalness),
            GeologyRegion.Range.CODEC.optionalFieldOf("peaks_and_valleys").forGetter(WaterEnvironment::peaksAndValleys),
            GeologyRegion.Range.CODEC.optionalFieldOf("temperature").forGetter(WaterEnvironment::temperature),
            GeologyRegion.Range.CODEC.optionalFieldOf("humidity").forGetter(WaterEnvironment::humidity),
            Codec.intRange(0, 384).optionalFieldOf("min_depth").forGetter(WaterEnvironment::minDepth),
            Codec.intRange(0, 384).optionalFieldOf("max_depth").forGetter(WaterEnvironment::maxDepth)
    ).apply(i, WaterEnvironment::new)).validate(value -> {
        if (!value.biomes().isEmpty()) {
            String id = value.biomes().startsWith("#") ? value.biomes().substring(1) : value.biomes();
            try { Identifier.parse(id); }
            catch (IllegalArgumentException ex) {
                return DataResult.error(() -> "Invalid biome id or tag: " + value.biomes());
            }
        }
        return DataResult.success(value);
    });

    /** Range criteria belong to the water environment, not to GeologyRegion entries. */
    public int matchScore(Holder<Biome> biome, BlockState fluidAbove, Climate.TargetPoint climate, int depth) {
        if ((depth == 0 && (maxDepth.isEmpty() || maxDepth.get() != 0)) || minDepth.isPresent() && depth < minDepth.get() || maxDepth.isPresent() && depth > maxDepth.get()) return -1;
        if (fluid.isPresent() && fluidAbove.getFluidState().getType() != fluid.get()) return -1;
        if (!biomes.isEmpty()) {
            boolean tag = biomes.startsWith("#");
            Identifier id = Identifier.parse(tag ? biomes.substring(1) : biomes);
            if (!(tag ? biome.is(TagKey.create(Registries.BIOME, id))
                    : biome.unwrapKey().map(key -> key.identifier().equals(id)).orElse(false))) return -1;
        }
        if (continentalness.isPresent() && !continentalness.get().contains(climate.continentalness())) return -1;
        if (peaksAndValleys.isPresent() && !peaksAndValleys.get().contains(
                GeologyRegion.peaksAndValleys(climate.weirdness()))) return -1;
        if (temperature.isPresent() && !temperature.get().contains(climate.temperature())) return -1;
        if (humidity.isPresent() && !humidity.get().contains(climate.humidity())) return -1;
        int specificity = 0;
        if (!biomes.isEmpty()) specificity++;
        if (continentalness.isPresent()) specificity++;
        if (peaksAndValleys.isPresent()) specificity++;
        if (temperature.isPresent()) specificity++;
        if (humidity.isPresent()) specificity++;
        if (minDepth.isPresent()) specificity++;
        if (maxDepth.isPresent()) specificity++;
        return specificity;
    }
}
