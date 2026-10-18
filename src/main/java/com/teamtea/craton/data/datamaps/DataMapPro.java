package com.teamtea.craton.data.datamaps;

import com.teamtea.craton.common.registry.CratonBlocks;
import com.teamtea.craton.common.registry.CratonDataMaps;
import com.teamtea.craton.common.registry.CratonDataMaps.OreDrops;
import com.teamtea.craton.common.registry.CratonDataMaps.Product;
import com.teamtea.craton.common.registry.CratonItems;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.DataMapProvider;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DataMapPro extends DataMapProvider {
    public DataMapPro(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        add(CratonBlocks.banded_iron_gneiss_ore,
                List.of(product(CratonItems.RAW_HEMATITE.value(), "c:ingots/iron")),
                List.of());
        add(CratonBlocks.skarn_iron_ore,
                List.of(product(CratonItems.RAW_MAGNETITE.value(), "c:ingots/iron")),
                List.of());
        add(CratonBlocks.sedimentary_copper_ore,
                List.of(product(CratonItems.RAW_COPPER_OXIDE.value(), "c:ingots/copper")),
                List.of());
        add(CratonBlocks.limestone_coal_ore,
                List.of(product(Items.COAL)),
                List.of());
        add(CratonBlocks.shale_coal_ore,
                List.of(product(Items.COAL)),
                List.of());
        add(CratonBlocks.metasomatic_lapis_ore,
                List.of(product(Items.LAPIS_LAZULI)),
                List.of());
        add(CratonBlocks.pegmatite_quartz_ore,
                List.of(product(Items.QUARTZ)),
                List.of());
        add(CratonBlocks.pegmatite_emerald_ore,
                List.of(product(Items.EMERALD)),
                List.of());
        add(CratonBlocks.fluorite_vein_ore,
                List.of(product(CratonItems.FLUORITE.value())),
                List.of());
        add(CratonBlocks.deep_hydrothermal_redstone_ore,
                List.of(product(Items.REDSTONE)),
                List.of());
        add(CratonBlocks.bauxite_ore,
                List.of(product(CratonItems.BAUXITE.value(), "c:ingots/aluminum")),
                List.of());
        add(CratonBlocks.peat,
                List.of(product(CratonBlocks.peat.value().asItem())),
                List.of());
        add(CratonBlocks.sedimentary_uranium_ore,
                List.of(product(CratonItems.RAW_URANIUM.value(), "c:ingots/uranium")),
                List.of());
        add(CratonBlocks.granite_cassiterite_ore,
                List.of(product(CratonItems.RAW_TIN.value(), "c:ingots/tin")),
                List.of());
        add(CratonBlocks.granite_wolframite_ore,
                List.of(product(CratonItems.RAW_TUNGSTEN.value(), "c:ingots/tungsten")),
                List.of());
        add(CratonBlocks.auriferous_gravel,
                List.of(product(Items.GOLD_NUGGET, "c:ingots/gold")),
                List.of());
        add(CratonBlocks.auriferous_sand,
                List.of(product(Items.GOLD_NUGGET, "c:ingots/gold")),
                List.of());
        add(CratonBlocks.ferriferous_gravel,
                List.of(product(Items.IRON_NUGGET, "c:ingots/iron")),
                List.of());
        add(CratonBlocks.ferriferous_sand,
                List.of(product(Items.IRON_NUGGET, "c:ingots/iron")),
                List.of());
        add(CratonBlocks.diamond_bearing_kimberlite,
                List.of(product(CratonItems.DIAMOND_FRAGMENT.value())),
                List.of());
        add(CratonBlocks.diamond_rich_kimberlite,
                List.of(product(Items.DIAMOND)),
                List.of());
        add(CratonBlocks.meteoric_iron_ore,
                List.of(product(CratonItems.METEORIC_IRON.value(), "c:ingots/iron")),
                List.of());
        add(CratonBlocks.quartz_vein_gold_ore,
                List.of(product(Items.RAW_GOLD, "c:ingots/gold")),
                List.of(bonus(Items.QUARTZ, 0.2)));
        add(CratonBlocks.quartz_vein_pyrite_ore,
                List.of(product(CratonItems.RAW_IRON_SULFIDE.value(), "c:ingots/iron")),
                List.of(bonus(Items.RAW_GOLD, 0.05, "c:ingots/gold"), bonus(Items.QUARTZ, 0.2)));
        add(CratonBlocks.porphyry_sulfide_ore,
                List.of(product(CratonItems.RAW_IRON_SULFIDE.value(), "c:ingots/iron")),
                List.of(bonus(CratonItems.RAW_COPPER_SULFIDE.value(), 0.05, "c:ingots/copper"), bonus(Items.RAW_GOLD, 0.01, "c:ingots/gold")));
        add(CratonBlocks.porphyry_copper_ore,
                List.of(product(CratonItems.RAW_COPPER_SULFIDE.value(), "c:ingots/copper")),
                List.of(bonus(Items.RAW_GOLD, 0.03, "c:ingots/gold"), bonus(CratonItems.RAW_MOLYBDENUM.value(), 0.05, "c:ingots/molybdenum")));
        add(CratonBlocks.vms_copper_ore,
                List.of(product(CratonItems.RAW_COPPER_SULFIDE.value(), "c:ingots/copper")),
                List.of(bonus(CratonItems.RAW_ZINC.value(), 0.05, "c:ingots/zinc")));
        add(CratonBlocks.skarn_copper_ore,
                List.of(product(CratonItems.RAW_COPPER_SULFIDE.value(), "c:ingots/copper")),
                List.of(bonus(Items.RAW_GOLD, 0.03, "c:ingots/gold")));
        add(CratonBlocks.magmatic_ni_cu_ore,
                List.of(product(CratonItems.RAW_NICKEL.value(), "c:ingots/nickel")),
                List.of(bonus(CratonItems.RAW_COPPER_SULFIDE.value(), 0.25, "c:ingots/copper")));
        add(CratonBlocks.porphyry_gold_ore,
                List.of(product(Items.RAW_GOLD, "c:ingots/gold")),
                List.of(bonus(CratonItems.RAW_COPPER_SULFIDE.value(), 0.1, "c:ingots/copper")));
        add(CratonBlocks.skarn_gold_ore,
                List.of(product(Items.RAW_GOLD, "c:ingots/gold")),
                List.of());
        add(CratonBlocks.epithermal_au_ag_ore,
                List.of(product(Items.RAW_GOLD, 40, "c:ingots/gold"), product(CratonItems.RAW_SILVER.value(), 60, "c:ingots/silver")),
                List.of());
        add(CratonBlocks.vms_pb_ag_ore,
                List.of(product(CratonItems.RAW_LEAD.value(), "c:ingots/lead")),
                List.of(bonus(CratonItems.RAW_SILVER.value(), 0.1, "c:ingots/silver")));
        add(CratonBlocks.vms_zinc_ore,
                List.of(product(CratonItems.RAW_ZINC.value(), "c:ingots/zinc")),
                List.of(bonus(CratonItems.RAW_LEAD.value(), 0.05, "c:ingots/lead")));
        add(CratonBlocks.magmatic_pge_ore,
                List.of(product(CratonItems.RAW_OSMIUM.value(), "c:ingots/osmium")),
                List.of(bonus(CratonItems.RAW_NICKEL.value(), 0.05, "c:ingots/nickel")));
        add(CratonBlocks.sedimentary_pb_zn_ore,
                List.of(product(CratonItems.RAW_LEAD.value(), 40, "c:ingots/lead"), product(CratonItems.RAW_ZINC.value(), 60, "c:ingots/zinc")),
                List.of());
        add(CratonBlocks.skarn_pb_zn_ore,
                List.of(product(CratonItems.RAW_LEAD.value(), 40, "c:ingots/lead"), product(CratonItems.RAW_ZINC.value(), 60, "c:ingots/zinc")),
                List.of(bonus(CratonItems.RAW_SILVER.value(), 0.05, "c:ingots/silver")));
        add(CratonBlocks.skarn_w_sn_ore,
                List.of(product(CratonItems.RAW_TIN.value(), 50, "c:ingots/tin"), product(CratonItems.RAW_TUNGSTEN.value(), 50, "c:ingots/tungsten")),
                List.of());
        add(CratonBlocks.suspicious_jadeitite,
                List.of(product(CratonItems.GREEN_JADE.value()), product(CratonItems.WHITE_JADE.value()), product(CratonItems.LAVENDER_JADE.value())),
                List.of());
    }

    private void add(Holder<Block> block, List<Product> main, List<Product> bonus) {
        builder(CratonDataMaps.ORE_DROPS).add(block.unwrapKey().orElseThrow(), new OreDrops(main, bonus), false);
    }

    private Product product(Item item) {
        return new Product(item, 1, 1, 1, 1.0, true, Optional.empty());
    }

    private Product product(Item item, String requiredIngotTag) {
        return product(item, 1, requiredIngotTag);
    }

    private Product product(Item item, int weight, String requiredIngotTag) {
        return new Product(item, weight, 1, 1, 1.0, true, Optional.of(
                TagKey.create(Registries.ITEM,
                        Identifier.parse(requiredIngotTag))));
    }

    private Product bonus(Item item, double chance) {
        return new Product(item, 1, 1, 1, chance, true, Optional.empty());
    }

    private Product bonus(Item item, double chance, String requiredIngotTag) {
        return bonus(item, chance, true, requiredIngotTag);
    }

    private Product bonus(Item item, double chance, boolean enabled, String requiredIngotTag) {
        return new Product(item, 1, 1, 1, chance, enabled, Optional.of(
                TagKey.create(Registries.ITEM,
                        Identifier.parse(requiredIngotTag))));
    }
}
