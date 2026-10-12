package com.teamtea.craton.common.registry;

import com.teamtea.craton.Craton;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.List;
import java.util.function.BiConsumer;

/** Registered mineral products. Item icons are placeholder models until textures are drawn. */
public final class CratonItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craton.MODID);
    public static final Holder<Item> BANDED_IRON_RAW = ITEMS.registerSimpleItem("banded_iron_raw");
    public static final Holder<Item> LIMESTONE_COAL = ITEMS.registerSimpleItem("limestone_coal");
    public static final Holder<Item> SEDIMENTARY_RAW_COPPER = ITEMS.registerSimpleItem("sedimentary_raw_copper");
    public static final Holder<Item> RAW_OSMIUM = ITEMS.registerSimpleItem("raw_osmium");
    public static final Holder<Item> MAGMATIC_RAW_NICKEL = ITEMS.registerSimpleItem("magmatic_raw_nickel");
    public static final Holder<Item> MAGMATIC_RAW_COPPER = ITEMS.registerSimpleItem("magmatic_raw_copper");
    public static final Holder<Item> QUARTZ_VEIN_RAW_GOLD = ITEMS.registerSimpleItem("quartz_vein_raw_gold");
    public static final Holder<Item> QUARTZ_VEIN_PYRITE = ITEMS.registerSimpleItem("quartz_vein_pyrite");
    public static final Holder<Item> FLUORITE = ITEMS.registerSimpleItem("fluorite");
    public static final Holder<Item> RAW_URANIUM = ITEMS.registerSimpleItem("raw_uranium");
    public static final Holder<Item> SEDIMENTARY_RAW_LEAD = ITEMS.registerSimpleItem("sedimentary_raw_lead");
    public static final Holder<Item> SEDIMENTARY_RAW_ZINC = ITEMS.registerSimpleItem("sedimentary_raw_zinc");
    public static final Holder<Item> SHALE_COAL = ITEMS.registerSimpleItem("shale_coal");
    public static final Holder<Item> RAW_TIN = ITEMS.registerSimpleItem("raw_tin");
    public static final Holder<Item> RAW_TUNGSTEN = ITEMS.registerSimpleItem("raw_tungsten");
    public static final Holder<Item> PORPHYRY_RAW_GOLD = ITEMS.registerSimpleItem("porphyry_raw_gold");
    public static final Holder<Item> PORPHYRY_RAW_COPPER = ITEMS.registerSimpleItem("porphyry_raw_copper");
    public static final Holder<Item> PORPHYRY_PYRITE = ITEMS.registerSimpleItem("porphyry_pyrite");
    public static final Holder<Item> EPITHERMAL_RAW_GOLD = ITEMS.registerSimpleItem("epithermal_raw_gold");
    public static final Holder<Item> EPITHERMAL_RAW_SILVER = ITEMS.registerSimpleItem("epithermal_raw_silver");
    public static final Holder<Item> VMS_RAW_COPPER = ITEMS.registerSimpleItem("vms_raw_copper");
    public static final Holder<Item> VMS_RAW_ZINC = ITEMS.registerSimpleItem("vms_raw_zinc");
    public static final Holder<Item> VMS_RAW_LEAD = ITEMS.registerSimpleItem("vms_raw_lead");
    public static final Holder<Item> VMS_RAW_SILVER = ITEMS.registerSimpleItem("vms_raw_silver");
    public static final Holder<Item> BAUXITE = ITEMS.registerSimpleItem("bauxite");
    public static final Holder<Item> GOLD_GRAIN = ITEMS.registerSimpleItem("gold_grain");
    public static final Holder<Item> IRON_GRAIN = ITEMS.registerSimpleItem("iron_grain");
    public static final Holder<Item> DIAMOND_FRAGMENT = ITEMS.registerSimpleItem("diamond_fragment");
    public static final Holder<Item> SKARN_RAW_IRON = ITEMS.registerSimpleItem("skarn_raw_iron");
    public static final Holder<Item> SKARN_RAW_COPPER = ITEMS.registerSimpleItem("skarn_raw_copper");
    public static final Holder<Item> SKARN_RAW_TUNGSTEN = ITEMS.registerSimpleItem("skarn_raw_tungsten");
    public static final Holder<Item> SKARN_RAW_TIN = ITEMS.registerSimpleItem("skarn_raw_tin");
    public static final Holder<Item> SKARN_RAW_LEAD = ITEMS.registerSimpleItem("skarn_raw_lead");
    public static final Holder<Item> SKARN_RAW_ZINC = ITEMS.registerSimpleItem("skarn_raw_zinc");
    public static final Holder<Item> SKARN_RAW_SILVER = ITEMS.registerSimpleItem("skarn_raw_silver");
    public static final Holder<Item> SKARN_RAW_GOLD = ITEMS.registerSimpleItem("skarn_raw_gold");
    public static final Holder<Item> METEORIC_IRON = ITEMS.registerSimpleItem("meteoric_iron");
    public static final Holder<Item> GREEN_JADE = ITEMS.registerSimpleItem("green_jade");
    public static final Holder<Item> WHITE_JADE = ITEMS.registerSimpleItem("white_jade");
    public static final Holder<Item> LAVENDER_JADE = ITEMS.registerSimpleItem("lavender_jade");
    public static final List<Holder<Item>> ALL = List.of(
            BANDED_IRON_RAW, LIMESTONE_COAL, SEDIMENTARY_RAW_COPPER, RAW_OSMIUM,
            MAGMATIC_RAW_NICKEL, MAGMATIC_RAW_COPPER, QUARTZ_VEIN_RAW_GOLD, QUARTZ_VEIN_PYRITE,
            FLUORITE, RAW_URANIUM, SEDIMENTARY_RAW_LEAD, SEDIMENTARY_RAW_ZINC,
            SHALE_COAL, RAW_TIN, RAW_TUNGSTEN, PORPHYRY_RAW_GOLD,
            PORPHYRY_RAW_COPPER, PORPHYRY_PYRITE, EPITHERMAL_RAW_GOLD, EPITHERMAL_RAW_SILVER,
            VMS_RAW_COPPER, VMS_RAW_ZINC, VMS_RAW_LEAD, VMS_RAW_SILVER,
            BAUXITE, GOLD_GRAIN, IRON_GRAIN, DIAMOND_FRAGMENT,
            SKARN_RAW_IRON, SKARN_RAW_COPPER, SKARN_RAW_TUNGSTEN, SKARN_RAW_TIN,
            SKARN_RAW_LEAD, SKARN_RAW_ZINC, SKARN_RAW_SILVER, SKARN_RAW_GOLD,
            METEORIC_IRON, GREEN_JADE, WHITE_JADE, LAVENDER_JADE);
    public static void addChinese(BiConsumer<String,String> add) {
        add.accept("item.craton.banded_iron_raw", "条带状铁原矿");
        add.accept("item.craton.limestone_coal", "石灰煤");
        add.accept("item.craton.sedimentary_raw_copper", "沉积型铜原矿");
        add.accept("item.craton.raw_osmium", "锇原矿");
        add.accept("item.craton.magmatic_raw_nickel", "岩浆型镍原矿");
        add.accept("item.craton.magmatic_raw_copper", "岩浆型铜原矿");
        add.accept("item.craton.quartz_vein_raw_gold", "石英脉金原矿");
        add.accept("item.craton.quartz_vein_pyrite", "石英脉黄铁矿");
        add.accept("item.craton.fluorite", "萤石");
        add.accept("item.craton.raw_uranium", "铀原矿");
        add.accept("item.craton.sedimentary_raw_lead", "沉积型铅原矿");
        add.accept("item.craton.sedimentary_raw_zinc", "沉积型锌原矿");
        add.accept("item.craton.shale_coal", "页岩煤");
        add.accept("item.craton.raw_tin", "锡原矿");
        add.accept("item.craton.raw_tungsten", "钨原矿");
        add.accept("item.craton.porphyry_raw_gold", "斑岩金原矿");
        add.accept("item.craton.porphyry_raw_copper", "斑岩铜原矿");
        add.accept("item.craton.porphyry_pyrite", "斑岩黄铁矿");
        add.accept("item.craton.epithermal_raw_gold", "浅成金原矿");
        add.accept("item.craton.epithermal_raw_silver", "浅成银原矿");
        add.accept("item.craton.vms_raw_copper", "VMS铜原矿");
        add.accept("item.craton.vms_raw_zinc", "VMS锌原矿");
        add.accept("item.craton.vms_raw_lead", "VMS铅原矿");
        add.accept("item.craton.vms_raw_silver", "VMS银原矿");
        add.accept("item.craton.bauxite", "铝土");
        add.accept("item.craton.gold_grain", "金粒");
        add.accept("item.craton.iron_grain", "铁粒");
        add.accept("item.craton.diamond_fragment", "碎钻");
        add.accept("item.craton.skarn_raw_iron", "矽卡岩铁原矿");
        add.accept("item.craton.skarn_raw_copper", "矽卡岩铜原矿");
        add.accept("item.craton.skarn_raw_tungsten", "矽卡岩钨原矿");
        add.accept("item.craton.skarn_raw_tin", "矽卡岩锡原矿");
        add.accept("item.craton.skarn_raw_lead", "矽卡岩铅原矿");
        add.accept("item.craton.skarn_raw_zinc", "矽卡岩锌原矿");
        add.accept("item.craton.skarn_raw_silver", "矽卡岩银原矿");
        add.accept("item.craton.skarn_raw_gold", "矽卡岩金原矿");
        add.accept("item.craton.meteoric_iron", "陨铁");
        add.accept("item.craton.green_jade", "绿色翡翠");
        add.accept("item.craton.white_jade", "白色翡翠");
        add.accept("item.craton.lavender_jade", "紫色翡翠");
    }
    public static void addEnglish(BiConsumer<String,String> add) {
        add.accept("item.craton.banded_iron_raw", "Banded Raw Iron");
        add.accept("item.craton.limestone_coal", "Limestone Coal");
        add.accept("item.craton.sedimentary_raw_copper", "Sedimentary Raw Copper");
        add.accept("item.craton.raw_osmium", "Raw Osmium");
        add.accept("item.craton.magmatic_raw_nickel", "Magmatic Raw Nickel");
        add.accept("item.craton.magmatic_raw_copper", "Magmatic Raw Copper");
        add.accept("item.craton.quartz_vein_raw_gold", "Quartz Vein Raw Gold");
        add.accept("item.craton.quartz_vein_pyrite", "Quartz Vein Pyrite");
        add.accept("item.craton.fluorite", "Fluorite");
        add.accept("item.craton.raw_uranium", "Raw Uranium");
        add.accept("item.craton.sedimentary_raw_lead", "Sedimentary Raw Lead");
        add.accept("item.craton.sedimentary_raw_zinc", "Sedimentary Raw Zinc");
        add.accept("item.craton.shale_coal", "Shale Coal");
        add.accept("item.craton.raw_tin", "Raw Tin");
        add.accept("item.craton.raw_tungsten", "Raw Tungsten");
        add.accept("item.craton.porphyry_raw_gold", "Porphyry Raw Gold");
        add.accept("item.craton.porphyry_raw_copper", "Porphyry Raw Copper");
        add.accept("item.craton.porphyry_pyrite", "Porphyry Pyrite");
        add.accept("item.craton.epithermal_raw_gold", "Epithermal Raw Gold");
        add.accept("item.craton.epithermal_raw_silver", "Epithermal Raw Silver");
        add.accept("item.craton.vms_raw_copper", "VMS Raw Copper");
        add.accept("item.craton.vms_raw_zinc", "VMS Raw Zinc");
        add.accept("item.craton.vms_raw_lead", "VMS Raw Lead");
        add.accept("item.craton.vms_raw_silver", "VMS Raw Silver");
        add.accept("item.craton.bauxite", "Bauxite");
        add.accept("item.craton.gold_grain", "Gold Grain");
        add.accept("item.craton.iron_grain", "Iron Grain");
        add.accept("item.craton.diamond_fragment", "Diamond Fragment");
        add.accept("item.craton.skarn_raw_iron", "Skarn Raw Iron");
        add.accept("item.craton.skarn_raw_copper", "Skarn Raw Copper");
        add.accept("item.craton.skarn_raw_tungsten", "Skarn Raw Tungsten");
        add.accept("item.craton.skarn_raw_tin", "Skarn Raw Tin");
        add.accept("item.craton.skarn_raw_lead", "Skarn Raw Lead");
        add.accept("item.craton.skarn_raw_zinc", "Skarn Raw Zinc");
        add.accept("item.craton.skarn_raw_silver", "Skarn Raw Silver");
        add.accept("item.craton.skarn_raw_gold", "Skarn Raw Gold");
        add.accept("item.craton.meteoric_iron", "Meteoric Iron");
        add.accept("item.craton.green_jade", "Green Jade");
        add.accept("item.craton.white_jade", "White Jade");
        add.accept("item.craton.lavender_jade", "Lavender Jade");
    }
    private CratonItems() {}
}
