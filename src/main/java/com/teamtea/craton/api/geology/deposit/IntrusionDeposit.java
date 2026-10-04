package com.teamtea.craton.api.geology.deposit;

import com.teamtea.craton.common.core.DepositCandidateSampler;
import com.teamtea.craton.common.core.DepositFieldEngine.ColumnContext;
import com.teamtea.craton.common.core.DepositRandomSequences;
import com.teamtea.craton.common.core.IntrusiveDepositField.IntrusionResult;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

/** Shared contract for intrusive bodies; each type owns its own geometry and ore zoning. */
public abstract class IntrusionDeposit extends FieldDeposit {
    protected IntrusionDeposit(Identifier type,Settings settings){super(type,settings,DepositRandomSequences.INTRUSIVE);}

    public abstract void sample(ColumnContext ctx,DepositCandidateSampler.Candidate candidate,int y,IntrusionResult best);

    public abstract BlockState placeOre(ColumnContext ctx,BlockState host,int y,IntrusionResult intrusion);
}
