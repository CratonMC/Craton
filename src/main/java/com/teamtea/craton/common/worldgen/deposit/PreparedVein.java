package com.teamtea.craton.common.worldgen.deposit;

import com.teamtea.craton.common.worldgen.noise.GeologicalNoise;

import com.teamtea.craton.api.geology.deposit.FieldDeposit;
import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.*;

/** Immutable candidate geometry and exact noise caches, confined to one chunk context. */
public class PreparedVein {
    private final int minY;
    private final FieldDeposit.Shape config;
    private final Segment[] segments;
    private final double cy,halfLength,thickness,halfHeight,mainAngle;
    private GeologicalNoise.CachedFbm warpX,warpY,warpZ,detail;
    private final long shapeSeed;
    private final PreparedPods pods;

    public PreparedVein(DepositCandidateSampler.Candidate c,int minY,FieldDeposit.Shape config,double frequency,int branches){
        this.minY=minY;this.config=config;this.shapeSeed=c.shapeSeed();
        halfLength=config.x(c.shapeSeed());thickness=config.z(c.shapeSeed());halfHeight=config.y(c.shapeSeed());
        cy=config.height(minY,c.verticalSeed());mainAngle=angle(c.rotationSeed());
        segments=new Segment[branches+1];
        segments[0]=new Segment(c.x(),c.z(),c.shapeSeed(),c.rotationSeed(),cy,mainAngle,halfLength,thickness,frequency,0,halfHeight);
        for(int i=0;i<branches;i++){
            long s=DepositCandidateSampler.mix(c.shapeSeed()^(0x9E3779B97F4A7C15L*(i+1)));
            double da=(rand01(s)-.5)*1.15;
            double length=halfLength*(.42+.30*rand01(s^0x51));
            double width=thickness*(.50+.28*rand01(s^0x91));
            double along=(rand01(s^0xA1)-.5)*halfLength*1.1;
            double lateral=veinCurve(c.shapeSeed(),along,frequency,0);
            double bx=c.x()+along*Math.cos(mainAngle)-lateral*Math.sin(mainAngle);
            double bz=c.z()+along*Math.sin(mainAngle)+lateral*Math.cos(mainAngle);
            double bcy=cy+veinRise(c.shapeSeed(),c.rotationSeed(),along,0);
            segments[i+1]=new Segment((int)Math.round(bx),(int)Math.round(bz),s,s^0x44,bcy,
                    mainAngle+da,length,width,frequency*1.18,i+1,halfHeight*.78);
        }
        pods=new PreparedPods(c,c.x(),cy,c.z(),halfLength,halfHeight,thickness,mainAngle,0x6E11L,config);
    }
    public int minY(){return minY;}

    public double score(int x,int y,int z){
        // fBm is clamped to [-1,1]. Expand the existing segment cutoffs by the full
        // possible domain warp, so this rejection cannot remove a contributing vein.
        boolean bodyPossible=false;
        for(Segment segment:segments)
            if(segment.possible(x+.5,y+.5,z+.5,config.broadNoise())){bodyPossible=true;break;}
        if(!bodyPossible&&!pods.possible(x,y,z)) return -9;
        if(warpX==null){
        warpX=new GeologicalNoise.CachedFbm(shapeSeed^0x111,.010,3);
        warpY=new GeologicalNoise.CachedFbm(shapeSeed^0x222,.012,3);
        warpZ=new GeologicalNoise.CachedFbm(shapeSeed^0x333,.010,3);
        detail=new GeologicalNoise.CachedFbm(shapeSeed^0x555,.075,2);
        }
        double wx=x+.5+warpX.sample(x,y,z)*45*config.broadNoise();
        double wy=y+.5+warpY.sample(x,y,z)*35*config.broadNoise();
        double wz=z+.5+warpZ.sample(x,y,z)*45*config.broadNoise();
        double best=segments[0].score(wx,wy,wz);
        for(int i=1;i<segments.length;i++) best=Math.max(best,segments[i].score(wx,wy,wz));
        double body=best+detail.sample(x,y,z)*config.detailNoise();
        return Math.max(body,pods.score(x,y,z)*.65-.04);
    }

    private static class Segment {
        final double x,z,cy,cos,sin,length,thickness,height,dip,slope;
        final long curveSeed,riseSeed;
        final double curveFrequency;
        GeologicalNoise.CachedFbm curve,rise;
        Segment(double x,double z,long shape,long rotation,double cy,double angle,double length,double thickness,
                double frequency,int branch,double height){
            this.x=x;this.z=z;this.cy=cy;this.cos=Math.cos(angle);this.sin=Math.sin(angle);
            this.length=length;this.thickness=thickness;this.height=height;
            dip=Math.tan(Math.toRadians(49+rand01(rotation^0xD1)*29));
            slope=(rand01(rotation^0x51)-.5)*.20;
            curveSeed=shape^branch;riseSeed=shape^0x71^branch;curveFrequency=.010+frequency*.20;
        }
        boolean possible(double px,double py,double pz,double broad){
            double along=(px-x)*cos+(pz-z)*sin;
            return Math.abs(along)<=length*1.15+45*broad*(Math.abs(cos)+Math.abs(sin))+1E-7
                    &&Math.abs(py-cy)<=height*1.15+35*broad+1E-7;
        }
        double score(double px,double py,double pz){
            double dx=px-x,dz=pz-z,along=dx*cos+dz*sin,across=-dx*sin+dz*cos;
            double lengthFade=1-Math.abs(along)/length;
            if(lengthFade<-.15) return -9;
            double verticalFade=1-Math.abs(py-cy)/height;
            if(verticalFade<-.15) return -9;
            if(curve==null){
                curve=new GeologicalNoise.CachedFbm(curveSeed,curveFrequency,3);
                rise=new GeologicalNoise.CachedFbm(riseSeed,.014,3);
            }
            double bend=curve.sample(along,0,0)*12;
            double lift=slope*along+rise.sample(along,0,0)*5;
            double plane=across-bend-(py-cy-lift)/dip;
            double localThickness=thickness*(.58+.42*Math.max(0,Math.min(lengthFade,verticalFade)));
            return Math.min(lengthFade,verticalFade)-Math.abs(plane)/localThickness;
        }
    }
}
