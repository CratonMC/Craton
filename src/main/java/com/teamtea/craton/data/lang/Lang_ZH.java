package com.teamtea.craton.data.lang;


import com.teamtea.craton.Craton;
import com.teamtea.craton.common.registry.CratonBlocks;
import net.minecraft.data.PackOutput;


public class Lang_ZH extends LangHelper {
    public Lang_ZH(PackOutput gen) {
        super(gen, Craton.MODID, "zh_cn");
    }


    @Override
    protected void addTranslations() {

        add("itemGroup." + modid + ".core", "克拉通");

        addBlock(CratonBlocks.PEGMATITE, "伟晶岩");
        addBlock(CratonBlocks.GNEISS, "片麻岩");
        addBlock(CratonBlocks.RHYOLITE, "流纹岩");
        addBlock(CratonBlocks.MARBLE, "大理岩");
        addBlock(CratonBlocks.LIMESTONE, "石灰岩");
        addBlock(CratonBlocks.GABBRO, "辉长岩");
    }


}
