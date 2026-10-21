package com.teamtea.craton.common.worldgen.geology;

import com.teamtea.craton.common.worldgen.noise.GeologicalNoise;

import com.teamtea.craton.Craton;
import com.teamtea.craton.api.geology.GeologyLayer;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import java.util.ArrayList;
import java.util.HashMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.biome.Climate;

import java.util.List;
import java.util.Map;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/** Stratigraphy sampler with a bounded column cache, owned by one chunk generation context. */
public class GeologyFieldSampler {
    private final List<Holder<GeologyLayer>> layers;
    private final int minY;
    private final boolean boundaryOnly;
    private static final int[] EMPTY_ACTIVE=new int[0];
    private static final double[] EMPTY_WIDTHS=new double[0];
    private static final GeologicalNoise.CachedOccupancy[] EMPTY_NOISES=new GeologicalNoise.CachedOccupancy[0];
    private final long boundaryRootSeed;
    private final double thicknessScale;
    private final double[] cumulativeThickness;
    private final GeologyTransition transition;
    private final GeologyFieldSampler[] sources;
    private final GeologyClimateSampler climate;
    private final GeologyRegionSampler regions;
    private final GeologicalNoise.CachedFbm capBlendNoise;
    private final GeologicalNoise.CachedFbm capRockNoise;
    private Long2ObjectOpenHashMap<GeologicalNoise.CachedOccupancy> contactNoise;
    private Column lastColumn,lastCapColumn;
    private BlockState lastCapRock;
    private Long2ObjectLinkedOpenHashMap<Column> columns;

    private static class Column {
        private final int x,z;
        private final double[] interfaces,weights,contactWidths;
        private final double capWidth;
        private final int[] activeLayers;
        private final GeologicalNoise.CachedOccupancy[] contactOccupancies;
        private Map<Block,List<Integer>> eligibleLayers;
        Column(int x,int z,double[] interfaces,double[] weights,double capWidth,int[] activeLayers,
               double[] contactWidths,GeologicalNoise.CachedOccupancy[] contactOccupancies){
            this.x=x;this.z=z;this.interfaces=interfaces;this.weights=weights;this.capWidth=capWidth;
            this.activeLayers=activeLayers;this.contactWidths=contactWidths;this.contactOccupancies=contactOccupancies;
        }
        int x(){return x;} int z(){return z;}
        double[] interfaces(){return interfaces;} double[] weights(){return weights;}
        double capWidth(){return capWidth;} int[] activeLayers(){return activeLayers;}
        double[] contactWidths(){return contactWidths;}
        GeologicalNoise.CachedOccupancy[] contactOccupancies(){return contactOccupancies;}
    }

    public GeologyFieldSampler(List<Holder<GeologyLayer>> layers,int minY,PositionalRandomFactory random){
        this(layers,minY,random,false,null);
    }

    public GeologyFieldSampler(List<Holder<GeologyLayer>> layers,int minY,PositionalRandomFactory random,
                               GeologyClimateSampler climate){
        this(layers,minY,random,false,climate);
    }

