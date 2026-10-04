package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.core.DepositRandomSequences;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.teamtea.craton.common.core.DepositFieldEngine.ColumnContext;
import com.teamtea.craton.common.core.*;
import static com.teamtea.craton.common.core.DepositFieldSupport.*;


public final class EpithermalDeposit extends OrdinaryDeposit {
    public static final MapCodec<EpithermalDeposit> CODEC =
            codecFor(1, false, EpithermalDeposit::new);

    public EpithermalDeposit(Settings settings) {
        super(DepositTypes.EPITHERMAL,settings,DepositRandomSequences.EPITHERMAL,20);
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }



    @Override
    public BlockState place(ColumnContext ctx,DepositCandidateSampler.Candidate c,
                            BlockState originalHost,BlockState state,int y){
        if(!originalHost.is(Blocks.ANDESITE)&&!isRhyolite(originalHost)) return null;

        Shape config=settings().shape();
        double score=veinSystemScore(c,ctx.x(),y,ctx.z(),ctx.minY(),config.x(c.shapeSeed()),config.z(c.shapeSeed()),.021,2,config);
        if(score<-.14) return null;
        double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.065,3);
        double p=GeologicalNoise.smoothstep(-.12,.42,score)*(.28+.62*grade);
        double occ=GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.occupancySeed(),.17);
        if(occ<p){
            if(grade>.67&&GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.oreSeed(),.11)<.34)
                return ore(this,0,state);
            return settings().rock();
        }
        return null;
    }

}
