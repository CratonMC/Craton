package com.teamtea.craton.api.geology.block;

import net.minecraft.data.BlockFamily;
import net.minecraft.world.level.block.Block;

public interface ExtendedBlockFamily {
    boolean hasVerticalSlab();

    Block getVerticalSlab();

    void setVerticalSlab(Block block);

    static Block getVerticalSlab(BlockFamily family) {
        return ((ExtendedBlockFamily) (family)).getVerticalSlab();
    }
}
