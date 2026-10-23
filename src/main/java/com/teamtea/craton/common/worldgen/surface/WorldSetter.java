package com.teamtea.craton.common.worldgen.surface;

import com.teamtea.craton.common.worldgen.deposit.DepositFieldEngine;
import com.teamtea.craton.common.worldgen.deposit.DepositGenerationContext;
import com.teamtea.craton.common.worldgen.geology.GeologyClimateSampler;
import com.teamtea.craton.common.worldgen.geology.GeologyFieldSampler;
import com.teamtea.craton.common.worldgen.geology.GeologyTransition;

import com.teamtea.craton.api.geology.GeologyLayer;
import com.teamtea.craton.api.geology.deposit.PeatDeposit;
import com.teamtea.craton.api.geology.GeologyProfile;
import com.teamtea.craton.common.registry.CratonBlocks;
import com.teamtea.craton.common.registry.CratonContents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.neoforged.neoforge.common.Tags;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.IdentityHashMap;

public class WorldSetter {
    private static final boolean DEBUG_DEPOSITS=Boolean.getBoolean("craton.debugDeposits");

    /** One buildSurface call owns this context for all columns; do not share it across generation threads. */
    public static class ChunkContext {
        private final PositionalRandomFactory random;
        private final GeologyClimateSampler climate;
        private final CratonContents.GenerationData generation;
        private final SeabedSediments sediments;
        private final SurfaceSedimentEngine surfaceSedimentEngine;
        private final ColumnSurface[] surfaces=new ColumnSurface[256];
        private int capturedColumns;
        private GeologyFieldSampler continuousGeology;
        private final Map<Holder<GeologyProfile>,GeologyFieldSampler> geology=new IdentityHashMap<>();
        private DepositGenerationContext deposits;
        private final Map<BlockState,Integer> materialFlags=new IdentityHashMap<>();
        private BlockState lastMaterial;
        private int lastMaterialFlags;

        /** Tags are stable during this chunk's generation; the cache expires with the context. */
        private int materialFlags(BlockState state){
            if(state==lastMaterial)return lastMaterialFlags;
            Integer cached=materialFlags.get(state);
            int flags;
            if(cached!=null)flags=cached;
            else{
                boolean base=state.is(BlockTags.BASE_STONE_OVERWORLD);
                boolean replace=base||state.is(BlockTags.DIRT)||state.is(Blocks.GRASS_BLOCK)
                        ||state.is(BlockTags.SAND)||state.is(Blocks.GRAVEL)||state.is(Blocks.BEDROCK)
                        ||state.is(Blocks.SANDSTONE)||state.is(Blocks.RED_SANDSTONE);
                flags=(replace?1:0)|(base?2:0);
                materialFlags.put(state,flags);
            }
            lastMaterial=state;lastMaterialFlags=flags;
            return flags;
        }
        private final BlockPos.MutableBlockPos surfacePosition=new BlockPos.MutableBlockPos(0,0,0);

        public ChunkContext(PositionalRandomFactory random){this(random,null);}

        public ChunkContext(PositionalRandomFactory random,GeologyClimateSampler climate){
            this(random, climate, null);
        }

        public ChunkContext(PositionalRandomFactory random,GeologyClimateSampler climate,BiomeManager biomeManager){
            this.random=random;this.climate=climate;
            this.generation=CratonContents.getGenerationData();
            this.surfaceSedimentEngine=new SurfaceSedimentEngine(random);
            this.sediments=new SeabedSediments(climate,random.fromHashOf(com.teamtea.craton.Craton.rl("seabed_sediments")).nextLong());
        }

        private record ColumnSurface(int x,int z,Holder<Biome> biome) {}

        public void captureColumn(int x,int z,Holder<Biome> biome){
            if(biome.is(BiomeTags.IS_NETHER)||biome.is(BiomeTags.IS_END)) return;
            int index=(Math.floorMod(z,16)<<4)|Math.floorMod(x,16);
            if(surfaces[index]==null) capturedColumns++;
            surfaces[index]=new ColumnSurface(x,z,biome);
        }

