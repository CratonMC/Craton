package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.core.DepositRandomSequences;

import com.teamtea.craton.common.core.DepositFieldEngine.ColumnContext;
import com.teamtea.craton.common.core.StratiformDepositField;
import net.minecraft.world.level.block.state.BlockState;
import static com.teamtea.craton.common.core.DepositFieldSupport.*;

public final class StratiformCoalDeposit extends FieldDeposit {
    public static final MapCodec<StratiformCoalDeposit> CODEC =
            codecFor(1, false, StratiformCoalDeposit::new);

    public StratiformCoalDeposit(Settings settings) {
        super(DepositTypes.COAL,settings,DepositRandomSequences.STRATIFORM);
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }

    public BlockState placeSample(ColumnContext ctx,BlockState host,int y,StratiformDepositField.DepositSample sample){
        if(occupies(sample.score(),sample.grade(),sample.candidate(),ctx.x(),y,ctx.z(),.10,.82))
            return ore(this,0,host);
        return host;
    }
}
