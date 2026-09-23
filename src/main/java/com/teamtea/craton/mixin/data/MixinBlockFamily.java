package com.teamtea.craton.mixin.data;


import com.teamtea.craton.api.block.ExtendedBlockFamily;
import net.minecraft.data.BlockFamily;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockFamily.class)
public class MixinBlockFamily implements ExtendedBlockFamily {
    @Override
    public boolean hasVerticalSlab() {
        return craton$verticalSlab != null;
    }

    @Override
    public void setVerticalSlab(Block block) {
        this.craton$verticalSlab = block;
    }

    @Unique
    Block craton$verticalSlab = null;

    @Override
    public Block getVerticalSlab() {
        return craton$verticalSlab;
    }

    // @Override
    // public boolean hasVerticalSlab() {
    //     return craton$verticalSlab;
    // }
    //
    // @Override
    // public void setVerticalSlab() {
    //     craton$verticalSlab = true;
    // }
}
