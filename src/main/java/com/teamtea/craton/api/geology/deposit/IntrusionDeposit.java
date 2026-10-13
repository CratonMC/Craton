package com.teamtea.craton.api.geology.deposit;

import com.teamtea.craton.common.core.DepositCandidateSampler;
import com.teamtea.craton.common.core.DepositFieldEngine.ColumnContext;
import com.teamtea.craton.common.core.DepositRandomSequences;
import com.teamtea.craton.common.core.DepositFieldSupport;
import com.teamtea.craton.common.core.GeologicalNoise;
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
        private final double warpFrequency,broadFrequency,detailFrequency;
        private final long warpZSalt,shapeNoiseSalt;
        private final Pod[] pods;
        private final double podAlongLimit,podAcrossLimit,podVerticalLimit;
        private int activePods;
        private Noise noise;

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
            double scale=shape.satelliteScale();
            int count=scale>0?Math.min(24,shape.satelliteCount()):0;
            pods=new Pod[count];
            for(int i=0;i<count;i++) pods[i]=new Pod(candidate,rx,ry,rz,scale,i);
            podAlongLimit=rx*1.27+24*scale;
            podAcrossLimit=rz*1.27+20*scale;
            podVerticalLimit=ry*.72+7*scale;
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

        public void prepareSatellites(){
            activePods=0;
            if(Math.abs(along)>podAlongLimit||Math.abs(across)>podAcrossLimit) return;
            for(int i=0;i<pods.length;i++){
                Pod pod=pods[i];
                pod.ax=(along-pod.px)/pod.sx;
                pod.az=(across-pod.pz)/pod.sz;
                if(Math.abs(pod.ax)>1.4||Math.abs(pod.az)>1.4) continue;
                pod.horizontal=1-pod.ax*pod.ax-pod.az*pod.az;
                activePods|=1<<i;
            }
        }

        public double satelliteScore(int x,int y,int z){
            if(activePods==0||Math.abs(y+.5-cy)>podVerticalLimit) return -9;
            double best=-9;
            for(int remaining=activePods;remaining!=0;remaining&=remaining-1){
                Pod pod=pods[Integer.numberOfTrailingZeros(remaining)];
                double ay=(y+.5-cy-pod.py)/pod.sy;
                if(Math.abs(ay)>1.4) continue;
                if(pod.broad==null){
                    pod.broad=new GeologicalNoise.CachedFbm(pod.seed,.040,3);
                    pod.detail=new GeologicalNoise.CachedFbm(pod.seed^0xE95L,.115,2);
                }
                double broad=pod.broad.sample(x,y,z)*.22;
                double detail=pod.detail.sample(x,y,z)*.07;
                best=Math.max(best,pod.horizontal-ay*ay+broad+detail);
            }
            return best;
        }

        private static final class Pod {
            private final long seed;
            private final double px,py,pz,sx,sy,sz;
            private double ax,az,horizontal;
            private GeologicalNoise.CachedFbm broad,detail;

            private Pod(DepositCandidateSampler.Candidate c,double rx,double ry,double rz,double scale,int i){
                seed=DepositCandidateSampler.mix(c.shapeSeed()^0x1A7L^(0x9E3779B97F4A7C15L*(i+1)));
                double theta=DepositFieldSupport.rand01(seed)*Math.PI*2;
                double latitude=(DepositFieldSupport.rand01(seed^0xA71L)-.5)*1.12;
                double shell=1.04+DepositFieldSupport.rand01(seed^0xB52L)*.22;
                double horizontal=Math.sqrt(1-latitude*latitude)*shell;
                px=rx*horizontal*Math.cos(theta);
                pz=rz*horizontal*Math.sin(theta);
                py=ry*latitude*shell;
                sx=(7+DepositFieldSupport.rand01(seed^0xC63L)*10)*scale;
                sz=(5+DepositFieldSupport.rand01(seed^0xD84L)*9)*scale;
                sy=Math.max(1.5,Math.min(5,ry*.38))*scale;
            }
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

    /** Candidate-specific noise samplers; no global or cross-thread mutable cache. */
    public static final class Noise {
        public final GeologicalNoise.CachedFbm warpX,warpZ;
        private final GeologicalNoise.CachedFbm broad,detail;
        private final DepositCandidateSampler.Candidate candidate;
        private final double broadAmplitude,detailAmplitude;
        private GeologicalNoise.CachedFbm grade,floor,satelliteGrade;
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
        public double satelliteGrade(int x,int y,int z){
            if(satelliteGrade==null) satelliteGrade=new GeologicalNoise.CachedFbm(candidate.gradeSeed(),.057,3);
            return .5+.5*satelliteGrade.sample(x,y,z);
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

    public abstract void prepareColumn(ColumnContext ctx,Prepared prepared,double scoreCutoff);

    public abstract void sample(ColumnContext ctx,Prepared prepared,int y,IntrusionResult best);

    public abstract BlockState placeOre(ColumnContext ctx,BlockState host,int y,IntrusionResult intrusion);
}
