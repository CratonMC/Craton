package com.teamtea.craton.data.lang;

import com.teamtea.craton.Craton;
import com.teamtea.craton.api.geology.block.ExtendedBlockFamily;
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
