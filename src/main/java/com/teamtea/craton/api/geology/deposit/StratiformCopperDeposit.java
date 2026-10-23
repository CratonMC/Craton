package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.worldgen.deposit.DepositRandomSequences;

import com.teamtea.craton.common.worldgen.deposit.DepositGenerationContext;
import com.teamtea.craton.common.worldgen.deposit.StratiformDepositField;
import net.minecraft.world.level.block.state.BlockState;
import com.teamtea.craton.common.registry.CratonBlocks;
import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.*;

public class StratiformCopperDeposit extends FieldDeposit {
    public static final MapCodec<StratiformCopperDeposit> CODEC =
            codecFor(1, false, StratiformCopperDeposit::new);

    public StratiformCopperDeposit(Settings settings) {
        super(DepositTypes.COPPER,settings,DepositRandomSequences.STRATIFORM);
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }

    /** Shared host predicate for generation and reconnaissance. */
    public boolean matchesHost(BlockState state) { return hostMatches(state,settings().hostTags()); }
    public static boolean supportsHost(BlockState state) {
        return state.is(CratonBlocks.LIMESTONE.getOrigin().getBaseBlock());
    }

    public BlockState placeSample(DepositGenerationContext ctx,BlockState host,int y,StratiformDepositField.DepositSample sample){
        if(occupies(sample.score(),sample.grade(),sample.candidate(),ctx.x(),y,ctx.z(),.14,.72))
            return abundantOre(this,ctx,sample.candidate(),y,0,host);
        return host;
    }
}
