package com.teamtea.craton.common.registry;

import com.google.common.base.Suppliers;
import com.teamtea.craton.Craton;
import com.teamtea.craton.api.block.ExtendedBlockFamily;
import com.teamtea.craton.common.block.VerticalSlabBlock;
import com.teamtea.craton.common.core.StoneCollection;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamily;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
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
    public static final StoneCollection ARKOSE_SANDSTONE = registerStoneCollection("arkose_sandstone", MapColor.SAND);
    public static final StoneCollection SHALE = registerStoneCollection("shale", MapColor.STONE);
    public static final StoneCollection ECLOGITE = registerStoneCollection("eclogite", MapColor.COLOR_BLACK);
    public static final StoneCollection SCHIST = registerStoneCollection("schist", MapColor.STONE);

    public static final StoneCollection KIMBERLITE = registerStoneCollection("kimberlite", MapColor.COLOR_BLACK);
    public static final StoneCollection SKARN = registerStoneCollection("skarn", MapColor.QUARTZ);
    public static final StoneCollection METEORITE = registerStoneCollection("meteorite", MapColor.STONE);
    public static final StoneCollection JADEITITE = registerStoneCollection("jadeitite", MapColor.COLOR_GREEN);

    public static final Holder<Block> banded_iron_gneiss_ore =registerOreBlock("banded_iron_gneiss_ore");
