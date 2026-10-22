package com.teamtea.craton.data.lang;

import com.teamtea.craton.Craton;
import com.teamtea.craton.api.block.ExtendedBlockFamily;
import com.teamtea.craton.common.registry.CratonBlocks;
import com.teamtea.craton.common.registry.CratonItems;
import com.teamtea.craton.common.block.StoneCollection;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;

import java.util.Map;

public class Lang_EN extends LangHelper {
    public Lang_EN(PackOutput gen) {
        super(gen, Craton.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("command.craton.excavate.start", "Excavating %s x %s chunks: X %s..%s, Z %s..%s. Bedrock is preserved.");
        add("command.craton.excavate.fast", "Fast mode enabled; clearing larger batches.");
        add("command.craton.excavate.busy", "An excavation is already running. Use /craton excavate status or cancel.");
        add("command.craton.excavate.idle", "No excavation is running.");
        add("command.craton.excavate.status", "Excavation: %s/%s columns complete, %s blocks removed.");
        add("command.craton.excavate.cancel", "Excavation cancelled after removing %s blocks.");
        add("command.craton.excavate.done", "Excavation complete: %s blocks removed; bedrock preserved.");
        add("command.craton.excavate.failed", "Excavation stopped: %s");

        // JEI translations: generated from this provider, not maintained only in JSON.
        add("jei.craton.ore_drops", "Ore drops");
        add("jei.craton.bonus_chance", "Bonus chance: %s (base)");
        add("jei.craton.main_chance", "Main product chance: %s (base)");
        add("jei.craton.quantity", "Quantity: %s–%s (before Fortune)");
        add("jei.craton.fortune_amount", "Fortune amount scale: %s");
        add("jei.craton.fortune_chance", "Fortune bonus chance scale: %s");
        add("jei.craton.veins", "Mineral Deposits");
        add("jei.craton.geology", "Geology & Strata");
        add("jei.craton.vein_type", "Type: %s");
        add("jei.craton.vein_cell", "Cell: %s, candidates: %s");
        add("jei.craton.vein_reach", "Reach: %s, frequency: %s");
        add("jei.craton.region", "Region: %s");
        add("jei.craton.ui.vein_ores", "Ores found in this deposit");
        add("jei.craton.ui.vein_frequency", "Generation frequency: %s");
        add("jei.craton.ui.vein_scale", "Deposit influence: about %s blocks");
        add("jei.craton.ui.vein_technical", "Actual distribution depends on geology");
        add("jei.craton.ui.geology_region", "Region: %s");
        add("jei.craton.ui.geology_more", "%s more layers not shown");
        add("jei.craton.ui.primary", "Main drop");
        add("jei.craton.ui.bonus", "Extra drop");
        add("jei.craton.ui.base_chance", "Base chance: %s");
        add("jei.craton.ui.drop_count", "Quantity: %s–%s");
        add("jei.craton.ui.fortune_effect", "Fortune affects actual drops");
        add("jei.craton.ui.host_rocks", "Ore host rocks");
        add("jei.craton.ui.host_lookup_note", "Host rocks (not a strata profile)");
        add("jei.craton.ui.strata_section", "Strata section · top to bottom");
        add("jei.craton.ui.layer_thickness", "Thickness %s blocks");

        add("itemGroup." + modid + ".core", "Craton");
        add("itemGroup." + modid + ".ores", "Craton Ores");

        addStoneCollection(CratonBlocks.GNEISS, "Gneiss");
        addStoneCollection(CratonBlocks.RHYOLITE, "Rhyolite");
        addStoneCollection(CratonBlocks.MARBLE, "Marble");
        addStoneCollection(CratonBlocks.LIMESTONE, "Limestone");
        addStoneCollection(CratonBlocks.GABBRO, "Gabbro");
        addStoneCollection(CratonBlocks.PEGMATITE, "Pegmatite");
        addStoneCollection(CratonBlocks.ARKOSE_SANDSTONE, "Arkose Sandstone");
        addStoneCollection(CratonBlocks.SHALE, "Shale");
        addStoneCollection(CratonBlocks.ECLOGITE, "Eclogite");
        addStoneCollection(CratonBlocks.SCHIST, "Schist");
        addStoneCollection(CratonBlocks.KIMBERLITE, "Kimberlite");
        addStoneCollection(CratonBlocks.SKARN, "Skarn");
        addStoneCollection(CratonBlocks.METEORITE, "Meteorite");
        addStoneCollection(CratonBlocks.JADEITITE, "Jadeitite");

        add(CratonBlocks.banded_iron_gneiss_ore.value().getDescriptionId(), "Banded Iron Ore");
//        add(CratonBlocks.banded_iron_marble_ore.value().getDescriptionId(), "Marble Banded Iron Ore");
//        add(CratonBlocks.banded_iron_stone_ore.value().getDescriptionId(), "Stone Banded Iron Ore");
        add(CratonBlocks.limestone_coal_ore.value().getDescriptionId(), "Limestone Coal Ore");
        add(CratonBlocks.sedimentary_copper_ore.value().getDescriptionId(), "Sedimentary Copper Ore");
        add(CratonBlocks.metasomatic_lapis_ore.value().getDescriptionId(), "Lapis Ore");
        add(CratonBlocks.magmatic_pge_ore.value().getDescriptionId(), "Magmatic PGE Ore");
        add(CratonBlocks.magmatic_ni_cu_ore.value().getDescriptionId(), "Magmatic Ni-Cu Ore");
        add(CratonBlocks.pegmatite_quartz_ore.value().getDescriptionId(), "Pegmatite Quartz Ore");
        add(CratonBlocks.pegmatite_emerald_ore.value().getDescriptionId(), "Pegmatite Emerald Ore");
        add(CratonBlocks.quartz_vein_gold_ore.value().getDescriptionId(), "Quartz Vein Gold Ore");
        add(CratonBlocks.quartz_vein_pyrite_ore.value().getDescriptionId(), "Pyrite Quartz Vein Ore");
        add(CratonBlocks.fluorite_vein_ore.value().getDescriptionId(), "Fluorite Vein Ore");
        add(CratonBlocks.sedimentary_uranium_ore.value().getDescriptionId(), "Sedimentary Uranium Ore");
        add(CratonBlocks.sedimentary_pb_zn_ore.value().getDescriptionId(), "Sedimentary Pb-Zn Ore");
        add(CratonBlocks.shale_coal_ore.value().getDescriptionId(), "Shale Coal Ore");
        add(CratonBlocks.deep_hydrothermal_redstone_ore.value().getDescriptionId(), "Redstone Ore");
        add(CratonBlocks.granite_cassiterite_ore.value().getDescriptionId(), "Cassiterite Ore");
        add(CratonBlocks.granite_wolframite_ore.value().getDescriptionId(), "Wolframite Ore");
        add(CratonBlocks.porphyry_gold_ore.value().getDescriptionId(), "Porphyry Gold Ore");
        add(CratonBlocks.porphyry_copper_ore.value().getDescriptionId(), "Porphyry Copper Ore");
        add(CratonBlocks.porphyry_sulfide_ore.value().getDescriptionId(), "Porphyry Sulfide Ore");
        add(CratonBlocks.epithermal_au_ag_ore.value().getDescriptionId(), "Epithermal Au-Ag Ore");
        add(CratonBlocks.vms_copper_ore.value().getDescriptionId(), "VMS Copper Ore");
        add(CratonBlocks.vms_zinc_ore.value().getDescriptionId(), "VMS Zinc Ore");
        add(CratonBlocks.vms_pb_ag_ore.value().getDescriptionId(), "VMS Lead-Silver Ore");
        add(CratonBlocks.bauxite_ore.value().getDescriptionId(), "Bauxite Ore");
        add(CratonBlocks.peat.value().getDescriptionId(), "Peat");
        add(CratonBlocks.auriferous_gravel.value().getDescriptionId(), "Auriferous Gravel");
        add(CratonBlocks.ferriferous_gravel.value().getDescriptionId(), "Ferriferous Gravel");
        add(CratonBlocks.auriferous_sand.value().getDescriptionId(), "Auriferous Sand");
        add(CratonBlocks.ferriferous_sand.value().getDescriptionId(), "Ferriferous Sand");
        add(CratonBlocks.ferriferous_red_sand.value().getDescriptionId(), "Ferriferous Red Sand");
        CratonItems.addEnglish(this::add);
        add(CratonBlocks.diamond_bearing_kimberlite.value().getDescriptionId(), "Diamond-Bearing Kimberlite");
        add(CratonBlocks.diamond_rich_kimberlite.value().getDescriptionId(), "Diamond-Rich Kimberlite");
        add(CratonBlocks.skarn_iron_ore.value().getDescriptionId(), "Skarn Iron Ore");
        add(CratonBlocks.skarn_copper_ore.value().getDescriptionId(), "Skarn Copper Ore");
        add(CratonBlocks.skarn_w_sn_ore.value().getDescriptionId(), "Skarn W-Sn Ore");
        add(CratonBlocks.skarn_pb_zn_ore.value().getDescriptionId(), "Skarn Pb-Zn Ore");
        add(CratonBlocks.skarn_gold_ore.value().getDescriptionId(), "Skarn Gold Ore");
        add(CratonBlocks.meteoric_iron_ore.value().getDescriptionId(), "Meteoric Iron Ore");
        add(CratonBlocks.suspicious_jadeitite.value().getDescriptionId(), "Suspicious Jadeitite");
    }

    private void addStoneCollection(StoneCollection collection, String name) {
        addStoneFamily(collection.origin().get(), name);
        addStoneFamily(collection.polished().get(), "Polished " + name);
        addStoneFamily(collection.brick().get(), name + " Brick");
        addStoneFamily(collection.mossyBrick().get(), "Mossy " + name + " Brick");
    }

    private void addStoneFamily(BlockFamily family, String name) {
        add(family.getBaseBlock().getDescriptionId(), name);

        VARIANT_NAMES.forEach((variant, suffix) -> {
            Block block = family.get(variant);
            if (block != null) {
                add(block.getDescriptionId(), name + " " + suffix);
            }
        });

        Block verticalSlab = ExtendedBlockFamily.getVerticalSlab(family);
        if (verticalSlab != null) {
            add(verticalSlab.getDescriptionId(), name + " Vertical Slab");
        }
    }

    private static final Map<BlockFamily.Variant, String> VARIANT_NAMES = Map.of(
            BlockFamily.Variant.STAIRS, "Stairs",
            BlockFamily.Variant.SLAB, "Slab",
            BlockFamily.Variant.WALL, "Wall",
            BlockFamily.Variant.PRESSURE_PLATE, "Pressure Plate",
            BlockFamily.Variant.BUTTON, "Button"
    );
}
