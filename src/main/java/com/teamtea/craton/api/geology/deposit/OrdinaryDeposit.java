package com.teamtea.craton.api.geology.deposit;

import com.teamtea.craton.common.worldgen.deposit.DepositCandidateSampler;
import com.teamtea.craton.common.worldgen.deposit.DepositGenerationContext;
import com.teamtea.craton.common.worldgen.deposit.DepositRandomSequences;
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

    protected OrdinaryDeposit(Identifier type,Settings settings,DepositRandomSequences sequence,int order,double requiredReach){
        super(type,settings,sequence,requiredReach);
        this.order=order;
    }

    public final int order(){return order;}

    /** Candidate-independent gate, evaluated once per definition batch at a given position. */
    public boolean canPlace(DepositGenerationContext ctx,BlockState originalHost,BlockState state,int y){return true;}

    public abstract @Nullable BlockState place(DepositGenerationContext ctx,DepositCandidateSampler.Candidate candidate,
                                               BlockState originalHost,BlockState state,int y);
}
