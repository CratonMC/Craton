package com.teamtea.craton.api.geology.deposit;

import com.teamtea.craton.common.worldgen.deposit.DepositCandidateSampler;
import com.teamtea.craton.common.worldgen.deposit.DepositGenerationContext;
import com.teamtea.craton.common.worldgen.deposit.DepositRandomSequences;
import com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport;
import com.teamtea.craton.common.worldgen.noise.GeologicalNoise;
import com.teamtea.craton.common.worldgen.deposit.PlutonGeometry;
import com.teamtea.craton.common.worldgen.deposit.PreparedPorphyry;
import com.teamtea.craton.common.worldgen.deposit.IntrusiveDepositField.IntrusionResult;
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
        double halo=Math.max(.08,contactReach/minRadius);
        // Includes attached sheets, composite lobes, domain warp and contact halos.
        double bodyReach=type.equals(DepositTypes.DIORITE)
                ?2.1+.25*shape.satelliteScale()+1.5*(shape.broadNoise()+shape.detailNoise()+halo)
                :1.75+1.3*(shape.broadNoise()+shape.detailNoise()+halo);
        return maxRadius*bodyReach+warp+12*shape.satelliteScale()+1;
    }

    /** One cached instance per candidate, owned by a single buildSurface context. */
    public static class Prepared {
        public final IntrusionDeposit deposit;
        public final DepositCandidateSampler.Candidate candidate;
        public final int originMinY;
        public final double rx,rz,ry,cy,angle,rotCos,rotSin,minRadius,noiseAmplitude;
        public double along,across,radial,minimumY,maximumY,scoreCutoff;
        private final double warpFrequency,broadFrequency,detailFrequency;
        private final long warpZSalt,shapeNoiseSalt;
        public final PlutonGeometry geometry;
        private Noise noise;
        private PreparedPorphyry porphyry;
        public PreparedPorphyry porphyry(DioriteIntrusionDeposit.Mineralization config){
            if(porphyry==null)porphyry=new PreparedPorphyry(this,config);
            return porphyry;
        }

        private Prepared(IntrusionDeposit deposit,DepositCandidateSampler.Candidate candidate,int minY,
                         long zSalt,long ySalt,double warpFrequency,long warpZSalt,long shapeNoiseSalt,
                         double broadFrequency,double detailFrequency){
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
            this.warpFrequency=warpFrequency;
            this.warpZSalt=warpZSalt;
            this.shapeNoiseSalt=shapeNoiseSalt;
            this.broadFrequency=broadFrequency;
            this.detailFrequency=detailFrequency;
            geometry=new PlutonGeometry(this,shape);
        }

        /** Initialized only if the conservative bounds admit a real sample. */
        public Noise noise(){
            if(noise==null) noise=new Noise(candidate,deposit.settings().shape(),warpFrequency,warpZSalt,
                    shapeNoiseSalt,broadFrequency,detailFrequency);
            return noise;
        }

        public boolean occupies(double body,double grade,int x,int y,int z,double outer,double innerProbability,double density){
            if(density<=0) return false;
            double envelope=GeologicalNoise.smoothstep(-.18,.42,body);
            double p=envelope*(outer+(innerProbability-outer)*GeologicalNoise.clamp(grade,0,1));
            if(body>.55) p=Math.max(p,Math.min(.96,innerProbability+.12));
            return noise().oreOccupancy(x,y,z)<p*density;
        }

        public void rotateColumn(int x,int z){
            double dx=x+.5-candidate.x(),dz=z+.5-candidate.z();
            along=dx*rotCos+dz*rotSin;
            across=-dx*rotSin+dz*rotCos;
        }

        public boolean outside(int y){
            return y+.5<minimumY-margin()||y+.5>maximumY+margin();
        }

        public boolean cannotImprove(double upperBound,IntrusionResult best){
            return upperBound<Math.max(scoreCutoff,best.score())-margin();
        }

        private double margin(){return 1.0e-9*(1+Math.abs(scoreCutoff));}
    }

    /** Candidate-specific noise samplers; no global or cross-thread mutable cache. */
    public static class Noise {
        public final GeologicalNoise.CachedFbm warpX,warpZ;
        private final GeologicalNoise.CachedFbm broad,detail;
        private final DepositCandidateSampler.Candidate candidate;
        private final double broadAmplitude,detailAmplitude;
        private GeologicalNoise.CachedFbm grade,floor;
        private GeologicalNoise.CachedOccupancy mix,rockOccupancy,oreOccupancy;

        private Noise(DepositCandidateSampler.Candidate c,Shape shape,double warpFrequency,long warpZSalt,
                      long shapeNoiseSalt,double broadFrequency,double detailFrequency){
            candidate=c;
            warpX=warpFrequency>0?new GeologicalNoise.CachedFbm(c.shapeSeed(),warpFrequency,3):null;
            warpZ=warpFrequency>0?new GeologicalNoise.CachedFbm(c.shapeSeed()^warpZSalt,warpFrequency,3):null;
            long seed=c.shapeSeed()^shapeNoiseSalt;
            broad=new GeologicalNoise.CachedFbm(seed,broadFrequency,3);
            detail=new GeologicalNoise.CachedFbm(seed^0xA4B1C2D3L,detailFrequency,2);
            broadAmplitude=shape.broadNoise();
            detailAmplitude=shape.detailNoise();
        }

        public double shape(int x,int y,int z){return broad.sample(x,y,z)*broadAmplitude+detail.sample(x,y,z)*detailAmplitude;}
        public double grade(int x,int y,int z){
            if(grade==null) grade=new GeologicalNoise.CachedFbm(candidate.gradeSeed(),.050,3);
            return .5+.5*grade.sample(x,y,z);
        }
        public double floor(int x,int y,int z){
            if(floor==null) floor=new GeologicalNoise.CachedFbm(candidate.alterationSeed(),.033,3);
            return floor.sample(x,y,z);
        }
        public double mix(int x,int y,int z){
            if(mix==null) mix=new GeologicalNoise.CachedOccupancy(candidate.oreSeed(),.105);
            return mix.sample(x,y,z);
        }
        public double rockOccupancy(int x,int y,int z){
            if(rockOccupancy==null) rockOccupancy=new GeologicalNoise.CachedOccupancy(candidate.occupancySeed(),.12);
            return rockOccupancy.sample(x,y,z);
        }
        public double oreOccupancy(int x,int y,int z){
            if(oreOccupancy==null) oreOccupancy=new GeologicalNoise.CachedOccupancy(candidate.occupancySeed(),.16);
            return oreOccupancy.sample(x,y,z);
        }
    }

    protected final Prepared prepare(DepositCandidateSampler.Candidate candidate,int minY,long zSalt,long ySalt,
                                     double warpFrequency,long warpZSalt,long shapeNoiseSalt,
                                     double broadFrequency,double detailFrequency){
        return new Prepared(this,candidate,minY,zSalt,ySalt,warpFrequency,warpZSalt,shapeNoiseSalt,broadFrequency,detailFrequency);
    }

    public abstract Prepared prepare(DepositCandidateSampler.Candidate candidate,int minY);

    public abstract void prepareColumn(DepositGenerationContext ctx,Prepared prepared,double scoreCutoff);

    public abstract void sample(DepositGenerationContext ctx,Prepared prepared,int y,IntrusionResult best);

    public abstract BlockState placeOre(DepositGenerationContext ctx,BlockState host,int y,IntrusionResult intrusion);
}
