package com.teamtea.craton.common.core;

import com.teamtea.craton.Craton;
import com.teamtea.craton.api.geology.GeologyLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/** Stratigraphy sampler with a bounded column cache, owned by one chunk generation context. */
public final class GeologyFieldSampler {
    private final List<Holder<GeologyLayer>> layers;
    private final int minY;
    private final long boundaryRootSeed;
    private final double thicknessScale;
    private final double[] cumulativeThickness;
    private Column lastColumn;
    private final Map<Long,Column> columns=new LinkedHashMap<>(256,.75f,true){
        @Override protected boolean removeEldestEntry(Map.Entry<Long,Column> eldest){return size()>1024;}
    };

    private record Column(int x,int z,double regionalWarp,double[] interfaces,double[] rawThresholds){}

    public GeologyFieldSampler(List<Holder<GeologyLayer>> layers,int minY,PositionalRandomFactory random){
        this.layers=layers;
        this.minY=minY;
        this.boundaryRootSeed=random.fromHashOf(Craton.rl("geology_boundary")).nextLong();
        double total=0;
        for(Holder<GeologyLayer> layer:layers) total+=Math.max(1,layer.value().thickness());
        // Keep a full profile within the nominal -64..64 overworld section without
        // making layer contacts follow each column's local terrain height.
        this.thicknessScale=total>0?Math.min(1,128.0/total):1;
        cumulativeThickness=new double[layers.size()];
        double boundary=0;
        for(int i=layers.size()-1;i>=0;i--){
            boundary+=thickness(i);
            cumulativeThickness[i]=boundary;
        }
    }

    private Column column(int x,int z){
        if(lastColumn!=null&&lastColumn.x()==x&&lastColumn.z()==z) return lastColumn;
        long key=((long)x<<32)^(z&0xffffffffL);
        Column column=columns.get(key);
        if(column==null){
            double regional=regionalWarp(x,z);
            double[] interfaces=new double[layers.size()],thresholds=new double[layers.size()];
            for(int i=0;i<layers.size();i++){
                double offset=boundaryOffset(layers.get(i).value(),x,z);
                interfaces[i]=minY+cumulativeThickness[i]+offset-regional;
                thresholds[i]=Math.max(1,cumulativeThickness[i]+offset);
            }
            column=new Column(x,z,regional,interfaces,thresholds);
            columns.put(key,column);
        }
        lastColumn=column;
        return column;
    }

    public List<Holder<GeologyLayer>> layers(){return layers;}
    public int minY(){return minY;}
    public double thickness(int index){return Math.max(1,layers.get(index).value().thickness())*thicknessScale;}

    public BlockState sample(int x,int y,int z){
        if(layers.isEmpty()) throw new IllegalArgumentException("Geology profile has no layers");

        // Only the nearest interface is blended. Far from an interface the original stratigraphy is exact.
        Column column=column(x,z);
        int nearest=-1;
        double nearestAbs=Double.MAX_VALUE,nearestDistance=0;
        for(int lowerIndex=1;lowerIndex<layers.size();lowerIndex++){
            double d=y+.5-column.interfaces()[lowerIndex];
            double a=Math.abs(d);
            if(a<nearestAbs){nearestAbs=a;nearest=lowerIndex;nearestDistance=d;}
        }

        if(nearest>=1){
            GeologyLayer lower=layers.get(nearest).value();
            GeologyLayer upper=layers.get(nearest-1).value();
            double width=contactWidth(lower,upper);
            if(nearestAbs<=width){
                double pUpper=GeologicalNoise.smoothstep(-width,width,nearestDistance);
                long seed=boundarySeed(nearest,lower,upper);
                double occupancy=GeologicalNoise.occupancy(x,y,z,seed,.19);
                return occupancy<pUpper?upper.blockState():lower.blockState();
            }
        }
        return sampleRaw(x,y,z);
    }

    /** Raw layer membership without contact mixing, useful for locating a named stratigraphic horizon. */
    public BlockState sampleRaw(int x,int y,int z){
        Column column=column(x,z);
        double depth=y-minY+column.regionalWarp();
        for(int i=layers.size()-1;i>=0;i--){
            if(depth<column.rawThresholds()[i]) return layers.get(i).value().blockState();
        }
        return layers.getFirst().value().blockState();
    }

    /** Top boundary of the requested layer index. Layers are stored top-to-bottom. */
    public double boundaryY(int layerIndex,int x,int z){
        return column(x,z).interfaces()[layerIndex];
    }

    public double[] strike(int layerIndex,int x,int z){
        double gx=boundaryY(layerIndex,x+4,z)-boundaryY(layerIndex,x-4,z);
        double gz=boundaryY(layerIndex,x,z+4)-boundaryY(layerIndex,x,z-4);
        double sx=-gz,sz=gx,length=Math.sqrt(sx*sx+sz*sz);
        return length<1E-6?new double[]{1,0}:new double[]{sx/length,sz/length};
    }

    private double contactWidth(GeologyLayer lower,GeologyLayer upper){
        // 1.8-3.8 blocks. Rougher/high-amplitude contacts get a slightly wider mixed zone.
        double rough=(Math.abs(lower.amplitude())+Math.abs(upper.amplitude()))*.06;
        return GeologicalNoise.clamp(1.8+rough,1.8,3.8);
    }

    private long boundarySeed(int boundaryIndex,GeologyLayer lower,GeologyLayer upper){
        long pair=((long)lower.seed()<<32)^(upper.seed()&0xffffffffL);
        long occurrence=DepositCandidateSampler.mix((boundaryIndex+1L)*0x9E3779B97F4A7C15L);
        return DepositCandidateSampler.mix(boundaryRootSeed^pair^occurrence^0x6A09E667F3BCC909L);
    }

    private double regionalWarp(int x,int z){
        return GeologicalNoise.fbm2(x,z,boundaryRootSeed^0x243F6A8885A308D3L,.0028,3)*23.0;
    }

    private double boundaryOffset(GeologyLayer layer,int x,int z){
        long seed=DepositCandidateSampler.mix(boundaryRootSeed^(layer.seed()*0x9E3779B97F4A7C15L));
        double primary=GeologicalNoise.fbm2(x,z,seed,Math.max(.001,(layer.freqX()+layer.freqZ())*.5),3)*layer.amplitude();
        double secondary=GeologicalNoise.fbm2(x,z,seed^0xBB67AE8584CAA73BL,.025,2)*layer.amplitude()*.18;
        return (primary+secondary)*thicknessScale;
    }
}
