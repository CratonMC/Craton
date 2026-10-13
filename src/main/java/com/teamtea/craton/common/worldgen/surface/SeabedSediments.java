package com.teamtea.craton.common.worldgen.surface;

import com.teamtea.craton.common.worldgen.geology.GeologyClimateSampler;
import com.teamtea.craton.common.worldgen.noise.GeologicalNoise;

import net.minecraft.world.level.block.state.BlockState;

/** Continuous surface cover, independent of the deep stratigraphic slots. */
public class SeabedSediments {
    public record Cover(BlockState material,int thickness) {}
    private final GeologyClimateSampler climate;
    private final GeologicalNoise.CachedFbm thicknessNoise;

    public SeabedSediments(GeologyClimateSampler climate,long seed){
        this.climate=climate;
        thicknessNoise=new GeologicalNoise.CachedFbm(seed,.015,3);
    }

    public Cover cover(BlockState material,int x,int z,int surfaceY){
        return cover(material,x,z,surfaceY,0,0);
    }

    public Cover cover(BlockState material,int x,int z,int surfaceY,double localSlope,double localBasin){
        double slope=localSlope,basin=GeologicalNoise.clamp(localBasin*.25,0,2);
        if(climate!=null){
            double center=climate.regionalSurfaceY(x,z);
            double west=climate.regionalSurfaceY(x-8,z),east=climate.regionalSurfaceY(x+8,z);
            double north=climate.regionalSurfaceY(x,z-8),south=climate.regionalSurfaceY(x,z+8);
            slope=Math.max(slope,Math.hypot((east-west)/16,(south-north)/16));
            double curvature=(west+east+north+south)*.25-center;
            // Actual floor below its regional trend behaves as a local sediment trap.
            basin+=GeologicalNoise.clamp((center-surfaceY)*.18+curvature*.35,-2,4);
        }
        double thickness=3.5+thicknessNoise.sample(x,0,z)*3+basin-slope*4;
        if(slope>.75) thickness=Math.min(thickness,2);
        return new Cover(material,(int)Math.round(GeologicalNoise.clamp(thickness,0,10)));
    }
}
