package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.core.DepositFieldEngine.ColumnContext;
import com.teamtea.craton.common.core.StratiformDepositField;
import com.teamtea.craton.common.registry.CratonBlocks;
import net.minecraft.world.level.block.state.BlockState;
import static com.teamtea.craton.common.core.DepositFieldSupport.*;

public final class StratiformCoalDeposit extends FieldDeposit {
    public static final MapCodec<StratiformCoalDeposit> CODEC =
            codecFor(1, false, StratiformCoalDeposit::new);

    public StratiformCoalDeposit(Settings settings) {
        super(DepositTypes.COAL,settings);
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }

    public static BlockState place(ColumnContext ctx,BlockState host,int y){
        StratiformDepositField.DepositSample sample=StratiformDepositField.layerBoundSample(
                ctx,ctx.stratiformCoal(),CratonBlocks.LIMESTONE.getOrigin().getBaseBlock().defaultBlockState(),y,0x11);
        if(sample!=null&&occupies(sample.score(),sample.grade(),sample.candidate(),ctx.x(),y,ctx.z(),.10,.82))
            return ore(ctx,sample.candidate(),0,host);
        return host;
    }
}
