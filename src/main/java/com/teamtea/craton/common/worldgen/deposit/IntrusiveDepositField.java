package com.teamtea.craton.common.worldgen.deposit;

import com.teamtea.craton.common.worldgen.noise.GeologicalNoise;

import com.teamtea.craton.api.geology.deposit.IntrusionDeposit;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;

import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.*;

public class IntrusiveDepositField {
    private IntrusiveDepositField(){}

    public static class IntrusionResult {
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

    static IntrusionResult sampleIntrusions(DepositGenerationContext ctx,int y){
        IntrusionResult best=sampleFamily(ctx,ctx.graniteColumns(),y,ctx.graniteResult(),0);
        best=better(best,sampleFamily(ctx,ctx.dioriteColumns(),y,ctx.dioriteResult(),1));
        best=better(best,sampleFamily(ctx,ctx.gabbroColumns(),y,ctx.gabbroResult(),2));
        return best;
    }

    private static IntrusionResult sampleFamily(DepositGenerationContext ctx,List<IntrusionDeposit.Prepared> candidates,
                                                 int y,IntrusionResult result,int kind){
        result.reset(kind);
        for(int i=0,size=candidates.size();i<size;i++){
            IntrusionDeposit.Prepared prepared=candidates.get(i);
            if(!prepared.outside(y))prepared.deposit.sample(ctx,prepared,y,result);
        }
        return result;
    }

    private static IntrusionResult better(IntrusionResult a,IntrusionResult b){return b.score()>a.score()?b:a;}

    static boolean intrusionOccupies(DepositGenerationContext ctx,int y,IntrusionResult r){
        // Continuous solid interior; only a two-block contact belt mixes host rock.
        if(r.surfaceDistance()>=1) return true;
        if(r.surfaceDistance()<=-1) return false;
        double probability=GeologicalNoise.smoothstep(-1,1,r.surfaceDistance());
        return ctx.preparedIntrusion(r.candidate()).noise().rockOccupancy(ctx.x(),y,ctx.z())<probability;
    }

    static BlockState applyIntrusiveOre(DepositGenerationContext ctx,BlockState host,int y,IntrusionResult r){
        return ((IntrusionDeposit)field(ctx,r.candidate())).placeOre(ctx,host,y,r);
    }

}
