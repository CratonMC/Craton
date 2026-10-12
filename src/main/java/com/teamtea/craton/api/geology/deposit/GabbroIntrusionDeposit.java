package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockState;
import com.teamtea.craton.common.core.DepositFieldEngine.ColumnContext;
import com.teamtea.craton.common.core.IntrusiveDepositField.IntrusionResult;
import com.teamtea.craton.common.core.*;
import static com.teamtea.craton.common.core.DepositFieldSupport.*;


public final class GabbroIntrusionDeposit extends IntrusionDeposit {
    public static final MapCodec<GabbroIntrusionDeposit> CODEC =
            codecFor(2, false, GabbroIntrusionDeposit::new);

    public GabbroIntrusionDeposit(Settings settings) {
        super(DepositTypes.GABBRO,settings);
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }



    @Override
    public Prepared prepare(DepositCandidateSampler.Candidate c,int minY){
        return prepare(c,minY,17,71,0,0,0x77,.009,.050);
    }

    @Override
    public void prepareColumn(ColumnContext ctx,Prepared prepared,double scoreCutoff){
        prepared.rotateColumn(ctx.x(),ctx.z());
        prepared.scoreCutoff=scoreCutoff;
        prepared.radial=Math.sqrt(sq(prepared.along/prepared.rx)+sq(prepared.across/prepared.rz));
        double extent=1+prepared.noiseAmplitude-scoreCutoff-prepared.radial;
        if(extent<-1.0e-9*(1+Math.abs(scoreCutoff))){
            prepared.minimumY=Double.POSITIVE_INFINITY;
            prepared.maximumY=Double.NEGATIVE_INFINITY;
            return;
        }
        prepared.bowl=prepared.cy+18*prepared.radial*prepared.radial
                +GeologicalNoise.fbm2(ctx.x(),ctx.z(),prepared.candidate.shapeSeed(),.014,3)*5;
        double halfHeight=prepared.ry*Math.max(0,extent)/.78;
        prepared.minimumY=prepared.bowl-halfHeight;
        prepared.maximumY=prepared.bowl+halfHeight;
    }

    @Override
    public void sample(ColumnContext ctx,Prepared prepared,int y,IntrusionResult best){
        if(prepared.outside(y)) return;
        DepositCandidateSampler.Candidate c=prepared.candidate;
        double radial=prepared.radial,bowl=prepared.bowl,half=prepared.ry;
        double vertical=Math.abs(y+.5-bowl)/half;
        double body=1-radial-vertical*.78;
        if(prepared.cannotImprove(body+prepared.noiseAmplitude,best)) return;
        double score=body
                +prepared.noise().shape(ctx.x(),y,ctx.z());
        double dist=score*prepared.minRadius;
        double ny=(y+.5-bowl)/half;
        if(score>best.score()) best.set(score,dist,c,ny,radial);
    }

    @Override
    public BlockState placeOre(ColumnContext ctx,BlockState host,int y,IntrusionResult intrusion){
        DepositCandidateSampler.Candidate c=intrusion.candidate();
        Prepared prepared=ctx.preparedIntrusion(c);
        if(intrusion.score()<=.02) return host;
        double floor=(-intrusion.normalizedY()-.12)+prepared.noise().floor(ctx.x(),y,ctx.z())*.20;
        if(floor<=.18) return host;
        double grade=prepared.noise().grade(ctx.x(),y,ctx.z());
        if(prepared.occupies(intrusion.score()+floor*.35,grade,ctx.x(),y,ctx.z(),.05,.70,1)){
            double mix=prepared.noise().mix(ctx.x(),y,ctx.z());
            return ore(this,mix>.78?1:0,host);
        }
        return host;
    }


}
