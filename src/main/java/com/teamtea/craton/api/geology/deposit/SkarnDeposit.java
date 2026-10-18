package com.teamtea.craton.api.geology.deposit;

import com.teamtea.craton.common.worldgen.deposit.DepositCandidateSampler;
import com.teamtea.craton.common.worldgen.noise.GeologicalNoise;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.worldgen.deposit.DepositRandomSequences;

import net.minecraft.core.Holder;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;
import com.teamtea.craton.common.worldgen.deposit.DepositGenerationContext;
import com.teamtea.craton.common.worldgen.deposit.IntrusiveDepositField.IntrusionResult;

import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.*;

public class SkarnDeposit extends FieldDeposit {
    public static final MapCodec<SkarnDeposit> CODEC =
            codecFor(5, true, SkarnDeposit::new);

    public SkarnDeposit(Settings settings) {
        super(DepositTypes.SKARN,settings,DepositRandomSequences.SKARN);
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }

    public BlockState placeContact(DepositGenerationContext ctx,BlockState originalHost,BlockState state,int y,IntrusionResult intrusion,long salt){
        if(!isCarbonate(originalHost)||intrusion.candidate()==null) return state;
        if(rand01(intrusion.candidate().alterationSeed()^salt)>=settings().placement().frequency()) return state;
        double reach=settings().shape().y(intrusion.candidate().shapeSeed());
        if(intrusion.surfaceDistance()>0||intrusion.surfaceDistance()<-reach) return state;

        DepositCandidateSampler.Candidate c=intrusion.candidate();
        Shape config=settings().shape();
        double broad=GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.alterationSeed()^salt,.012,3);
        double detail=GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.shapeSeed()^0x5CA1L^salt,.052,2);
        double patch=.23+broad*config.broadNoise()+detail*config.detailNoise();
        double metasomatism=GeologicalNoise.smoothstep(-config.y(c.shapeSeed()),0,intrusion.surfaceDistance());
        double grade=.5+.5*GeologicalNoise.fbm(ctx.x()-41,y+19,ctx.z()+67,c.gradeSeed()^salt,.065,3);
        double score=Math.min(patch,metasomatism);
        double alterP=GeologicalNoise.smoothstep(-.15,.45,score)*(.42+.42*grade);
        if(GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.alterationSeed()^salt,.15)>alterP) return state;

        // A carbonate-marble contact facies is distinct from the silicate skarn assemblage.
        // Fertility is fixed per parent; local mineralization still follows the contact field.
        if(settings().ores().size()>5&&intrusion.kind()==0
                &&rand01(c.alterationSeed()^salt^0x4C41504953L)<.40&&broad>-.03){
            BlockState marble=com.teamtea.craton.common.registry.CratonBlocks.MARBLE.getOrigin().getBaseBlock().defaultBlockState();
            if(grade>.53&&detail>-.04&&oreAvailable(5))return abundantOre(this,ctx,c,y,5,marble);
            return marble;
        }

        BlockState altered=settings().rock();
        int first=intrusion.kind()==0?0:intrusion.kind()==1?2:4;
        int second=intrusion.kind()==0?1:intrusion.kind()==1?3:2;
        if(grade<.62||!oreAvailable(first)&&!oreAvailable(second)) return altered;
        double mix=GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.oreSeed()^salt,.115);
        double threshold=intrusion.kind()==0?.50:intrusion.kind()==1?.51:.58;
        int mineral=mix<threshold?first:second;
        if(!oreAvailable(mineral))mineral=mineral==first?second:first;
        return abundantOre(this,ctx,c,y,mineral,altered);
    }

}
