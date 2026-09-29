package com.teamtea.craton.common.core;

/**
 * Stateless deterministic gradient noise utilities for single-point geology sampling.
 * No neighborhood state is required, so the same xyz always reconstructs the same field.
 */
public final class GeologicalNoise {
    private GeologicalNoise(){}

    public static double perlin(double x,double y,double z,long seed){
        int x0=floor(x),y0=floor(y),z0=floor(z);
        double xf=x-x0,yf=y-y0,zf=z-z0;
        double u=fade(xf),v=fade(yf),w=fade(zf);

        double n000=grad(hash(x0,y0,z0,seed),xf,yf,zf);
        double n100=grad(hash(x0+1,y0,z0,seed),xf-1,yf,zf);
        double n010=grad(hash(x0,y0+1,z0,seed),xf,yf-1,zf);
        double n110=grad(hash(x0+1,y0+1,z0,seed),xf-1,yf-1,zf);
        double n001=grad(hash(x0,y0,z0+1,seed),xf,yf,zf-1);
        double n101=grad(hash(x0+1,y0,z0+1,seed),xf-1,yf,zf-1);
        double n011=grad(hash(x0,y0+1,z0+1,seed),xf,yf-1,zf-1);
        double n111=grad(hash(x0+1,y0+1,z0+1,seed),xf-1,yf-1,zf-1);

        double nx00=lerp(u,n000,n100),nx10=lerp(u,n010,n110);
        double nx01=lerp(u,n001,n101),nx11=lerp(u,n011,n111);
        return clamp(lerp(w,lerp(v,nx00,nx10),lerp(v,nx01,nx11))*0.93,-1,1);
    }

    public static double fbm(double x,double y,double z,long seed,double frequency,int octaves){
        double sum=0,amp=1,norm=0,f=frequency;
        for(int i=0;i<octaves;i++){
            sum+=perlin(x*f,y*f,z*f,DepositCandidateSampler.mix(seed+i*0x9E3779B97F4A7C15L))*amp;
            norm+=amp;
            amp*=0.5;
            f*=2.03;
        }
        return norm==0?0:sum/norm;
    }

    public static double fbm2(double x,double z,long seed,double frequency,int octaves){
        return fbm(x,0,z,seed,frequency,octaves);
    }

    /** Correlated field in [0,1], useful as stable block occupancy rather than white-noise hash. */
    public static double occupancy(double x,double y,double z,long seed,double frequency){
        double broad=fbm(x,y,z,seed,frequency,3);
        double detail=perlin(x*frequency*3.7,y*frequency*3.7,z*frequency*3.7,seed^0xD1B54A32D192ED03L);
        double micro=hashUnit(floor(x),floor(y),floor(z),seed^0x94D049BB133111EBL)*2-1;
        return clamp(0.5+0.5*(broad*0.72+detail*0.20+micro*0.08),0,1);
    }

    public static double smoothstep(double edge0,double edge1,double x){
        if(edge0==edge1) return x<edge0?0:1;
        double t=clamp((x-edge0)/(edge1-edge0),0,1);
        return t*t*(3-2*t);
    }

    public static double hashUnit(int x,int y,int z,long seed){
        long h=seed;
        h^=mixCoord(x,0x9E3779B185EBCA87L);
        h^=mixCoord(y,0xC2B2AE3D27D4EB4FL);
        h^=mixCoord(z,0x165667B19E3779F9L);
        h=DepositCandidateSampler.mix(h);
        return ((h>>>11)&((1L<<53)-1))*0x1.0p-53;
    }

    private static long hash(int x,int y,int z,long seed){
        long h=seed;
        h^=mixCoord(x,0x632BE59BD9B4E019L);
        h^=mixCoord(y,0x9E3779B97F4A7C15L);
        h^=mixCoord(z,0xC6BC279692B5C323L);
        return DepositCandidateSampler.mix(h);
    }

    private static long mixCoord(long v,long mul){return DepositCandidateSampler.mix(v*mul);}

    private static double grad(long hash,double x,double y,double z){
        int h=(int)(hash&15);
        double u=h<8?x:y;
        double v=h<4?y:(h==12||h==14?x:z);
        return ((h&1)==0?u:-u)+((h&2)==0?v:-v);
    }

    private static double fade(double t){return t*t*t*(t*(t*6-15)+10);}
    private static double lerp(double t,double a,double b){return a+t*(b-a);}
    private static int floor(double v){int i=(int)v;return v<i?i-1:i;}
    public static double clamp(double v,double min,double max){return Math.max(min,Math.min(max,v));}
}