    private GeologyFieldSampler(List<Holder<GeologyLayer>> layers,int minY,PositionalRandomFactory random,
                                boolean boundaryOnly,GeologyClimateSampler climate){
        this.boundaryOnly=boundaryOnly;
        this.transition=null;this.sources=null;this.climate=climate;this.regions=null;
        this.layers=List.copyOf(layers);
        this.minY=minY;
        this.boundaryRootSeed=random.fromHashOf(Craton.rl("geology_boundary")).nextLong();
        this.capBlendNoise=new GeologicalNoise.CachedFbm(boundaryRootSeed^0xA54FF53A5F1D36F1L,.11,1);
        this.capRockNoise=new GeologicalNoise.CachedFbm(boundaryRootSeed^0x3C6EF372FE94F82BL,.025,1);
        double total=0;
        for(Holder<GeologyLayer> layer:layers){
            if(!layer.value().hasValidParameters()) throw new IllegalArgumentException("Invalid geology layer parameters");
            total+=Math.max(1,layer.value().thickness());
        }
        if(!Double.isFinite(total)) throw new IllegalArgumentException("Geology profile thickness overflow");
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

    /** All sources use one world-wide slot layout; only continuous thicknesses vary per column. */
    public GeologyFieldSampler(GeologyTransition transition,int minY,PositionalRandomFactory random,
                               GeologyClimateSampler climate){
        this.transition=transition;this.climate=climate;this.boundaryOnly=false;
        this.regions=new GeologyRegionSampler(transition,climate);
        this.layers=transition.layers();this.minY=minY;
        this.boundaryRootSeed=random.fromHashOf(Craton.rl("geology_boundary")).nextLong();
        this.capBlendNoise=new GeologicalNoise.CachedFbm(boundaryRootSeed^0xA54FF53A5F1D36F1L,.11,1);
        this.capRockNoise=new GeologicalNoise.CachedFbm(boundaryRootSeed^0x3C6EF372FE94F82BL,.025,3);
        this.thicknessScale=1;
        sources=new GeologyFieldSampler[transition.profiles().size()];
        cumulativeThickness=new double[layers.size()];
        for(int source=0;source<sources.length;source++){
            sources[source]=new GeologyFieldSampler(transition.profiles().get(source).value().layers(),minY,random,true,null);
            for(int i=0;i<sources[source].layers().size();i++){
                int slot=transition.slot(source,i);
                cumulativeThickness[slot]=Math.max(cumulativeThickness[slot],sources[source].thickness(i));
            }
        }
        double cumulative=0;
        for(int i=layers.size()-1;i>=0;i--){cumulative+=cumulativeThickness[i];cumulativeThickness[i]=cumulative;}
    }

    private Column column(int x,int z){
        if(lastColumn!=null&&lastColumn.x()==x&&lastColumn.z()==z) return lastColumn;
        long key=((long)x<<32)^(z&0xffffffffL);
        if(columns==null)columns=new Long2ObjectLinkedOpenHashMap<>(128);
        Column column=columns.getAndMoveToLast(key);
        if(column==null){
            Climate.TargetPoint point=climate==null?null:climate.sample(x,z);
            double upperScale=point!=null&&minY==-64?climate.upperSectionScale(point):1;
            if(transition!=null){
                double[] weights=regions.weights(x,z);
                double[] interfaces=new double[layers.size()];
                double capThickness=0;
                int activeSources=0;
                for(int source=0;source<sources.length;source++){
                    if(weights[source]<=0) continue;
                    GeologyFieldSampler sampler=sources[source];
                    activeSources++;
                    capThickness+=weights[source]*sampler.layerThickness(0,x,z);
                    for(int i=0;i<sampler.layers().size();i++)
                        interfaces[transition.slot(source,i)]+=weights[source]*sampler.layerThickness(i,x,z);
                }
                double top=minY;
                for(int i=interfaces.length-1;i>=0;i--){top+=interfaces[i];interfaces[i]=stretchUpperBoundary(top,upperScale);}
                column=prepareColumn(x,z,interfaces,weights,activeSources>1?Math.min(4,capThickness*.25):2);
            }else{
                double regional=regionalWarp(x,z);
                double[] interfaces=new double[layers.size()];
                double lowerBoundary=minY;
                // Keep the contacts ordered even when independent noise displaces a thin layer.
                for(int i=layers.size()-1;i>=0;i--){
                    double offset=boundaryOffset(layers.get(i).value(),x,z);
                    double desired=minY+cumulativeThickness[i]+offset-regional;
                    lowerBoundary=Math.max(lowerBoundary+Math.min(1.0,thickness(i)),desired);
                    interfaces[i]=stretchUpperBoundary(lowerBoundary,upperScale);
                }
                column=prepareColumn(x,z,interfaces,null,2);
            }
            columns.putAndMoveToLast(key,column);
            if(columns.size()>1024)columns.removeFirst();
        }
        lastColumn=column;
        return column;
    }

    private static double stretchUpperBoundary(double y,double scale){
        // Regional uplift moves shallow contacts together; it does not multiply bed thickness.
        return y>0?y+(scale-1)*64*GeologicalNoise.smoothstep(0,24,y):y;
    }

    public List<Holder<GeologyLayer>> layers(){return layers;}
    public int minY(){return minY;}
    public double thickness(int index){
        if(transition!=null) return cumulativeThickness[index]-(index+1<layers.size()?cumulativeThickness[index+1]:0);
        return Math.max(1,layers.get(index).value().thickness())*thicknessScale;
    }

    public BlockState sample(int x,int y,int z){
        if(layers.isEmpty()) throw new IllegalArgumentException("Geology profile has no layers");

        // Only the nearest interface is blended. Far from an interface the original stratigraphy is exact.
        Column column=column(x,z);
        if(!layers.isEmpty()&&useCap(column,x,y,z)) return cap(column,x,y,z);
        int nearest=-1;
        double nearestAbs=Double.MAX_VALUE,nearestDistance=0;
        // Descending contacts: only the two neighbors around this height can be nearest.
        int[] active=column.activeLayers();
        int contactCount=Math.max(0,active.length-1);
        int lo=0,hi=contactCount;
        double center=y+.5;
        while(lo<hi){int mid=(lo+hi)>>>1;if(column.interfaces()[active[mid+1]]>center) lo=mid+1;else hi=mid;}
        for(int j=Math.max(0,lo-1);j<=lo&&j<contactCount;j++){
            double d=center-column.interfaces()[active[j+1]],a=Math.abs(d);
            if(a<nearestAbs){nearestAbs=a;nearest=j;nearestDistance=d;}
        }
        if(nearest>=0&&nearestAbs<=column.contactWidths()[nearest]){
            double pUpper=GeologicalNoise.smoothstep(-column.contactWidths()[nearest],column.contactWidths()[nearest],nearestDistance);
            double occupancy=column.contactOccupancies()[nearest].sample(x,y,z);
            return layers.get(occupancy<pUpper?active[nearest]:active[nearest+1]).value().blockState();
        }
        return rawBelowCap(column,y);
    }

    /** Return the exact registered horizon selected by the same contact/cap logic as sample(). */
    public Holder<GeologyLayer> sampleLayer(int x, int y, int z) {
        if (layers.isEmpty()) throw new IllegalArgumentException("Geology profile has no layers");
        Column column = column(x, z);
        if (useCap(column, x, y, z)) return capLayer(column, x, z);
        int[] active = column.activeLayers();
        double center = y + .5;
        int nearest = -1;
        double nearestAbs = Double.MAX_VALUE, nearestDistance = 0;
        int lo = 0, hi = Math.max(0, active.length - 1);
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (column.interfaces()[active[mid + 1]] > center) lo = mid + 1;
            else hi = mid;
        }
        for (int j = Math.max(0, lo - 1); j <= lo && j < active.length - 1; j++) {
            double d = center - column.interfaces()[active[j + 1]];
            if (Math.abs(d) < nearestAbs) {
                nearestAbs = Math.abs(d); nearest = j; nearestDistance = d;
            }
        }
        if (nearest >= 0 && nearestAbs <= column.contactWidths()[nearest]) {
            double pUpper = GeologicalNoise.smoothstep(-column.contactWidths()[nearest],
                    column.contactWidths()[nearest], nearestDistance);
            double occupancy = column.contactOccupancies()[nearest].sample(x, y, z);
            return layers.get(occupancy < pUpper ? active[nearest] : active[nearest + 1]);
        }
        for (int j = active.length - 1; j >= 0; j--) {
            int i = active[j];
            if (center < column.interfaces()[i]) return layers.get(i);
        }
        return active.length == 0 ? layers.get(0) : layers.get(active[0]);
    }

