package com.teamtea.craton.common.core;

import com.teamtea.craton.api.geology.deposit.FieldDeposit;
import com.teamtea.craton.api.geology.deposit.BandedIronFormation;
import com.teamtea.craton.common.registry.CratonBlocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;


import com.teamtea.craton.common.core.DepositFieldEngine.ColumnContext;

public final class DepositFieldSupport {
    private DepositFieldSupport(){}

    public static FieldDeposit field(ColumnContext ctx,DepositCandidateSampler.Candidate c){return (FieldDeposit)ctx.owners().get(c);}
    public static FieldDeposit.Shape shape(ColumnContext ctx,DepositCandidateSampler.Candidate c){return field(ctx,c).settings().shape();}
    public static BlockState rock(ColumnContext ctx,DepositCandidateSampler.Candidate c){return field(ctx,c).settings().rock();}
    public static BlockState ore(FieldDeposit d,int index,BlockState host){
        if(index<0||index>=d.settings().ores().size()) return host;
        return d.settings().ores().get(index).value().getOreState(host);
    }
    public static BlockState ore(ColumnContext ctx,DepositCandidateSampler.Candidate c,int index,BlockState host){
        return ore(field(ctx,c),index,host);
    }

    /* ---------------- shared geometry ---------------- */

    /** Small correlated pods follow the parent body's orientation and stay near its outer envelope. */
    public static double satellitePods(DepositCandidateSampler.Candidate c,int x,int y,int z,
                                        double cx,double cy,double cz,double rx,double ry,double rz,
                                        double angle,long variant,FieldDeposit.Shape config){
        if(config.satelliteCount()<=0||config.satelliteScale()<=0) return -9;
        double rotDx=x+.5-cx,rotDz=z+.5-cz;
        double rotCos=Math.cos(angle),rotSin=Math.sin(angle);
        double rotAlong=rotDx*rotCos+rotDz*rotSin,rotAcross=-rotDx*rotSin+rotDz*rotCos;
        if(Math.abs(rotAlong)>rx*1.27+24*config.satelliteScale()||Math.abs(rotAcross)>rz*1.27+20*config.satelliteScale()
                ||Math.abs(y+.5-cy)>ry*.72+7*config.satelliteScale()) return -9;
        double best=-9;
        for(int i=0;i<Math.min(24,config.satelliteCount());i++){
            long seed=DepositCandidateSampler.mix(c.shapeSeed()^variant^(0x9E3779B97F4A7C15L*(i+1)));
            double theta=rand01(seed)*Math.PI*2;
            double latitude=(rand01(seed^0xA71L)-.5)*1.12;
            double shell=1.04+rand01(seed^0xB52L)*.22;
            double horizontal=Math.sqrt(1-latitude*latitude)*shell;
            double px=rx*horizontal*Math.cos(theta),pz=rz*horizontal*Math.sin(theta);
            double py=ry*latitude*shell;
            double sx=(7+rand01(seed^0xC63L)*10)*config.satelliteScale();
            double sz=(5+rand01(seed^0xD84L)*9)*config.satelliteScale();
            double sy=Math.max(1.5,Math.min(5,ry*.38))*config.satelliteScale();
            double ax=(rotAlong-px)/sx,az=(rotAcross-pz)/sz,ay=(y+.5-cy-py)/sy;
            if(Math.abs(ax)>1.4||Math.abs(az)>1.4||Math.abs(ay)>1.4) continue;
            double broad=GeologicalNoise.fbm(x,y,z,seed,.040,3)*.22;
            double detail=GeologicalNoise.fbm(x,y,z,seed^0xE95L,.115,2)*.07;
            best=Math.max(best,1-sq(ax)-sq(az)-sq(ay)+broad+detail);
        }
        return best;
    }

    public static double veinSystemScore(DepositCandidateSampler.Candidate c,int x,int y,int z,int minY,
                                          double halfLength,double thickness,double curveFreq,int branches,FieldDeposit.Shape config){
        double warpX=GeologicalNoise.fbm(x,y,z,c.shapeSeed()^0x111,.010,3)*45*config.broadNoise();
        double warpY=GeologicalNoise.fbm(x,y,z,c.shapeSeed()^0x222,.012,3)*35*config.broadNoise();
        double warpZ=GeologicalNoise.fbm(x,y,z,c.shapeSeed()^0x333,.010,3)*45*config.broadNoise();
        double wx=x+.5+warpX,wz=z+.5+warpZ,wy=y+.5+warpY;
        double cy=config.height(minY,c.verticalSeed());
        double mainAngle=angle(c.rotationSeed());
        double best=singleVeinScore(c.x(),c.z(),c.shapeSeed(),c.rotationSeed(),wx,wy,wz,cy,
                mainAngle,halfLength,thickness,curveFreq,0,config.y(c.shapeSeed()));
        for(int i=0;i<branches;i++){
            long s=DepositCandidateSampler.mix(c.shapeSeed()^(0x9E3779B97F4A7C15L*(i+1)));
            double da=(rand01(s)-.5)*1.15;
            double branchLength=halfLength*(.42+.30*rand01(s^0x51));
            double branchThickness=thickness*(.50+.28*rand01(s^0x91));
            double along=(rand01(s^0xA1)-.5)*halfLength*1.1;
            double lateral=veinCurve(c.shapeSeed(),along,curveFreq,0);
            double bx=c.x()+along*Math.cos(mainAngle)-lateral*Math.sin(mainAngle);
            double bz=c.z()+along*Math.sin(mainAngle)+lateral*Math.cos(mainAngle);
            double bcy=cy+veinRise(c.shapeSeed(),c.rotationSeed(),along,0);
            best=Math.max(best,singleVeinScore((int)Math.round(bx),(int)Math.round(bz),s,s^0x44,wx,wy,wz,bcy,
                    mainAngle+da,branchLength,branchThickness,curveFreq*1.18,i+1,config.y(c.shapeSeed())*.78));
        }
        double body=best+GeologicalNoise.fbm(x,y,z,c.shapeSeed()^0x555,.075,2)*config.detailNoise();
        double pods=satellitePods(c,x,y,z,c.x(),cy,c.z(),halfLength,config.y(c.shapeSeed()),thickness,mainAngle,0x6E11L,config);
        return Math.max(body,pods*.65-.04);
    }

