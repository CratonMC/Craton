package com.teamtea.craton.api.geology.ore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public record OreType(
        List<Variant> variants
) {

    public static final Codec<OreType> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Variant.CODEC.listOf()
                            .fieldOf("variants")
                            .forGetter(OreType::variants)
            ).apply(instance, OreType::new));

    public BlockState getOreState(BlockState host) {
        Holder<Block> hostHolder = host.getBlock().builtInRegistryHolder();

        for (Variant variant : variants) {
            if (variant.hosts().contains(hostHolder)) {
                return variant.result();
            }
        }

        return host;
    }

    public record Variant(
            HolderSet<Block> hosts,
            BlockState result
    ) {

        public static final Codec<Variant> CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                        RegistryCodecs.holderSet(Registries.BLOCK)
                                .fieldOf("hosts")
                                .forGetter(Variant::hosts),
                        BlockState.CODEC
                                .fieldOf("result")
                                .forGetter(Variant::result)
                ).apply(instance, Variant::new));
    }
}