package com.teamtea.craton.data.tag;


import com.teamtea.craton.common.registry.CratonBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;


public final class CBlockTagProvider extends BlockTagsProvider {
    public CBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId) {
        super(output, lookupProvider, modId);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        for (CratonBlocks.StoneCollection collection : CratonBlocks.STONE_COLLECTIONS) {
            addStoneFamilyTags(collection.origin().get());
            addStoneFamilyTags(collection.polished().get());
        }
    }

    private void addStoneFamilyTags(BlockFamily family) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(family.getBaseBlock())
                .add(family.get(BlockFamily.Variant.STAIRS))
                .add(family.get(BlockFamily.Variant.SLAB))
                .add(family.get(BlockFamily.Variant.WALL));

        tag(BlockTags.NEEDS_STONE_TOOL)
                .add(family.getBaseBlock())
                .add(family.get(BlockFamily.Variant.STAIRS))
                .add(family.get(BlockFamily.Variant.SLAB))
                .add(family.get(BlockFamily.Variant.WALL));

        tag(BlockTags.WALLS)
                .add(family.get(BlockFamily.Variant.WALL));
    }

}
