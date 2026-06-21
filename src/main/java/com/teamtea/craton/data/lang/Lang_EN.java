package com.teamtea.craton.data.lang;

import com.teamtea.craton.Craton;
import com.teamtea.craton.common.registry.CratonBlocks;
import com.teamtea.craton.common.core.StoneCollection;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.PackOutput;


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
        add(family.get(BlockFamily.Variant.STAIRS).getDescriptionId(), name + " Stairs");
        add(family.get(BlockFamily.Variant.SLAB).getDescriptionId(), name + " Slab");
        add(family.get(BlockFamily.Variant.WALL).getDescriptionId(), name + " Wall");
    }


}
