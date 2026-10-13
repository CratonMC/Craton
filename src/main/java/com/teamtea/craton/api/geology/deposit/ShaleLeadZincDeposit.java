package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.worldgen.deposit.DepositGenerationContext;
import com.teamtea.craton.common.worldgen.deposit.DepositRandomSequences;
import com.teamtea.craton.common.worldgen.deposit.StratiformDepositField.DepositSample;
import net.minecraft.world.level.block.state.BlockState;

import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.*;

/** Layer-bound, pinching Pb-Zn lenses in shale. */
public class ShaleLeadZincDeposit extends FieldDeposit {
    public static final MapCodec<ShaleLeadZincDeposit> CODEC =
            codecFor(1,false,ShaleLeadZincDeposit::new);

    public ShaleLeadZincDeposit(Settings settings) {
        super(DepositTypes.SHALE_PB_ZN,settings,DepositRandomSequences.STRATIFORM);
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }

    public BlockState placeSample(DepositGenerationContext ctx,BlockState host,int y,DepositSample sample) {
        return occupies(sample.score(),sample.grade(),sample.candidate(),ctx.x(),y,ctx.z(),.10,.68)
                ?abundantOre(this,ctx,sample.candidate(),y,0,host):host;
    }
}
