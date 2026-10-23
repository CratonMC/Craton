package com.teamtea.craton.common.registry;

import com.teamtea.craton.Craton;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.List;
import java.util.function.BiConsumer;

/** Mineral products shared across deposit types. Vanilla products reuse vanilla items. */
public class CratonItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craton.MODID);
    public static final Holder<Item> SURVEY_CHART = ITEMS.registerItem("survey_chart",com.teamtea.craton.common.item.SurveyChartItem::new);
    public static final Holder<Item> HEMATITE_NUGGET = ITEMS.registerSimpleItem("hematite_nugget");
    public static final Holder<Item> MAGNETITE_NUGGET = ITEMS.registerSimpleItem("magnetite_nugget");
    public static final Holder<Item> RAW_HEMATITE = ITEMS.registerSimpleItem("raw_hematite");
    public static final Holder<Item> RAW_MAGNETITE = ITEMS.registerSimpleItem("raw_magnetite");
    public static final Holder<Item> RAW_IRON_SULFIDE = ITEMS.registerSimpleItem("raw_iron_sulfide");
    public static final Holder<Item> RAW_COPPER_OXIDE = ITEMS.registerSimpleItem("raw_copper_oxide");
    public static final Holder<Item> RAW_COPPER_SULFIDE = ITEMS.registerSimpleItem("raw_copper_sulfide");
    public static final Holder<Item> RAW_GOLD = ITEMS.registerSimpleItem("raw_gold");
    public static final Holder<Item> RAW_SILVER = ITEMS.registerSimpleItem("raw_silver");
    public static final Holder<Item> RAW_NICKEL = ITEMS.registerSimpleItem("raw_nickel");
    public static final Holder<Item> RAW_LEAD = ITEMS.registerSimpleItem("raw_lead");
    public static final Holder<Item> RAW_ZINC = ITEMS.registerSimpleItem("raw_zinc");
    public static final Holder<Item> RAW_TIN = ITEMS.registerSimpleItem("raw_tin");
    public static final Holder<Item> RAW_TUNGSTEN = ITEMS.registerSimpleItem("raw_tungsten");
    public static final Holder<Item> RAW_URANIUM = ITEMS.registerSimpleItem("raw_uranium");
    public static final Holder<Item> RAW_OSMIUM = ITEMS.registerSimpleItem("raw_osmium");
    public static final Holder<Item> FLUORITE = ITEMS.registerSimpleItem("fluorite");
    public static final Holder<Item> BAUXITE = ITEMS.registerSimpleItem("bauxite");

    public static final Holder<Item> METEORIC_IRON = ITEMS.registerSimpleItem("meteoric_iron");
    public static final Holder<Item> GREEN_JADE = ITEMS.registerSimpleItem("green_jade");
    public static final Holder<Item> WHITE_JADE = ITEMS.registerSimpleItem("white_jade");
    public static final Holder<Item> LAVENDER_JADE = ITEMS.registerSimpleItem("lavender_jade");
    public static final List<Holder<Item>> ALL = List.of(
            HEMATITE_NUGGET, MAGNETITE_NUGGET, RAW_HEMATITE, RAW_MAGNETITE, RAW_IRON_SULFIDE, RAW_COPPER_OXIDE,
            RAW_COPPER_SULFIDE, RAW_GOLD, RAW_SILVER, RAW_NICKEL, RAW_LEAD,
            RAW_ZINC, RAW_TIN, RAW_TUNGSTEN, RAW_URANIUM,
            METEORIC_IRON, GREEN_JADE, WHITE_JADE,
            LAVENDER_JADE);

    public static void addChinese(BiConsumer<String,String> add) {
        add.accept("item.craton.survey_chart", "空白矿产调查图");
        add.accept("item.craton.mineral_survey_map", "矿产调查图");
        add.accept("survey.craton.blank_hint", "使用后保持调查图在手中，直至调查完成。");
        add.accept("survey.craton.unsupported", "矿产调查仅支持噪声生成的主世界。");
        add.accept("survey.craton.no_data", "地质数据尚未就绪。");
        add.accept("survey.craton.busy", "你的矿产调查正在进行中。");
        add.accept("survey.craton.queue_full", "调查队列已满，请稍后重试。");
        add.accept("survey.craton.started", "开始调查，请保持空白图在当前手中。");
        add.accept("survey.craton.finished", "矿产调查图已完成。");
        add.accept("survey.craton.cancelled", "调查已取消，未消耗空白图。");
        add.accept("survey.craton.failed", "调查失败，未消耗空白图。");
        add.accept("survey.craton.approximate", "表示预计矿化区域，范围与颜色仅供初步找矿。");
        add.accept("survey.craton.shading", "色斑深浅用于显示范围，不代表品位或储量。");
        add.accept("survey.craton.coverage", "覆盖范围：%s × %s格；上方为北。");
        add.accept("survey.craton.legend.metals", "灰：铁；黑：煤；橙：铜；紫：铅锌");
        add.accept("survey.craton.legend.other", "黄绿：铀；浅蓝：锡钨；金黄：金；青：镍／铂族");
        add.accept("survey.craton.legend.contacts", "绿：宝石；棕：铝土；红：红石；粉：伟晶岩；品红：接触交代");

        
        add.accept("item.craton.hematite_nugget", "赤铁矿粒");
        add.accept("item.craton.magnetite_nugget", "磁铁矿粒");
        add.accept("item.craton.raw_hematite", "赤铁矿原矿");
        add.accept("item.craton.raw_magnetite", "磁铁矿原矿");
        add.accept("item.craton.raw_iron_sulfide", "硫化铁原矿");
        add.accept("item.craton.raw_copper_oxide", "氧化铜原矿");
        add.accept("item.craton.raw_copper_sulfide", "硫化铜原矿");
        add.accept("item.craton.raw_gold", "金原矿");
        add.accept("item.craton.raw_silver", "银原矿");
        add.accept("item.craton.raw_nickel", "镍原矿");
        add.accept("item.craton.raw_lead", "铅原矿");
        add.accept("item.craton.raw_zinc", "锌原矿");
        add.accept("item.craton.raw_tin", "锡原矿");
        add.accept("item.craton.raw_tungsten", "钨原矿");
        add.accept("item.craton.raw_uranium", "铀原矿");
        add.accept("item.craton.raw_osmium", "锇原矿");
        add.accept("item.craton.fluorite", "萤石");
        add.accept("item.craton.bauxite", "铝土");
        add.accept("item.craton.meteoric_iron", "陨铁");
        add.accept("item.craton.green_jade", "绿色翡翠");
        add.accept("item.craton.white_jade", "白色翡翠");
        add.accept("item.craton.lavender_jade", "紫色翡翠");
    }
    public static void addEnglish(BiConsumer<String,String> add) {
        add.accept("item.craton.survey_chart", "Blank Mineral Survey Chart");
        add.accept("item.craton.mineral_survey_map", "Mineral Survey Map");
        add.accept("survey.craton.blank_hint", "Use and keep this chart in hand until the survey is complete.");
        add.accept("survey.craton.unsupported", "Mineral surveys require a noise-generated Overworld.");
        add.accept("survey.craton.no_data", "Geology data is not ready.");
        add.accept("survey.craton.busy", "Your survey is already in progress.");
        add.accept("survey.craton.queue_full", "Survey queue is full. Try again shortly.");
        add.accept("survey.craton.started", "Survey started. Keep the blank chart in this hand.");
        add.accept("survey.craton.finished", "Mineral survey map completed.");
        add.accept("survey.craton.cancelled", "Survey cancelled. The blank chart was not consumed.");
        add.accept("survey.craton.failed", "Survey failed. The blank chart was not consumed.");
        add.accept("survey.craton.approximate", "Prospective mineralization; footprints and colors are approximate.");
        add.accept("survey.craton.shading", "Shading illustrates footprints, not grade or reserves.");
        add.accept("survey.craton.coverage", "Coverage: %s × %s blocks; north is up.");
        add.accept("survey.craton.legend.metals", "Gray: iron · Black: coal · Orange: copper · Purple: lead/zinc");
        add.accept("survey.craton.legend.other", "Lime: uranium · Light blue: tin/tungsten · Gold: gold · Cyan: nickel/PGE");
        add.accept("survey.craton.legend.contacts", "Green: gems · Brown: bauxite · Red: redstone · Pink: pegmatite · Magenta: contact ores");

        
        add.accept("item.craton.hematite_nugget", "Hematite Nugget");
        add.accept("item.craton.magnetite_nugget", "Magnetite Nugget");
        add.accept("item.craton.raw_hematite", "Raw Hematite");
        add.accept("item.craton.raw_magnetite", "Raw Magnetite");
        add.accept("item.craton.raw_iron_sulfide", "Raw Iron Sulfide");
        add.accept("item.craton.raw_copper_oxide", "Raw Copper Oxide");
        add.accept("item.craton.raw_copper_sulfide", "Raw Copper Sulfide");
        add.accept("item.craton.raw_gold", "Raw Gold");
        add.accept("item.craton.raw_silver", "Raw Silver");
        add.accept("item.craton.raw_nickel", "Raw Nickel");
        add.accept("item.craton.raw_lead", "Raw Lead");
        add.accept("item.craton.raw_zinc", "Raw Zinc");
        add.accept("item.craton.raw_tin", "Raw Tin");
        add.accept("item.craton.raw_tungsten", "Raw Tungsten");
        add.accept("item.craton.raw_uranium", "Raw Uranium");
        add.accept("item.craton.raw_osmium", "Raw Osmium");
        add.accept("item.craton.fluorite", "Fluorite");
        add.accept("item.craton.bauxite", "Bauxite");
        add.accept("item.craton.meteoric_iron", "Meteoric Iron");
        add.accept("item.craton.green_jade", "Green Jade");
        add.accept("item.craton.white_jade", "White Jade");
        add.accept("item.craton.lavender_jade", "Lavender Jade");
    }
    private CratonItems() {}
}
