package com.teamtea.craton.common.core;

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
        IntrusionResult best=sampleFamily(ctx,ctx.graniteColumns(),y,ctx.graniteResult(),0);
        best=better(best,sampleFamily(ctx,ctx.dioriteColumns(),y,ctx.dioriteResult(),1));
        best=better(best,sampleFamily(ctx,ctx.gabbroColumns(),y,ctx.gabbroResult(),2));
        return best;
    }

    private static IntrusionResult sampleFamily(ColumnContext ctx,List<IntrusionDeposit.Prepared> candidates,
                                                 int y,IntrusionResult result,int kind){
        result.reset(kind);
        for(IntrusionDeposit.Prepared prepared:candidates)
            prepared.deposit.sample(ctx,prepared,y,result);
        return result;
    }

    private static IntrusionResult better(IntrusionResult a,IntrusionResult b){return b.score()>a.score()?b:a;}

    static boolean intrusionOccupies(ColumnContext ctx,int y,IntrusionResult r){
        double p=GeologicalNoise.smoothstep(-.08,.20,r.score());
        if(p<=0) return false;
        if(r.score()>.28) p=.985;
        double occ=ctx.preparedIntrusion(r.candidate()).noise().rockOccupancy(ctx.x(),y,ctx.z());
        return occ<p;
    }

    static BlockState applyIntrusiveOre(ColumnContext ctx,BlockState host,int y,IntrusionResult r){
        return ((IntrusionDeposit)field(ctx,r.candidate())).placeOre(ctx,host,y,r);
    }

    static BlockState applyIntrusiveSatellite(ColumnContext ctx,BlockState host,int y){
        BlockState state=host;
        for(IntrusionDeposit.Prepared prepared:ctx.graniteColumns()){
            state=intrusivePod(ctx,state,y,prepared);
            if(!state.equals(host)) return state;
        }
        for(IntrusionDeposit.Prepared prepared:ctx.dioriteColumns()){
            state=intrusivePod(ctx,state,y,prepared);
            if(!state.equals(host)) return state;
        }
        for(IntrusionDeposit.Prepared prepared:ctx.gabbroColumns()){
            state=intrusivePod(ctx,state,y,prepared);
            if(!state.equals(host)) return state;
        }
        return host;
    }

    private static BlockState intrusivePod(ColumnContext ctx,BlockState host,int y,IntrusionDeposit.Prepared prepared){
        DepositCandidateSampler.Candidate c=prepared.candidate;
        double pod=prepared.satelliteScore(ctx.x(),y,ctx.z());
        if(pod<-.12) return host;
        double grade=prepared.noise().satelliteGrade(ctx.x(),y,ctx.z());
        if(!prepared.occupies(pod*.62,grade,ctx.x(),y,ctx.z(),.06,.50,1)) return host;
        return rock(ctx,c);
    }
}
