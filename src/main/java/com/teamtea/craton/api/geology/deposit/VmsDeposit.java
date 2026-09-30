package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.registry.CratonBlocks;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;
import com.teamtea.craton.common.core.DepositFieldEngine.ColumnContext;
import com.teamtea.craton.common.core.*;
import static com.teamtea.craton.common.core.DepositFieldSupport.*;


public final class VmsDeposit extends FieldDeposit {
    public static final MapCodec<VmsDeposit> CODEC =
            codecFor(3, false, VmsDeposit::new);

    public VmsDeposit(Settings settings) {
        super(DepositTypes.VMS,settings);
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }



    public static BlockState applyVms(ColumnContext ctx,BlockState originalHost,BlockState state,int y){
        boolean cap=originalHost.is(Blocks.TUFF)||originalHost.is(Blocks.BASALT);
        boolean footwall=originalHost.is(Blocks.ANDESITE)||originalHost.is(CratonBlocks.GABBRO.getOrigin().getBaseBlock());
        if(!cap&&!footwall) return state;
        List<Integer> tuffLayers=matchingLayers(ctx,Blocks.TUFF.defaultBlockState());
        if(tuffLayers.isEmpty()) return state;

        double province=.5+.5*GeologicalNoise.fbm2(ctx.x(),ctx.z(),DepositRandomSequences.VMS.salt()^0x564D5350L,.0019,4);
        if(province<.46) return state;

        for(DepositCandidateSampler.Candidate c:ctx.vms()){
            int layer=tuffLayers.get(index(c.verticalSeed(),tuffLayers.size()));
            if(layer==0||layer+1>=ctx.geology().layers().size()
                    ||!ctx.geology().layers().get(layer-1).value().blockState().is(Blocks.BASALT)
                    ||!ctx.geology().layers().get(layer+1).value().blockState().is(CratonBlocks.GABBRO.getOrigin().getBaseBlock())) continue;
            double cy=ctx.geology().boundaryY(layer,ctx.x(),ctx.z())-2.0;
            double angle=angle(c.rotationSeed());
            double rotDx=ctx.x()+.5-c.x(),rotDz=ctx.z()+.5-c.z();
            double rotCos=Math.cos(angle),rotSin=Math.sin(angle);
            double rotAlong=rotDx*rotCos+rotDz*rotSin,rotAcross=-rotDx*rotSin+rotDz*rotCos;
            FieldDeposit.Shape config=shape(ctx,c);
            double rx=config.x(c.shapeSeed()),rz=config.z(c.shapeSeed()^91),ry=config.y(c.shapeSeed()^173);
            double broad=GeologicalNoise.fbm2(ctx.x(),ctx.z(),c.shapeSeed()^0x46A7L,.009,3);
            double detail=GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.shapeSeed(),.042,3);
            double upper=y+.5-cy;
            double vertical=upper>0?upper/(ry*.55):upper/(ry*1.30);
            double lens=1-sq(rotAlong/rx)-sq(rotAcross/rz)-sq(vertical)+broad*config.broadNoise()+detail*config.detailNoise();

            double feeder=feederScore(c,ctx.x(),y,ctx.z(),cy,Math.min(rx,rz)*.45,30+rand01(c.shapeSeed()^0x99)*25);
            double satellites=satellitePods(c,ctx.x(),y,ctx.z(),c.x(),cy,c.z(),rx,ry,rz,angle,0x564D53L,config);
            if(!cap) lens=-9;
            double score=Math.max(Math.max(lens,feeder*.70),cap?satellites*.65-.04:-9);
            if(score<-.15) continue;

            double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.075,3);
            if(!occupies(score,grade,c,ctx.x(),y,ctx.z(),.10,.78)) continue;
            double mix=GeologicalNoise.occupancy(ctx.x()+13,y,ctx.z()-7,c.oreSeed(),.12);
            if(feeder*.70>lens) return ore(ctx,c,mix<.70?0:1,state);
            if(mix<.43) return ore(ctx,c,0,state);
            if(mix<.78) return ore(ctx,c,1,state);
            return ore(ctx,c,2,state);
        }
        return state;
    }

    private static double feederScore(DepositCandidateSampler.Candidate c,int x,int y,int z,double top,double radius,double depth){
        if(y>top+2||y<top-depth) return -9;
        double t=(top-y)/depth;
        double warpX=GeologicalNoise.fbm(x,y,z,c.shapeSeed()^0x321,.035,3)*8;
        double warpZ=GeologicalNoise.fbm(x,y,z,c.shapeSeed()^0x765,.035,3)*8;
        double dx=x+.5-c.x()+warpX,dz=z+.5-c.z()+warpZ;
        double widening=radius*(.22+.52*(1-t));
        double d=Math.hypot(dx,dz)/Math.max(1,widening);
        double stringer=.5+.5*GeologicalNoise.fbm(x*1.35,y,z*1.35,c.alterationSeed(),.095,3);
        return 1-d+(stringer-.5)*.55;
    }

}
