package com.teamtea.craton.api.geology.ore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamtea.craton.common.registry.CratonRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.codec.RegistryFixedCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record OreVariant(
        Holder<Block> stone,
        Holder<OreType> ore,
        BlockState result
) {
    public static final Codec<OreVariant> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    BuiltInRegistries.BLOCK.holderByNameCodec()
                            .fieldOf("stone")
                            .forGetter(OreVariant::stone),

                    RegistryFixedCodec.create(CratonRegistries.ORE_TYPE)
                            .fieldOf("ore")
                            .forGetter(OreVariant::ore),

                    BlockState.CODEC
                            .fieldOf("result")
                            .forGetter(OreVariant::result)
            ).apply(instance, OreVariant::new));
}
