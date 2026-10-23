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

    /** Shared host predicate for generation and reconnaissance. */
    public boolean matchesHost(BlockState state) { return hostMatches(state,settings().hostTags()); }
    public static boolean supportsHost(BlockState state) {
        return state.is(CratonBlocks.LIMESTONE.getOrigin().getBaseBlock()) || state.is(CratonBlocks.SHALE.getOrigin().getBaseBlock());
    }

    public BlockState placeSample(DepositGenerationContext ctx,BlockState host,int y,StratiformDepositField.DepositSample sample){
        if(occupies(sample.score(),sample.grade(),sample.candidate(),ctx.x(),y,ctx.z(),.10,.82)) {
            // Match the ore type's own host variants instead of assuming that every
            // newly configured host is limestone or shale.
            int preferred=host.is(CratonBlocks.SHALE.getOrigin().getBaseBlock())?1:0;
            for (int i=0;i<settings().ores().size();i++) {
                if (!settings().ores().get(i).value().getOreState(host).equals(host) && oreAvailable(i)) {
                    if (i==preferred) return abundantOre(this,ctx,sample.candidate(),y,i,host);
                }
            }
            for (int i=0;i<settings().ores().size();i++)
                if (!settings().ores().get(i).value().getOreState(host).equals(host) && oreAvailable(i))
                    return abundantOre(this,ctx,sample.candidate(),y,i,host);
        }
        return host;
    }
}
