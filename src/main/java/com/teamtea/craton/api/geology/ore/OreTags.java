package com.teamtea.craton.api.geology.ore;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/** Common ore and host-rock tags shared by block and item DataGen. */
public final class OreTags {
    private OreTags() {}

    public static final class Blocks {
        private Blocks() {}
        public static final TagKey<Block> ORES_GOLD = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/gold"));
        public static final TagKey<Block> ORES_IN_GROUND_GRAVEL = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores_in_ground/gravel"));
        public static final TagKey<Block> ORES_IN_GROUND_SAND = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores_in_ground/sand"));
        public static final TagKey<Block> ORES_IRON = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/iron"));
        public static final TagKey<Block> ORES_IN_GROUND_GNEISS = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores_in_ground/gneiss"));
        public static final TagKey<Block> ORES_ALUMINUM = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/aluminum"));
        public static final TagKey<Block> ORES_IN_GROUND_BASALT = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores_in_ground/basalt"));
        public static final TagKey<Block> ORES_REDSTONE = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/redstone"));
        public static final TagKey<Block> ORES_IN_GROUND_ECLOGITE = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores_in_ground/eclogite"));
        public static final TagKey<Block> ORES_DIAMOND = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/diamond"));
        public static final TagKey<Block> ORES_IN_GROUND_KIMBERLITE = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores_in_ground/kimberlite"));
        public static final TagKey<Block> ORES_SILVER = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/silver"));
        public static final TagKey<Block> ORES_IN_GROUND_ANDESITE = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores_in_ground/andesite"));
        public static final TagKey<Block> ORES_IN_GROUND_QUARTZ_BLOCK = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores_in_ground/quartz_block"));
        public static final TagKey<Block> ORES_IN_GROUND_RHYOLITE = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores_in_ground/rhyolite"));
        public static final TagKey<Block> ORES_IN_GROUND_RED_SAND = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores_in_ground/red_sand"));
        public static final TagKey<Block> ORES_FLUORITE = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/fluorite"));
        public static final TagKey<Block> ORES_IN_GROUND_GRANITE = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores_in_ground/granite"));
        public static final TagKey<Block> ORES_IN_GROUND_PEGMATITE = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores_in_ground/pegmatite"));
        public static final TagKey<Block> ORES_TIN = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/tin"));
        public static final TagKey<Block> ORES_TUNGSTEN = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/tungsten"));
        public static final TagKey<Block> ORES_COAL = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/coal"));
        public static final TagKey<Block> ORES_IN_GROUND_LIMESTONE = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores_in_ground/limestone"));
        public static final TagKey<Block> ORES_COPPER = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/copper"));
        public static final TagKey<Block> ORES_NICKEL = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/nickel"));
        public static final TagKey<Block> ORES_IN_GROUND_GABBRO = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores_in_ground/gabbro"));
        public static final TagKey<Block> ORES_OSMIUM = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/osmium"));
        public static final TagKey<Block> ORES_PLATINUM = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/platinum"));
        public static final TagKey<Block> ORES_LAPIS = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/lapis"));
        public static final TagKey<Block> ORES_IN_GROUND_MARBLE = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores_in_ground/marble"));
        public static final TagKey<Block> ORES_IN_GROUND_METEORITE = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores_in_ground/meteorite"));
        public static final TagKey<Block> ORES_IN_GROUND_DIRT = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores_in_ground/dirt"));
        public static final TagKey<Block> ORES_IN_GROUND_GRASS_BLOCK = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores_in_ground/grass_block"));
        public static final TagKey<Block> ORES_IN_GROUND_MUD = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores_in_ground/mud"));
        public static final TagKey<Block> ORES_EMERALD = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/emerald"));
        public static final TagKey<Block> ORES_QUARTZ = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/quartz"));
        public static final TagKey<Block> ORES_IN_GROUND_DIORITE = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores_in_ground/diorite"));
        public static final TagKey<Block> ORES_PYRITE = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/pyrite"));
        public static final TagKey<Block> ORES_LEAD = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/lead"));
        public static final TagKey<Block> ORES_ZINC = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/zinc"));
        public static final TagKey<Block> ORES_IN_GROUND_SHALE = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores_in_ground/shale"));
        public static final TagKey<Block> ORES_URANIUM = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/uranium"));
        public static final TagKey<Block> ORES_IN_GROUND_ARKOSE_SANDSTONE = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores_in_ground/arkose_sandstone"));
        public static final TagKey<Block> ORES_IN_GROUND_SKARN = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores_in_ground/skarn"));
        public static final TagKey<Block> ORES_JADE = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/jade"));
        public static final TagKey<Block> ORES_IN_GROUND_JADEITITE = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores_in_ground/jadeitite"));
        public static final TagKey<Block> ORES_IN_GROUND_TUFF = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores_in_ground/tuff"));
    }

