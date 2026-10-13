package com.teamtea.craton.api.geology.deposit;

import com.teamtea.craton.common.worldgen.deposit.DepositCandidateSampler;
import com.teamtea.craton.common.worldgen.deposit.PreparedPegmatite;
import com.teamtea.craton.common.worldgen.noise.GeologicalNoise;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.worldgen.deposit.DepositRandomSequences;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.teamtea.craton.common.worldgen.deposit.DepositGenerationContext;

import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.*;

public class HydrothermalVeinDeposit extends OrdinaryDeposit {
    public static final MapCodec<HydrothermalVeinDeposit> CODEC =
            codecFor(4, false, HydrothermalVeinDeposit::new);

    public HydrothermalVeinDeposit(Settings settings) {
        super(DepositTypes.VEIN,settings,DepositRandomSequences.VEIN,30,PreparedPegmatite.requiredReach(settings.shape()));
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }

    public java.util.List<PreparedPegmatite.Attachment> attachedTo(IntrusionDeposit.Prepared parent,long salt){
        var out=new java.util.ArrayList<PreparedPegmatite.Attachment>();
        var placement=placement();
        for(int i=0;i<placement.maxCandidates();i++){
            long chance=DepositCandidateSampler.mix(parent.candidate.shapeSeed()^salt^((i+1L)*0x504547L));
            if(rand01(chance)>=placement.frequency())continue;
            var attachment=PreparedPegmatite.attach(parent,settings().shape(),salt,i);
            if(attachment!=null)out.add(attachment);
        }
        return java.util.List.copyOf(out);
    }

    @Override
    public boolean canPlace(DepositGenerationContext ctx,BlockState originalHost,BlockState state,int y){
        return !state.isAir()&&!state.is(Blocks.WATER)&&!state.is(Blocks.LAVA)&&!state.is(Blocks.BEDROCK);
    }

    @Override
    public BlockState place(DepositGenerationContext ctx,DepositCandidateSampler.Candidate c,
                            BlockState originalHost,BlockState state,int y){
        // Parent association determines provenance; surrounding strata may also be cut.
        if(!canPlace(ctx,originalHost,state,y)) return null;

        Shape config=settings().shape();
        PreparedPegmatite geometry=ctx.pegmatite(c,config);
        double score=geometry.score(ctx.x(),y,ctx.z());
        if(score<-.15) return null;
        // Solid pegmatite inside the dike; contact mixing is independent of ore grade.
        if(GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.occupancySeed()^0x504547L,.18)
                >GeologicalNoise.smoothstep(-1,1,geometry.surfaceDistance(score))) return null;
        double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.075,3);
        double p=GeologicalNoise.smoothstep(-.14,.38,score)*(.34+.58*grade);
        if(GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.occupancySeed(),.18)>p) return settings().rock();
        double mix=GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.oreSeed(),.105);
        BlockState pegmatite=settings().rock();
        // Mafic or schist country rock is a coarse proxy for a favorable reactive wall rock,
        // not a claim that all such rocks provide chromium or vanadium.
        boolean emeraldHost=state.is(com.teamtea.craton.common.registry.CratonBlocks.SCHIST.getOrigin().getBaseBlock())
                ||state.is(com.teamtea.craton.common.registry.CratonBlocks.GABBRO.getOrigin().getBaseBlock())||state.is(Blocks.BASALT);
        if(settings().ores().size()>4&&emeraldHost&&rand01(c.alterationSeed()^0x454D4552414C44L)<.40
                &&geometry.wallFraction()>.25&&geometry.wallFraction()<.90&&grade>.53&&mix>.42&&mix<.68)
            return abundantOre(this,ctx,c,y,4,pegmatite);
        if(geometry.wallFraction()>.30&&grade>.72&&mix<.22) return abundantOre(this,ctx,c,y,0,pegmatite);
        if(geometry.wallFraction()>.45&&grade>.58&&mix<.55) return abundantOre(this,ctx,c,y,1,pegmatite);
        if(geometry.wallFraction()>.55&&mix>.82) return abundantOre(this,ctx,c,y,2,pegmatite);
        if(geometry.wallFraction()<.55&&mix<.82) return abundantOre(this,ctx,c,y,3,pegmatite);
        return pegmatite;
    }

}
