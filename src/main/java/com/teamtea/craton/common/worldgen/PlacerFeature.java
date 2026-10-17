package com.teamtea.craton.common.worldgen;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.api.geology.GeologyProfile;
import com.teamtea.craton.common.registry.CratonContents;
import com.teamtea.craton.common.worldgen.deposit.DepositFieldEngine;
import com.teamtea.craton.common.worldgen.deposit.DepositGenerationContext;
import com.teamtea.craton.common.worldgen.geology.GeologyClimateSampler;
import com.teamtea.craton.common.worldgen.geology.GeologyFieldSampler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.common.Tags;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

/** One invocation per chunk; world-aligned candidates may extend beyond its write bounds. */
public record PlacerFeature() implements Feature {
    public static final MapCodec<PlacerFeature> CODEC=MapCodec.unit(PlacerFeature::new);

    @Override public MapCodec<PlacerFeature> codec(){return CODEC;}
    @Override public java.util.stream.Stream<Holder<Feature>> getSubFeatures(){return java.util.stream.Stream.empty();}

    @Override public boolean place(WorldGenLevel level,ChunkGenerator generator,RandomSource ignored,BlockPos origin){
        var generation=CratonContents.getGenerationData();
        if(generation.depositPlan().maximumPlacerDepth()<=0||!(generator instanceof NoiseBasedChunkGenerator noise))return false;
        int minX=Math.floorDiv(origin.getX(),16)*16,minZ=Math.floorDiv(origin.getZ(),16)*16;
        var chunk=level.getChunk(origin);
        // Avoid climate and candidate contexts in chunks without usable riverbed sediment.
        if(!hasSediment(chunk,minX,minZ,generation.depositPlan().maximumPlacerDepth(),level::getBiome))return false;
        var settings=noise.generatorSettings().value();
        var randomState=level.getLevel().getChunkSource().randomState();
        // Match the surface generator and survey, rather than the decoration RNG supplied to this feature.
        var root=settings.getRandomSource().newInstance(randomState.seed()).forkPositional();
        var climate=new GeologyClimateSampler(randomState.createClimateSampler(SamplerContext.EMPTY_UNCACHED),settings.seaLevel());
        return placeChunk(chunk,minX,minZ,generation,root,climate,level::getBiome)>0;
    }

    static boolean hasSediment(ChunkAccess chunk,int minX,int minZ,double depth,Function<BlockPos,Holder<Biome>> biomes){
        if(depth<=0)return false;
        int minY=chunk.getHeightAccessorForGeneration().getMinY(),maxY=chunk.getHeightAccessorForGeneration().getMaxY();
        var pos=new BlockPos.MutableBlockPos(minX,minY,minZ);
        for(int z=minZ;z<minZ+16;z++)for(int x=minX;x<minX+16;x++){
            int top=chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG,x,z);
            if(top<minY||top>maxY||!biomes.apply(pos.set(x,top,z)).is(Tags.Biomes.IS_RIVER))continue;
            int bottom=(int)Math.max(minY+1,Math.ceil(top-depth));
            for(int y=top;y>=bottom;y--){
                var state=chunk.getBlockState(pos.setY(y));
                if(state.is(Blocks.SAND)||state.is(Blocks.RED_SAND)||state.is(Blocks.GRAVEL))return true;
            }
        }
        return false;
    }

    /** Shared core for feature execution and isolated chunk-boundary checks. */
    public static int placeChunk(ChunkAccess chunk,int minX,int minZ,CratonContents.GenerationData generation,
                                 PositionalRandomFactory root,GeologyClimateSampler climate,
                                 Function<BlockPos,Holder<Biome>> biomes){
        double depth=generation.depositPlan().maximumPlacerDepth();
        if(depth<=0)return 0;
        int minY=chunk.getHeightAccessorForGeneration().getMinY(),maxY=chunk.getHeightAccessorForGeneration().getMaxY();
        DepositGenerationContext deposits=null;
        GeologyFieldSampler continuous=null;
        Map<Holder<GeologyProfile>,GeologyFieldSampler> profiles=null;
        BlockPos.MutableBlockPos pos=new BlockPos.MutableBlockPos(minX,minY,minZ);
        int changed=0;
        for(int z=minZ;z<minZ+16;z++)for(int x=minX;x<minX+16;x++){
            int surface=chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG,x,z);
            if(surface<minY||surface>maxY)continue;
            pos.set(x,surface,z);
            var biome=biomes.apply(pos);
            if(!biome.is(Tags.Biomes.IS_RIVER))continue;
            var surfaceState=chunk.getBlockState(pos);
            boolean sediment=false;
            int bottom=(int)Math.max(minY+1,Math.ceil(surface-depth));
            for(int y=surface;y>=bottom;y--){
                var state=chunk.getBlockState(pos.setY(y));
                if(state.is(Blocks.SAND)||state.is(Blocks.RED_SAND)||state.is(Blocks.GRAVEL)){sediment=true;break;}
            }
            if(!sediment)continue;
            var profile=generation.getGeologyProfile(climate.sample(x,z));
            if(profile.isEmpty()||profile.get().value().layers().isEmpty())continue;
            GeologyFieldSampler geology;
            if(generation.transition()!=null&&!generation.transition().layers().isEmpty()){
                if(continuous==null)continuous=new GeologyFieldSampler(generation.transition(),minY,root,climate);
                geology=continuous;
            }else{
                if(profiles==null)profiles=new IdentityHashMap<>();
                geology=profiles.get(profile.get());
                if(geology==null){
                    geology=new GeologyFieldSampler(profile.get().value().layers(),minY,root,climate);
                    profiles.put(profile.get(),geology);
                }
            }
            if(deposits==null)deposits=new DepositGenerationContext(root,generation.depositPlan());
            DepositFieldEngine.prepare(deposits,geology,x,z,minY,surface,surface,surfaceState,biome);
            if(deposits.placer().isEmpty())continue;
            for(int y=surface;y>=bottom;y--){
                var old=chunk.getBlockState(pos.setY(y));
                if(!old.is(Blocks.SAND)&&!old.is(Blocks.RED_SAND)&&!old.is(Blocks.GRAVEL))continue;
                var state=DepositFieldEngine.applyPlacer(deposits,old,y);
                if(!state.equals(old)){
                    chunk.setBlockState(pos,state,Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS);
                    changed++;
                }
            }
        }
        return changed;
    }
}