//    public static final Holder<Block> banded_iron_marble_ore =registerOreBlock("banded_iron_marble_ore");
//    public static final Holder<Block> banded_iron_stone_ore =registerOreBlock("banded_iron_stone_ore");

    public static final Holder<Block> limestone_coal_ore = registerOreBlock("limestone_coal_ore");
    public static final Holder<Block> sedimentary_copper_ore = registerOreBlock("sedimentary_copper_ore");
    public static final Holder<Block> metasomatic_lapis_ore = registerOreBlock("metasomatic_lapis_ore");
    public static final Holder<Block> magmatic_pge_ore = registerOreBlock("magmatic_pge_ore");
    public static final Holder<Block> magmatic_ni_cu_ore = registerOreBlock("magmatic_ni_cu_ore");
    public static final Holder<Block> pegmatite_quartz_ore = registerOreBlock("pegmatite_quartz_ore");
    public static final Holder<Block> pegmatite_emerald_ore = registerOreBlock("pegmatite_emerald_ore");
    public static final Holder<Block> quartz_vein_gold_ore = registerOreBlock("quartz_vein_gold_ore");
    public static final Holder<Block> quartz_vein_pyrite_ore = registerOreBlock("quartz_vein_pyrite_ore");
    public static final Holder<Block> fluorite_vein_ore = registerOreBlock("fluorite_vein_ore");
    public static final Holder<Block> sedimentary_uranium_ore = registerOreBlock("sedimentary_uranium_ore");
    public static final Holder<Block> sedimentary_pb_zn_ore = registerOreBlock("sedimentary_pb_zn_ore");
    public static final Holder<Block> shale_coal_ore = registerOreBlock("shale_coal_ore");
    public static final Holder<Block> deep_hydrothermal_redstone_ore = registerOreBlock("deep_hydrothermal_redstone_ore");
    public static final Holder<Block> granite_cassiterite_ore = registerOreBlock("granite_cassiterite_ore");
    public static final Holder<Block> granite_wolframite_ore = registerOreBlock("granite_wolframite_ore");
    public static final Holder<Block> porphyry_gold_ore = registerOreBlock("porphyry_gold_ore");
    public static final Holder<Block> porphyry_copper_ore = registerOreBlock("porphyry_copper_ore");
    public static final Holder<Block> porphyry_sulfide_ore = registerOreBlock("porphyry_sulfide_ore");
    public static final Holder<Block> epithermal_au_ag_ore = registerOreBlock("epithermal_au_ag_ore");
    public static final Holder<Block> vms_copper_ore = registerOreBlock("vms_copper_ore");
    public static final Holder<Block> vms_zinc_ore = registerOreBlock("vms_zinc_ore");
    public static final Holder<Block> vms_pb_ag_ore = registerOreBlock("vms_pb_ag_ore");
    public static final Holder<Block> bauxite_ore = registerOreBlock("bauxite_ore");

    public static final Holder<Block> peat = registerPlaceholderBlock("peat", Blocks.MUD);
    public static final Holder<Block> auriferous_gravel = registerPlaceholderBlock("auriferous_gravel", Blocks.GRAVEL);
    public static final Holder<Block> iron_bearing_gravel = registerPlaceholderBlock("iron_bearing_gravel", Blocks.GRAVEL);
    public static final Holder<Block> gold_bearing_sand = registerPlaceholderBlock("gold_bearing_sand", Blocks.SAND);
    public static final Holder<Block> iron_sand = registerPlaceholderBlock("iron_sand", Blocks.RED_SAND);

    public static final Holder<Block> diamond_bearing_kimberlite = registerOreBlock("diamond_bearing_kimberlite");
    public static final Holder<Block> diamond_rich_kimberlite = registerOreBlock("diamond_rich_kimberlite");
    public static final Holder<Block> skarn_iron_ore = registerOreBlock("skarn_iron_ore");
    public static final Holder<Block> skarn_copper_ore = registerOreBlock("skarn_copper_ore");
    public static final Holder<Block> skarn_w_sn_ore = registerOreBlock("skarn_w_sn_ore");
    public static final Holder<Block> skarn_pb_zn_ore = registerOreBlock("skarn_pb_zn_ore");
    public static final Holder<Block> skarn_gold_ore = registerOreBlock("skarn_gold_ore");
    public static final Holder<Block> meteoric_iron_ore = registerOreBlock("meteoric_iron_ore");
    public static final Holder<Block> suspicious_jadeitite = registerOreBlock("suspicious_jadeitite");

    public static final List<Holder<Block>> ORE_BLOCKS = List.of(
            banded_iron_gneiss_ore,
            limestone_coal_ore,
            sedimentary_copper_ore,
            metasomatic_lapis_ore,
            magmatic_pge_ore,
            magmatic_ni_cu_ore,
            pegmatite_quartz_ore,
            pegmatite_emerald_ore,
            quartz_vein_gold_ore,
            quartz_vein_pyrite_ore,
            fluorite_vein_ore,
            sedimentary_uranium_ore,
            sedimentary_pb_zn_ore,
            shale_coal_ore,
            deep_hydrothermal_redstone_ore,
            granite_cassiterite_ore,
            granite_wolframite_ore,
            porphyry_gold_ore,
            porphyry_copper_ore,
            porphyry_sulfide_ore,
            epithermal_au_ag_ore,
            vms_copper_ore,
            vms_zinc_ore,
            vms_pb_ag_ore,
            bauxite_ore,
            peat,
            auriferous_gravel,
            iron_bearing_gravel,
            gold_bearing_sand,
            iron_sand,
            diamond_bearing_kimberlite,
            diamond_rich_kimberlite,
            skarn_iron_ore,
            skarn_copper_ore,
            skarn_w_sn_ore,
            skarn_pb_zn_ore,
            skarn_gold_ore,
            meteoric_iron_ore,
            suspicious_jadeitite
    );

    public static final List<StoneCollection> STONE_COLLECTIONS = List.of(
            GNEISS,
            RHYOLITE,
            MARBLE,
            LIMESTONE,
            GABBRO,
            PEGMATITE,
            ARKOSE_SANDSTONE,
            SHALE,
            ECLOGITE,
            SCHIST,
            KIMBERLITE,
            SKARN,
            METEORITE,
            JADEITITE
    );

    private static StoneCollection registerStoneCollection(String name, MapColor mapColor) {
        return new StoneCollection(
                registerFullStoneFamily(name, mapColor),
                registerBasicStoneFamily("polished_" + name, mapColor),
                registerBrickStoneFamily(name + "_brick", mapColor),
                registerBasicStoneFamily("mossy_" + name + "_brick", mapColor)
        );
    }

    private static Supplier<BlockFamily> registerFullStoneFamily(String name, MapColor mapColor) {
        return registerStoneFamily(name, mapColor, true, true);
    }

    private static Supplier<BlockFamily> registerBasicStoneFamily(String name, MapColor mapColor) {
        return registerStoneFamily(name, mapColor, false, false);
    }

    private static Supplier<BlockFamily> registerBrickStoneFamily(String name, MapColor mapColor) {
        return registerStoneFamily(name, mapColor, true, false);
    }

    private static Supplier<BlockFamily> registerStoneFamily(
            String name,
            MapColor mapColor,
            boolean hasPressurePlate,
            boolean hasButton
    ) {
        DeferredBlock<Block> block = registerStone(name, mapColor);
        DeferredBlock<StairBlock> stairs = registerStairs(name + "_stairs", block);
        DeferredBlock<SlabBlock> slab = registerSlab(name + "_slab", mapColor);
        DeferredBlock<WallBlock> wall = registerWall(name + "_wall", mapColor);
        DeferredBlock<VerticalSlabBlock> verticalSlab = registerVerticalSlab(name + "_vertical_slab", mapColor);

        DeferredBlock<PressurePlateBlock> pressurePlate =
                hasPressurePlate ? registerPressurePlate(name + "_pressure_plate", block) : null;

        DeferredBlock<ButtonBlock> button =
                hasButton ? registerButton(name + "_button", block) : null;

        return Suppliers.memoize(() -> {
            BlockFamily.Builder builder = new BlockFamily.Builder(block.get())
                    .stairs(stairs.get())
                    .slab(slab.get())
                    .wall(wall.get())
                    .generateStonecutterRecipe();

            if (pressurePlate != null) {
                builder.pressurePlate(pressurePlate.get());
            }

            if (button != null) {
                builder.button(button.get());
            }

            BlockFamily family = builder.getFamily();

            if (family instanceof ExtendedBlockFamily e) {
                e.setVerticalSlab(verticalSlab.get());
            }

            return family;
        });
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

    private static DeferredBlock<PressurePlateBlock> registerPressurePlate(String name, DeferredBlock<? extends Block> base) {
        Identifier id = Identifier.fromNamespaceAndPath(Craton.MODID, name);

        DeferredBlock<PressurePlateBlock> block = BLOCKS.register(name, () -> new PressurePlateBlock(
                BlockSetType.STONE,
                BlockBehaviour.Properties.ofFullCopy(base.get())
                        .setId(ResourceKey.create(Registries.BLOCK, id))
        ));

        registerBlockItem(name, block);
        return block;
    }

    private static DeferredBlock<ButtonBlock> registerButton(String name, DeferredBlock<? extends Block> base) {
        Identifier id = Identifier.fromNamespaceAndPath(Craton.MODID, name);

        DeferredBlock<ButtonBlock> block = BLOCKS.register(name, () -> new ButtonBlock(
                BlockSetType.STONE,
                20,
                BlockBehaviour.Properties.ofFullCopy(base.get())
                        .setId(ResourceKey.create(Registries.BLOCK, id))
        ));

        registerBlockItem(name, block);
        return block;
    }

    private static DeferredBlock<VerticalSlabBlock> registerVerticalSlab(String name, MapColor mapColor) {
        Identifier id = Identifier.fromNamespaceAndPath(Craton.MODID, name);

        DeferredBlock<VerticalSlabBlock> block = BLOCKS.register(name, () -> new VerticalSlabBlock(
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

    private static DeferredBlock<Block> registerOreBlock(String name) {
        return registerPlaceholderBlock(name, Blocks.STONE);
    }

    private static DeferredBlock<Block> registerPlaceholderBlock(String name, Block template) {
        Identifier id = Identifier.fromNamespaceAndPath(Craton.MODID, name);
        DeferredBlock<Block> block = BLOCKS.register(name, () -> new Block(
                BlockBehaviour.Properties.ofFullCopy(template)
                        .setId(ResourceKey.create(Registries.BLOCK, id))
        ));
        registerBlockItem(name, block);
        return block;
    }
}
