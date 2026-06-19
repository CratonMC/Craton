package com.teamtea.craton.data.tag;


import com.teamtea.craton.Craton;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;


public final class CItemTagProvider extends ItemTagsProvider {

    public CItemTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> providerCompletableFuture) {
        super(packOutput, providerCompletableFuture, Craton.MODID);
    }


    // @Override
    // public String getName() {
    //     return "ES Item Tags";
    // }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {

    }


    public Identifier srl(String croptopia, String name) {
        return Identifier.fromNamespaceAndPath(croptopia, name);
    }

}
