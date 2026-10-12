package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.core.DepositRandomSequences;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.teamtea.craton.common.core.DepositFieldEngine.ColumnContext;
import com.teamtea.craton.common.core.*;
import static com.teamtea.craton.common.core.DepositFieldSupport.*;


public final class HydrothermalVeinDeposit extends OrdinaryDeposit {
    public static final MapCodec<HydrothermalVeinDeposit> CODEC =
            codecFor(4, false, HydrothermalVeinDeposit::new);

    public HydrothermalVeinDeposit(Settings settings) {
        super(DepositTypes.VEIN,settings,DepositRandomSequences.VEIN,30);
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }



    @Override
    public BlockState place(ColumnContext ctx,DepositCandidateSampler.Candidate c,
                            BlockState originalHost,BlockState state,int y){
        if(!originalHost.is(Blocks.GRANITE)&&!isPegmatite(originalHost)) return null;

        Shape config=settings().shape();
        double score=veinSystemScore(c,ctx.x(),y,ctx.z(),ctx.minY(),config.x(c.shapeSeed()),config.z(c.shapeSeed()),.013,3,config);
        if(score<-.15) return null;
        double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.075,3);
        double p=GeologicalNoise.smoothstep(-.14,.38,score)*(.34+.58*grade);
        if(GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.occupancySeed(),.18)>p) return null;
        double mix=GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.oreSeed(),.105);
        BlockState pegmatite=settings().rock();
        if(grade>.68&&mix<.34) return ore(this,0,pegmatite);
        if(grade>.58&&mix<.55) return ore(this,1,pegmatite);
        if(mix>.82) return ore(this,2,pegmatite);
        if(mix<.72) return ore(this,3,pegmatite);
        return pegmatite;
    }

}
