package com.teamtea.craton.common.core;

import com.teamtea.craton.api.geology.deposit.FieldDeposit;
import com.teamtea.craton.api.geology.deposit.IntrusionDeposit;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;


import com.teamtea.craton.common.core.DepositFieldEngine.ColumnContext;

import static com.teamtea.craton.common.core.DepositFieldSupport.*;

public final class IntrusiveDepositField {
    private IntrusiveDepositField(){}

    public static final class IntrusionResult {
        private double score,surfaceDistance,normalizedY,radial;
        private int kind;
        private DepositCandidateSampler.Candidate candidate;

        public IntrusionResult(int kind) { reset(kind); }

        public void reset(int kind) {
            this.score=-99;
            this.surfaceDistance=-999;
            this.kind=kind;
            this.candidate=null;
            this.normalizedY=0;
            this.radial=0;
        }

        public void set(double score,double surfaceDistance,DepositCandidateSampler.Candidate candidate,
                 double normalizedY,double radial) {
            this.score=score;
            this.surfaceDistance=surfaceDistance;
            this.candidate=candidate;
            this.normalizedY=normalizedY;
            this.radial=radial;
        }

        public double score(){return score;}
        public double surfaceDistance(){return surfaceDistance;}
        public int kind(){return kind;}
        public DepositCandidateSampler.Candidate candidate(){return candidate;}
        public double normalizedY(){return normalizedY;}
        public double radial(){return radial;}
    }

    static IntrusionResult sampleIntrusions(ColumnContext ctx,int y){
        IntrusionResult best=sampleFamily(ctx,ctx.granite(),y,ctx.graniteResult(),0);
        best=better(best,sampleFamily(ctx,ctx.diorite(),y,ctx.dioriteResult(),1));
        best=better(best,sampleFamily(ctx,ctx.gabbro(),y,ctx.gabbroResult(),2));
        return best;
    }

    private static IntrusionResult sampleFamily(ColumnContext ctx,List<DepositCandidateSampler.Candidate> candidates,
                                                 int y,IntrusionResult result,int kind){
        result.reset(kind);
        for(DepositCandidateSampler.Candidate candidate:candidates)
            ((IntrusionDeposit)field(ctx,candidate)).sample(ctx,candidate,y,result);
        return result;
    }

    private static IntrusionResult better(IntrusionResult a,IntrusionResult b){return b.score()>a.score()?b:a;}

    static boolean intrusionOccupies(ColumnContext ctx,int y,IntrusionResult r){
        double p=GeologicalNoise.smoothstep(-.08,.20,r.score());
        if(r.score()>.28) p=.985;
        double occ=GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),r.candidate().occupancySeed(),.12);
        return occ<p;
    }

    static BlockState applyIntrusiveOre(ColumnContext ctx,BlockState host,int y,IntrusionResult r){
        return ((IntrusionDeposit)field(ctx,r.candidate())).placeOre(ctx,host,y,r);
    }

    static BlockState applyIntrusiveSatellite(ColumnContext ctx,BlockState host,int y){
        BlockState state=host;
        for(DepositCandidateSampler.Candidate c:ctx.granite()){
            state=intrusivePod(ctx,state,y,c,0);
            if(!state.equals(host)) return state;
        }
        for(DepositCandidateSampler.Candidate c:ctx.diorite()){
            state=intrusivePod(ctx,state,y,c,1);
            if(!state.equals(host)) return state;
        }
        for(DepositCandidateSampler.Candidate c:ctx.gabbro()){
            state=intrusivePod(ctx,state,y,c,2);
            if(!state.equals(host)) return state;
        }
        return host;
    }

    private static BlockState intrusivePod(ColumnContext ctx,BlockState host,int y,DepositCandidateSampler.Candidate c,int kind){
        double cy,rx,ry,rz;
        if(kind==0){
            FieldDeposit.Shape config=shape(ctx,c);
            cy=config.height(ctx.minY(),c.verticalSeed());
            rx=config.x(c.shapeSeed());
            rz=config.z(c.shapeSeed()^0xA7);
            ry=config.y(c.shapeSeed()^0xC3);
        }else if(kind==1){
            FieldDeposit.Shape config=shape(ctx,c);
            cy=config.height(ctx.minY(),c.verticalSeed());
            rx=config.x(c.shapeSeed());
            rz=config.z(c.shapeSeed()^17);
            ry=config.y(c.shapeSeed()^31);
        }else{
            FieldDeposit.Shape config=shape(ctx,c);
            cy=config.height(ctx.minY(),c.verticalSeed());
            rx=config.x(c.shapeSeed());
            rz=config.z(c.shapeSeed()^17);
            ry=config.y(c.shapeSeed()^71);
        }
        double pod=satellitePods(c,ctx.x(),y,ctx.z(),c.x(),cy,c.z(),rx,ry,rz,angle(c.rotationSeed()),0x1A7L,shape(ctx,c));
        if(pod<-.12) return host;
        double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.057,3);
        if(!occupies(pod*.62,grade,c,ctx.x(),y,ctx.z(),.06,.50)) return host;
        return rock(ctx,c);
    }
}
