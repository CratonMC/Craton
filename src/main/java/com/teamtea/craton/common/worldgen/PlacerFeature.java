package com.teamtea.craton.common.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamtea.craton.api.geology.deposit.Deposit;
import com.teamtea.craton.common.registry.CratonRegistries;
import net.minecraft.core.registries.codec.RegistryFixedCodec;
import com.teamtea.craton.api.geology.deposit.PlacerDeposit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/** World-generation entry point: placer geology and block writes belong to PlacerDeposit. */
public record PlacerFeature(Holder<Deposit> deposit) implements Feature {
    public static final MapCodec<PlacerFeature> CODEC = RecordCodecBuilder.<PlacerFeature>mapCodec(i -> i.group(
            RegistryFixedCodec.create(CratonRegistries.DEPOSIT).fieldOf("deposit").forGetter(PlacerFeature::deposit)
    ).apply(i, PlacerFeature::new));
    @Override public MapCodec<PlacerFeature> codec() { return CODEC; }
    @Override public java.util.stream.Stream<Holder<Feature>> getSubFeatures() { return java.util.stream.Stream.empty(); }
    @Override public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        return deposit.value() instanceof PlacerDeposit placer && placer.generateFeature(level, generator, random, origin);
    }
    public static String inspect(WorldGenLevel level, ChunkGenerator generator, BlockPos origin) {
        return PlacerDeposit.inspect(level, generator, origin);
    }
}
