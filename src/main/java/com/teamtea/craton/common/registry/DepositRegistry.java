package com.teamtea.craton.common.registry;

import com.teamtea.craton.Craton;
import com.teamtea.craton.api.geology.deposit.BandedIronFormation;
import com.teamtea.craton.api.geology.deposit.Deposit;
import com.teamtea.craton.api.geology.deposit.DepositTypes;
import com.teamtea.craton.api.geology.deposit.FieldDeposit;
import com.teamtea.craton.api.geology.ore.OreType;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.List;

public final class DepositRegistry {
    public static final ResourceKey<Deposit> BIF = key("bif");
    public static final ResourceKey<Deposit> BIF2 = key("bif2");
    public static final ResourceKey<Deposit> BIF3 = key("bif3");
    private static ResourceKey<Deposit> key(String name) { return ResourceKey.create(CratonRegistries.DEPOSIT, Craton.rl(name)); }

    private static void field(BootstrapContext<Deposit> ctx, HolderGetter<OreType> ores, String name, Identifier type,
                              FieldDeposit.Placement placement, FieldDeposit.Shape shape, BlockState rock,
                              ResourceKey<OreType>... oreKeys) {
        ctx.register(key(name), new FieldDeposit(type, new FieldDeposit.Settings(placement, shape, rock,
                Arrays.stream(oreKeys).<Holder<OreType>>map(ores::getOrThrow).toList(),
                type.equals(DepositTypes.WEATHERING)?List.of(Blocks.DYED_TERRACOTTA.red().defaultBlockState(),
                        Blocks.DYED_TERRACOTTA.orange().defaultBlockState(),Blocks.DYED_TERRACOTTA.brown().defaultBlockState()):List.of())));
    }
    private static FieldDeposit.Placement p(int cell, int count, double reach, double frequency) {
        return new FieldDeposit.Placement(cell, count, reach, frequency);
    }
    private static FieldDeposit.Shape s(double x0,double x1,double z0,double z1,double y0,double y1,
                                        double h0,double h1,double broad,double detail) {
        return new FieldDeposit.Shape(x0,x1,z0,z1,y0,y1,h0,h1,broad,detail,6,1.0);
    }

    public static void bootstrap(BootstrapContext<Deposit> ctx) {
        var ores=ctx.lookup(CratonRegistries.ORE_TYPE);
        for (int i=0;i<3;i++) ctx.register(i==0?BIF:i==1?BIF2:BIF3,
                new BandedIronFormation(ores.getOrThrow(OreTypeRegistry.BIF_IRON),800,300,i==0?3:i==1?6:5,0,30,4,120,.25,8,.32,.075));
        field(ctx,ores,"granite_intrusion",DepositTypes.GRANITE,p(224,2,210,.35),s(75,140,58,110,38,72,35,112,.18,.06),Blocks.GRANITE.defaultBlockState(),OreTypeRegistry.GRANITE_W_SN);
        field(ctx,ores,"diorite_intrusion",DepositTypes.DIORITE,p(192,2,165,.30),s(52,104,40,82,42,84,38,120,.21,.07),Blocks.DIORITE.defaultBlockState(),OreTypeRegistry.DIORITE_CU_AU);
        field(ctx,ores,"gabbro_intrusion",DepositTypes.GABBRO,p(224,2,220,.22),s(82,160,55,115,17,34,28,96,.17,.06),CratonBlocks.GABBRO.getOrigin().getBaseBlock().defaultBlockState(),OreTypeRegistry.GABBRO_NI_CU,OreTypeRegistry.GABBRO_PGE);
        field(ctx,ores,"epithermal",DepositTypes.EPITHERMAL,p(160,2,205,.42),s(88,88,3.6,3.6,55,55,45,125,.20,.09),Blocks.QUARTZ_BLOCK.defaultBlockState(),OreTypeRegistry.EPITHERMAL_AU_AG);
        field(ctx,ores,"vms",DepositTypes.VMS,p(192,2,130,.55),s(42,88,26,61,6,14,0,0,.29,.09),Blocks.TUFF.defaultBlockState(),OreTypeRegistry.VMS_CHALCOPYRITE,OreTypeRegistry.VMS_SPHALERITE,OreTypeRegistry.VMS_GALENA,OreTypeRegistry.VMS_PYRITE);
        field(ctx,ores,"skarn",DepositTypes.SKARN,p(160,0,0,1),s(0,0,0,0,12,12,0,0,.46,.15),CratonBlocks.MARBLE.getOrigin().getBaseBlock().defaultBlockState(),OreTypeRegistry.SKARN_W_SN,OreTypeRegistry.SKARN_PB_ZN_AG,OreTypeRegistry.SKARN_COPPER,OreTypeRegistry.SKARN_GOLD,OreTypeRegistry.SKARN_IRON);
        field(ctx,ores,"hydrothermal_vein",DepositTypes.VEIN,p(192,2,245,.40),s(125,125,2.5,2.5,82,82,45,125,.20,.09),Blocks.QUARTZ_BLOCK.defaultBlockState(),OreTypeRegistry.QUARTZ_VEIN_GOLD,OreTypeRegistry.QUARTZ_VEIN_PYRITE,OreTypeRegistry.HYDROTHERMAL_FLUORITE);
        field(ctx,ores,"weathering",DepositTypes.WEATHERING,p(160,2,160,.36),s(62,110,62,110,13,13,0,0,.28,.08),Blocks.DYED_TERRACOTTA.orange().defaultBlockState(),OreTypeRegistry.BASALT_BAUXITE);
        field(ctx,ores,"kimberlite",DepositTypes.KIMBERLITE,p(256,1,95,.18),s(6,26,6,26,48,62,48,92,.18,.06),Blocks.BLACKSTONE.defaultBlockState(),OreTypeRegistry.KIMBERLITE_DIAMOND);
        field(ctx,ores,"jadeitite",DepositTypes.JADEITITE,p(224,1,120,.22),s(48,48,19,19,11,11,18,68,.22,.07),Blocks.DYED_TERRACOTTA.green().defaultBlockState(),OreTypeRegistry.JADEITITE_JADE);
        field(ctx,ores,"placer",DepositTypes.PLACER,p(128,2,95,.48),s(28,64,28,64,4,4,0,0,.25,.08),Blocks.GRAVEL.defaultBlockState(),OreTypeRegistry.PLACER_GOLD,OreTypeRegistry.PLACER_IRON);
        field(ctx,ores,"stratiform_coal",DepositTypes.COAL,p(192,2,190,.40),s(72,130,32,74,2.3,6.5,0,0,.25,.07),Blocks.DYED_TERRACOTTA.black().defaultBlockState(),OreTypeRegistry.LIMESTONE_COAL);
        field(ctx,ores,"stratiform_copper",DepositTypes.COPPER,p(192,2,190,.38),s(72,130,32,74,2.3,6.5,0,0,.25,.07),Blocks.DYED_TERRACOTTA.orange().defaultBlockState(),OreTypeRegistry.SEDIMENTARY_COPPER);
        field(ctx,ores,"sandstone_uranium",DepositTypes.URANIUM,p(192,2,190,.34),s(80,135,36,74,2.8,5,0,0,.22,.07),Blocks.DYED_TERRACOTTA.lime().defaultBlockState(),OreTypeRegistry.SANDSTONE_URANIUM);
    }
}
