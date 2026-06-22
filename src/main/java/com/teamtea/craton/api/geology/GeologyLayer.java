package com.teamtea.craton.api.geology;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record GeologyLayer(
        BlockState blockState,
        double thickness,
        double amplitude,
        double freqX,
        double freqZ,
        int seed
) {
    public static final Codec<GeologyLayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec()
                    .fieldOf("block")
                    .xmap(Block::defaultBlockState, BlockState::getBlock)
                    .forGetter(GeologyLayer::blockState),

            Codec.DOUBLE.optionalFieldOf("thickness", 24.0)
                    .forGetter(GeologyLayer::thickness),

            Codec.DOUBLE.optionalFieldOf("amplitude", 12.0)
                    .forGetter(GeologyLayer::amplitude),

            Codec.DOUBLE.optionalFieldOf("freq_x", 0.01)
                    .forGetter(GeologyLayer::freqX),

            Codec.DOUBLE.optionalFieldOf("freq_z", 0.01)
                    .forGetter(GeologyLayer::freqZ),

            Codec.INT.optionalFieldOf("seed", 0)
                    .forGetter(GeologyLayer::seed)
    ).apply(instance, GeologyLayer::new));
}
