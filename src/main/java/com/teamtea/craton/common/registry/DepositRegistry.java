package com.teamtea.craton.common.registry;

import com.teamtea.craton.Craton;
import com.teamtea.craton.api.geology.deposit.BandedIronFormation;
import com.teamtea.craton.api.geology.deposit.Deposit;
import com.teamtea.craton.api.geology.deposit.DepositTypes;
import com.teamtea.craton.api.geology.deposit.DioriteIntrusionDeposit;
import com.teamtea.craton.api.geology.deposit.FieldDeposit;
import com.teamtea.craton.api.geology.deposit.KimberliteDeposit;
import com.teamtea.craton.api.geology.deposit.MeteoriteDeposit;
import com.teamtea.craton.api.geology.ore.OreType;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.List;

public class DepositRegistry {
    public static final ResourceKey<Deposit> METEORITE = key("meteorite");
    public static final ResourceKey<Deposit> PLACER = key("placer");
    public static final ResourceKey<Deposit> BIF = key("bif");
    public static final ResourceKey<Deposit> BIF2 = key("bif2");
    public static final ResourceKey<Deposit> BIF3 = key("bif3");
    private static ResourceKey<Deposit> key(String name) { return ResourceKey.create(CratonRegistries.DEPOSIT, Craton.rl(name)); }

