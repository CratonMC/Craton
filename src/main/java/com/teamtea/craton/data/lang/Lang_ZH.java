package com.teamtea.craton.data.lang;


import com.teamtea.craton.Craton;
import com.teamtea.craton.api.geology.block.ExtendedBlockFamily;
import com.teamtea.craton.common.registry.CratonBlocks;
import com.teamtea.craton.common.core.StoneCollection;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;

import java.util.Map;


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

        VARIANT_NAMES_ZH.forEach((variant, suffix) -> {
            Block block = family.get(variant);
            if (block != null) {
                add(block.getDescriptionId(), name + suffix);
            }
        });

        Block verticalSlab = ExtendedBlockFamily.getVerticalSlab(family);
        if (verticalSlab != null) {
            add(verticalSlab.getDescriptionId(), name + "竖半砖");
        }
    }

    private static final Map<BlockFamily.Variant, String> VARIANT_NAMES_ZH = Map.of(
            BlockFamily.Variant.STAIRS, "楼梯",
            BlockFamily.Variant.SLAB, "台阶",
            BlockFamily.Variant.WALL, "墙",
            BlockFamily.Variant.PRESSURE_PLATE, "压力板",
            BlockFamily.Variant.BUTTON, "按钮"
    );
}
