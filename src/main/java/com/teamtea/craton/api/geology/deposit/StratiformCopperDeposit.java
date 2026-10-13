package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.worldgen.deposit.DepositRandomSequences;

import com.teamtea.craton.common.worldgen.deposit.DepositGenerationContext;
import com.teamtea.craton.common.worldgen.deposit.StratiformDepositField;
import net.minecraft.world.level.block.state.BlockState;
import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.*;

public class StratiformCopperDeposit extends FieldDeposit {
    public static final MapCodec<StratiformCopperDeposit> CODEC =
            codecFor(1, false, StratiformCopperDeposit::new);

    public StratiformCopperDeposit(Settings settings) {
        super(DepositTypes.COPPER,settings,DepositRandomSequences.STRATIFORM);
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }

    public BlockState placeSample(DepositGenerationContext ctx,BlockState host,int y,StratiformDepositField.DepositSample sample){
        if(occupies(sample.score(),sample.grade(),sample.candidate(),ctx.x(),y,ctx.z(),.14,.72))
            return abundantOre(this,ctx,sample.candidate(),y,0,host);
        return host;
    }
}
