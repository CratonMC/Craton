package com.teamtea.craton.data.tag;

import com.teamtea.craton.api.block.ExtendedBlockFamily;
import com.teamtea.craton.api.geology.ore.OreTags;
import com.teamtea.craton.common.registry.CratonBlocks;
import com.teamtea.craton.common.block.StoneCollection;
import com.teamtea.craton.common.registry.CratonTags;
import com.teamtea.craton.Craton;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class CBlockTagProvider extends BlockTagsProvider {
    public CBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId) {
        super(output, lookupProvider, modId);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        tag(OreTags.Blocks.ORES_GOLD)
                .add(CratonBlocks.auriferous_gravel.value())
                .add(CratonBlocks.auriferous_sand.value())
                .add(CratonBlocks.epithermal_au_ag_ore.value())
                .add(CratonBlocks.porphyry_gold_ore.value())
                .add(CratonBlocks.quartz_vein_gold_ore.value())
                .add(CratonBlocks.skarn_gold_ore.value());
        tag(OreTags.Blocks.ORES_IN_GROUND_GRAVEL)
                .add(CratonBlocks.auriferous_gravel.value())
                .add(CratonBlocks.ferriferous_gravel.value());
        tag(OreTags.Blocks.ORES_IN_GROUND_SAND)
                .add(CratonBlocks.auriferous_sand.value())
                .add(CratonBlocks.ferriferous_sand.value());
        tag(OreTags.Blocks.ORES_IRON)
                .add(CratonBlocks.banded_iron_gneiss_ore.value())
                .add(CratonBlocks.ferriferous_gravel.value())
                .add(CratonBlocks.ferriferous_red_sand.value())
                .add(CratonBlocks.ferriferous_sand.value())
                .add(CratonBlocks.meteoric_iron_ore.value())
                .add(CratonBlocks.skarn_iron_ore.value());
        tag(OreTags.Blocks.ORES_IN_GROUND_GNEISS)
                .add(CratonBlocks.banded_iron_gneiss_ore.value());
        tag(OreTags.Blocks.ORES_ALUMINUM)
                .add(CratonBlocks.bauxite_ore.value());
        tag(OreTags.Blocks.ORES_IN_GROUND_BASALT)
                .add(CratonBlocks.bauxite_ore.value())
                .add(CratonBlocks.vms_copper_ore.value())
                .add(CratonBlocks.vms_pb_ag_ore.value())
                .add(CratonBlocks.vms_zinc_ore.value());
        tag(OreTags.Blocks.ORES_REDSTONE)
                .add(CratonBlocks.deep_hydrothermal_redstone_ore.value());
        tag(OreTags.Blocks.ORES_IN_GROUND_ECLOGITE)
                .add(CratonBlocks.deep_hydrothermal_redstone_ore.value());
        tag(OreTags.Blocks.ORES_DIAMOND)
                .add(CratonBlocks.diamond_bearing_kimberlite.value())
                .add(CratonBlocks.diamond_rich_kimberlite.value());
        tag(OreTags.Blocks.ORES_IN_GROUND_KIMBERLITE)
                .add(CratonBlocks.diamond_bearing_kimberlite.value())
                .add(CratonBlocks.diamond_rich_kimberlite.value());
        tag(OreTags.Blocks.ORES_SILVER)
                .add(CratonBlocks.epithermal_au_ag_ore.value())
                .add(CratonBlocks.vms_pb_ag_ore.value());
        tag(OreTags.Blocks.ORES_IN_GROUND_ANDESITE)
                .add(CratonBlocks.epithermal_au_ag_ore.value())
                .add(CratonBlocks.vms_copper_ore.value())
                .add(CratonBlocks.vms_pb_ag_ore.value())
                .add(CratonBlocks.vms_zinc_ore.value());
        tag(OreTags.Blocks.ORES_IN_GROUND_QUARTZ_BLOCK)
                .add(CratonBlocks.epithermal_au_ag_ore.value())
                .add(CratonBlocks.fluorite_vein_ore.value())
                .add(CratonBlocks.quartz_vein_gold_ore.value())
                .add(CratonBlocks.quartz_vein_pyrite_ore.value());
        tag(OreTags.Blocks.ORES_IN_GROUND_RHYOLITE)
                .add(CratonBlocks.epithermal_au_ag_ore.value());
        tag(OreTags.Blocks.ORES_IN_GROUND_RED_SAND)
                .add(CratonBlocks.ferriferous_red_sand.value());
        tag(OreTags.Blocks.ORES_FLUORITE)
                .add(CratonBlocks.fluorite_vein_ore.value());
        tag(OreTags.Blocks.ORES_IN_GROUND_GRANITE)
                .add(CratonBlocks.fluorite_vein_ore.value())
                .add(CratonBlocks.granite_cassiterite_ore.value())
                .add(CratonBlocks.granite_wolframite_ore.value())
                .add(CratonBlocks.quartz_vein_gold_ore.value())
                .add(CratonBlocks.quartz_vein_pyrite_ore.value());
        tag(OreTags.Blocks.ORES_IN_GROUND_PEGMATITE)
                .add(CratonBlocks.fluorite_vein_ore.value())
                .add(CratonBlocks.pegmatite_emerald_ore.value())
                .add(CratonBlocks.pegmatite_quartz_ore.value())
                .add(CratonBlocks.quartz_vein_gold_ore.value())
                .add(CratonBlocks.quartz_vein_pyrite_ore.value());
        tag(OreTags.Blocks.ORES_TIN)
                .add(CratonBlocks.granite_cassiterite_ore.value())
                .add(CratonBlocks.skarn_w_sn_ore.value());
        tag(OreTags.Blocks.ORES_TUNGSTEN)
                .add(CratonBlocks.granite_wolframite_ore.value())
                .add(CratonBlocks.skarn_w_sn_ore.value());
        tag(OreTags.Blocks.ORES_COAL)
                .add(CratonBlocks.limestone_coal_ore.value())
                .add(CratonBlocks.shale_coal_ore.value());
        tag(OreTags.Blocks.ORES_IN_GROUND_LIMESTONE)
                .add(CratonBlocks.limestone_coal_ore.value())
                .add(CratonBlocks.sedimentary_copper_ore.value());
        tag(OreTags.Blocks.ORES_COPPER)
                .add(CratonBlocks.magmatic_ni_cu_ore.value())
                .add(CratonBlocks.porphyry_copper_ore.value())
                .add(CratonBlocks.sedimentary_copper_ore.value())
                .add(CratonBlocks.skarn_copper_ore.value())
                .add(CratonBlocks.vms_copper_ore.value());
        tag(OreTags.Blocks.ORES_NICKEL)
                .add(CratonBlocks.magmatic_ni_cu_ore.value());
        tag(OreTags.Blocks.ORES_IN_GROUND_GABBRO)
                .add(CratonBlocks.magmatic_ni_cu_ore.value())
                .add(CratonBlocks.magmatic_pge_ore.value())
                .add(CratonBlocks.vms_copper_ore.value())
                .add(CratonBlocks.vms_pb_ag_ore.value())
                .add(CratonBlocks.vms_zinc_ore.value());
        tag(OreTags.Blocks.ORES_OSMIUM)
                .add(CratonBlocks.magmatic_pge_ore.value());
        tag(OreTags.Blocks.ORES_PLATINUM)
                .add(CratonBlocks.magmatic_pge_ore.value());
        tag(OreTags.Blocks.ORES_LAPIS)
                .add(CratonBlocks.metasomatic_lapis_ore.value());
        tag(OreTags.Blocks.ORES_IN_GROUND_MARBLE)
                .add(CratonBlocks.metasomatic_lapis_ore.value());
        tag(OreTags.Blocks.ORES_IN_GROUND_METEORITE)
                .add(CratonBlocks.meteoric_iron_ore.value());
        tag(OreTags.Blocks.ORES_IN_GROUND_DIRT)
                .add(CratonBlocks.peat.value());
        tag(OreTags.Blocks.ORES_IN_GROUND_GRASS_BLOCK)
                .add(CratonBlocks.peat.value());
        tag(OreTags.Blocks.ORES_IN_GROUND_MUD)
                .add(CratonBlocks.peat.value());
        tag(OreTags.Blocks.ORES_EMERALD)
                .add(CratonBlocks.pegmatite_emerald_ore.value());
        tag(OreTags.Blocks.ORES_QUARTZ)
                .add(CratonBlocks.pegmatite_quartz_ore.value());
        tag(OreTags.Blocks.ORES_IN_GROUND_DIORITE)
                .add(CratonBlocks.porphyry_copper_ore.value())
                .add(CratonBlocks.porphyry_gold_ore.value())
                .add(CratonBlocks.porphyry_sulfide_ore.value());
        tag(OreTags.Blocks.ORES_PYRITE)
                .add(CratonBlocks.porphyry_sulfide_ore.value())
                .add(CratonBlocks.quartz_vein_pyrite_ore.value());
        tag(OreTags.Blocks.ORES_LEAD)
                .add(CratonBlocks.sedimentary_pb_zn_ore.value())
                .add(CratonBlocks.skarn_pb_zn_ore.value())
                .add(CratonBlocks.vms_pb_ag_ore.value());
        tag(OreTags.Blocks.ORES_ZINC)
                .add(CratonBlocks.sedimentary_pb_zn_ore.value())
                .add(CratonBlocks.skarn_pb_zn_ore.value())
                .add(CratonBlocks.vms_zinc_ore.value());
        tag(OreTags.Blocks.ORES_IN_GROUND_SHALE)
                .add(CratonBlocks.sedimentary_pb_zn_ore.value())
                .add(CratonBlocks.shale_coal_ore.value());
        tag(OreTags.Blocks.ORES_URANIUM)
                .add(CratonBlocks.sedimentary_uranium_ore.value());
        tag(OreTags.Blocks.ORES_IN_GROUND_ARKOSE_SANDSTONE)
                .add(CratonBlocks.sedimentary_uranium_ore.value());
        tag(OreTags.Blocks.ORES_IN_GROUND_SKARN)
                .add(CratonBlocks.skarn_copper_ore.value())
                .add(CratonBlocks.skarn_gold_ore.value())
                .add(CratonBlocks.skarn_iron_ore.value())
                .add(CratonBlocks.skarn_pb_zn_ore.value())
                .add(CratonBlocks.skarn_w_sn_ore.value());
        tag(OreTags.Blocks.ORES_JADE)
                .add(CratonBlocks.suspicious_jadeitite.value());
        tag(OreTags.Blocks.ORES_IN_GROUND_JADEITITE)
                .add(CratonBlocks.suspicious_jadeitite.value());
        tag(OreTags.Blocks.ORES_IN_GROUND_TUFF)
                .add(CratonBlocks.vms_copper_ore.value())
                .add(CratonBlocks.vms_pb_ag_ore.value())
                .add(CratonBlocks.vms_zinc_ore.value());
        for (StoneCollection collection : CratonBlocks.STONE_COLLECTIONS) {
            // addStoneFamilyTags(collection.origin().get());
            // addStoneFamilyTags(collection.polished().get());
            for (BlockFamily blockFamily : collection.getAll()) {
                addStoneFamilyTags(blockFamily);
            }
            tag(collection == CratonBlocks.ECLOGITE
                    ? BlockTags.DEEPSLATE_ORE_REPLACEABLES
                    : BlockTags.STONE_ORE_REPLACEABLES).add(collection.getOrigin().getBaseBlock());
        }
        TagKey<Block> allOres = TagKey.create(Registries.BLOCK, Craton.rl("ores"));
        TagKey<Block> commonOres = TagKey.create(Registries.BLOCK, net.minecraft.resources.Identifier.parse("c:ores"));
        for (var holder : CratonBlocks.ORE_BLOCKS) {
            Block ore = holder.value();
            tag(allOres).add(ore);
            tag(commonOres).add(ore);
            if (ore == CratonBlocks.peat.value() || ore == CratonBlocks.auriferous_gravel.value()
                    || ore == CratonBlocks.ferriferous_gravel.value()
                    || ore == CratonBlocks.auriferous_sand.value()
                    || ore == CratonBlocks.ferriferous_sand.value()
                    || ore == CratonBlocks.ferriferous_red_sand.value()) {
                tag(BlockTags.MINEABLE_WITH_SHOVEL).add(ore);
            } else {
                tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ore);
                tag(BlockTags.NEEDS_STONE_TOOL).add(ore);
            }
        }
    }

    private void addStoneFamilyTags(BlockFamily family) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(family.getBaseBlock())
                .add(family.get(BlockFamily.Variant.STAIRS))
                .add(family.get(BlockFamily.Variant.SLAB))
                .add(family.get(BlockFamily.Variant.WALL))
                .add(ExtendedBlockFamily.getVerticalSlab(family));

        tag(BlockTags.NEEDS_STONE_TOOL)
                .add(family.getBaseBlock())
                .add(family.get(BlockFamily.Variant.STAIRS))
                .add(family.get(BlockFamily.Variant.SLAB))
                .add(family.get(BlockFamily.Variant.WALL))
                .add(ExtendedBlockFamily.getVerticalSlab(family));

        tag(BlockTags.WALLS)
                .add(family.get(BlockFamily.Variant.WALL));

        tag(CratonTags.Blocks.VERTICAL_SLABS).add(ExtendedBlockFamily.getVerticalSlab(family));

    }
    protected record Appender(TagAppender<Block> app) implements TagAppender<Block> {
        @Override
        public Appender add(ResourceKey<Block> element) {
            app.add(element);
            return this;
        }

        @Override
        public Appender addOptional(ResourceKey<Block> element) {
            app.addOptional(element);
            return this;
        }

        @Override
        public Appender addTag(TagKey<Block> tag) {
            app.addTag(tag);
            return this;
        }

        @Override
        public Appender addOptionalTag(TagKey<Block> tag) {
            app.addOptionalTag(tag);
            return this;
        }

        @Override
        public Appender add(TagEntry entry) {
            app.add(entry);
            return this;
        }

        @Override
        public Appender replace(boolean value) {
            app.replace(value);
            return this;
        }

        @Override
        public Appender remove(ResourceKey<Block> element) {
            app.remove(element);
            return this;
        }

        @Override
        public Appender remove(TagKey<Block> tag) {
            app.remove(tag);
            return this;
        }

        public Appender add(Block... blocks) {
            for (Block block : blocks) {
                add(BuiltInRegistries.BLOCK.wrapAsHolder(block).keyOrNull());
            }
            return this;
        }

        public Appender addAll(Iterable<Block> blocks) {
            for (Block block : blocks) {
                add(block);
            }
            return this;
        }
    }

    @Override
    protected Appender tag(TagKey<Block> tag) {
        return new Appender(super.tag(tag));
    }
}
