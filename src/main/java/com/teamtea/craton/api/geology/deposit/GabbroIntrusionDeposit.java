package com.teamtea.craton.api.geology.deposit;

import com.teamtea.craton.common.worldgen.deposit.DepositCandidateSampler;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockState;
import com.teamtea.craton.common.worldgen.deposit.DepositGenerationContext;
import com.teamtea.craton.common.worldgen.deposit.IntrusiveDepositField.IntrusionResult;

import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.*;

public class GabbroIntrusionDeposit extends IntrusionDeposit {
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
    public void prepareColumn(DepositGenerationContext ctx,Prepared prepared,double scoreCutoff){
        prepared.geometry.prepareColumn(ctx,prepared,scoreCutoff);
    }

    @Override
    public void sample(DepositGenerationContext ctx,Prepared prepared,int y,IntrusionResult best){
        if(prepared.outside(y)) return;
        double upper=prepared.geometry.upperBound(prepared,y);
        if(prepared.cannotImprove(upper,best)) return;
        double score=prepared.geometry.sample(prepared,ctx.x(),y,ctx.z());
        if(score>best.score()) best.set(score,score*prepared.minRadius,prepared.candidate,
                prepared.geometry.normalizedY(prepared,y),prepared.geometry.radial(prepared,y));
    }

    @Override
    public BlockState placeOre(DepositGenerationContext ctx,BlockState host,int y,IntrusionResult intrusion){
        DepositCandidateSampler.Candidate c=intrusion.candidate();
        Prepared prepared=ctx.preparedIntrusion(c);
        if(intrusion.score()<=.02) return host;
        double floor=(-intrusion.normalizedY()-.12)+prepared.noise().floor(ctx.x(),y,ctx.z())*.20;
        if(floor<=.18) return host;
        if(!ctx.enriched(this,c,y))return host;
        double grade=prepared.noise().grade(ctx.x(),y,ctx.z());
        if(prepared.occupies(intrusion.score()+floor*.35,grade,ctx.x(),y,ctx.z(),.05,.70,1)){
            double mix=prepared.noise().mix(ctx.x(),y,ctx.z());
            return ore(this,mix>.78?1:0,host);
        }
        return host;
    }

}