    public static final class Items {
        private Items() {}
        public static final TagKey<Item> ORES_GOLD = TagKey.create(Registries.ITEM, Identifier.parse("c:ores/gold"));
        public static final TagKey<Item> ORES_IN_GROUND_GRAVEL = TagKey.create(Registries.ITEM, Identifier.parse("c:ores_in_ground/gravel"));
        public static final TagKey<Item> ORES_IN_GROUND_SAND = TagKey.create(Registries.ITEM, Identifier.parse("c:ores_in_ground/sand"));
        public static final TagKey<Item> ORES_IRON = TagKey.create(Registries.ITEM, Identifier.parse("c:ores/iron"));
        public static final TagKey<Item> ORES_IN_GROUND_GNEISS = TagKey.create(Registries.ITEM, Identifier.parse("c:ores_in_ground/gneiss"));
        public static final TagKey<Item> ORES_ALUMINUM = TagKey.create(Registries.ITEM, Identifier.parse("c:ores/aluminum"));
        public static final TagKey<Item> ORES_IN_GROUND_BASALT = TagKey.create(Registries.ITEM, Identifier.parse("c:ores_in_ground/basalt"));
        public static final TagKey<Item> ORES_REDSTONE = TagKey.create(Registries.ITEM, Identifier.parse("c:ores/redstone"));
        public static final TagKey<Item> ORES_IN_GROUND_ECLOGITE = TagKey.create(Registries.ITEM, Identifier.parse("c:ores_in_ground/eclogite"));
        public static final TagKey<Item> ORES_DIAMOND = TagKey.create(Registries.ITEM, Identifier.parse("c:ores/diamond"));
        public static final TagKey<Item> ORES_IN_GROUND_KIMBERLITE = TagKey.create(Registries.ITEM, Identifier.parse("c:ores_in_ground/kimberlite"));
        public static final TagKey<Item> ORES_SILVER = TagKey.create(Registries.ITEM, Identifier.parse("c:ores/silver"));
        public static final TagKey<Item> ORES_IN_GROUND_ANDESITE = TagKey.create(Registries.ITEM, Identifier.parse("c:ores_in_ground/andesite"));
        public static final TagKey<Item> ORES_IN_GROUND_QUARTZ_BLOCK = TagKey.create(Registries.ITEM, Identifier.parse("c:ores_in_ground/quartz_block"));
        public static final TagKey<Item> ORES_IN_GROUND_RHYOLITE = TagKey.create(Registries.ITEM, Identifier.parse("c:ores_in_ground/rhyolite"));
        public static final TagKey<Item> ORES_IN_GROUND_RED_SAND = TagKey.create(Registries.ITEM, Identifier.parse("c:ores_in_ground/red_sand"));
        public static final TagKey<Item> ORES_FLUORITE = TagKey.create(Registries.ITEM, Identifier.parse("c:ores/fluorite"));
        public static final TagKey<Item> ORES_IN_GROUND_GRANITE = TagKey.create(Registries.ITEM, Identifier.parse("c:ores_in_ground/granite"));
        public static final TagKey<Item> ORES_IN_GROUND_PEGMATITE = TagKey.create(Registries.ITEM, Identifier.parse("c:ores_in_ground/pegmatite"));
        public static final TagKey<Item> ORES_TIN = TagKey.create(Registries.ITEM, Identifier.parse("c:ores/tin"));
        public static final TagKey<Item> ORES_TUNGSTEN = TagKey.create(Registries.ITEM, Identifier.parse("c:ores/tungsten"));
        public static final TagKey<Item> ORES_COAL = TagKey.create(Registries.ITEM, Identifier.parse("c:ores/coal"));
        public static final TagKey<Item> ORES_IN_GROUND_LIMESTONE = TagKey.create(Registries.ITEM, Identifier.parse("c:ores_in_ground/limestone"));
        public static final TagKey<Item> ORES_COPPER = TagKey.create(Registries.ITEM, Identifier.parse("c:ores/copper"));
        public static final TagKey<Item> ORES_NICKEL = TagKey.create(Registries.ITEM, Identifier.parse("c:ores/nickel"));
        public static final TagKey<Item> ORES_IN_GROUND_GABBRO = TagKey.create(Registries.ITEM, Identifier.parse("c:ores_in_ground/gabbro"));
        public static final TagKey<Item> ORES_OSMIUM = TagKey.create(Registries.ITEM, Identifier.parse("c:ores/osmium"));
        public static final TagKey<Item> ORES_PLATINUM = TagKey.create(Registries.ITEM, Identifier.parse("c:ores/platinum"));
        public static final TagKey<Item> ORES_LAPIS = TagKey.create(Registries.ITEM, Identifier.parse("c:ores/lapis"));
        public static final TagKey<Item> ORES_IN_GROUND_MARBLE = TagKey.create(Registries.ITEM, Identifier.parse("c:ores_in_ground/marble"));
        public static final TagKey<Item> ORES_IN_GROUND_METEORITE = TagKey.create(Registries.ITEM, Identifier.parse("c:ores_in_ground/meteorite"));
        public static final TagKey<Item> ORES_IN_GROUND_DIRT = TagKey.create(Registries.ITEM, Identifier.parse("c:ores_in_ground/dirt"));
        public static final TagKey<Item> ORES_IN_GROUND_GRASS_BLOCK = TagKey.create(Registries.ITEM, Identifier.parse("c:ores_in_ground/grass_block"));
        public static final TagKey<Item> ORES_IN_GROUND_MUD = TagKey.create(Registries.ITEM, Identifier.parse("c:ores_in_ground/mud"));
        public static final TagKey<Item> ORES_EMERALD = TagKey.create(Registries.ITEM, Identifier.parse("c:ores/emerald"));
        public static final TagKey<Item> ORES_QUARTZ = TagKey.create(Registries.ITEM, Identifier.parse("c:ores/quartz"));
        public static final TagKey<Item> ORES_IN_GROUND_DIORITE = TagKey.create(Registries.ITEM, Identifier.parse("c:ores_in_ground/diorite"));
        public static final TagKey<Item> ORES_PYRITE = TagKey.create(Registries.ITEM, Identifier.parse("c:ores/pyrite"));
        public static final TagKey<Item> ORES_LEAD = TagKey.create(Registries.ITEM, Identifier.parse("c:ores/lead"));
        public static final TagKey<Item> ORES_ZINC = TagKey.create(Registries.ITEM, Identifier.parse("c:ores/zinc"));
        public static final TagKey<Item> ORES_IN_GROUND_SHALE = TagKey.create(Registries.ITEM, Identifier.parse("c:ores_in_ground/shale"));
        public static final TagKey<Item> ORES_URANIUM = TagKey.create(Registries.ITEM, Identifier.parse("c:ores/uranium"));
        public static final TagKey<Item> ORES_IN_GROUND_ARKOSE_SANDSTONE = TagKey.create(Registries.ITEM, Identifier.parse("c:ores_in_ground/arkose_sandstone"));
        public static final TagKey<Item> ORES_IN_GROUND_SKARN = TagKey.create(Registries.ITEM, Identifier.parse("c:ores_in_ground/skarn"));
        public static final TagKey<Item> ORES_JADE = TagKey.create(Registries.ITEM, Identifier.parse("c:ores/jade"));
        public static final TagKey<Item> ORES_IN_GROUND_JADEITITE = TagKey.create(Registries.ITEM, Identifier.parse("c:ores_in_ground/jadeitite"));
        public static final TagKey<Item> ORES_IN_GROUND_TUFF = TagKey.create(Registries.ITEM, Identifier.parse("c:ores_in_ground/tuff"));
    }
}
