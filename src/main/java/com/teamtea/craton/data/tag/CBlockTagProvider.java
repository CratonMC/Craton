package com.teamtea.craton.data.tag;


import com.teamtea.craton.api.geology.block.ExtendedBlockFamily;
import com.teamtea.craton.common.registry.CratonBlocks;
import com.teamtea.craton.common.core.StoneCollection;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;


public final class CBlockTagProvider extends BlockTagsProvider {
    public CBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId) {
        super(output, lookupProvider, modId);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        for (StoneCollection collection : CratonBlocks.STONE_COLLECTIONS) {
            // addStoneFamilyTags(collection.origin().get());
            // addStoneFamilyTags(collection.polished().get());
            for (BlockFamily blockFamily : collection.getAll()) {
                addStoneFamilyTags(blockFamily);
            }
        }
    }

    private void addStoneFamilyTags(BlockFamily family) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(family.getBaseBlock())
                .add(family.get(BlockFamily.Variant.STAIRS))
                .add(family.get(BlockFamily.Variant.SLAB))
                .add(family.get(BlockFamily.Variant.WALL))
                .add(ExtendedBlockFamily.getVerticalSlab(family));

        tag(BlockTags.NEEDS_STONE_TOOL)
                .add(family.getBaseBlock())
                .add(family.get(BlockFamily.Variant.STAIRS))
                .add(family.get(BlockFamily.Variant.SLAB))
                .add(family.get(BlockFamily.Variant.WALL))
                .add(ExtendedBlockFamily.getVerticalSlab(family));

        tag(BlockTags.WALLS)
                .add(family.get(BlockFamily.Variant.WALL));
    }

}