    /** Exact source horizon of the upper cap, including blended geological profiles. */
    private Holder<GeologyLayer> capLayer(Column column, int x, int z) {
        if (transition == null) return layers.get(0);
        for (int source = 0; source < sources.length; source++) {
            if (column.weights()[source] == 1) return sources[source].layers.get(0);
        }
        double pick = GeologicalNoise.clamp(.5 + 2.0 * capRockNoise.sample(x, 0, z), 0, 1);
        double sum = 0;
        Holder<GeologyLayer> fallback = layers.get(0);
        for (int source = 0; source < sources.length; source++) {
            if (column.weights()[source] <= 0) continue;
            fallback = sources[source].layers.get(0);
            sum += column.weights()[source];
            if (pick < sum) return fallback;
        }
        return fallback;
    }

    /** Raw layer membership without contact mixing, useful for locating a named stratigraphic horizon. */
    public BlockState sampleRaw(int x,int y,int z){
        Column column=column(x,z);
        if(!layers.isEmpty()&&useCap(column,x,y,z)) return cap(column,x,y,z);
        return rawBelowCap(column,y);
    }

    private BlockState rawBelowCap(Column column,int y){
        int[] active=column.activeLayers();
        for(int j=active.length-1;j>=0;j--){
            int i=active[j];
            if(y+.5<column.interfaces()[i]) return layers.get(i).value().blockState();
        }
        return active.length==0?Blocks.STONE.defaultBlockState():layers.get(active[0]).value().blockState();
    }

