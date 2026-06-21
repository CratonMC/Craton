package com.teamtea.craton.data.tag;


import com.teamtea.craton.Craton;
import com.teamtea.craton.common.registry.CratonBlocks;
import com.teamtea.craton.common.core.StoneCollection;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;


public final class CItemTagProvider extends ItemTagsProvider {

    public CItemTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> providerCompletableFuture) {
        super(packOutput, providerCompletableFuture, Craton.MODID);
    }

    @Override
    protected void addTags(HolderLookup.@NonNull Provider pProvider) {
        for (StoneCollection collection : CratonBlocks.STONE_COLLECTIONS) {
            // addStoneFamilyTags(collection.origin().get());
            // addStoneFamilyTags(collection.polished().get());
            for (BlockFamily blockFamily : collection.getAll()) {
                addStoneFamilyTags(blockFamily);
            }
        }
    }

    private void addStoneFamilyTags(BlockFamily family) {
        tag( ItemTags.WALLS)
                .add(family.get(BlockFamily.Variant.WALL).asItem());
    }

}
