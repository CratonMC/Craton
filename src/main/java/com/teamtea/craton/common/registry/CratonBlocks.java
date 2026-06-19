package com.teamtea.craton.common.registry;

import com.teamtea.craton.Craton;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CratonBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(Craton.MODID);

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Craton.MODID);

    public static final DeferredBlock<Block> PEGMATITE = registerStone("pegmatite", MapColor.COLOR_PINK);
    public static final DeferredBlock<Block> GNEISS = registerStone("gneiss", MapColor.STONE);
    public static final DeferredBlock<Block> RHYOLITE = registerStone("rhyolite", MapColor.COLOR_RED);
    public static final DeferredBlock<Block> MARBLE = registerStone("marble", MapColor.QUARTZ);
    public static final DeferredBlock<Block> LIMESTONE = registerStone("limestone", MapColor.SAND);
    public static final DeferredBlock<Block> GABBRO = registerStone("gabbro", MapColor.COLOR_BLACK);

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

        ITEMS.register(name, () -> new BlockItem(
                block.get(),
                new Item.Properties()
                        .setId(ResourceKey.create(Registries.ITEM, id))
                        .useBlockDescriptionPrefix()
        ));

        return block;
    }
}