    public static double singleVeinScore(double centerX,double centerZ,long shapeSeed,long rotationSeed,
                                  double x,double y,double z,double cy,double angle,
                                          double halfLength,double thickness,double curveFreq,int branch,double halfHeight){
        double rotDx=x-centerX,rotDz=z-centerZ;
        double rotCos=Math.cos(angle),rotSin=Math.sin(angle);
        double rotAlong=rotDx*rotCos+rotDz*rotSin,rotAcross=-rotDx*rotSin+rotDz*rotCos;
        double curve=veinCurve(shapeSeed,rotAlong,curveFreq,branch);
        double rise=veinRise(shapeSeed,rotationSeed,rotAlong,branch);
        double dip=49+rand01(rotationSeed^0xD1)*29;
        double plane=rotAcross-curve-(y-cy-rise)/Math.tan(Math.toRadians(dip));
        double lengthFade=1-Math.abs(rotAlong)/halfLength;
        if(lengthFade<-.15) return -9;
        double verticalFade=1-Math.abs(y-cy)/halfHeight;
        if(verticalFade<-.15) return -9;
        double localThickness=thickness*(.58+.42*Math.max(0,Math.min(lengthFade,verticalFade)));
        return Math.min(lengthFade,verticalFade)-Math.abs(plane)/localThickness;
    }

    public static double veinCurve(long shapeSeed,double along,double frequency,int branch){
        return GeologicalNoise.fbm(along,0,0,shapeSeed^branch,.010+frequency*.20,3)*12;
    }

    public static double veinRise(long shapeSeed,long rotationSeed,double along,int branch){
        return (rand01(rotationSeed^0x51)-.5)*.20*along
                +GeologicalNoise.fbm(along,0,0,shapeSeed^0x71^branch,.014,3)*5;
    }

    public static boolean occupies(double body,double grade,DepositCandidateSampler.Candidate c,int x,int y,int z,
                                    double outer,double innerProbability){
        double envelope=GeologicalNoise.smoothstep(-.18,.42,body);
        double p=envelope*(outer+(innerProbability-outer)*GeologicalNoise.clamp(grade,0,1));
        if(body>.55) p=Math.max(p,Math.min(.96,innerProbability+.12));
        return GeologicalNoise.occupancy(x,y,z,c.occupancySeed(),.16)<p;
    }

    public static List<Integer> matchingLayers(ColumnContext ctx,BlockState target){
        return ctx.layerIndices().getOrDefault(target.getBlock(),List.of());
    }

    public static double shapeNoise(int x,int y,int z,long seed,double broadFrequency,double detailFrequency,
                                     double broadAmplitude,double detailAmplitude){
        return GeologicalNoise.fbm(x,y,z,seed,broadFrequency,3)*broadAmplitude
                +GeologicalNoise.fbm(x,y,z,seed^0xA4B1C2D3L,detailFrequency,2)*detailAmplitude;
    }

    public static double shapeNoise2(int x,int z,long seed,double broadFrequency,double detailFrequency,
                                      double broadAmplitude,double detailAmplitude){
        return GeologicalNoise.fbm2(x,z,seed,broadFrequency,3)*broadAmplitude
                +GeologicalNoise.fbm2(x,z,seed^0xA4B1C2D3L,detailFrequency,2)*detailAmplitude;
    }

    public static int index(long seed,int size){return Math.min(size-1,(int)(rand01(seed)*size));}
    public static double angle(long seed){return rand01(seed)*Math.PI*2;}
    public static double rand01(long seed){long z=DepositCandidateSampler.mix(seed);return ((z>>>11)&((1L<<53)-1))*0x1.0p-53;}
    public static double proximity(int x,int z,DepositCandidateSampler.Candidate c,double radius){return GeologicalNoise.clamp(1-Math.hypot(x+.5-c.x(),z+.5-c.z())/radius,0,1);}
    public static long salt(String id){return DepositCandidateSampler.salt(id);}

    public static boolean isCarbonate(BlockState state){return isLimestone(state)||isMarble(state);}
    public static boolean isLimestone(BlockState state){return state.is(CratonBlocks.LIMESTONE.getOrigin().getBaseBlock());}
    public static boolean isMarble(BlockState state){return state.is(CratonBlocks.MARBLE.getOrigin().getBaseBlock());}
    public static boolean isRhyolite(BlockState state){return state.is(CratonBlocks.RHYOLITE.getOrigin().getBaseBlock());}
    public static boolean isPegmatite(BlockState state){return state.is(CratonBlocks.PEGMATITE.getOrigin().getBaseBlock());}

    public static double sq(double x){return x*x;}
}
