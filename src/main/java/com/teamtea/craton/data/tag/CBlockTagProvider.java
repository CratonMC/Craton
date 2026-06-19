package com.teamtea.craton.data.tag;


import com.teamtea.craton.common.registry.CratonBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;


public final class CBlockTagProvider extends BlockTagsProvider {
    public CBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId) {
        super(output, lookupProvider, modId);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(
                        CratonBlocks.PEGMATITE.get(),
                        CratonBlocks.GNEISS.get(),
                        CratonBlocks.RHYOLITE.get(),
                        CratonBlocks.MARBLE.get(),
                        CratonBlocks.LIMESTONE.get(),
                        CratonBlocks.GABBRO.get()
                );

        tag(BlockTags.NEEDS_STONE_TOOL)
                .add(
                        CratonBlocks.PEGMATITE.get(),
                        CratonBlocks.GNEISS.get(),
                        CratonBlocks.RHYOLITE.get(),
                        CratonBlocks.MARBLE.get(),
                        CratonBlocks.LIMESTONE.get(),
                        CratonBlocks.GABBRO.get()
                );
    }

    public Identifier srl(String croptopia, String name) {
        return Identifier.fromNamespaceAndPath(croptopia, name);
    }


}
