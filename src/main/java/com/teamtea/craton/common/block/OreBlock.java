package com.teamtea.craton.common.block;

import com.teamtea.craton.common.registry.CratonDataMaps;
import net.minecraft.world.level.block.Block;

/** The host is immutable block identity; mineral drops are reloadable block data. */
public class OreBlock extends Block {
    private final Block host;

    public OreBlock(Block host, Properties properties){
        super(properties);
        this.host=host;
    }

    public Block host(){return host;}

    /** Reloadable product definition attached to this registered block. */
    public CratonDataMaps.OreDrops oreDrops() {
        return builtInRegistryHolder().getData(CratonDataMaps.ORE_DROPS);
    }
}
