package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.core.DepositFieldEngine.ColumnContext;
import com.teamtea.craton.common.core.StratiformDepositField;
import com.teamtea.craton.common.registry.CratonBlocks;
import net.minecraft.world.level.block.state.BlockState;
import static com.teamtea.craton.common.core.DepositFieldSupport.*;

public final class StratiformCopperDeposit extends FieldDeposit {
    public static final MapCodec<StratiformCopperDeposit> CODEC =
            codecFor(1, false, StratiformCopperDeposit::new);

    public StratiformCopperDeposit(Settings settings) {
        super(DepositTypes.COPPER,settings);
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }

    public static BlockState place(ColumnContext ctx,BlockState host,int y){
        StratiformDepositField.DepositSample sample=StratiformDepositField.layerBoundSample(
                ctx,ctx.stratiformCopper(),CratonBlocks.LIMESTONE.getOrigin().getBaseBlock().defaultBlockState(),y,0x29);
        if(sample!=null&&occupies(sample.score(),sample.grade(),sample.candidate(),ctx.x(),y,ctx.z(),.14,.72))
            return ore(ctx,sample.candidate(),0,host);
        return host;
    }
}
