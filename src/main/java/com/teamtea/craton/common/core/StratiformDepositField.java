package com.teamtea.craton.common.core;

import com.teamtea.craton.api.geology.deposit.FieldDeposit;
import com.teamtea.craton.api.geology.deposit.SandstoneUraniumDeposit;
import com.teamtea.craton.api.geology.deposit.StratiformCoalDeposit;
import com.teamtea.craton.api.geology.deposit.StratiformCopperDeposit;
import com.teamtea.craton.common.registry.CratonBlocks;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

import com.teamtea.craton.common.core.DepositFieldEngine.ColumnContext;

import static com.teamtea.craton.common.core.DepositFieldSupport.*;

public final class StratiformDepositField {
    private StratiformDepositField(){}

    public static BlockState applyStratiform(ColumnContext ctx,BlockState host,int y){
        if(isLimestone(host)){
            BlockState coal=StratiformCoalDeposit.place(ctx,host,y);
            if(!coal.equals(host)) return coal;
            BlockState copper=StratiformCopperDeposit.place(ctx,host,y);
            if(!copper.equals(host)) return copper;
        }
        if(host.is(Blocks.SANDSTONE)||host.is(Blocks.RED_SANDSTONE)){
            return SandstoneUraniumDeposit.place(ctx,host,y);
        }
        return host;
    }

    public static final class DepositSample {
        public DepositSample(){}
        private double score,grade;
        private DepositCandidateSampler.Candidate candidate;
        public void reset(){score=Double.NEGATIVE_INFINITY;grade=0;candidate=null;}
        public void set(double score,double grade,DepositCandidateSampler.Candidate candidate){
            this.score=score;this.grade=grade;this.candidate=candidate;
        }
        public double score(){return score;}
        public double grade(){return grade;}
        public DepositCandidateSampler.Candidate candidate(){return candidate;}
    }

    public static DepositSample layerBoundSample(ColumnContext ctx,List<DepositCandidateSampler.Candidate> candidates,
                                                   BlockState targetHost,int y,long variant){
        List<Integer> targetLayers=matchingLayers(ctx,targetHost);
        if(targetLayers.isEmpty()) return null;
        DepositSample best=ctx.stratiformSample();
        best.reset();
        for(DepositCandidateSampler.Candidate c:candidates){
            int layer=targetLayers.get(index(c.verticalSeed()^variant,targetLayers.size()));
            double angle=angle(c.rotationSeed());
            double rotDx=ctx.x()+.5-c.x(),rotDz=ctx.z()+.5-c.z();
            double rotCos=Math.cos(angle),rotSin=Math.sin(angle);
            double rotAlong=rotDx*rotCos+rotDz*rotSin,rotAcross=-rotDx*rotSin+rotDz*rotCos;
            FieldDeposit.Shape config=shape(ctx,c);
            double rx=config.x(c.shapeSeed()^variant);
            double rz=config.z(c.shapeSeed()^(variant*31));
            double d2=sq(rotAlong/rx)+sq(rotAcross/rz);
            double boundary=ctx.geology().boundaryY(layer,ctx.x(),ctx.z());
            double thickness=config.y(c.gradeSeed()^variant);
            double layerThickness=ctx.geology().thickness(layer);
            double offset=Math.min(layerThickness*.65,thickness+2+rand01(c.verticalSeed()^0x7788L)*layerThickness*.15);
            double vertical=Math.abs(y+.5-(boundary-offset))/thickness;
            double warp=shapeNoise(ctx.x(),y,ctx.z(),c.shapeSeed(),.011,.054,config.broadNoise(),config.detailNoise());
            double center=boundary-offset;
            double satellite=satellitePods(c,ctx.x(),y,ctx.z(),c.x(),center,c.z(),rx,thickness*1.6,rz,angle,variant,config);
            double score=Math.max(1-d2-vertical*.72+warp,satellite*.65-.04);
            double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.065,3);
            if(best.candidate()==null||score>best.score()) best.set(score,grade,c);
        }
        return best.candidate()==null?null:best;
    }


}
