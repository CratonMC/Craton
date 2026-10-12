package com.teamtea.craton.common.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** The host is immutable block identity; mineral drops are reloadable block data. */
public class OreBlock extends Block {
    // public static final MapCodec<OreBlock> CODEC=RecordCodecBuilder.mapCodec(instance -> instance.group(
    //         BuiltInRegistries.BLOCK.byNameCodec().fieldOf("host").forGetter(OreBlock::host),
    //         BlockBehaviour.propertiesCodec()
    // ).apply(instance,OreBlock::new));

    private final Block host;

    public OreBlock(Block host, Properties properties){
        super(properties);
        this.host=host;
    }

    public Block host(){return host;}

    // @Override public MapCodec<? extends Block> codec(){return CODEC;}
}
