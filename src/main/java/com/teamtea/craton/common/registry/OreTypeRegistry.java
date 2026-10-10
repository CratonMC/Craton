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
public final class OreTypeRegistry {
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

        register(context,BIF_IRON,List.of(
                variant(gneiss,CratonBlocks.banded_iron_gneiss_ore)));
        register(context,LIMESTONE_COAL,CratonBlocks.limestone_coal_ore,limestone);
        register(context,SEDIMENTARY_COPPER,CratonBlocks.sedimentary_copper_ore,limestone);
        register(context,METASOMATIC_LAPIS,CratonBlocks.metasomatic_lapis_ore,marble);
        register(context,GABBRO_PGE,CratonBlocks.magmatic_pge_ore,gabbro);
        register(context,GABBRO_NI_CU,CratonBlocks.magmatic_ni_cu_ore,gabbro);
        register(context,PEGMATITE_QUARTZ,CratonBlocks.pegmatite_quartz_ore,pegmatite);
        register(context,PEGMATITE_EMERALD,CratonBlocks.pegmatite_emerald_ore,pegmatite);
        register(context,QUARTZ_VEIN_GOLD,CratonBlocks.quartz_vein_gold_ore,Blocks.GRANITE,pegmatite,Blocks.QUARTZ_BLOCK);
        register(context,QUARTZ_VEIN_PYRITE,CratonBlocks.quartz_vein_pyrite_ore,Blocks.GRANITE,pegmatite,Blocks.QUARTZ_BLOCK);
        register(context,HYDROTHERMAL_FLUORITE,CratonBlocks.fluorite_vein_ore,Blocks.GRANITE,pegmatite,Blocks.QUARTZ_BLOCK);
        register(context,SANDSTONE_URANIUM,CratonBlocks.sedimentary_uranium_ore,Blocks.SANDSTONE,Blocks.RED_SANDSTONE);
        // Shale and eclogite are not registered yet; these use temporary host proxies.
        register(context,SHALE_PB_ZN,CratonBlocks.sedimentary_pb_zn_ore,Blocks.DEEPSLATE);
        register(context,SHALE_COAL,CratonBlocks.shale_coal_ore,Blocks.DEEPSLATE);
        register(context,DEEP_HYDROTHERMAL_REDSTONE,CratonBlocks.deep_hydrothermal_redstone_ore,Blocks.DEEPSLATE);
        register(context,GRANITE_CASSITERITE,CratonBlocks.granite_cassiterite_ore,Blocks.GRANITE);
        register(context,GRANITE_WOLFRAMITE,CratonBlocks.granite_wolframite_ore,Blocks.GRANITE);
        register(context,PORPHYRY_GOLD,CratonBlocks.porphyry_gold_ore,Blocks.DIORITE);
        register(context,PORPHYRY_COPPER,CratonBlocks.porphyry_copper_ore,Blocks.DIORITE);
        register(context,PORPHYRY_SULFIDE,CratonBlocks.porphyry_sulfide_ore,Blocks.DIORITE);
        register(context,EPITHERMAL_AU_AG,CratonBlocks.epithermal_au_ag_ore,Blocks.ANDESITE,rhyolite,Blocks.QUARTZ_BLOCK);
        register(context,VMS_CHALCOPYRITE,CratonBlocks.vms_copper_ore,Blocks.TUFF,Blocks.BASALT,Blocks.ANDESITE,gabbro);
        register(context,VMS_SPHALERITE,CratonBlocks.vms_zinc_ore,Blocks.TUFF,Blocks.BASALT,Blocks.ANDESITE,gabbro);
        register(context,VMS_GALENA,CratonBlocks.vms_pb_ag_ore,Blocks.TUFF,Blocks.BASALT,Blocks.ANDESITE,gabbro);
        register(context,BASALT_BAUXITE,CratonBlocks.bauxite_ore,Blocks.BASALT);
        register(context,PEAT,CratonBlocks.peat,Blocks.MUD);
        register(context,PLACER_GOLD,List.of(
                variant(Blocks.GRAVEL,CratonBlocks.auriferous_gravel),
                variant(Blocks.SAND,CratonBlocks.auriferous_sand)));
        register(context,PLACER_IRON,List.of(
                variant(Blocks.GRAVEL,CratonBlocks.ferriferous_gravel),
                variant(Blocks.RED_SAND,CratonBlocks.ferriferous_sand)));
        register(context,KIMBERLITE_DIAMOND,CratonBlocks.diamond_bearing_kimberlite,Blocks.BLACKSTONE);
        register(context,KIMBERLITE_DIAMOND_RICH,CratonBlocks.diamond_rich_kimberlite,
                CratonBlocks.diamond_bearing_kimberlite.value());
        register(context,SKARN_IRON,CratonBlocks.skarn_iron_ore,marble);
        register(context,SKARN_COPPER,CratonBlocks.skarn_copper_ore,marble);
        register(context,SKARN_W_SN,CratonBlocks.skarn_w_sn_ore,marble);
        register(context,SKARN_PB_ZN_AG,CratonBlocks.skarn_pb_zn_ore,marble);
        register(context,SKARN_GOLD,CratonBlocks.skarn_gold_ore,marble);
        register(context,METEORITIC_IRON,CratonBlocks.meteoric_iron_ore,Blocks.BASALT);
        register(context,JADEITITE_JADE,CratonBlocks.suspicious_jadeitite,gneiss,Blocks.DEEPSLATE);
    }

    private static OreType.Variant variant(Block host,Holder<Block> result){
        return new OreType.Variant(HolderSet.direct(host.builtInRegistryHolder()),result.value().defaultBlockState());
    }

    private static void register(BootstrapContext<OreType> context,ResourceKey<OreType> key,
                                 Holder<Block> result,Block... hosts){
        List<OreType.Variant> variants=new ArrayList<>(hosts.length);
        for(Block host:hosts) variants.add(variant(host,result));
        register(context,key,variants);
    }

    private static void register(BootstrapContext<OreType> context,ResourceKey<OreType> key,
                                 List<OreType.Variant> variants){
        context.register(key,new OreType(List.copyOf(variants)));
    }
}
