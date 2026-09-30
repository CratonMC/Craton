package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.teamtea.craton.common.core.DepositFieldEngine.ColumnContext;
import com.teamtea.craton.common.core.*;
import static com.teamtea.craton.common.core.DepositFieldSupport.*;


public final class EpithermalDeposit extends FieldDeposit {
    public static final MapCodec<EpithermalDeposit> CODEC =
            codecFor(1, false, EpithermalDeposit::new);

    public EpithermalDeposit(Settings settings) {
        super(DepositTypes.EPITHERMAL,settings);
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }



    public static BlockState applyEpithermal(ColumnContext ctx,BlockState originalHost,BlockState state,int y){
        if(!originalHost.is(Blocks.ANDESITE)&&!isRhyolite(originalHost)) return state;
        for(DepositCandidateSampler.Candidate c:ctx.epithermal()){
            FieldDeposit.Shape config=shape(ctx,c);
            double score=veinSystemScore(c,ctx.x(),y,ctx.z(),ctx.minY(),config.x(c.shapeSeed()),config.z(c.shapeSeed()),.021,2,config);
            if(score<-.14) continue;
            double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.065,3);
            double p=GeologicalNoise.smoothstep(-.12,.42,score)*(.28+.62*grade);
            double occ=GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.occupancySeed(),.17);
            if(occ<p){
                if(grade>.67&&GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.oreSeed(),.11)<.34)
                    return ore(ctx,c,0,state);
                return rock(ctx,c);
            }
        }
        return state;
    }

}
