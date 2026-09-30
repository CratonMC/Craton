package com.teamtea.craton.api.geology.deposit;

/** Immutable values shared by deposit definitions, never by active column sampling. */
public abstract class AbstractDeposit implements Deposit {
    private final Placement placement;

    protected AbstractDeposit(Placement placement) {
        this.placement=placement;
    }

    @Override
    public final Placement placement() {
        return placement;
    }
}
