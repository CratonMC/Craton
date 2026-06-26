package com.teamtea.craton.data.lang;


import com.teamtea.craton.Craton;
import com.teamtea.craton.api.geology.block.ExtendedBlockFamily;
import com.teamtea.craton.common.registry.CratonBlocks;
import com.teamtea.craton.common.core.StoneCollection;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.PackOutput;


public class Lang_ZH extends LangHelper {
    public Lang_ZH(PackOutput gen) {
        super(gen, Craton.MODID, "zh_cn");
    }


    @Override
    protected void addTranslations() {

        add("itemGroup." + modid + ".core", "克拉通");

        addStoneCollection(CratonBlocks.GNEISS, "片麻岩");
        addStoneCollection(CratonBlocks.RHYOLITE, "流纹岩");
        addStoneCollection(CratonBlocks.MARBLE, "大理岩");
        addStoneCollection(CratonBlocks.LIMESTONE, "石灰岩");
        addStoneCollection(CratonBlocks.GABBRO, "辉长岩");
        addStoneCollection(CratonBlocks.PEGMATITE, "伟晶岩");
    }

    private void addStoneCollection(StoneCollection collection, String name) {
        addStoneFamily(collection.origin().get(), name);
        addStoneFamily(collection.polished().get(), "磨制" + name);
        addStoneFamily(collection.brick().get(), name + "砖块");
        addStoneFamily(collection.mossyBrick().get(), "覆苔的" + name + "砖块");
    }

    private void addStoneFamily(BlockFamily family, String name) {
        add(family.getBaseBlock().getDescriptionId(), name);
        add(family.get(BlockFamily.Variant.STAIRS).getDescriptionId(), name + "楼梯");
        add(family.get(BlockFamily.Variant.SLAB).getDescriptionId(), name + "台阶");
        add(family.get(BlockFamily.Variant.WALL).getDescriptionId(), name + "墙");
        add(family.get(BlockFamily.Variant.PRESSURE_PLATE).getDescriptionId(), name + "压力板");
        add(family.get(BlockFamily.Variant.BUTTON).getDescriptionId(), name + "按钮");
        add(ExtendedBlockFamily.getVerticalSlab(family), name + "竖半砖");
    }


}
