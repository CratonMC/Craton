package com.teamtea.craton.common.worldgen.deposit;

import com.teamtea.craton.common.worldgen.geology.GeologyFieldSampler;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.rock;

/**
 * Deposit pipeline: prepare once per column, then apply underground or applyPlacer to riverbed sediment.
 * DepositPlan compiles rules; DepositGenerationContext owns mutable state and caches.
 */
public class DepositFieldEngine {
    private DepositFieldEngine(){}

    /** Prepare once before sampling a column; reuse the existing chunk context. */
    public static DepositGenerationContext prepare(DepositGenerationContext context,GeologyFieldSampler geology,int x,int z,
                                                   int minY,int topY,int surfaceY,BlockState surfaceState,Holder<Biome> biome){
        context.prepareColumn(geology,x,z,minY,topY,surfaceY,surfaceState,biome);
        return context;
    }

    /** Use the actual riverbed sediment as the placer host. */
    public static BlockState applyPlacer(DepositGenerationContext ctx,BlockState sediment,int y){
        return ctx.applyCandidates(ctx.placer(),sediment,sediment,y);
    }

    /** Surface organic deposits must see the original soil, before geological replacement. */
    public static BlockState applyPeat(DepositGenerationContext ctx,BlockState soil,int y){
        return ctx.applyCandidates(ctx.peat(),soil,soil,y);
    }

    /** Apply deposits to one underground block; host is the original stratum and state is the current result. */
    public static BlockState apply(DepositGenerationContext ctx,BlockState host,BlockState initialState,int y){
        BlockState state=initialState;

        // 1. Apply BIF and stratiform deposits while the host remains unchanged.
        if(state.equals(host)) state=ctx.applyBif(host,y);
        if(state.equals(host)) state=StratiformDepositField.applyStratiform(ctx,state,y);

        // 2. Apply intrusions, then evaluate skarn contacts against the same geometry.
        state=applyIntrusionsAndContacts(ctx,host,state,y);

        // 3. Process ordinary deposits in stable order; accept the first change at each priority.
        return ctx.applyOrdinary(host,state,y);
    }

    private static BlockState applyIntrusionsAndContacts(DepositGenerationContext ctx,BlockState host,BlockState state,int y){
        IntrusiveDepositField.IntrusionResult intrusion=IntrusiveDepositField.sampleIntrusions(ctx,y);
        if(intrusion.candidate()!=null&&IntrusiveDepositField.intrusionOccupies(ctx,y,intrusion)){
            state=rock(ctx,intrusion.candidate());
            state=IntrusiveDepositField.applyIntrusiveOre(ctx,state,y,intrusion);
        }
        if(intrusion.candidate()!=null&&intrusion.score()<=.12) state=ctx.applySkarn(host,state,y,intrusion);
        return state;
    }
}
