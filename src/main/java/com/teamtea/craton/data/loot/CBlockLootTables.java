package com.teamtea.craton.data.loot;

import com.teamtea.craton.Craton;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class CBlockLootTables extends BlockLootSubProvider {

    public CBlockLootTables(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    private final Map<ResourceKey<LootTable>, LootTable.Builder> map = new HashMap<>();

    @Override
    protected @NonNull Iterable<Block> getKnownBlocks() {
        return map.keySet()
                .stream()
                .map(lootTableResourceKey -> BuiltInRegistries.BLOCK.stream()
                        .filter(block ->
                        {
                            Optional<ResourceKey<LootTable>> lootTable = block.getLootTable();
                            return lootTable.isPresent() && lootTable.get().equals(lootTableResourceKey);
                        })
                        .findFirst())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
    protected void add(@NonNull Block block, LootTable.@NonNull Builder builder) {
        super.add(block, builder);
        // Copy
        this.map.put(block.getLootTable().orElseThrow(() -> new IllegalStateException("Block " + block + " does not have loot table")), builder);
    }

    protected void dropSelfWithContents(Set<Block> blocks) {
        for (Block block : blocks) {
            add(block, createSingleItemTable(block));
        }
    }

    @Override
    protected void generate() {
        Set<Block> blocks = BuiltInRegistries.BLOCK.stream()
                .filter(block -> Craton.MODID.equals(BuiltInRegistries.BLOCK.getKey(block).getNamespace()))
                .filter(block -> block.getLootTable().isPresent())
                .filter(block -> block.asItem() != Items.AIR)
                .collect(Collectors.toSet());

        dropSelfWithContents(blocks);

    }
}