        /** Runs after vanilla surface rules, while this chunk is still being generated. */
        public void finishSurface(ChunkAccess chunk,int seaLevel){
            if(capturedColumns==0) return;
            if(climate==null) throw new IllegalStateException("Surface finishing requires a climate sampler");
            int minY=chunk.getHeightAccessorForGeneration().getMinY();
            int[] tops=new int[256];
            int[] waterSurfaces=new int[256];
            // Snapshot the real floor before any writes; do not query/generate adjacent chunks.
            BlockPos.MutableBlockPos heightScan = new BlockPos.MutableBlockPos();
            for(int i=0;i<surfaces.length;i++){
                ColumnSurface surface=surfaces[i];
                if (surface == null) {
                    tops[i] = Integer.MIN_VALUE;
                    waterSurfaces[i] = Integer.MIN_VALUE;
                } else {
                    // Chunk heightmaps may expose the first free Y. Resolve the actual
                    // occupied position explicitly instead of assuming an off-by-one.
                    int floorHeight = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, surface.x(), surface.z());
                    int surfaceHeight = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, surface.x(), surface.z());
                    tops[i] = occupiedY(chunk, heightScan, surface.x(), surface.z(), floorHeight, true);
                    waterSurfaces[i] = occupiedY(chunk, heightScan, surface.x(), surface.z(), surfaceHeight, false);
                }
            }
            // Resolve actual submerged columns before any geology/sediment writes.
            // Only chunk-owned coordinates are queried; neighboring chunks are never loaded.
            boolean[] submergedColumns = new boolean[256];
            BlockPos.MutableBlockPos scan = new BlockPos.MutableBlockPos();
            BlockColumn scanColumn = new BlockColumn() {
                @Override public BlockState getBlock(int y) { return chunk.getBlockState(scan.setY(y)); }
                @Override public void setBlock(int y, BlockState state) { throw new UnsupportedOperationException(); }
            };
            for (int i = 0; i < surfaces.length; i++) {
                ColumnSurface surface = surfaces[i];
                if (surface == null || tops[i] < minY) continue;
                scan.set(surface.x(), minY, surface.z());
                submergedColumns[i] = SurfaceSedimentEngine.classifyWater(scanColumn, tops[i],
                        waterSurfaces[i], seaLevel, minY, surface.biome(), climate.sample(surface.x(), surface.z())).submerged();
            }
            BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos();
            BlockColumn column = new BlockColumn() {
                @Override public BlockState getBlock(int y) { return chunk.getBlockState(position.setY(y)); }
                @Override public void setBlock(int y, BlockState state) {
                    chunk.setBlockState(position.setY(y), state,
                            Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS);
                }
            };
            for(int index=0;index<surfaces.length;index++){
                ColumnSurface surface=surfaces[index];
                if(surface==null) continue;
                int x=surface.x(),z=surface.z();
                position.set(x,minY,z);
                Climate.TargetPoint point=climate.sample(x,z);
                int top=tops[index];
                if(top<minY||top>chunk.getHeightAccessorForGeneration().getMaxY()) continue;
                BlockState material=column.getBlock(top);
                SurfaceSedimentEngine.WaterColumn water = SurfaceSedimentEngine.classifyWater(
                        column, top, waterSurfaces[index], seaLevel, minY, surface.biome(), point);
                SeabedSediments.Cover cover=null;
                // Do not run the legacy seabed painter over datapack-defined sediment environments.
                boolean customSediment = false;
                // An environment match alone is insufficient: geology and climate may reject
                // every datapack rule, in which case the legacy seabed painter must run.
                if (!DEBUG_DEPOSITS && top >= minY && top < seaLevel
                        && water.submerged()) {
                    var profile = generation.getGeologyProfile(point);
                    if (profile.isPresent()) {
                        customSediment = surfaceSedimentEngine.appliesAt(column, x, z, top,
                                seaLevel, minY, surface.biome(), point, geology(profile.get(), minY), water);
                    }
                }
                if(!customSediment && (surface.biome().is(Tags.Biomes.IS_OCEAN) ||
                        Climate.unquantizeCoord(point.continentalness()) < -0.19) && top>=minY&&top<seaLevel
                        &&(material.is(BlockTags.SAND)||material.is(Blocks.GRAVEL))
                        &&(surface.biome().is(Tags.Biomes.IS_OCEAN)
                        ||top<chunk.getHeightAccessorForGeneration().getMaxY()&&column.getBlock(top+1).is(Blocks.WATER))){
                    int lx=Math.floorMod(x,16),lz=Math.floorMod(z,16);
                    int west=Math.max(0,lx-4),east=Math.min(15,lx+4),north=Math.max(0,lz-4),south=Math.min(15,lz+4);
                    int hW=validHeight(tops[(lz<<4)|west],top),hE=validHeight(tops[(lz<<4)|east],top);
                    int hN=validHeight(tops[(north<<4)|lx],top),hS=validHeight(tops[(south<<4)|lx],top);
                    double slope=Math.hypot((hE-(double)hW)/(east-west),(hS-(double)hN)/(south-north));
                    double basin=(hW+(double)hE+hN+hS)*.25-top;
                    cover=sediments.cover(material,x,z,top,slope,basin);
                }
                rebuildColumnExtension(column,position,x,z,top,chunk,point,surface.biome(),this,cover);
                if(!DEBUG_DEPOSITS && customSediment) {
                    var profile = generation.getGeologyProfile(point);
                    if(profile.isPresent()) surfaceSedimentEngine.apply(column,x,z,top,seaLevel,
                            minY,surface.biome(),point,geology(profile.get(),minY),water);
                }
                // Exposed shoreline: reuse the actual adjacent submerged sediment
                // profile. No independent dry-bank material/temperature rule.
                if (!DEBUG_DEPOSITS && !water.submerged() && top >= seaLevel - 1 && top <= seaLevel + 2) {
                    com.teamtea.craton.api.geology.surface.SurfaceSediment bankSource = findBankSource(
                            chunk, x, z, top, seaLevel, minY, surfaces, tops,
                            waterSurfaces, submergedColumns, scan, scanColumn);
                    if (bankSource != null) {
                        surfaceSedimentEngine.applyBank(column, x, z, top, minY, bankSource);
                    }
                }
            }
        }

        /** Pick the closest verified submerged column, using its own climate and
         *  geology. Read only current-chunk snapshots to avoid generation recursion.
         */
        private com.teamtea.craton.api.geology.surface.SurfaceSediment findBankSource(
                ChunkAccess chunk, int x, int z, int bankY, int seaLevel, int minY,
                ColumnSurface[] surfaces, int[] tops, int[] waterSurfaces, boolean[] submerged,
                BlockPos.MutableBlockPos scan, BlockColumn sourceColumn) {
            int lx = Math.floorMod(x, 16), lz = Math.floorMod(z, 16);
            for (int distance = 1; distance <= 2; distance++) {
                for (int dz = -distance; dz <= distance; dz++) {
                    int dxAbs = distance - Math.abs(dz);
                    for (int sign = -1; sign <= 1; sign += 2) {
                        if (dxAbs == 0 && sign == 1) continue;
                        int nx = lx + sign * dxAbs, nz = lz + dz;
                        if (nx < 0 || nx >= 16 || nz < 0 || nz >= 16) continue;
                        int i = (nz << 4) | nx;
                        ColumnSurface neighbor = surfaces[i];
                        if (neighbor == null || !submerged[i] || Math.abs(tops[i] - bankY) > 3) continue;
                        int neighborY = tops[i];
                        Climate.TargetPoint point = climate.sample(neighbor.x(), neighbor.z());
                        var profile = generation.getGeologyProfile(point);
                        if (profile.isEmpty()) continue;
                        scan.set(neighbor.x(), minY, neighbor.z());
                        var water = new SurfaceSedimentEngine.WaterColumn(neighborY, waterSurfaces[i], true);
                        var selected = surfaceSedimentEngine.selectSubmergedRule(sourceColumn,
                                neighbor.x(), neighbor.z(), neighborY, neighbor.biome(), point,
                                geology(profile.get(), minY), water, minY);
                        if (selected != null) return selected;
                    }
                }
            }
            return null;
        }

        private static int occupiedY(ChunkAccess chunk, BlockPos.MutableBlockPos scan,
                                     int x, int z, int height, boolean solidFloor) {
            int minY = chunk.getHeightAccessorForGeneration().getMinY();
            int maxY = chunk.getHeightAccessorForGeneration().getMaxY();
            int candidate = Math.min(height, maxY);
            if (candidate > minY) {
                BlockState state = chunk.getBlockState(scan.set(x, candidate, z));
                if (state.isAir() || (solidFloor && !state.getFluidState().isEmpty())) {
                    candidate--;
                }
            }
            return candidate;
        }

        private static int validHeight(int neighbor,int fallback){return neighbor==Integer.MIN_VALUE?fallback:neighbor;}

        private DepositGenerationContext deposits(){
            if(deposits==null) deposits=new DepositGenerationContext(random,generation.depositPlan());
            return deposits;
        }

        private GeologyFieldSampler geology(Holder<GeologyProfile> profile,int minY){
            if(climate!=null){
                if(continuousGeology==null){
                    GeologyTransition transition=generation.transition();
                    if(transition!=null&&!transition.layers().isEmpty())
                        continuousGeology=new GeologyFieldSampler(transition,minY,random,climate);
                }
                if(continuousGeology!=null) return continuousGeology;
            }
            return geology.computeIfAbsent(profile,key -> new GeologyFieldSampler(key.value().layers(),minY,random,climate));
        }
    }

    /** @deprecated Compatibility alias; use rebuildColumnExtension for new calls. */
    @Deprecated
    public static void rebuildCloumnExtension(BlockColumn column,BlockPos.MutableBlockPos pos,int x,int z,
                                              int startingHeight,ChunkAccess chunk,Climate.TargetPoint climate,Holder<Biome> biome,
                                              PositionalRandomFactory random){
        rebuildColumnExtension(column,pos,x,z,startingHeight,chunk,climate,biome,random);
    }

    /** @deprecated Compatibility alias; use rebuildColumnExtension for new calls. */
    @Deprecated
    public static void rebuildCloumnExtension(BlockColumn column,BlockPos.MutableBlockPos pos,int x,int z,
                                              int startingHeight,ChunkAccess chunk,Climate.TargetPoint climate,Holder<Biome> biome,
                                              ChunkContext context){
        rebuildColumnExtension(column,pos,x,z,startingHeight,chunk,climate,biome,context,null);
    }

    public static void rebuildColumnExtension(BlockColumn column,BlockPos.MutableBlockPos pos,int x,int z,
                                              int startingHeight,ChunkAccess chunk,Climate.TargetPoint climate,Holder<Biome> biome,
                                              PositionalRandomFactory random){
        rebuildColumnExtension(column,pos,x,z,startingHeight,chunk,climate,biome,new ChunkContext(random));
    }

    public static void rebuildColumnExtension(BlockColumn column,BlockPos.MutableBlockPos pos,int x,int z,
                                              int startingHeight,ChunkAccess chunk,Climate.TargetPoint climate,Holder<Biome> biome,
                                              ChunkContext context){
        rebuildColumnExtension(column,pos,x,z,startingHeight,chunk,climate,biome,context,null);
    }

    private static void rebuildColumnExtension(BlockColumn column,BlockPos.MutableBlockPos pos,int x,int z,
                                              int startingHeight,ChunkAccess chunk,Climate.TargetPoint climate,Holder<Biome> biome,
                                              ChunkContext context,SeabedSediments.Cover cover){
        // 1. Resolve geology and replaceable heights; surfaceY and underground topY have different meanings.
        boolean debugmode=DEBUG_DEPOSITS;
        if(biome.is(BiomeTags.IS_NETHER)||biome.is(BiomeTags.IS_END)) return;
        Optional<Holder<GeologyProfile>> optional=context.generation.getGeologyProfile(climate);
        if(optional.isEmpty()) return;
        List<Holder<GeologyLayer>> layers=optional.get().value().layers();
        if(layers.isEmpty()) return;

        LevelHeightAccessor height=chunk.getHeightAccessorForGeneration();
        int minY=height.getMinY();
        // Respect the caller's heightmap snapshot: seabed and river profiles are
        // selected against the same physical floor, not a post-write heightmap.
        if(startingHeight<minY||startingHeight>height.getMaxY()) return;
        BlockState surfaceState=column.getBlock(startingHeight);
        // Do not mutate the BlockPos local owned by vanilla's surface builder.
        int topY=startingHeight-getSurfaceCut(surfaceState,context.random,context.surfacePosition.set(x,startingHeight,z));
        topY=Math.max(minY,topY);

        GeologyFieldSampler geology=null;
        DepositGenerationContext depositContext=null;

        // 2. Traverse blocks and prepare geology and deposits only when first needed.
        boolean river=biome.is(Tags.Biomes.IS_RIVER);
        double placerDepth=context.generation.depositPlan().maximumPlacerDepth();
        double peatDepth=context.generation.depositPlan().maximumPeatDepth();
        boolean wetland=peatDepth>0&&biome.is(Tags.Biomes.IS_SWAMP)&&!biome.is(Tags.Biomes.IS_OCEAN);
        for(int y=minY;y<=startingHeight;y++){
            BlockState current=column.getBlock(y);
            if(y==minY){
                if(!current.is(Blocks.BEDROCK)) column.setBlock(y,Blocks.BEDROCK.defaultBlockState());
                continue;
            }
            boolean peatSoil=wetland&&PeatDeposit.soil(current)&&startingHeight-y<=peatDepth;
            int material=context.materialFlags(current);
            if((material&1)==0&&!peatSoil) {
                if(debugmode) column.setBlock(y,Blocks.AIR.defaultBlockState());
                continue;
            }
            // Surface depth protects loose cover, not the vanilla natural stone beneath it.
            boolean rebuildRock=y<=topY||(material&2)!=0||current.is(Blocks.BEDROCK)
                    ||current.is(Blocks.SANDSTONE)||current.is(Blocks.RED_SANDSTONE)||cover!=null;
            boolean riverMaterial=river&&(current.is(Blocks.SAND)||current.is(Blocks.RED_SAND)||current.is(Blocks.GRAVEL));
            if(depositContext==null&&(rebuildRock||peatSoil)){
                geology=context.geology(optional.get(),minY);
                depositContext=DepositFieldEngine.prepareUnderground(context.deposits(),geology,x,z,minY,topY,startingHeight,surfaceState,biome);
            }
            // Keep loose riverbed sediment for the later placer feature.
            boolean riverSediment=riverMaterial&&startingHeight-y<=placerDepth;
            BlockState host=current,state=current;
            if(cover!=null&&startingHeight-y<cover.thickness()){
                state=cover.material();
                if(!state.equals(current)) column.setBlock(y,state);
                continue;
            }
            // 3. Apply underground strata while preserving riverbed sediment.
            if(rebuildRock&&!riverSediment&&!peatSoil){
                host=geology.sample(x,y,z);
                state=DepositFieldEngine.apply(depositContext,host,host,y);
            }
            if(peatSoil) state=DepositFieldEngine.applyPeat(depositContext,current,y);
            // 4. Apply debug output or write changed blocks only.
            if(debugmode){
                if(state.equals(host)||state.equals(current)||state.is(Blocks.DIORITE)
                        ||state.is(Blocks.GRANITE)||state.is(CratonBlocks.GABBRO.getOrigin().getBaseBlock())){
                    column.setBlock(y,Blocks.AIR.defaultBlockState());
                    continue;
                }
            }else if(state.equals(current)) continue;
            column.setBlock(y,state);
        }
    }

    private static int getSurfaceCut(BlockState state,PositionalRandomFactory random,BlockPos pos){
        if(state.is(Blocks.GRASS_BLOCK)) return 2;
        if(state.is(BlockTags.DIRT)) return random.at(pos).nextInt(1,2);
        if(state.is(BlockTags.SAND)) return random.at(pos).nextInt(3,5);
        return random.at(pos).nextInt(0,1);
    }

}
