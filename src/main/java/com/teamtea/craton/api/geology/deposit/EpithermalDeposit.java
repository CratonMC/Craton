package com.teamtea.craton.api.geology.deposit;

import com.teamtea.craton.common.worldgen.deposit.DepositCandidateSampler;
import com.teamtea.craton.common.worldgen.noise.GeologicalNoise;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.worldgen.deposit.DepositRandomSequences;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.teamtea.craton.common.worldgen.deposit.DepositGenerationContext;

import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.*;

public class EpithermalDeposit extends OrdinaryDeposit {
    public static final MapCodec<EpithermalDeposit> CODEC =
            codecFor(1, false, EpithermalDeposit::new);

    public EpithermalDeposit(Settings settings) {
        super(DepositTypes.EPITHERMAL,settings,DepositRandomSequences.EPITHERMAL,20);
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }

    @Override
    public boolean canPlace(DepositGenerationContext ctx,BlockState originalHost,BlockState state,int y){
        return state.equals(originalHost)&&(originalHost.is(Blocks.ANDESITE)||isRhyolite(originalHost));
    }

    @Override
    public BlockState place(DepositGenerationContext ctx,DepositCandidateSampler.Candidate c,
                            BlockState originalHost,BlockState state,int y){
        if(!state.equals(originalHost)) return null;
        if(!originalHost.is(Blocks.ANDESITE)&&!isRhyolite(originalHost)) return null;

        Shape config=settings().shape();
        double score=ctx.vein(c,config,.021,2).score(ctx.x(),y,ctx.z());
        if(score<-.14) return null;
        double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.065,3);
        double p=GeologicalNoise.smoothstep(-.12,.42,score)*(.28+.62*grade);
        double occ=GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.occupancySeed(),.17);
        if(occ<p){
            if(grade>.57&&GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.oreSeed(),.11)<.48)
                return abundantOre(this,ctx,c,y,0,state,settings().rock());
            return settings().rock();
        }
        return null;
    }

}
