package com.teamtea.craton.api.geology.deposit;

import com.teamtea.craton.common.core.DepositCandidateSampler;
import com.teamtea.craton.common.core.DepositFieldEngine.ColumnContext;
import com.teamtea.craton.common.core.DepositRandomSequences;
import com.teamtea.craton.common.core.IntrusiveDepositField.IntrusionResult;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

/** Shared contract for intrusive bodies; each type owns its own geometry and ore zoning. */
public abstract class IntrusionDeposit extends FieldDeposit {
    protected IntrusionDeposit(Identifier type,Settings settings){
        super(type,settings,DepositRandomSequences.INTRUSIVE,requiredReach(type,settings.shape(),0));
    }

    /** Horizontal envelope of the intrusion and its contact halo, independent of other deposit geometries. */
    public final double contactReach(double maximumSkarnReach){
        return requiredReach(getType(),settings().shape(),maximumSkarnReach);
    }

    private static double requiredReach(Identifier type,Shape shape,double contactReach){
        double maxRadius=Math.max(shape.radiusXMax(),shape.radiusZMax());
        double minRadius=Math.min(Math.min(shape.radiusXMin(),shape.radiusZMin()),shape.radiusYMin());
        // Granite and diorite displace both horizontal coordinates independently.
        double warp=type.equals(DepositTypes.GRANITE)?8*Math.sqrt(2)
                :type.equals(DepositTypes.DIORITE)?7*Math.sqrt(2):0;
        double body=maxRadius*(1+shape.broadNoise()+shape.detailNoise()+contactReach/minRadius)+warp;
        double pods=shape.satelliteCount()>0&&shape.satelliteScale()>0
                ?Math.hypot(shape.radiusXMax()*1.27+24*shape.satelliteScale(),
                        shape.radiusZMax()*1.27+20*shape.satelliteScale()):0;
        return Math.max(body,pods)+1;
    }

    public abstract void sample(ColumnContext ctx,DepositCandidateSampler.Candidate candidate,int y,IntrusionResult best);

    public abstract BlockState placeOre(ColumnContext ctx,BlockState host,int y,IntrusionResult intrusion);
}
