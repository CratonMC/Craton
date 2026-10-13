package com.teamtea.craton.common.worldgen.deposit;

import com.teamtea.craton.api.geology.deposit.DioriteIntrusionDeposit;
import com.teamtea.craton.api.geology.deposit.IntrusionDeposit;
import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.*;

/** Candidate-local ore clusters. Maximum-size vertical envelopes retain a physical host-rock gap. */
public class PreparedPorphyry {
    public record Cluster(double x,double y,double z,double radiusX,double radiusY,double radiusZ){}
    private final Cluster[] clusters;
    private final double cos,sin;
    private final int minY;
    private final double[] horizontal;
    private boolean ready;
    private int lastX,lastZ,active;
    private double radial;
    private int selected=-1;

    public PreparedPorphyry(IntrusionDeposit.Prepared p,DioriteIntrusionDeposit.Mineralization config){
        cos=p.rotCos;sin=p.rotSin;minY=p.originMinY;
        long seed=DepositCandidateSampler.mix(p.candidate.gradeSeed()^0x434C5553544552L)
                ^DepositCandidateSampler.mix(p.candidate.alterationSeed()^0x504F525048595259L);
        int requested=config.clusterMin()+index(seed,config.clusterMax()-config.clusterMin()+1);
        double low=Math.max(minY+1,p.cy-.50*p.ry)+.5,high=p.cy+.35*p.ry-.5;
        double span=Math.max(1,high-low),gap=config.clusterGap()+1;
        int count=Math.max(1,Math.min(requested,1+(int)(span/(gap+2))));
        clusters=new Cluster[count];horizontal=new double[count];
        double[] weights=new double[count],heights=new double[count];
        weights[0]=count==1?1:count==2?.60:.50;
        double sum=0;
        for(int i=1;i<count;i++){weights[i]=.65+rand01(seed+i*0x9E3779B97F4A7C15L);sum+=weights[i];}
        for(int i=1;i<count;i++)weights[i]*=(1-weights[0])/sum;
        double base=.10+.14*config.oreDensity();
        double radius=config.radiusFraction()>0?config.radiusFraction():1;
        double height=config.heightFraction()>0?config.heightFraction():1;
        double total=0;
        for(int i=0;i<count;i++){
            heights[i]=Math.max(2,p.ry*height*base*Math.cbrt(weights[i]));
            total+=2*heights[i];
        }
        double fit=Math.min(1,(span-(count-1)*gap)/total);
        double used=(count-1)*gap+total*fit;
        double cursor=low+(span-used)*(.25+.50*rand01(seed^0x11L));
        // Candidate-dependent ordering keeps the main cluster from occupying a fixed level.
        int main=index(seed^0x22L,count);
        for(int slot=0;slot<count;slot++){
            int i=(slot-main+count)%count;
            long local=DepositCandidateSampler.mix(seed^((i+1L)*0x632BE59BD9B4E019L));
            double ry=heights[i]*fit,y=Math.floor(cursor+ry)+.5;
            double ny=(y-p.cy)/p.ry;
            double along=p.geometry.model().stockAxisAlong(ny)*p.rx+(rand01(local)-.5)*p.rx*.06;
            double across=p.geometry.model().stockAxisAcross(ny)*p.rz+(rand01(local^0x33L)-.5)*p.rz*.06;
            double x=Math.floor(p.candidate.x()+along*cos-across*sin)+.5;
            double z=Math.floor(p.candidate.z()+along*sin+across*cos)+.5;
            double volumeScale=Math.cbrt(weights[i]),stretch=.9+.2*rand01(local^0x44L);
            double rx=Math.max(1.6,p.rx*.29*radius*base*volumeScale*stretch);
            double rz=Math.max(1.6,p.rz*.25*radius*base*volumeScale/stretch);
            clusters[slot]=new Cluster(x,y,z,rx,ry,rz);
            cursor+=2*ry+gap;
        }
    }

    public int count(){return clusters.length;}
    public Cluster cluster(int index){return clusters[index];}
    public int selected(){return selected;}
    public double radial(){return radial;}

    private void column(int x,int z){
        if(ready&&lastX==x&&lastZ==z)return;
        ready=true;lastX=x;lastZ=z;active=0;
        for(int i=0;i<clusters.length;i++){
            Cluster c=clusters[i];double dx=x+.5-c.x(),dz=z+.5-c.z();
            double a=(dx*cos+dz*sin)/c.radiusX(),b=(-dx*sin+dz*cos)/c.radiusZ();
            horizontal[i]=a*a+b*b;
            if(horizontal[i]<1)active|=1<<i;
        }
    }

    /** No noise can expand this envelope; scale is at most one. */
    public double sample(int x,int y,int z,double scale){
        selected=-1;
        if(y<=minY)return Double.POSITIVE_INFINITY;
        column(x,z);double inverse=1/(scale*scale);
        for(int bits=active;bits!=0;bits&=bits-1){
            int i=Integer.numberOfTrailingZeros(bits);Cluster c=clusters[i];
            double v=(y+.5-c.y())/c.radiusY();
            double score=(horizontal[i]+v*v)*inverse;
            if(score<1){selected=i;radial=Math.sqrt(horizontal[i]*inverse);return score;}
        }
        return Double.POSITIVE_INFINITY;
    }
}
