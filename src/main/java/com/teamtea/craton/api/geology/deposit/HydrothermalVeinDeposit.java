package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.teamtea.craton.common.core.DepositFieldEngine.ColumnContext;
import com.teamtea.craton.common.core.*;
import static com.teamtea.craton.common.core.DepositFieldSupport.*;


public final class HydrothermalVeinDeposit extends FieldDeposit {
    public static final MapCodec<HydrothermalVeinDeposit> CODEC =
            codecFor(3, false, HydrothermalVeinDeposit::new);

    public HydrothermalVeinDeposit(Settings settings) {
        super(DepositTypes.VEIN,settings);
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }



    public static BlockState applyVeins(ColumnContext ctx,BlockState originalHost,BlockState state,int y){
        if(!originalHost.is(Blocks.GRANITE)&&!isPegmatite(originalHost)) return state;
        for(DepositCandidateSampler.Candidate c:ctx.veins()){
            FieldDeposit.Shape config=shape(ctx,c);
            double score=veinSystemScore(c,ctx.x(),y,ctx.z(),ctx.minY(),config.x(c.shapeSeed()),config.z(c.shapeSeed()),.013,3,config);
            if(score<-.15) continue;
            double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.075,3);
            double p=GeologicalNoise.smoothstep(-.14,.38,score)*(.34+.58*grade);
            if(GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.occupancySeed(),.18)>p) continue;
            double mix=GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.oreSeed(),.105);
            if(grade>.73&&mix<.24) return ore(ctx,c,0,state);
            if(grade>.60&&mix<.52) return ore(ctx,c,1,state);
            if(mix>.78) return ore(ctx,c,2,state);
            return rock(ctx,c);
        }
        return state;
    }

}
