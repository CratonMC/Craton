package com.teamtea.craton.data.loot;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;


public class CLootTableProvider extends LootTableProvider {


    public CLootTableProvider(PackOutput generator, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(generator, Set.of(), List.of(new SubProviderEntry(
                CBlockLootTables::new,
                LootContextParamSets.BLOCK
        )),lookupProvider);
    }

}
