package com.teamtea.craton.common.registry;

import com.teamtea.craton.Craton;
import com.teamtea.craton.api.geology.ore.OreType;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

/** An OreType is one mineralization; its variants select the block from the host rock. */
public class OreTypeRegistry {
    public static final ResourceKey<OreType> BIF_IRON = key("bif_iron");
    public static final ResourceKey<OreType> LIMESTONE_COAL = key("limestone_coal");
    public static final ResourceKey<OreType> SEDIMENTARY_COPPER = key("sedimentary_copper");
    public static final ResourceKey<OreType> METASOMATIC_LAPIS = key("metasomatic_lapis");
    public static final ResourceKey<OreType> GABBRO_PGE = key("gabbro_pge");
    public static final ResourceKey<OreType> GABBRO_NI_CU = key("gabbro_ni_cu");
    public static final ResourceKey<OreType> PEGMATITE_QUARTZ = key("pegmatite_quartz");
    public static final ResourceKey<OreType> PEGMATITE_EMERALD = key("pegmatite_emerald");
    public static final ResourceKey<OreType> QUARTZ_VEIN_GOLD = key("quartz_vein_gold");
    public static final ResourceKey<OreType> QUARTZ_VEIN_PYRITE = key("quartz_vein_pyrite");
    public static final ResourceKey<OreType> HYDROTHERMAL_FLUORITE = key("hydrothermal_fluorite");
    public static final ResourceKey<OreType> SANDSTONE_URANIUM = key("sandstone_uranium");
    public static final ResourceKey<OreType> SHALE_PB_ZN = key("shale_pb_zn");
    public static final ResourceKey<OreType> SHALE_COAL = key("shale_coal");
    public static final ResourceKey<OreType> DEEP_HYDROTHERMAL_REDSTONE = key("deep_hydrothermal_redstone");
    public static final ResourceKey<OreType> GRANITE_CASSITERITE = key("granite_cassiterite");
    public static final ResourceKey<OreType> GRANITE_WOLFRAMITE = key("granite_wolframite");
    public static final ResourceKey<OreType> PORPHYRY_GOLD = key("porphyry_gold");
    public static final ResourceKey<OreType> PORPHYRY_COPPER = key("porphyry_copper");
    public static final ResourceKey<OreType> PORPHYRY_SULFIDE = key("porphyry_sulfide");
    public static final ResourceKey<OreType> EPITHERMAL_AU_AG = key("epithermal_au_ag");
    public static final ResourceKey<OreType> VMS_CHALCOPYRITE = key("vms_chalcopyrite");
    public static final ResourceKey<OreType> VMS_SPHALERITE = key("vms_sphalerite");
    public static final ResourceKey<OreType> VMS_GALENA = key("vms_galena");
    public static final ResourceKey<OreType> BASALT_BAUXITE = key("basalt_bauxite");
    public static final ResourceKey<OreType> PEAT = key("peat");
    public static final ResourceKey<OreType> PLACER_GOLD = key("placer_gold");
    public static final ResourceKey<OreType> PLACER_IRON = key("placer_iron");
    public static final ResourceKey<OreType> KIMBERLITE_DIAMOND = key("kimberlite_diamond");
    public static final ResourceKey<OreType> KIMBERLITE_DIAMOND_RICH = key("kimberlite_diamond_rich");
    public static final ResourceKey<OreType> SKARN_IRON = key("skarn_iron");
    public static final ResourceKey<OreType> SKARN_COPPER = key("skarn_copper");
    public static final ResourceKey<OreType> SKARN_W_SN = key("skarn_w_sn");
    public static final ResourceKey<OreType> SKARN_PB_ZN_AG = key("skarn_pb_zn_ag");
    public static final ResourceKey<OreType> SKARN_GOLD = key("skarn_gold");
    public static final ResourceKey<OreType> METEORITIC_IRON = key("meteoritic_iron");
    public static final ResourceKey<OreType> JADEITITE_JADE = key("jadeitite_jade");

    private OreTypeRegistry(){}

    private static ResourceKey<OreType> key(String name){
        return ResourceKey.create(CratonRegistries.ORE_TYPE,Craton.rl(name));
    }

