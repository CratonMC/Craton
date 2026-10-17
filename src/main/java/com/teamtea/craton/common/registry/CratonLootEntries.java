package com.teamtea.craton.common.registry;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.Craton;
import com.teamtea.craton.common.loot.OreProductsEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CratonLootEntries {
    public static final DeferredRegister<MapCodec<? extends LootPoolEntryContainer>> TYPES =
            DeferredRegister.create(Registries.LOOT_POOL_ENTRY_TYPE, Craton.MODID);
    static { TYPES.register("ore_products", () -> OreProductsEntry.CODEC); }
    private CratonLootEntries() {}
}
