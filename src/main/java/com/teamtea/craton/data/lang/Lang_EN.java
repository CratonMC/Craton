package com.teamtea.craton.data.lang;

import com.teamtea.craton.Craton;
import com.teamtea.craton.common.registry.CratonBlocks;
import net.minecraft.data.PackOutput;


public class Lang_EN extends LangHelper {
    public Lang_EN(PackOutput gen) {
        super(gen, Craton.MODID, "en_us");
    }


    @Override
    protected void addTranslations() {
        add("itemGroup." + modid + ".core", "Craton");

        addBlock(CratonBlocks.PEGMATITE, "Pegmatite");
        addBlock(CratonBlocks.GNEISS, "Gneiss");
        addBlock(CratonBlocks.RHYOLITE, "Rhyolite");
        addBlock(CratonBlocks.MARBLE, "Marble");
        addBlock(CratonBlocks.LIMESTONE, "Limestone");
        addBlock(CratonBlocks.GABBRO, "Gabbro");
    }


}
