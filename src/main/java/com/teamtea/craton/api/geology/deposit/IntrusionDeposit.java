package com.teamtea.craton.api.geology.deposit;

import com.teamtea.craton.common.core.DepositCandidateSampler;
import com.teamtea.craton.common.core.DepositFieldEngine.ColumnContext;
import com.teamtea.craton.common.core.DepositRandomSequences;
import com.teamtea.craton.common.core.DepositFieldSupport;
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

    /** One cached instance per candidate, owned by a single buildSurface context. */
    public static final class Prepared {
        public final IntrusionDeposit deposit;
        public final DepositCandidateSampler.Candidate candidate;
        public final int originMinY;
        public final double rx,rz,ry,cy,angle,rotCos,rotSin,minRadius,noiseAmplitude;
        public double along,across,radialLower,radial,bowl,minimumY,maximumY,scoreCutoff;

        private Prepared(IntrusionDeposit deposit,DepositCandidateSampler.Candidate candidate,int minY,
                         long zSalt,long ySalt){
            this.deposit=deposit;
            this.candidate=candidate;
            this.originMinY=minY;
            Shape shape=deposit.settings().shape();
            rx=shape.x(candidate.shapeSeed());
            rz=shape.z(candidate.shapeSeed()^zSalt);
            ry=shape.y(candidate.shapeSeed()^ySalt);
            cy=shape.height(minY,candidate.verticalSeed());
            angle=DepositFieldSupport.angle(candidate.rotationSeed());
            rotCos=Math.cos(angle);
            rotSin=Math.sin(angle);
            minRadius=Math.min(Math.min(rx,rz),ry);
            noiseAmplitude=shape.broadNoise()+shape.detailNoise();
        }

        public void rotateColumn(int x,int z){
            double dx=x+.5-candidate.x(),dz=z+.5-candidate.z();
            along=dx*rotCos+dz*rotSin;
            across=-dx*rotSin+dz*rotCos;
        }

        /** Lower bound after both independently bounded horizontal warp components. */
        public void prepareEllipsoidColumn(int x,int z,double cutoff,double warp,boolean cupola){
            rotateColumn(x,z);
            scoreCutoff=cutoff;
            // Keep the bound conservative even near the world coordinate limits.
            double roundoff=4*Math.ulp(Math.max(Math.abs((double)x),Math.abs((double)z))+.5);
            double rotatedWarp=(warp+roundoff)*(Math.abs(rotCos)+Math.abs(rotSin));
            double a=Math.max(0,Math.abs(along)-rotatedWarp)/rx;
            double b=Math.max(0,Math.abs(across)-rotatedWarp)/rz;
            radialLower=Math.sqrt(a*a+b*b);
            double extent=1+noiseAmplitude-cutoff;
            minimumY=cy-ry*extent;
            maximumY=cy+ry*extent;
            if(cupola){
                minimumY=Math.min(minimumY,cy+ry*.62-ry*.42*extent);
                maximumY=Math.max(maximumY,cy+ry*.62+ry*.42*extent);
            }
            if(radialLower>extent+margin()) maximumY=Double.NEGATIVE_INFINITY;
        }

        public boolean outside(int y){
            return y+.5<minimumY-margin()||y+.5>maximumY+margin();
        }

        public boolean cannotImprove(double upperBound,IntrusionResult best){
            return upperBound<Math.max(scoreCutoff,best.score())-margin();
        }

        private double margin(){return 1.0e-9*(1+Math.abs(scoreCutoff));}
    }

    protected final Prepared prepare(DepositCandidateSampler.Candidate candidate,int minY,long zSalt,long ySalt){
        return new Prepared(this,candidate,minY,zSalt,ySalt);
    }

    public abstract Prepared prepare(DepositCandidateSampler.Candidate candidate,int minY);

    public abstract void prepareColumn(ColumnContext ctx,Prepared prepared,double scoreCutoff);

    public abstract void sample(ColumnContext ctx,Prepared prepared,int y,IntrusionResult best);

    public abstract BlockState placeOre(ColumnContext ctx,BlockState host,int y,IntrusionResult intrusion);
}
