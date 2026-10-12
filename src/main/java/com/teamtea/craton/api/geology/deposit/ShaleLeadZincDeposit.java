package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.core.DepositFieldEngine.ColumnContext;
import com.teamtea.craton.common.core.DepositRandomSequences;
import com.teamtea.craton.common.core.StratiformDepositField.DepositSample;
import net.minecraft.world.level.block.state.BlockState;

import static com.teamtea.craton.common.core.DepositFieldSupport.*;

/** Layer-bound, pinching Pb-Zn lenses in shale. */
public final class ShaleLeadZincDeposit extends FieldDeposit {
    public static final MapCodec<ShaleLeadZincDeposit> CODEC =
            codecFor(1,false,ShaleLeadZincDeposit::new);

    public ShaleLeadZincDeposit(Settings settings) {
        super(DepositTypes.SHALE_PB_ZN,settings,DepositRandomSequences.STRATIFORM);
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }

    public BlockState placeSample(ColumnContext ctx,BlockState host,int y,DepositSample sample) {
        return occupies(sample.score(),sample.grade(),sample.candidate(),ctx.x(),y,ctx.z(),.10,.68)
                ?ore(this,0,host):host;
    }
}
