package com.teamtea.craton.data.lang;


import com.teamtea.craton.Craton;
import com.teamtea.craton.api.block.ExtendedBlockFamily;
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

        addStoneCollection(CratonBlocks.ARKOSE_SANDSTONE, "长石砂岩");
        addStoneCollection(CratonBlocks.SHALE, "页岩");
        addStoneCollection(CratonBlocks.ECLOGITE, "榴辉岩");
        addStoneCollection(CratonBlocks.SCHIST, "片岩");
        addStoneCollection(CratonBlocks.KIMBERLITE, "金伯利岩");
        addStoneCollection(CratonBlocks.SKARN, "矽卡岩");
        addStoneCollection(CratonBlocks.METEORITE, "陨石");
        addStoneCollection(CratonBlocks.JADEITITE, "玉岩");
        add(CratonBlocks.banded_iron_gneiss_ore.value().getDescriptionId(), "条带铁矿石");
//        add(CratonBlocks.banded_iron_marble_ore.value().getDescriptionId(), "大理岩条带铁矿石");
//        add(CratonBlocks.banded_iron_stone_ore.value().getDescriptionId(), "石头条带铁矿石");
        add(CratonBlocks.limestone_coal_ore.value().getDescriptionId(), "石灰岩煤矿石");
        add(CratonBlocks.sedimentary_copper_ore.value().getDescriptionId(), "沉积铜矿石");
        add(CratonBlocks.metasomatic_lapis_ore.value().getDescriptionId(), "青金石矿石");
        add(CratonBlocks.magmatic_pge_ore.value().getDescriptionId(), "岩浆型铂族矿石");
        add(CratonBlocks.magmatic_ni_cu_ore.value().getDescriptionId(), "岩浆型镍铜矿石");
        add(CratonBlocks.pegmatite_quartz_ore.value().getDescriptionId(), "伟晶岩石英矿石");
        add(CratonBlocks.pegmatite_emerald_ore.value().getDescriptionId(), "伟晶岩绿宝石矿石");
        add(CratonBlocks.quartz_vein_gold_ore.value().getDescriptionId(), "石英脉金矿石");
        add(CratonBlocks.quartz_vein_pyrite_ore.value().getDescriptionId(), "黄铁矿石英脉矿石");
        add(CratonBlocks.fluorite_vein_ore.value().getDescriptionId(), "萤石脉矿石");
        add(CratonBlocks.sedimentary_uranium_ore.value().getDescriptionId(), "沉积铀矿石");
        add(CratonBlocks.sedimentary_pb_zn_ore.value().getDescriptionId(), "沉积铅锌矿石");
        add(CratonBlocks.shale_coal_ore.value().getDescriptionId(), "页岩煤矿石");
        add(CratonBlocks.deep_hydrothermal_redstone_ore.value().getDescriptionId(), "红石矿石");
        add(CratonBlocks.granite_cassiterite_ore.value().getDescriptionId(), "锡石矿石");
        add(CratonBlocks.granite_wolframite_ore.value().getDescriptionId(), "黑钨矿石");
        add(CratonBlocks.porphyry_gold_ore.value().getDescriptionId(), "斑岩金矿石");
        add(CratonBlocks.porphyry_copper_ore.value().getDescriptionId(), "斑岩铜矿石");
        add(CratonBlocks.porphyry_sulfide_ore.value().getDescriptionId(), "斑岩硫化矿石");
        add(CratonBlocks.epithermal_au_ag_ore.value().getDescriptionId(), "浅成金银矿石");
        add(CratonBlocks.vms_copper_ore.value().getDescriptionId(), "VMS 铜矿石");
        add(CratonBlocks.vms_zinc_ore.value().getDescriptionId(), "VMS 锌矿石");
        add(CratonBlocks.vms_pb_ag_ore.value().getDescriptionId(), "VMS 铅银矿石");
        add(CratonBlocks.bauxite_ore.value().getDescriptionId(), "铝土矿石");
        add(CratonBlocks.peat.value().getDescriptionId(), "泥炭");
        add(CratonBlocks.auriferous_gravel.value().getDescriptionId(), "含金砂砾");
        add(CratonBlocks.iron_bearing_gravel.value().getDescriptionId(), "含铁砂砾");
        add(CratonBlocks.gold_bearing_sand.value().getDescriptionId(), "金砂");
        add(CratonBlocks.iron_sand.value().getDescriptionId(), "铁砂");
        add(CratonBlocks.diamond_bearing_kimberlite.value().getDescriptionId(), "含钻金伯利岩");
        add(CratonBlocks.diamond_rich_kimberlite.value().getDescriptionId(), "金伯利钻石体");
        add(CratonBlocks.skarn_iron_ore.value().getDescriptionId(), "矽卡岩铁矿石");
        add(CratonBlocks.skarn_copper_ore.value().getDescriptionId(), "矽卡岩铜矿石");
        add(CratonBlocks.skarn_w_sn_ore.value().getDescriptionId(), "矽卡岩钨锡矿石");
        add(CratonBlocks.skarn_pb_zn_ore.value().getDescriptionId(), "矽卡岩铅锌矿石");
        add(CratonBlocks.skarn_gold_ore.value().getDescriptionId(), "矽卡岩金矿石");
        add(CratonBlocks.meteoric_iron_ore.value().getDescriptionId(), "陨铁矿石");
        add(CratonBlocks.suspicious_jadeitite.value().getDescriptionId(), "可疑的玉岩");
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
