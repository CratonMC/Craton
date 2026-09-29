package com.teamtea.craton.common.registry;

import com.teamtea.craton.Craton;
import com.teamtea.craton.api.geology.ore.OreType;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.references.BlockItemIds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * OreType names describe a concrete ore occurrence in a concrete deposit family.
 * Cross-deposit resource equivalence (iron/copper/gold/...) belongs in tags, not here.
 * Most entries deliberately use coloured terracotta as temporary visual placeholders.
 */
public final class OreTypeRegistry {
    public static final ResourceKey<OreType> BIF_IRON = key("bif_iron");
    public static final ResourceKey<OreType> LIMESTONE_COAL = key("limestone_coal");
    public static final ResourceKey<OreType> SEDIMENTARY_COPPER = key("sedimentary_copper");
    public static final ResourceKey<OreType> SHALE_PB_ZN = key("shale_pb_zn");
    public static final ResourceKey<OreType> SANDSTONE_URANIUM = key("sandstone_uranium");

    public static final ResourceKey<OreType> GRANITE_W_SN = key("granite_w_sn");
    public static final ResourceKey<OreType> DIORITE_CU_AU = key("diorite_cu_au");
    public static final ResourceKey<OreType> GABBRO_NI_CU = key("gabbro_ni_cu");
    public static final ResourceKey<OreType> GABBRO_PGE = key("gabbro_pge");

    public static final ResourceKey<OreType> EPITHERMAL_AU_AG = key("epithermal_au_ag");

    public static final ResourceKey<OreType> VMS_CHALCOPYRITE = key("vms_chalcopyrite");
    public static final ResourceKey<OreType> VMS_SPHALERITE = key("vms_sphalerite");
    public static final ResourceKey<OreType> VMS_GALENA = key("vms_galena");
    public static final ResourceKey<OreType> VMS_PYRITE = key("vms_pyrite");

    public static final ResourceKey<OreType> SKARN_IRON = key("skarn_iron");
    public static final ResourceKey<OreType> SKARN_COPPER = key("skarn_copper");
    public static final ResourceKey<OreType> SKARN_W_SN = key("skarn_w_sn");
    public static final ResourceKey<OreType> SKARN_PB_ZN_AG = key("skarn_pb_zn_ag");
    public static final ResourceKey<OreType> SKARN_GOLD = key("skarn_gold");

    public static final ResourceKey<OreType> QUARTZ_VEIN_GOLD = key("quartz_vein_gold");
    public static final ResourceKey<OreType> QUARTZ_VEIN_PYRITE = key("quartz_vein_pyrite");
    public static final ResourceKey<OreType> HYDROTHERMAL_FLUORITE = key("hydrothermal_fluorite");

    public static final ResourceKey<OreType> BASALT_BAUXITE = key("basalt_bauxite");

    public static final ResourceKey<OreType> KIMBERLITE_DIAMOND = key("kimberlite_diamond");
    public static final ResourceKey<OreType> METEORITIC_IRON = key("meteoritic_iron");
    public static final ResourceKey<OreType> JADEITITE_JADE = key("jadeitite_jade");

    public static final ResourceKey<OreType> PLACER_GOLD = key("placer_gold");
    public static final ResourceKey<OreType> PLACER_IRON = key("placer_iron");

    private static ResourceKey<OreType> key(String name){
        return ResourceKey.create(CratonRegistries.ORE_TYPE,Craton.rl(name));
    }

