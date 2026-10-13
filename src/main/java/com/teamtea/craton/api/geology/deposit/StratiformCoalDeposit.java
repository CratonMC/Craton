package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.worldgen.deposit.DepositRandomSequences;

import com.teamtea.craton.common.worldgen.deposit.DepositGenerationContext;
import com.teamtea.craton.common.worldgen.deposit.StratiformDepositField;
import com.teamtea.craton.common.registry.CratonBlocks;
import net.minecraft.world.level.block.state.BlockState;
import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.*;

public class StratiformCoalDeposit extends FieldDeposit {
    public static final MapCodec<StratiformCoalDeposit> CODEC =
            codecFor(2, false, StratiformCoalDeposit::new);

    public StratiformCoalDeposit(Settings settings) {
        super(DepositTypes.COAL,settings,DepositRandomSequences.STRATIFORM);
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }

    public BlockState placeSample(DepositGenerationContext ctx,BlockState host,int y,StratiformDepositField.DepositSample sample){
        if(occupies(sample.score(),sample.grade(),sample.candidate(),ctx.x(),y,ctx.z(),.10,.82))
            return abundantOre(this,ctx,sample.candidate(),y,host.is(CratonBlocks.SHALE.getOrigin().getBaseBlock())?1:0,host);
        return host;
    }
}
