package com.teamtea.craton.common.registry;

import com.google.common.base.Suppliers;
import com.teamtea.craton.Craton;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamily;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

public class CratonBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(Craton.MODID);

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Craton.MODID);

    public static final StoneCollection GNEISS = registerStoneCollection("gneiss", MapColor.STONE);
    public static final StoneCollection RHYOLITE = registerStoneCollection("rhyolite", MapColor.COLOR_RED);
    public static final StoneCollection MARBLE = registerStoneCollection("marble", MapColor.QUARTZ);
    public static final StoneCollection LIMESTONE = registerStoneCollection("limestone", MapColor.SAND);
    public static final StoneCollection GABBRO = registerStoneCollection("gabbro", MapColor.COLOR_BLACK);
    public static final StoneCollection PEGMATITE = registerStoneCollection("pegmatite", MapColor.COLOR_PINK);

    public static final List<StoneCollection> STONE_COLLECTIONS = List.of(
            GNEISS,
            RHYOLITE,
            MARBLE,
            LIMESTONE,
            GABBRO,
            PEGMATITE
    );

    public record StoneCollection(
            Supplier<BlockFamily> origin,
            Supplier<BlockFamily> polished
    ) {
        public BlockFamily getOrigin() {
            return origin.get();
        }

        public BlockFamily getPolished() {
            return polished.get();
        }
    }

    private static StoneCollection registerStoneCollection(String name, MapColor mapColor) {
        return new StoneCollection(
                registerStoneFamily(name, mapColor),
                registerStoneFamily("polished_" + name, mapColor)
        );
    }

    private static Supplier<BlockFamily> registerStoneFamily(String name, MapColor mapColor) {
        DeferredBlock<Block> block = registerStone(name, mapColor);
        DeferredBlock<StairBlock> stairs = registerStairs(name + "_stairs", block);
        DeferredBlock<SlabBlock> slab = registerSlab(name + "_slab", mapColor);
        DeferredBlock<WallBlock> wall = registerWall(name + "_wall", mapColor);

        return Suppliers.memoize(() -> new BlockFamily.Builder(block.get())
                .stairs(stairs.get())
                .slab(slab.get())
                .wall(wall.get())
                .getFamily());
    }

    private static DeferredBlock<Block> registerStone(String name, MapColor mapColor) {
        Identifier id = Identifier.fromNamespaceAndPath(Craton.MODID, name);

        DeferredBlock<Block> block = BLOCKS.register(name, () -> new Block(
                BlockBehaviour.Properties.of()
                        .setId(ResourceKey.create(Registries.BLOCK, id))
                        .mapColor(mapColor)
                        .strength(1.5F, 6.0F)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.STONE)
        ));

        registerBlockItem(name, block);
        return block;
    }

    private static DeferredBlock<StairBlock> registerStairs(String name, DeferredBlock<? extends Block> base) {
        Identifier id = Identifier.fromNamespaceAndPath(Craton.MODID, name);

        DeferredBlock<StairBlock> block = BLOCKS.register(name, () -> new StairBlock(
                base.get().defaultBlockState(),
                BlockBehaviour.Properties.ofFullCopy(base.get())
                        .setId(ResourceKey.create(Registries.BLOCK, id))
        ));

        registerBlockItem(name, block);
        return block;
    }

    private static DeferredBlock<SlabBlock> registerSlab(String name, MapColor mapColor) {
        Identifier id = Identifier.fromNamespaceAndPath(Craton.MODID, name);

        DeferredBlock<SlabBlock> block = BLOCKS.register(name, () -> new SlabBlock(
                BlockBehaviour.Properties.of()
                        .setId(ResourceKey.create(Registries.BLOCK, id))
                        .mapColor(mapColor)
                        .strength(1.5F, 6.0F)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.STONE)
        ));

        registerBlockItem(name, block);
        return block;
    }

    private static DeferredBlock<WallBlock> registerWall(String name, MapColor mapColor) {
        Identifier id = Identifier.fromNamespaceAndPath(Craton.MODID, name);

        DeferredBlock<WallBlock> block = BLOCKS.register(name, () -> new WallBlock(
                BlockBehaviour.Properties.of()
                        .setId(ResourceKey.create(Registries.BLOCK, id))
                        .mapColor(mapColor)
                        .strength(1.5F, 6.0F)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.STONE)
        ));

        registerBlockItem(name, block);
        return block;
    }

    private static void registerBlockItem(String name, DeferredBlock<? extends Block> block) {
        Identifier id = Identifier.fromNamespaceAndPath(Craton.MODID, name);

        ITEMS.register(name, () -> new BlockItem(
                block.get(),
                new Item.Properties()
                        .setId(ResourceKey.create(Registries.ITEM, id))
                        .useBlockDescriptionPrefix()
        ));
    }
}