    public static void bootstrap(BootstrapContext<OreType> context){
        var blocks=context.lookup(Registries.BLOCK);
        var stoneReplaceables=blocks.getOrThrow(BlockTags.STONE_ORE_REPLACEABLES);
        var deepslateReplaceables=blocks.getOrThrow(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

        List<OreType.Variant> bif=new ArrayList<>();
        bif.add(new OreType.Variant(HolderSet.direct(CratonBlocks.GNEISS.getOrigin().getBaseBlock().builtInRegistryHolder()),CratonBlocks.banded_iron_gneiss_ore.value().defaultBlockState()));
        bif.add(new OreType.Variant(HolderSet.direct(CratonBlocks.MARBLE.getOrigin().getBaseBlock().builtInRegistryHolder()),CratonBlocks.banded_iron_marble_ore.value().defaultBlockState()));
        bif.add(new OreType.Variant(HolderSet.direct(blocks.getOrThrow(BlockItemIds.STONE.block())),CratonBlocks.banded_iron_stone_ore.value().defaultBlockState()));
        bif.add(new OreType.Variant(stoneReplaceables,Blocks.IRON_ORE.defaultBlockState()));
        bif.add(new OreType.Variant(deepslateReplaceables,Blocks.DEEPSLATE_IRON_ORE.defaultBlockState()));
        context.register(BIF_IRON,new OreType(List.copyOf(bif)));

        registerPlaceholder(context,LIMESTONE_COAL,Blocks.DYED_TERRACOTTA.black().defaultBlockState());
        registerPlaceholder(context,SEDIMENTARY_COPPER,Blocks.DYED_TERRACOTTA.orange().defaultBlockState());
        registerPlaceholder(context,SHALE_PB_ZN,Blocks.DYED_TERRACOTTA.gray().defaultBlockState());
        registerPlaceholder(context,SANDSTONE_URANIUM,Blocks.DYED_TERRACOTTA.lime().defaultBlockState());

        registerPlaceholder(context,GRANITE_W_SN,Blocks.DYED_TERRACOTTA.lightGray().defaultBlockState());
        registerPlaceholder(context,DIORITE_CU_AU,Blocks.DYED_TERRACOTTA.yellow().defaultBlockState());
        registerPlaceholder(context,GABBRO_NI_CU,Blocks.DYED_TERRACOTTA.green().defaultBlockState());
        registerPlaceholder(context,GABBRO_PGE,Blocks.DYED_TERRACOTTA.purple().defaultBlockState());

        registerPlaceholder(context,EPITHERMAL_AU_AG,Blocks.DYED_TERRACOTTA.pink().defaultBlockState());

        registerPlaceholder(context,VMS_CHALCOPYRITE,Blocks.DYED_TERRACOTTA.orange().defaultBlockState());
        registerPlaceholder(context,VMS_SPHALERITE,Blocks.DYED_TERRACOTTA.brown().defaultBlockState());
        registerPlaceholder(context,VMS_GALENA,Blocks.DYED_TERRACOTTA.lightGray().defaultBlockState());
        registerPlaceholder(context,VMS_PYRITE,Blocks.DYED_TERRACOTTA.yellow().defaultBlockState());

        registerPlaceholder(context,SKARN_IRON,Blocks.DYED_TERRACOTTA.red().defaultBlockState());
        registerPlaceholder(context,SKARN_COPPER,Blocks.DYED_TERRACOTTA.orange().defaultBlockState());
        registerPlaceholder(context,SKARN_W_SN,Blocks.DYED_TERRACOTTA.white().defaultBlockState());
        registerPlaceholder(context,SKARN_PB_ZN_AG,Blocks.DYED_TERRACOTTA.lightGray().defaultBlockState());
        registerPlaceholder(context,SKARN_GOLD,Blocks.DYED_TERRACOTTA.yellow().defaultBlockState());

        registerPlaceholder(context,QUARTZ_VEIN_GOLD,Blocks.DYED_TERRACOTTA.yellow().defaultBlockState());
        registerPlaceholder(context,QUARTZ_VEIN_PYRITE,Blocks.DYED_TERRACOTTA.brown().defaultBlockState());
        registerPlaceholder(context,HYDROTHERMAL_FLUORITE,Blocks.DYED_TERRACOTTA.cyan().defaultBlockState());

        registerPlaceholder(context,BASALT_BAUXITE,Blocks.DYED_TERRACOTTA.orange().defaultBlockState());

        registerPlaceholder(context,KIMBERLITE_DIAMOND,Blocks.DYED_TERRACOTTA.lightBlue().defaultBlockState());
        registerPlaceholder(context,METEORITIC_IRON,Blocks.DYED_TERRACOTTA.gray().defaultBlockState());
        registerPlaceholder(context,JADEITITE_JADE,Blocks.DYED_TERRACOTTA.green().defaultBlockState());

        registerPlaceholder(context,PLACER_GOLD,Blocks.DYED_TERRACOTTA.yellow().defaultBlockState());
        registerPlaceholder(context,PLACER_IRON,Blocks.DYED_TERRACOTTA.red().defaultBlockState());
    }

    private static void registerPlaceholder(BootstrapContext<OreType> context,ResourceKey<OreType> key,BlockState result){
        List<OreType.Variant> variants=new ArrayList<>();
        for(Block host:List.of(
                Blocks.STONE,Blocks.DEEPSLATE,Blocks.GRANITE,Blocks.DIORITE,Blocks.ANDESITE,Blocks.TUFF,Blocks.BASALT,
                Blocks.SANDSTONE,Blocks.RED_SANDSTONE,Blocks.SAND,Blocks.RED_SAND,Blocks.GRAVEL,
                CratonBlocks.GABBRO.getOrigin().getBaseBlock(),CratonBlocks.GNEISS.getOrigin().getBaseBlock(),
                CratonBlocks.LIMESTONE.getOrigin().getBaseBlock(),CratonBlocks.MARBLE.getOrigin().getBaseBlock(),
                CratonBlocks.RHYOLITE.getOrigin().getBaseBlock(),CratonBlocks.PEGMATITE.getOrigin().getBaseBlock())){
            variants.add(new OreType.Variant(HolderSet.direct(host.builtInRegistryHolder()),result));
        }
        context.register(key,new OreType(List.copyOf(variants)));
    }
}
