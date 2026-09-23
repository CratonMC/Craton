package com.teamtea.craton.data.loot;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;


public class CLootTableProvider extends LootTableProvider {


    public CLootTableProvider() {
        super(BuiltInLootTables.all(), List.of(new LootTableProvider.SubProviderEntry(
                CBlockLootTables::new,
                // Loot table generator for the 'empty' param set
                LootContextParamSets.BLOCK
        )));
    }

}
