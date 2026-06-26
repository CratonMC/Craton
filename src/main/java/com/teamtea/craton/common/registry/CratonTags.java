package com.teamtea.craton.common.registry;

import com.teamtea.craton.Craton;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class CratonTags {
    public static class Blocks {
        public static final TagKey<Block> VERTICAL_SLABS = TagKey.create(
                Registries.BLOCK,
                Craton.rl("vertical_slabs")
        );
    }

    public static class Items {
        public static final TagKey<Item> VERTICAL_SLABS = TagKey.create(
                Registries.ITEM,
                Craton.rl("vertical_slabs")
        );
    }

}