    public static void bootstrap(BootstrapContext<OreType> context){
        Block gneiss=CratonBlocks.GNEISS.getOrigin().getBaseBlock();
        Block marble=CratonBlocks.MARBLE.getOrigin().getBaseBlock();
        Block limestone=CratonBlocks.LIMESTONE.getOrigin().getBaseBlock();
        Block gabbro=CratonBlocks.GABBRO.getOrigin().getBaseBlock();
        Block pegmatite=CratonBlocks.PEGMATITE.getOrigin().getBaseBlock();
        Block rhyolite=CratonBlocks.RHYOLITE.getOrigin().getBaseBlock();
        Block arkose=CratonBlocks.ARKOSE_SANDSTONE.getOrigin().getBaseBlock();
        Block shale=CratonBlocks.SHALE.getOrigin().getBaseBlock();
        Block eclogite=CratonBlocks.ECLOGITE.getOrigin().getBaseBlock();
        Block skarn=CratonBlocks.SKARN.getOrigin().getBaseBlock();
        Block kimberlite=CratonBlocks.KIMBERLITE.getOrigin().getBaseBlock();
        Block jadeitite=CratonBlocks.JADEITITE.getOrigin().getBaseBlock();

        register(context,BIF_IRON,List.of(
                variant(gneiss,CratonBlocks.banded_iron_gneiss_ore)), "c:ingots/iron");
        register(context,LIMESTONE_COAL,CratonBlocks.limestone_coal_ore,limestone);
        register(context,SEDIMENTARY_COPPER,List.of("c:ingots/copper"),CratonBlocks.sedimentary_copper_ore,limestone);
        register(context,METASOMATIC_LAPIS,CratonBlocks.metasomatic_lapis_ore,marble);
        register(context,GABBRO_PGE,List.of("c:ingots/osmium"),CratonBlocks.magmatic_pge_ore,gabbro);
        register(context,GABBRO_NI_CU,List.of("c:ingots/nickel", "c:ingots/copper"),CratonBlocks.magmatic_ni_cu_ore,gabbro);
        register(context,PEGMATITE_QUARTZ,CratonBlocks.pegmatite_quartz_ore,pegmatite);
        register(context,PEGMATITE_EMERALD,CratonBlocks.pegmatite_emerald_ore,pegmatite);
        register(context,QUARTZ_VEIN_GOLD,List.of("c:ingots/gold"),CratonBlocks.quartz_vein_gold_ore,Blocks.GRANITE,pegmatite,Blocks.QUARTZ_BLOCK);
        register(context,QUARTZ_VEIN_PYRITE,List.of("c:ingots/iron"),CratonBlocks.quartz_vein_pyrite_ore,Blocks.GRANITE,pegmatite,Blocks.QUARTZ_BLOCK);
        register(context,HYDROTHERMAL_FLUORITE,List.of("c:gems/fluorite"),CratonBlocks.fluorite_vein_ore,Blocks.GRANITE,pegmatite,Blocks.QUARTZ_BLOCK);
        register(context,SANDSTONE_URANIUM,List.of("c:ingots/uranium"),CratonBlocks.sedimentary_uranium_ore,arkose);
        register(context,SHALE_PB_ZN,List.of("c:ingots/lead", "c:ingots/zinc"),CratonBlocks.sedimentary_pb_zn_ore,shale);
        register(context,SHALE_COAL,CratonBlocks.shale_coal_ore,shale);
        register(context,DEEP_HYDROTHERMAL_REDSTONE,CratonBlocks.deep_hydrothermal_redstone_ore,eclogite);
        register(context,GRANITE_CASSITERITE,List.of("c:ingots/tin"),CratonBlocks.granite_cassiterite_ore,Blocks.GRANITE);
        register(context,GRANITE_WOLFRAMITE,List.of("c:ingots/tungsten"),CratonBlocks.granite_wolframite_ore,Blocks.GRANITE);
        register(context,PORPHYRY_GOLD,List.of("c:ingots/gold"),CratonBlocks.porphyry_gold_ore,Blocks.DIORITE);
        register(context,PORPHYRY_COPPER,List.of("c:ingots/copper"),CratonBlocks.porphyry_copper_ore,Blocks.DIORITE);
        register(context,PORPHYRY_SULFIDE,List.of("c:ingots/iron"),CratonBlocks.porphyry_sulfide_ore,Blocks.DIORITE);
        register(context,EPITHERMAL_AU_AG,List.of("c:ingots/gold", "c:ingots/silver"),CratonBlocks.epithermal_au_ag_ore,Blocks.ANDESITE,rhyolite,Blocks.QUARTZ_BLOCK);
        register(context,VMS_CHALCOPYRITE,List.of("c:ingots/copper"),CratonBlocks.vms_copper_ore,Blocks.TUFF,Blocks.BASALT,Blocks.ANDESITE,gabbro);
        register(context,VMS_SPHALERITE,List.of("c:ingots/zinc"),CratonBlocks.vms_zinc_ore,Blocks.TUFF,Blocks.BASALT,Blocks.ANDESITE,gabbro);
        register(context,VMS_GALENA,List.of("c:ingots/lead"),CratonBlocks.vms_pb_ag_ore,Blocks.TUFF,Blocks.BASALT,Blocks.ANDESITE,gabbro);
        register(context,BASALT_BAUXITE,List.of("c:ingots/aluminum"),CratonBlocks.bauxite_ore,Blocks.BASALT);
        register(context,PEAT,CratonBlocks.peat,Blocks.MUD,Blocks.DIRT,Blocks.GRASS_BLOCK);
        register(context,PLACER_GOLD,List.of(
                variant(Blocks.GRAVEL,CratonBlocks.auriferous_gravel),
                variant(Blocks.SAND,CratonBlocks.auriferous_sand)), "c:ingots/gold");
        register(context,PLACER_IRON,List.of(
                variant(Blocks.GRAVEL,CratonBlocks.ferriferous_gravel),
                variant(Blocks.SAND,CratonBlocks.ferriferous_sand),
                variant(Blocks.RED_SAND,CratonBlocks.ferriferous_red_sand)), "c:ingots/iron");
        register(context,KIMBERLITE_DIAMOND,CratonBlocks.diamond_bearing_kimberlite,kimberlite);
        register(context,KIMBERLITE_DIAMOND_RICH,CratonBlocks.diamond_rich_kimberlite,
                kimberlite);
        register(context,SKARN_IRON,List.of("c:ingots/iron"),CratonBlocks.skarn_iron_ore,skarn);
        register(context,SKARN_COPPER,List.of("c:ingots/copper"),CratonBlocks.skarn_copper_ore,skarn);
        register(context,SKARN_W_SN,List.of("c:ingots/tungsten", "c:ingots/tin"),CratonBlocks.skarn_w_sn_ore,skarn);
        register(context,SKARN_PB_ZN_AG,List.of("c:ingots/lead", "c:ingots/zinc"),CratonBlocks.skarn_pb_zn_ore,skarn);
        register(context,SKARN_GOLD,List.of("c:ingots/gold"),CratonBlocks.skarn_gold_ore,skarn);
        register(context,METEORITIC_IRON,List.of("c:ingots/iron"),CratonBlocks.meteoric_iron_ore,CratonBlocks.METEORITE.getOrigin().getBaseBlock());
        register(context,JADEITITE_JADE,CratonBlocks.suspicious_jadeitite,jadeitite);
    }

    private static OreType.Variant variant(Block host,Holder<Block> result){
        return new OreType.Variant(HolderSet.direct(host.builtInRegistryHolder()),result.value().defaultBlockState());
    }

    private static void register(BootstrapContext<OreType> context,ResourceKey<OreType> key,
                                 Holder<Block> result,Block... hosts){
        register(context,key,List.of(),result,hosts);
    }

    private static void register(BootstrapContext<OreType> context,ResourceKey<OreType> key,
                                 List<String> requiredIngotTags,Holder<Block> result,Block... hosts){
        List<OreType.Variant> variants=new ArrayList<>(hosts.length);
        for(Block host:hosts) variants.add(variant(host,result));
        register(context,key,variants,requiredIngotTags.toArray(String[]::new));
    }

    private static void register(BootstrapContext<OreType> context,ResourceKey<OreType> key,
                                 List<OreType.Variant> variants,String... requiredIngotTags){
        var tags=java.util.Arrays.stream(requiredIngotTags)
                .map(id -> net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                        net.minecraft.resources.Identifier.parse(id))).toList();
        context.register(key,new OreType(List.copyOf(variants),tags));
    }
}