    private Column prepareColumn(int x,int z,double[] interfaces,double[] weights,double capWidth){
        if(boundaryOnly) return new Column(x,z,interfaces,weights,capWidth,EMPTY_ACTIVE,EMPTY_WIDTHS,EMPTY_NOISES);
        int count=0;
        for(int i=0;i<interfaces.length;i++)
            if(interfaces[i]-(i+1<interfaces.length?interfaces[i+1]:minY)>1E-9) count++;
        int[] active=new int[count];
        double[] widths=count<=1?EMPTY_WIDTHS:new double[count-1];
        GeologicalNoise.CachedOccupancy[] occupancies=count<=1?EMPTY_NOISES:new GeologicalNoise.CachedOccupancy[count-1];
        int cursor=0;
        for(int i=0;i<interfaces.length;i++){
            double thickness=interfaces[i]-(i+1<interfaces.length?interfaces[i+1]:minY);
            if(thickness<=1E-9) continue;
            if(cursor>0){
                int upper=active[cursor-1];
                double upperThickness=interfaces[upper]-(upper+1<interfaces.length?interfaces[upper+1]:minY);
                GeologyLayer lowerLayer=layers.get(i).value(),upperLayer=layers.get(upper).value();
                widths[cursor-1]=Math.min(contactWidth(lowerLayer,upperLayer),.45*Math.min(thickness,upperThickness));
                occupancies[cursor-1]=contactNoise(boundarySeed(i,lowerLayer,upperLayer));
            }
            active[cursor++]=i;
        }
        return new Column(x,z,interfaces,weights,capWidth,active,widths,occupancies);
    }

    private GeologicalNoise.CachedOccupancy contactNoise(long seed){
        if(contactNoise==null)contactNoise=new Long2ObjectOpenHashMap<>();
        GeologicalNoise.CachedOccupancy noise=contactNoise.get(seed);
        if(noise==null){noise=new GeologicalNoise.CachedOccupancy(seed,.19);contactNoise.put(seed,noise);}
        return noise;
    }

    /** Top boundary of the requested layer index. Layers are stored top-to-bottom. */
    public double boundaryY(int layerIndex,int x,int z){
        return column(x,z).interfaces()[layerIndex];
    }

