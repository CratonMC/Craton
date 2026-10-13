package com.teamtea.craton.common.worldgen.deposit;

import com.teamtea.craton.common.worldgen.noise.GeologicalNoise;

import com.teamtea.craton.api.geology.deposit.FieldDeposit;
import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.*;

/** Candidate-fixed satellite transforms and exact lattice caches. */
public class PreparedPods {
    private final double cx,cz,rx,rz,cos,sin,scale;
    private double cy,ry;
    private Pod[] pods;
    private final int count;
    private final long shapeSeed,variant;
    private boolean initialized;
    private int columnX,columnZ,activePods;
    private double along,across;
    public PreparedPods(DepositCandidateSampler.Candidate c,double cx,double cy,double cz,double rx,double ry,double rz,
                        double angle,long variant,FieldDeposit.Shape config){
        this.cx=cx;this.cy=cy;this.cz=cz;this.rx=rx;this.ry=ry;this.rz=rz;
        cos=Math.cos(angle);sin=Math.sin(angle);scale=config.satelliteScale();
        count=scale<=0?0:Math.min(24,config.satelliteCount());shapeSeed=c.shapeSeed();this.variant=variant;
    }
    private void initializePods(){
        pods=new Pod[count];
        for(int i=0;i<pods.length;i++){
            long seed=DepositCandidateSampler.mix(shapeSeed^variant^(0x9E3779B97F4A7C15L*(i+1)));
            double theta=rand01(seed)*Math.PI*2;
            double latitude=(rand01(seed^0xA71L)-.5)*1.12;
            double shell=1.04+rand01(seed^0xB52L)*.22;
            double horizontal=Math.sqrt(1-latitude*latitude)*shell;
            pods[i]=new Pod(rx*horizontal*Math.cos(theta),ry*latitude*shell,rz*horizontal*Math.sin(theta),
                    (7+rand01(seed^0xC63L)*10)*scale,
                    Math.max(1.5,Math.min(5,ry*.38))*scale,(5+rand01(seed^0xD84L)*9)*scale,
                    seed,latitude,shell);
        }
    }
    /** Only the vertical geometry changes between columns; seeds and lateral geometry stay fixed. */
    public void retargetVertical(double centerY,double radiusY){
        cy=centerY;
        if(ry==radiusY) return;
        ry=radiusY;
        if(pods!=null) for(Pod pod:pods){
            // Keep the original multiplication order for exact shape reconstruction.
            pod.y=ry*pod.latitude*pod.shell;
            pod.sy=Math.max(1.5,Math.min(5,ry*.38))*scale;
        }
    }
    private void column(int x,int z){
        if(initialized&&columnX==x&&columnZ==z) return;
        initialized=true;columnX=x;columnZ=z;
        double dx=x+.5-cx,dz=z+.5-cz;
        along=dx*cos+dz*sin;across=-dx*sin+dz*cos;
        activePods=0;
        if(Math.abs(along)>rx*1.27+24*scale||Math.abs(across)>rz*1.27+20*scale) return;
        if(pods==null) initializePods();
        for(int i=0;i<pods.length;i++){
            Pod p=pods[i];
            double ax=(along-p.x)/p.sx,az=(across-p.z)/p.sz;
            if(Math.abs(ax)>1.4||Math.abs(az)>1.4) continue;
            p.horizontal=1-sq(ax)-sq(az);
            activePods|=1<<i;
        }
    }
    public boolean possible(int x,int y,int z){
        if(count==0||Math.abs(y+.5-cy)>ry*.72+7*scale) return false;
        column(x,z);
        return activePods!=0;
    }
    public double score(int x,int y,int z){
        if(!possible(x,y,z)) return -9;
        double best=-9;
        for(int bits=activePods;bits!=0;bits&=bits-1){
            Pod p=pods[Integer.numberOfTrailingZeros(bits)];
            double ay=(y+.5-cy-p.y)/p.sy;
            if(Math.abs(ay)>1.4) continue;
            if(p.broad==null){
                p.broad=new GeologicalNoise.CachedFbm(p.seed,.040,3);
                p.detail=new GeologicalNoise.CachedFbm(p.seed^0xE95L,.115,2);
            }
            double broad=p.broad.sample(x,y,z)*.22,detail=p.detail.sample(x,y,z)*.07;
            best=Math.max(best,p.horizontal-sq(ay)+broad+detail);
        }
        return best;
    }
    private static class Pod {
        final double x,z,sx,sz,latitude,shell;
        double y,sy;
        final long seed;
        double horizontal;
        GeologicalNoise.CachedFbm broad,detail;
        Pod(double x,double y,double z,double sx,double sy,double sz,long seed,double latitude,double shell){
            this.x=x;this.y=y;this.z=z;this.sx=sx;this.sy=sy;this.sz=sz;this.seed=seed;this.latitude=latitude;this.shell=shell;
        }
    }
}