    private static HolderSet<Block> hostSet(BootstrapContext<Deposit> ctx, String path) {
        return ctx.lookup(Registries.BLOCK).getOrThrow(TagKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath("c", "geology/hosts/" + path)));
    }
    private static HolderSet<Block> noHosts() { return HolderSet.direct(List.of()); }

    private static void field(BootstrapContext<Deposit> ctx, HolderGetter<OreType> ores, String name, Identifier type,
                              FieldDeposit.Placement placement, FieldDeposit.Shape shape, BlockState rock,
                              ResourceKey<OreType>... oreKeys) {
        FieldDeposit.Settings settings=new FieldDeposit.Settings(placement,shape,rock,
                Arrays.stream(oreKeys).<Holder<OreType>>map(ores::getOrThrow).toList(),
                List.of()).withAbundance(com.teamtea.craton.api.geology.deposit.Abundance.forType(type));
        if(type.equals(DepositTypes.EPITHERMAL)) settings=settings.withHosts(hostSet(ctx,"epithermal"),noHosts(),noHosts(),noHosts(),noHosts(),noHosts());
        else if(type.equals(DepositTypes.VMS)) settings=settings.withHosts(hostSet(ctx,"vms"),hostSet(ctx,"vms_cap"),hostSet(ctx,"vms_footwall"),hostSet(ctx,"vms_upper"),hostSet(ctx,"vms_lower"),hostSet(ctx,"vms_lens"));
        else if(type.equals(DepositTypes.WEATHERING)) settings=settings.withHosts(hostSet(ctx,"weathering"),noHosts(),noHosts(),noHosts(),noHosts(),noHosts());
        else if(type.equals(DepositTypes.JADEITITE)) settings=settings.withHosts(hostSet(ctx,"jadeitite"),noHosts(),noHosts(),noHosts(),noHosts(),noHosts());
        else if(type.equals(DepositTypes.DEEP_REDSTONE)) settings=settings.withHosts(hostSet(ctx,"deep_redstone"),noHosts(),noHosts(),noHosts(),noHosts(),noHosts());
        else if(type.equals(DepositTypes.COAL)) settings=settings.withHosts(hostSet(ctx,"stratiform_coal"),noHosts(),noHosts(),noHosts(),noHosts(),noHosts());
        else if(type.equals(DepositTypes.COPPER)) settings=settings.withHosts(hostSet(ctx,"stratiform_copper"),noHosts(),noHosts(),noHosts(),noHosts(),noHosts());
        else if(type.equals(DepositTypes.SHALE_PB_ZN)) settings=settings.withHosts(hostSet(ctx,"shale_pb_zn"),noHosts(),noHosts(),noHosts(),noHosts(),noHosts());
        else if(type.equals(DepositTypes.URANIUM)) settings=settings.withHosts(hostSet(ctx,"sandstone_uranium"),noHosts(),noHosts(),noHosts(),noHosts(),noHosts());
        else if(type.equals(DepositTypes.SKARN)) settings=settings.withHosts(hostSet(ctx,"skarn"),noHosts(),noHosts(),noHosts(),noHosts(),noHosts());
        else if(type.equals(DepositTypes.VEIN)) settings=settings.withHosts(hostSet(ctx,"hydrothermal_emerald"),noHosts(),noHosts(),noHosts(),noHosts(),noHosts());
        else if(type.equals(DepositTypes.PEAT)) settings=settings.withHosts(hostSet(ctx,"peat_soil"),noHosts(),noHosts(),noHosts(),noHosts(),noHosts());
        Deposit deposit;
        if(type.equals(DepositTypes.KIMBERLITE))
            deposit=new KimberliteDeposit(settings,new KimberliteDeposit.RichBody(.35,2.2,4.2));
        else if(type.equals(DepositTypes.DIORITE))
            deposit=new DioriteIntrusionDeposit(settings,new DioriteIntrusionDeposit.Mineralization(.42,.68,.75,.55,true,2,4,3));
        else deposit=DepositTypes.createField(type,settings);
        ctx.register(key(name),deposit);
    }
    /** Deposits without their own rock bodies mineralize the existing geological host. */
    @SafeVarargs
    private static void field(BootstrapContext<Deposit> ctx, HolderGetter<OreType> ores, String name, Identifier type,
                              FieldDeposit.Placement placement, FieldDeposit.Shape shape,
                              ResourceKey<OreType>... oreKeys) {
        field(ctx, ores, name, type, placement, shape, Blocks.AIR.defaultBlockState(), oreKeys);
    }

    private static FieldDeposit.Placement p(int cell, int count, double reach, double frequency) {
        return new FieldDeposit.Placement(cell, count, reach, frequency);
    }
    private static FieldDeposit.Shape s(double x0,double x1,double z0,double z1,double y0,double y1,
                                        double h0,double h1,double broad,double detail) {
        return new FieldDeposit.Shape(x0,x1,z0,z1,y0,y1,h0,h1,broad,detail,6,1.0);
    }

    private static FieldDeposit.Shape branches(FieldDeposit.Shape shape,int count) {
        return new FieldDeposit.Shape(shape.radiusXMin(),shape.radiusXMax(),shape.radiusZMin(),shape.radiusZMax(),
                shape.radiusYMin(),shape.radiusYMax(),shape.heightMin(),shape.heightMax(),
                shape.broadNoise(),shape.detailNoise(),count,shape.satelliteScale());
    }

    public static void bootstrap(BootstrapContext<Deposit> ctx) {
        var ores=ctx.lookup(CratonRegistries.ORE_TYPE);
        ctx.register(METEORITE, new MeteoriteDeposit(ores.getOrThrow(OreTypeRegistry.METEORITIC_IRON),
                CratonBlocks.METEORITE.getOrigin().getBaseBlock().defaultBlockState(), 3, 6, .13f, 128,
                ctx.lookup(Registries.BLOCK).getOrThrow(TagKey.create(Registries.BLOCK,
                        Identifier.fromNamespaceAndPath("c", "geology/meteorite_replaceable")))));
        ctx.register(BIF,new BandedIronFormation(ores.getOrThrow(OreTypeRegistry.BIF_IRON),
                360,90,3.8,0,30,4,100,.90,.18,5,.32,.075).withHosts(hostSet(ctx,"bif")));
        ctx.register(BIF2,new BandedIronFormation(ores.getOrThrow(OreTypeRegistry.BIF_IRON),
                240,65,3.2,0,30,4,80,.85,.14,4,.32,.075).withHosts(hostSet(ctx,"bif")));
        ctx.register(BIF3,new BandedIronFormation(ores.getOrThrow(OreTypeRegistry.BIF_IRON),
                160,45,2.8,0,30,4,60,.80,.08,3,.32,.075).withHosts(hostSet(ctx,"bif")));
        field(ctx,ores,"granite_intrusion",DepositTypes.GRANITE,p(320,1,210,0.65),branches(s(75,140,58,110,38,72,35,112,.18,.06),3),Blocks.GRANITE.defaultBlockState(),OreTypeRegistry.GRANITE_CASSITERITE,OreTypeRegistry.GRANITE_WOLFRAMITE);
        field(ctx,ores,"diorite_intrusion",DepositTypes.DIORITE,p(288,1,165,0.7),branches(s(52,104,40,82,42,84,38,120,.21,.07),2),Blocks.DIORITE.defaultBlockState(),OreTypeRegistry.PORPHYRY_COPPER,OreTypeRegistry.PORPHYRY_GOLD,OreTypeRegistry.PORPHYRY_SULFIDE);
        field(ctx,ores,"gabbro_intrusion",DepositTypes.GABBRO,p(352,1,220,0.6),branches(s(82,160,55,115,17,34,28,96,.17,.06),2),CratonBlocks.GABBRO.getOrigin().getBaseBlock().defaultBlockState(),OreTypeRegistry.GABBRO_NI_CU,OreTypeRegistry.GABBRO_PGE);
        field(ctx,ores,"epithermal",DepositTypes.EPITHERMAL,p(224,1,205,0.65),s(74.8,74.8,3.06,3.06,46.75,46.75,45,125,.20,.09),OreTypeRegistry.EPITHERMAL_AU_AG);
        field(ctx,ores,"vms",DepositTypes.VMS,p(256,1,130,0.7),s(35.7,74.8,22.1,51.85,5.1,11.9,0,0,.29,.09),OreTypeRegistry.VMS_CHALCOPYRITE,OreTypeRegistry.VMS_SPHALERITE,OreTypeRegistry.VMS_GALENA);
        field(ctx,ores,"skarn",DepositTypes.SKARN,p(160,0,0,1),s(0,0,0,0,12,12,0,0,.46,.15),CratonBlocks.SKARN.getOrigin().getBaseBlock().defaultBlockState(),OreTypeRegistry.SKARN_W_SN,OreTypeRegistry.SKARN_PB_ZN_AG,OreTypeRegistry.SKARN_COPPER,OreTypeRegistry.SKARN_GOLD,OreTypeRegistry.SKARN_IRON,OreTypeRegistry.METASOMATIC_LAPIS);
        field(ctx,ores,"hydrothermal_vein",DepositTypes.VEIN,p(192,2,245,.35),branches(s(36,72,1.1,2.0,12,28,45,125,.20,.09),2),CratonBlocks.PEGMATITE.getOrigin().getBaseBlock().defaultBlockState(),OreTypeRegistry.QUARTZ_VEIN_GOLD,OreTypeRegistry.QUARTZ_VEIN_PYRITE,OreTypeRegistry.HYDROTHERMAL_FLUORITE,OreTypeRegistry.PEGMATITE_QUARTZ,OreTypeRegistry.PEGMATITE_EMERALD);
        field(ctx,ores,"weathering",DepositTypes.WEATHERING,p(224,1,160,0.55),s(62,110,62,110,13,13,0,0,.28,.08),OreTypeRegistry.BASALT_BAUXITE);
        field(ctx,ores,"kimberlite",DepositTypes.KIMBERLITE,p(384,1,95,0.2),s(6,26,6,26,48,62,48,92,.18,.06),CratonBlocks.KIMBERLITE.getOrigin().getBaseBlock().defaultBlockState(),OreTypeRegistry.KIMBERLITE_DIAMOND,OreTypeRegistry.KIMBERLITE_DIAMOND_RICH);
        field(ctx,ores,"jadeitite",DepositTypes.JADEITITE,p(320,1,120,0.25),s(40.8,40.8,16.15,16.15,9.35,9.35,18,68,.22,.07),CratonBlocks.JADEITITE.getOrigin().getBaseBlock().defaultBlockState(),OreTypeRegistry.JADEITITE_JADE);
        field(ctx,ores,"peat",DepositTypes.PEAT,p(160,2,70,.60),branches(s(12,28,10,24,2,5,0,0,.16,.04),0),OreTypeRegistry.PEAT);
        field(ctx,ores,"placer",DepositTypes.PLACER,p(160,2,95,1.0),s(28,64,28,64,4,4,0,0,.25,.08),OreTypeRegistry.PLACER_GOLD,OreTypeRegistry.PLACER_IRON);
        field(ctx,ores,"stratiform_coal",DepositTypes.COAL,p(256,2,190,0.45),s(61.2,110.5,27.2,62.9,1.955,5.525,0,0,.25,.07),OreTypeRegistry.LIMESTONE_COAL,OreTypeRegistry.SHALE_COAL);
        field(ctx,ores,"stratiform_copper",DepositTypes.COPPER,p(288,1,190,0.65),s(61.2,110.5,27.2,62.9,1.955,5.525,0,0,.25,.07),OreTypeRegistry.SEDIMENTARY_COPPER);
        field(ctx,ores,"sandstone_uranium",DepositTypes.URANIUM,p(320,1,190,0.6),s(68.0,114.75,30.6,62.9,2.38,4.25,0,0,.22,.07),OreTypeRegistry.SANDSTONE_URANIUM);
        field(ctx,ores,"shale_pb_zn",DepositTypes.SHALE_PB_ZN,p(288,1,190,0.65),s(55.25,97.75,23.8,52.7,1.7,3.825,0,0,.25,.08),OreTypeRegistry.SHALE_PB_ZN);
        field(ctx,ores,"deep_hydrothermal_redstone",DepositTypes.DEEP_REDSTONE,p(256,1,140,0.6),s(23.8,47.6,2.55,5.95,2.55,5.1,0,0,.24,.08),OreTypeRegistry.DEEP_HYDROTHERMAL_REDSTONE);
    }
}
