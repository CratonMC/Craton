package com.teamtea.craton.api.geology.deposit;

import com.teamtea.craton.common.core.DepositCandidateSampler;
import com.teamtea.craton.common.core.DepositFieldEngine.ColumnContext;
import com.teamtea.craton.common.core.DepositRandomSequences;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/** Per-candidate rule. A null result lets the next candidate try the same position. */
public abstract class OrdinaryDeposit extends FieldDeposit {
    private final int order;

    protected OrdinaryDeposit(Identifier type,Settings settings,DepositRandomSequences sequence,int order){
        super(type,settings,sequence);
        this.order=order;
    }

    public final int order(){return order;}

    public abstract @Nullable BlockState place(ColumnContext ctx,DepositCandidateSampler.Candidate candidate,
                                               BlockState originalHost,BlockState state,int y);
}
