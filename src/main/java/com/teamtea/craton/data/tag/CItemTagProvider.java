package com.teamtea.craton.data.tag;

import com.teamtea.craton.Craton;
import com.teamtea.craton.api.block.ExtendedBlockFamily;
import com.teamtea.craton.api.geology.ore.OreTags;
import com.teamtea.craton.common.registry.CratonBlocks;
import com.teamtea.craton.common.registry.CratonItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import com.teamtea.craton.common.block.StoneCollection;
import com.teamtea.craton.common.registry.CratonTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import org.jspecify.annotations.NonNull;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.concurrent.CompletableFuture;

public class CItemTagProvider extends ItemTagsProvider {

    public CItemTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> providerCompletableFuture) {
        super(packOutput, providerCompletableFuture, Craton.MODID);
    }

    @Override
    protected void addTags(HolderLookup.@NonNull Provider pProvider) {
        addMineralTags();
        for (StoneCollection collection : CratonBlocks.STONE_COLLECTIONS) {
            // addStoneFamilyTags(collection.origin().get());
            // addStoneFamilyTags(collection.polished().get());
            for (BlockFamily blockFamily : collection.getAll()) {
                addStoneFamilyTags(blockFamily);
            }
        }
    }

    private TagKey<Item> key(String path) {
        return TagKey.create(Registries.ITEM, Identifier.parse(path));
    }

    private void addMineralTags() {
        for (var ore : CratonBlocks.ORE_BLOCKS) {
            tag(key("craton:ore_items")).add(ore.value().asItem());
            tag(key("c:ores")).add(ore.value().asItem());
        }
        tag(OreTags.Items.ORES_GOLD)
                .add(CratonBlocks.auriferous_gravel.value().asItem())
                .add(CratonBlocks.auriferous_sand.value().asItem())
                .add(CratonBlocks.epithermal_au_ag_ore.value().asItem())
                .add(CratonBlocks.porphyry_gold_ore.value().asItem())
                .add(CratonBlocks.quartz_vein_gold_ore.value().asItem())
                .add(CratonBlocks.skarn_gold_ore.value().asItem());
        tag(OreTags.Items.ORES_IN_GROUND_GRAVEL)
                .add(CratonBlocks.auriferous_gravel.value().asItem())
                .add(CratonBlocks.ferriferous_gravel.value().asItem());
        tag(OreTags.Items.ORES_IN_GROUND_SAND)
                .add(CratonBlocks.auriferous_sand.value().asItem())
                .add(CratonBlocks.ferriferous_sand.value().asItem());
        tag(OreTags.Items.ORES_IRON)
                .add(CratonBlocks.banded_iron_gneiss_ore.value().asItem())
                .add(CratonBlocks.ferriferous_gravel.value().asItem())
                .add(CratonBlocks.ferriferous_red_sand.value().asItem())
                .add(CratonBlocks.ferriferous_sand.value().asItem())
                .add(CratonBlocks.meteoric_iron_ore.value().asItem())
                .add(CratonBlocks.skarn_iron_ore.value().asItem());
        tag(OreTags.Items.ORES_IN_GROUND_GNEISS)
                .add(CratonBlocks.banded_iron_gneiss_ore.value().asItem());
        tag(OreTags.Items.ORES_ALUMINUM)
                .add(CratonBlocks.bauxite_ore.value().asItem());
        tag(OreTags.Items.ORES_IN_GROUND_BASALT)
                .add(CratonBlocks.bauxite_ore.value().asItem())
                .add(CratonBlocks.vms_copper_ore.value().asItem())
                .add(CratonBlocks.vms_pb_ag_ore.value().asItem())
                .add(CratonBlocks.vms_zinc_ore.value().asItem());
        tag(OreTags.Items.ORES_REDSTONE)
                .add(CratonBlocks.deep_hydrothermal_redstone_ore.value().asItem());
        tag(OreTags.Items.ORES_IN_GROUND_ECLOGITE)
                .add(CratonBlocks.deep_hydrothermal_redstone_ore.value().asItem());
        tag(OreTags.Items.ORES_DIAMOND)
                .add(CratonBlocks.diamond_bearing_kimberlite.value().asItem())
                .add(CratonBlocks.diamond_rich_kimberlite.value().asItem());
        tag(OreTags.Items.ORES_IN_GROUND_KIMBERLITE)
                .add(CratonBlocks.diamond_bearing_kimberlite.value().asItem())
                .add(CratonBlocks.diamond_rich_kimberlite.value().asItem());
        tag(OreTags.Items.ORES_SILVER)
                .add(CratonBlocks.epithermal_au_ag_ore.value().asItem())
                .add(CratonBlocks.vms_pb_ag_ore.value().asItem());
        tag(OreTags.Items.ORES_IN_GROUND_ANDESITE)
                .add(CratonBlocks.epithermal_au_ag_ore.value().asItem())
                .add(CratonBlocks.vms_copper_ore.value().asItem())
                .add(CratonBlocks.vms_pb_ag_ore.value().asItem())
                .add(CratonBlocks.vms_zinc_ore.value().asItem());
        tag(OreTags.Items.ORES_IN_GROUND_QUARTZ_BLOCK)
                .add(CratonBlocks.epithermal_au_ag_ore.value().asItem())
                .add(CratonBlocks.fluorite_vein_ore.value().asItem())
                .add(CratonBlocks.quartz_vein_gold_ore.value().asItem())
                .add(CratonBlocks.quartz_vein_pyrite_ore.value().asItem());
        tag(OreTags.Items.ORES_IN_GROUND_RHYOLITE)
                .add(CratonBlocks.epithermal_au_ag_ore.value().asItem());
        tag(OreTags.Items.ORES_IN_GROUND_RED_SAND)
                .add(CratonBlocks.ferriferous_red_sand.value().asItem());
        tag(OreTags.Items.ORES_FLUORITE)
                .add(CratonBlocks.fluorite_vein_ore.value().asItem());
        tag(OreTags.Items.ORES_IN_GROUND_GRANITE)
                .add(CratonBlocks.fluorite_vein_ore.value().asItem())
                .add(CratonBlocks.granite_cassiterite_ore.value().asItem())
                .add(CratonBlocks.granite_wolframite_ore.value().asItem())
                .add(CratonBlocks.quartz_vein_gold_ore.value().asItem())
                .add(CratonBlocks.quartz_vein_pyrite_ore.value().asItem());
        tag(OreTags.Items.ORES_IN_GROUND_PEGMATITE)
                .add(CratonBlocks.fluorite_vein_ore.value().asItem())
                .add(CratonBlocks.pegmatite_emerald_ore.value().asItem())
                .add(CratonBlocks.pegmatite_quartz_ore.value().asItem())
                .add(CratonBlocks.quartz_vein_gold_ore.value().asItem())
                .add(CratonBlocks.quartz_vein_pyrite_ore.value().asItem());
        tag(OreTags.Items.ORES_TIN)
                .add(CratonBlocks.granite_cassiterite_ore.value().asItem())
                .add(CratonBlocks.skarn_w_sn_ore.value().asItem());
        tag(OreTags.Items.ORES_TUNGSTEN)
                .add(CratonBlocks.granite_wolframite_ore.value().asItem())
                .add(CratonBlocks.skarn_w_sn_ore.value().asItem());
        tag(OreTags.Items.ORES_COAL)
                .add(CratonBlocks.limestone_coal_ore.value().asItem())
                .add(CratonBlocks.shale_coal_ore.value().asItem());
        tag(OreTags.Items.ORES_IN_GROUND_LIMESTONE)
                .add(CratonBlocks.limestone_coal_ore.value().asItem())
                .add(CratonBlocks.sedimentary_copper_ore.value().asItem());
        tag(OreTags.Items.ORES_COPPER)
                .add(CratonBlocks.magmatic_ni_cu_ore.value().asItem())
                .add(CratonBlocks.porphyry_copper_ore.value().asItem())
                .add(CratonBlocks.sedimentary_copper_ore.value().asItem())
                .add(CratonBlocks.skarn_copper_ore.value().asItem())
                .add(CratonBlocks.vms_copper_ore.value().asItem());
        tag(OreTags.Items.ORES_NICKEL)
                .add(CratonBlocks.magmatic_ni_cu_ore.value().asItem());
        tag(OreTags.Items.ORES_IN_GROUND_GABBRO)
                .add(CratonBlocks.magmatic_ni_cu_ore.value().asItem())
                .add(CratonBlocks.magmatic_pge_ore.value().asItem())
                .add(CratonBlocks.vms_copper_ore.value().asItem())
                .add(CratonBlocks.vms_pb_ag_ore.value().asItem())
                .add(CratonBlocks.vms_zinc_ore.value().asItem());
        tag(OreTags.Items.ORES_OSMIUM)
                .add(CratonBlocks.magmatic_pge_ore.value().asItem());
        tag(OreTags.Items.ORES_PLATINUM)
                .add(CratonBlocks.magmatic_pge_ore.value().asItem());
        tag(OreTags.Items.ORES_LAPIS)
                .add(CratonBlocks.metasomatic_lapis_ore.value().asItem());
        tag(OreTags.Items.ORES_IN_GROUND_MARBLE)
                .add(CratonBlocks.metasomatic_lapis_ore.value().asItem());
        tag(OreTags.Items.ORES_IN_GROUND_METEORITE)
                .add(CratonBlocks.meteoric_iron_ore.value().asItem());
        tag(OreTags.Items.ORES_IN_GROUND_DIRT)
                .add(CratonBlocks.peat.value().asItem());
        tag(OreTags.Items.ORES_IN_GROUND_GRASS_BLOCK)
                .add(CratonBlocks.peat.value().asItem());
        tag(OreTags.Items.ORES_IN_GROUND_MUD)
                .add(CratonBlocks.peat.value().asItem());
        tag(OreTags.Items.ORES_EMERALD)
                .add(CratonBlocks.pegmatite_emerald_ore.value().asItem());
        tag(OreTags.Items.ORES_QUARTZ)
                .add(CratonBlocks.pegmatite_quartz_ore.value().asItem());
        tag(OreTags.Items.ORES_IN_GROUND_DIORITE)
                .add(CratonBlocks.porphyry_copper_ore.value().asItem())
                .add(CratonBlocks.porphyry_gold_ore.value().asItem())
                .add(CratonBlocks.porphyry_sulfide_ore.value().asItem());
        tag(OreTags.Items.ORES_PYRITE)
                .add(CratonBlocks.porphyry_sulfide_ore.value().asItem())
                .add(CratonBlocks.quartz_vein_pyrite_ore.value().asItem());
        tag(OreTags.Items.ORES_LEAD)
                .add(CratonBlocks.sedimentary_pb_zn_ore.value().asItem())
                .add(CratonBlocks.skarn_pb_zn_ore.value().asItem())
                .add(CratonBlocks.vms_pb_ag_ore.value().asItem());
        tag(OreTags.Items.ORES_ZINC)
                .add(CratonBlocks.sedimentary_pb_zn_ore.value().asItem())
                .add(CratonBlocks.skarn_pb_zn_ore.value().asItem())
                .add(CratonBlocks.vms_zinc_ore.value().asItem());
        tag(OreTags.Items.ORES_IN_GROUND_SHALE)
                .add(CratonBlocks.sedimentary_pb_zn_ore.value().asItem())
                .add(CratonBlocks.shale_coal_ore.value().asItem());
        tag(OreTags.Items.ORES_URANIUM)
                .add(CratonBlocks.sedimentary_uranium_ore.value().asItem());
        tag(OreTags.Items.ORES_IN_GROUND_ARKOSE_SANDSTONE)
                .add(CratonBlocks.sedimentary_uranium_ore.value().asItem());
        tag(OreTags.Items.ORES_IN_GROUND_SKARN)
                .add(CratonBlocks.skarn_copper_ore.value().asItem())
                .add(CratonBlocks.skarn_gold_ore.value().asItem())
                .add(CratonBlocks.skarn_iron_ore.value().asItem())
                .add(CratonBlocks.skarn_pb_zn_ore.value().asItem())
                .add(CratonBlocks.skarn_w_sn_ore.value().asItem());
        tag(OreTags.Items.ORES_JADE)
                .add(CratonBlocks.suspicious_jadeitite.value().asItem());
        tag(OreTags.Items.ORES_IN_GROUND_JADEITITE)
                .add(CratonBlocks.suspicious_jadeitite.value().asItem());
        tag(OreTags.Items.ORES_IN_GROUND_TUFF)
                .add(CratonBlocks.vms_copper_ore.value().asItem())
                .add(CratonBlocks.vms_pb_ag_ore.value().asItem())
                .add(CratonBlocks.vms_zinc_ore.value().asItem());
        // All Craton raw mineral items share one collection tag; specialized mineral tags are deferred.
        String[] minerals = {"hematite", "magnetite", "iron_sulfide", "copper_oxide", "copper_sulfide",
                "gold", "silver", "nickel", "lead", "zinc", "tin", "tungsten", "uranium", "osmium"};
        Holder<Item>[] products = new Holder[]{
                CratonItems.RAW_HEMATITE, CratonItems.RAW_MAGNETITE, CratonItems.RAW_IRON_SULFIDE,
                CratonItems.RAW_COPPER_OXIDE, CratonItems.RAW_COPPER_SULFIDE,
                CratonItems.RAW_GOLD, CratonItems.RAW_SILVER, CratonItems.RAW_NICKEL,
                CratonItems.RAW_LEAD, CratonItems.RAW_ZINC, CratonItems.RAW_TIN,
                CratonItems.RAW_TUNGSTEN, CratonItems.RAW_URANIUM, CratonItems.RAW_OSMIUM};
        for (int i = 0; i < minerals.length; i++) {
            Item item = products[i].value();
            tag(key("craton:raw_minerals")).add(item);
            tag(key("c:raw_materials")).add(item);
            if (i == 0 || i == 1) {
                // Hematite and magnetite are iron-bearing raw materials.
                tag(key("c:raw_materials/iron")).add(item);
            } else if (i >= 5) {
                tag(key("c:raw_materials/" + minerals[i])).add(item);
            }
        }
    }

    private void addStoneFamilyTags(BlockFamily family) {
        tag(ItemTags.WALLS)
                .add(family.get(BlockFamily.Variant.WALL).asItem());

        tag(CratonTags.Items.VERTICAL_SLABS).add(ExtendedBlockFamily.getVerticalSlab(family).asItem()
        );
    }

    protected record Appender(TagAppender<Item> app) implements TagAppender<Item> {
        @Override
        public Appender add(ResourceKey<Item> element) {
            app.add(element);
            return this;
        }

        @Override
        public Appender addOptional(ResourceKey<Item> element) {
            app.addOptional(element);
            return this;
        }

        @Override
        public Appender addTag(TagKey<Item> tag) {
            app.addTag(tag);
            return this;
        }

        @Override
        public Appender addOptionalTag(TagKey<Item> tag) {
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
        public Appender remove(ResourceKey<Item> element) {
            app.remove(element);
            return this;
        }

        @Override
        public Appender remove(TagKey<Item> tag) {
            app.remove(tag);
            return this;
        }

        public Appender add(Item... items) {
            for (Item item : items) {
                add(BuiltInRegistries.ITEM.wrapAsHolder(item).keyOrNull());
            }
            return this;
        }

        public Appender add(Holder<Item>... items) {
            for (Holder<Item> item : items) {
                add(item.keyOrNull());
            }
            return this;
        }
    }

    @Override
    protected Appender tag(TagKey<Item> tag) {
        return new Appender(super.tag(tag));
    }
}
