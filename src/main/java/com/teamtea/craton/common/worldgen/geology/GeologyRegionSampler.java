package com.teamtea.craton.common.worldgen.geology;

import java.util.LinkedHashMap;
import java.util.Map;

/** World-aligned spatial blending of regional weights; owned by one chunk context. */
public class GeologyRegionSampler {
    private static final int SPACING=64;
    private final GeologyTransition transition;
    private final GeologyClimateSampler climate;
    private int cellX,cellZ;
    private boolean initialized;
    private double[] a,b,c,d;
    private final Map<Long,double[]> lattice=new LinkedHashMap<>(64,.75f,true){
        @Override protected boolean removeEldestEntry(Map.Entry<Long,double[]> eldest){return size()>512;}
    };

    public GeologyRegionSampler(GeologyTransition transition,GeologyClimateSampler climate){
        this.transition=transition;this.climate=climate;
    }

    public double[] weights(int x,int z){
        // Explicit all-hard datapacks keep their original classification.
        if(!transition.hasSmoothTransitions()) return transition.weights(climate.sample(x,z));
        int gx=Math.floorDiv(x,SPACING),gz=Math.floorDiv(z,SPACING);
        double tx=smooth(Math.floorMod(x,SPACING)/(double)SPACING);
        double tz=smooth(Math.floorMod(z,SPACING)/(double)SPACING);
        if(!initialized||gx!=cellX||gz!=cellZ){
            a=corner(gx,gz);b=corner(gx+1,gz);c=corner(gx,gz+1);d=corner(gx+1,gz+1);
            cellX=gx;cellZ=gz;initialized=true;
        }
        double[] weights=new double[a.length];
        double total=0;
        for(int i=0;i<weights.length;i++){
            double north=a[i]+tx*(b[i]-a[i]),south=c[i]+tx*(d[i]-c[i]);
            weights[i]=north+tz*(south-north);
            total+=weights[i];
        }
        // Datapacks need not supply a catch-all region. Empty corners must not
        // dilute the weight sum or erase a valid local region between grid nodes.
        if(total<=0) return transition.weights(climate.sample(x,z));
        for(int i=0;i<weights.length;i++) weights[i]/=total;
        return weights;
    }

    private double[] corner(int x,int z){
        long key=((long)x<<32)^(z&0xffffffffL);
        return lattice.computeIfAbsent(key,ignored -> transition.weights(climate.sample(x*SPACING,z*SPACING)));
    }

    private static double smooth(double t){return t*t*(3-2*t);}
}
