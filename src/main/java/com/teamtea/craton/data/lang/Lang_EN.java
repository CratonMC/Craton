package com.teamtea.craton.data.lang;

import com.teamtea.craton.Craton;
import com.teamtea.craton.api.block.ExtendedBlockFamily;
import com.teamtea.craton.common.registry.CratonBlocks;
import com.teamtea.craton.common.core.StoneCollection;
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
        add("itemGroup." + modid + ".core", "Craton");

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
