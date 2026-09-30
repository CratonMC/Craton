package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;
import com.teamtea.craton.common.core.DepositFieldEngine.ColumnContext;
import com.teamtea.craton.common.core.IntrusiveDepositField.IntrusionResult;
import com.teamtea.craton.common.core.*;
import static com.teamtea.craton.common.core.DepositFieldSupport.*;


public final class SkarnDeposit extends FieldDeposit {
    public static final MapCodec<SkarnDeposit> CODEC =
            codecFor(5, true, SkarnDeposit::new);

    public SkarnDeposit(Settings settings) {
        super(DepositTypes.SKARN,settings);
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }



    public static BlockState applySkarn(ColumnContext ctx,BlockState host,BlockState state,int y,IntrusionResult intrusion){
        for(Holder<Deposit> entry:ctx.definitions().getOrDefault(DepositTypes.SKARN,List.of())){
            long salt=DepositCandidateSampler.salt(entry.unwrapKey().orElseThrow().identifier().toString());
            state=applySkarnInstance(ctx,host,state,y,intrusion,(FieldDeposit)entry.value(),salt);
        }
        return state;
    }

    private static BlockState applySkarnInstance(ColumnContext ctx,BlockState originalHost,BlockState state,int y,IntrusionResult intrusion,FieldDeposit definition,long salt){
        if(!isCarbonate(originalHost)||intrusion.candidate()==null) return state;
        if(rand01(intrusion.candidate().alterationSeed()^salt)>=definition.settings().placement().frequency()) return state;
        double reach=definition.settings().shape().y(intrusion.candidate().shapeSeed());
        if(intrusion.surfaceDistance()>0||intrusion.surfaceDistance()<-reach) return state;

        DepositCandidateSampler.Candidate c=intrusion.candidate();
        FieldDeposit.Shape config=definition.settings().shape();
        double broad=GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.alterationSeed()^salt,.012,3);
        double detail=GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.shapeSeed()^0x5CA1L^salt,.052,2);
        double patch=.23+broad*config.broadNoise()+detail*config.detailNoise();
        double metasomatism=GeologicalNoise.smoothstep(-config.y(c.shapeSeed()),0,intrusion.surfaceDistance());
        double grade=.5+.5*GeologicalNoise.fbm(ctx.x()-41,y+19,ctx.z()+67,c.gradeSeed()^salt,.065,3);
        double score=Math.min(patch,metasomatism);
        double alterP=GeologicalNoise.smoothstep(-.15,.45,score)*(.42+.42*grade);
        if(GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.alterationSeed()^salt,.15)>alterP) return state;

        BlockState altered=definition.settings().rock();
        if(grade<.62) return altered;
        double mix=GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.oreSeed()^salt,.115);
        return switch(intrusion.kind()){
            case 0 -> mix<.50
                    ?ore(definition,0,altered)
                    :ore(definition,1,altered);
            case 1 -> mix<.51
                    ?ore(definition,2,altered)
                    :ore(definition,3,altered);
            default -> mix<.58
                    ?ore(definition,4,altered)
                    :ore(definition,2,altered);
        };
    }

}
