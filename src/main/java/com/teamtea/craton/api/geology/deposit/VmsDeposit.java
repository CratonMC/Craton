package com.teamtea.craton.api.geology.deposit;

import com.teamtea.craton.common.worldgen.deposit.DepositCandidateSampler;
import com.teamtea.craton.common.worldgen.deposit.PreparedPods;
import com.teamtea.craton.common.worldgen.geology.GeologyFieldSampler;
import com.teamtea.craton.common.worldgen.noise.GeologicalNoise;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.worldgen.deposit.DepositRandomSequences;

import com.teamtea.craton.common.registry.CratonBlocks;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;
import com.teamtea.craton.common.worldgen.deposit.DepositGenerationContext;

import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.*;

public class VmsDeposit extends OrdinaryDeposit {
    public static final MapCodec<VmsDeposit> CODEC =
            codecFor(3, false, VmsDeposit::new);

    public VmsDeposit(Settings settings) {
        super(DepositTypes.VMS,settings,DepositRandomSequences.VMS,10);
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }

    @Override
    public boolean canPlace(DepositGenerationContext ctx,BlockState originalHost,BlockState state,int y){
        return state.equals(originalHost)&&(originalHost.is(Blocks.TUFF)||originalHost.is(Blocks.BASALT)||originalHost.is(Blocks.ANDESITE)||originalHost.is(CratonBlocks.GABBRO.getOrigin().getBaseBlock()));
    }

    @Override
    public BlockState place(DepositGenerationContext ctx,DepositCandidateSampler.Candidate c,
                            BlockState originalHost,BlockState state,int y){
        if(!state.equals(originalHost)) return null;
        boolean cap=originalHost.is(Blocks.TUFF)||originalHost.is(Blocks.BASALT);
        boolean footwall=originalHost.is(Blocks.ANDESITE)||originalHost.is(CratonBlocks.GABBRO.getOrigin().getBaseBlock());
        if(!cap&&!footwall) return null;
        Column column=(Column)ctx.columnGeometry(c);
        if(column==null){column=prepareColumn(ctx,c);ctx.columnGeometry(c,column);}
        if(!column.valid()) return null;
        double cy=column.cy(),angle=column.angle(),rotAlong=column.along(),rotAcross=column.across();
        Shape config=settings().shape();
        double rx=column.rx(),rz=column.rz(),ry=column.ry(),broad=column.broad();
        double upper=y+.5-cy;
        double vertical=upper>0?upper/(ry*.55):upper/(ry*1.30);
        double depth=column.feederDepth(),radius=Math.min(rx,rz)*.45;
        double maxLens=1-sq(rotAlong/rx)-sq(rotAcross/rz)-sq(vertical)
                +broad*config.broadNoise()+config.detailNoise();
        double rawDx=ctx.x()+.5-c.x(),rawDz=ctx.z()+.5-c.z();
        double minFeederDistance=Math.max(0,Math.hypot(rawDx,rawDz)-Math.sqrt(128));
        boolean feederPossible=y<=cy+2&&y>=cy-depth
                &&(1-minFeederDistance/Math.max(1,radius*(.22+.52*(1-(cy-y)/depth)))+.275)*.70>=-.15-1E-9;
        if((!cap||maxLens<-.15-1E-9)&&!feederPossible&&(!cap||!column.pods().possible(ctx.x(),y,ctx.z()))) return null;
        double detail=GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.shapeSeed(),.042,3);
        double lens=1-sq(rotAlong/rx)-sq(rotAcross/rz)-sq(vertical)+broad*config.broadNoise()+detail*config.detailNoise();

        double feeder=feederScore(c,ctx.x(),y,ctx.z(),cy,Math.min(rx,rz)*.45,column.feederDepth());
        double satellites=column.pods().score(ctx.x(),y,ctx.z());
        if(!cap) lens=-9;
        double score=Math.max(Math.max(lens,feeder*.70),cap?satellites*.65-.04:-9);
        if(score<-.15) return null;
        if(!ctx.enriched(this,c,y))return null;

        double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.075,3);
        if(!occupies(score,grade,c,ctx.x(),y,ctx.z(),.10,.78)) return null;
        double mix=GeologicalNoise.occupancy(ctx.x()+13,y,ctx.z()-7,c.oreSeed(),.12);
        int mineral=feeder*.70>lens?selectOre(mix,.70,.30,0):selectOre(mix,.43,.35,.22);
        return ore(this,mineral,state);
    }

    private static class Column {
        double cy,angle,along,across,rx,rz,ry,broad,feederDepth;
        PreparedPods pods;
        boolean valid(){return this!=INVALID_COLUMN;}
        double cy(){return cy;} double angle(){return angle;}
        double along(){return along;} double across(){return across;}
        double rx(){return rx;} double rz(){return rz;} double ry(){return ry;}
        double broad(){return broad;} double feederDepth(){return feederDepth;}
        PreparedPods pods(){return pods;}
    }
    private static final Column INVALID_COLUMN=new Column();
    private Column prepareColumn(DepositGenerationContext ctx,DepositCandidateSampler.Candidate c){
        List<Integer> tuffLayers=matchingLayers(ctx,Blocks.TUFF.defaultBlockState(),c.x(),c.z());
        if(tuffLayers.isEmpty()) return INVALID_COLUMN;

        if(!ctx.vmsProvince()) return INVALID_COLUMN;

        int layer=tuffLayers.get(index(c.verticalSeed(),tuffLayers.size()));
        if(!isVolcanicHorizon(ctx.geology(),layer,c.x(),c.z())) return INVALID_COLUMN;
        if(ctx.geology().layerThickness(layer,ctx.x(),ctx.z())<=1E-9) return INVALID_COLUMN;
        double cy=ctx.geology().boundaryY(layer,ctx.x(),ctx.z())-2.0;
        double angle=angle(c.rotationSeed());
        double rotDx=ctx.x()+.5-c.x(),rotDz=ctx.z()+.5-c.z();
        double rotCos=Math.cos(angle),rotSin=Math.sin(angle);
        double rotAlong=rotDx*rotCos+rotDz*rotSin,rotAcross=-rotDx*rotSin+rotDz*rotCos;
        Shape config=settings().shape();
        double rx=config.x(c.shapeSeed()),rz=config.z(c.shapeSeed()^91),ry=config.y(c.shapeSeed()^173);
        double broad=GeologicalNoise.fbm2(ctx.x(),ctx.z(),c.shapeSeed()^0x46A7L,.009,3);
        Column column=(Column)ctx.reusableGeometry(c);
        if(column==null){column=new Column();ctx.reusableGeometry(c,column);}
        column.cy=cy;column.angle=angle;column.along=rotAlong;column.across=rotAcross;
        column.rx=rx;column.rz=rz;column.ry=ry;column.broad=broad;
        column.feederDepth=30+rand01(c.shapeSeed()^0x99)*25;
        column.pods=ctx.ordinaryPods(c,cy,rx,ry,rz,angle,0x564D53L,config);
        return column;
    }

    public static boolean isVolcanicHorizon(GeologyFieldSampler geology,int layer,int x,int z){
        int upper=geology.neighborLayerIndex(layer,-1,x,z);
        int lower=geology.neighborLayerIndex(layer,1,x,z);
        return upper>=0&&lower>=0
                &&geology.layers().get(upper).value().blockState().is(Blocks.BASALT)
                &&geology.layers().get(lower).value().blockState().is(CratonBlocks.GABBRO.getOrigin().getBaseBlock());
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
