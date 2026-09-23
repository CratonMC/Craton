package com.teamtea.craton.data.loot;

import com.teamtea.craton.Craton;
import com.teamtea.craton.common.block.VerticalSlabBlock;
import net.minecraft.advancements.predicates.StatePropertiesPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.MatchBlock;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class CBlockLootTables extends BlockLootSubProvider {

    public CBlockLootTables(LootTableSubProvider.Context output) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), output);
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
            if (!(block instanceof VerticalSlabBlock verticalSlabBlock)) {
                add(block, createSingleItemTable(block));
            } else {
                add(block, createSlabItemTable(block));
            }
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

    protected LootTable.Builder createSlabItemTable(Block slab) {
        return LootTable.lootTable()
                .withPool(
                        LootPool.lootPool()
                                .setRolls(ContextIntProviders.exactly(1))
                                .add(
                                        this.applyExplosionDecay(
                                                slab,
                                                LootItem.lootTableItem(slab)
                                                        .apply(
                                                                SetItemCountFunction.setCount(ContextIntProviders.exactly(2))
                                                                        .when(MatchBlock.blockMatches(BuiltInRegistries.BLOCK, slab
                                                                                , StatePropertiesPredicate.Builder.properties().hasProperty(VerticalSlabBlock.TYPE, VerticalSlabBlock.Type.DOUBLE))
                                                                        )
                                                        )
                                        )
                                )
                );
    }
}