    /** Actual local thickness after boundary noise and ordering constraints. */
    public double layerThickness(int layerIndex,int x,int z){
        double[] boundaries=column(x,z).interfaces();
        double bottom=layerIndex+1<boundaries.length?boundaries[layerIndex+1]:minY;
        return boundaries[layerIndex]-bottom;
    }

    /** Find real adjacent horizons, skipping slots which have pinched out here. */
    public int neighborLayerIndex(int layerIndex,int direction,int x,int z){
        if(direction!=-1&&direction!=1) throw new IllegalArgumentException("Layer direction must be -1 or 1");
        for(int i=layerIndex+direction;i>=0&&i<layers.size();i+=direction)
            if(layerThickness(i,x,z)>1E-9) return i;
        return -1;
    }

    /** Anchor each deposit once at its own center, rather than reselecting its horizon per column. */
    public List<Integer> matchingLayersAt(BlockState host,int x,int z){
        Column column=column(x,z);
        if(column.eligibleLayers==null)column.eligibleLayers=new HashMap<>();
        Block block=host.getBlock();
        List<Integer> result=column.eligibleLayers.get(block);
        if(result==null){
            int count=0,first=-1,second=-1;
            for(int i=0;i<layers.size();i++){
                double bottom=i+1<layers.size()?column.interfaces()[i+1]:minY;
                if(layers.get(i).value().blockState().getBlock().equals(block)&&column.interfaces()[i]-bottom>=1){
                    if(count==0)first=i;else if(count==1)second=i;
                    count++;
                }
            }
            if(count==0)result=List.of();
            else if(count==1)result=List.of(first);
            else if(count==2)result=List.of(first,second);
            else{
                Integer[] indices=new Integer[count];int cursor=0;
                for(int i=0;i<layers.size();i++){
                    double bottom=i+1<layers.size()?column.interfaces()[i+1]:minY;
                    if(layers.get(i).value().blockState().getBlock().equals(block)&&column.interfaces()[i]-bottom>=1)indices[cursor++]=i;
                }
                result=List.of(indices);
            }
            column.eligibleLayers.put(block,result);
        }
        return result;
    }

    private boolean useCap(Column column,int x,int y,int z){
        double d=y+.5-column.interfaces()[0],width=column.capWidth();
        if(d>=width) return true;
        if(d<=-width) return false;
        // Fade through a narrow coherent belt instead of creating a flat seam at the reference top.
        double pick=.5+.5*capBlendNoise.sample(x,y,z);
        return pick<GeologicalNoise.smoothstep(-width,width,d);
    }

    private BlockState cap(Column column,int x,int y,int z){
        if(lastCapColumn==column)return lastCapRock;
        lastCapColumn=column;
        return lastCapRock=capRock(column,x,z);
    }

    /** Above the finite profile, continue one shallow rock rather than repeating beds. */
    private BlockState capRock(Column column,int x,int z){
        return capLayer(column, x, z).value().blockState();
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
        long occurrence=GeologicalNoise.mix((boundaryIndex+1L)*0x9E3779B97F4A7C15L);
        return GeologicalNoise.mix(boundaryRootSeed^pair^occurrence^0x6A09E667F3BCC909L);
    }

    private double regionalWarp(int x,int z){
        return GeologicalNoise.fbm2(x,z,boundaryRootSeed^0x243F6A8885A308D3L,.0028,3)*23.0;
    }

    private double boundaryOffset(GeologyLayer layer,int x,int z){
        long seed=GeologicalNoise.mix(boundaryRootSeed^(layer.seed()*0x9E3779B97F4A7C15L));
        double primary=GeologicalNoise.fbm2(x,z,seed,Math.max(.001,(layer.freqX()+layer.freqZ())*.5),3)*layer.amplitude();
        double secondary=GeologicalNoise.fbm2(x,z,seed^0xBB67AE8584CAA73BL,.025,2)*layer.amplitude()*.18;
        return (primary+secondary)*thicknessScale;
    }
}
