package com.teamtea.craton.common.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamtea.craton.api.geology.deposit.Deposit;
import com.teamtea.craton.api.geology.deposit.MeteoriteDeposit;
import com.teamtea.craton.common.registry.CratonRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.codec.RegistryFixedCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/** Feature adapter: loads a reloadable meteorite definition and delegates generation. */
public record MeteoriteFeature(Holder<Deposit> deposit) implements Feature {
    public static final MapCodec<MeteoriteFeature> CODEC = RecordCodecBuilder.<MeteoriteFeature>mapCodec(i -> i.group(
            RegistryFixedCodec.create(CratonRegistries.DEPOSIT).fieldOf("deposit").forGetter(MeteoriteFeature::deposit)
    ).apply(i, MeteoriteFeature::new));
    @Override public java.util.stream.Stream<Holder<Feature>> getSubFeatures() { return java.util.stream.Stream.empty(); }
    @Override public MapCodec<MeteoriteFeature> codec() { return CODEC; }
    @Override public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        return deposit.value() instanceof MeteoriteDeposit meteorite && meteorite.generateFeature(level, generator, random, origin);
    }
}
