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
import java.util.function.LongPredicate;

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
        var sampler=randomState.createClimateSampler(SamplerContext.EMPTY_UNCACHED);
        var climate=new GeologyClimateSampler(sampler,settings.seaLevel());
        var resolver=noise.getBiomeSource().createResolver(sampler);
        LongPredicate river=key->resolver.getNoiseBiome(Math.floorDiv((int)(key>>32),4),
                Math.floorDiv(settings.seaLevel(),4),Math.floorDiv((int)key,4)).is(Tags.Biomes.IS_RIVER);
        return placeChunk(chunk,minX,minZ,generation,root,climate,level::getBiome,river)>0;
    }

    /** Operator diagnostics use the same world seed and current chunk, without placing blocks. */
    public static String inspect(WorldGenLevel level,ChunkGenerator generator,BlockPos origin){
        var generation=CratonContents.getGenerationData();
        if(generation.depositPlan().maximumPlacerDepth()<=0)return "Craton placer: status=disabled_or_missing_definition";
        if(!(generator instanceof NoiseBasedChunkGenerator noise))return "Craton placer: status=unsupported_generator";
        var settings=noise.generatorSettings().value();
        var randomState=level.getLevel().getChunkSource().randomState();
        var root=settings.getRandomSource().newInstance(randomState.seed()).forkPositional();
        var sampler=randomState.createClimateSampler(SamplerContext.EMPTY_UNCACHED);
        var climate=new GeologyClimateSampler(sampler,settings.seaLevel());
        var resolver=noise.getBiomeSource().createResolver(sampler);
        LongPredicate river=key->resolver.getNoiseBiome(Math.floorDiv((int)(key>>32),4),
                Math.floorDiv(settings.seaLevel(),4),Math.floorDiv((int)key,4)).is(Tags.Biomes.IS_RIVER);
        return inspectChunk(level.getChunk(origin),Math.floorDiv(origin.getX(),16)*16,Math.floorDiv(origin.getZ(),16)*16,
                generation,root,climate,level::getBiome,river);
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
        return placeChunk(chunk,minX,minZ,generation,root,climate,biomes,null);
    }

    public static int placeChunk(ChunkAccess chunk,int minX,int minZ,CratonContents.GenerationData generation,
                                 PositionalRandomFactory root,GeologyClimateSampler climate,
                                 Function<BlockPos,Holder<Biome>> biomes,LongPredicate riverLookup){
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
            if(deposits==null){
                deposits=new DepositGenerationContext(root,generation.depositPlan());
                deposits.placerRiver(riverLookup);
            }
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

    /** Diagnose the natural-placement funnel; no block writes or candidate rerolls. */
    public static String inspectChunk(ChunkAccess chunk,int minX,int minZ,CratonContents.GenerationData generation,
                                      PositionalRandomFactory root,GeologyClimateSampler climate,
                                      Function<BlockPos,Holder<Biome>> biomes){
        return inspectChunk(chunk,minX,minZ,generation,root,climate,biomes,null);
    }

    public static String inspectChunk(ChunkAccess chunk,int minX,int minZ,CratonContents.GenerationData generation,
                                      PositionalRandomFactory root,GeologyClimateSampler climate,
                                      Function<BlockPos,Holder<Biome>> biomes,LongPredicate riverLookup){
        double depth=generation.depositPlan().maximumPlacerDepth();
        if(depth<=0)return "Craton placer: status=disabled_or_missing_definition";
        var definition=generation.depositPlan().definitions().stream().map(d->d.deposit())
                .filter(d->d instanceof com.teamtea.craton.api.geology.deposit.PlacerDeposit)
                .map(d->(com.teamtea.craton.api.geology.deposit.PlacerDeposit)d).findFirst().orElseThrow();
        int minY=chunk.getHeightAccessorForGeneration().getMinY(),maxY=chunk.getHeightAccessorForGeneration().getMaxY();
        int river=0,sediment=0,profiles=0,candidates=0,sourced=0,gold=0,iron=0,other=0,existingGold=0,existingIron=0;
        double maxGold=0,maxIron=0;
        DepositGenerationContext deposits=null;
        GeologyFieldSampler continuous=null;
        Map<Holder<GeologyProfile>,GeologyFieldSampler> fallback=new IdentityHashMap<>();
        var pos=new BlockPos.MutableBlockPos(minX,minY,minZ);
        for(int z=minZ;z<minZ+16;z++)for(int x=minX;x<minX+16;x++){
            int surface=chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG,x,z);
            if(surface<minY||surface>maxY)continue;
            var biome=biomes.apply(pos.set(x,surface,z));
            if(!biome.is(Tags.Biomes.IS_RIVER))continue;
            river++;
            var surfaceState=chunk.getBlockState(pos);
            int bottom=(int)Math.max(minY+1,Math.ceil(surface-depth));
            boolean eligible=false;
            for(int y=surface;y>=bottom;y--){
                var state=chunk.getBlockState(pos.setY(y));
                if(state.is(Blocks.SAND)||state.is(Blocks.RED_SAND)||state.is(Blocks.GRAVEL))eligible=true;
                String id=state.toString();
                if(id.contains("auriferous"))existingGold++;
                else if(id.contains("ferriferous"))existingIron++;
            }
            if(!eligible)continue;
            sediment++;
            var profile=generation.getGeologyProfile(climate.sample(x,z));
            if(profile.isEmpty()||profile.get().value().layers().isEmpty())continue;
            profiles++;
            GeologyFieldSampler geology;
            if(generation.transition()!=null&&!generation.transition().layers().isEmpty()){
                if(continuous==null)continuous=new GeologyFieldSampler(generation.transition(),minY,root,climate);
                geology=continuous;
            }else{
                geology=fallback.get(profile.get());
                if(geology==null){geology=new GeologyFieldSampler(profile.get().value().layers(),minY,root,climate);fallback.put(profile.get(),geology);}
            }
            if(deposits==null){
                deposits=new DepositGenerationContext(root,generation.depositPlan());
                deposits.placerRiver(riverLookup);
            }
            DepositFieldEngine.prepare(deposits,geology,x,z,minY,surface,surface,surfaceState,biome);
            if(deposits.placer().isEmpty())continue;
            candidates++;
            var source=deposits.sourceStrength(definition);
            maxGold=Math.max(maxGold,source.gold());maxIron=Math.max(maxIron,source.iron());
            if(Math.max(source.gold(),source.iron())>.10)sourced++;
            for(int y=surface;y>=bottom;y--){
                var old=chunk.getBlockState(pos.setY(y));
                if(!old.is(Blocks.SAND)&&!old.is(Blocks.RED_SAND)&&!old.is(Blocks.GRAVEL))continue;
                var result=DepositFieldEngine.applyPlacer(deposits,old,y);
                if(result.equals(old))continue;
                String id=result.toString();
                if(id.contains("auriferous"))gold++;
                else if(id.contains("ferriferous"))iron++;
                else other++;
            }
        }
        String status=gold+iron+other==0&&existingGold+existingIron>0?"already_mineralized":river==0?"no_river":sediment==0?"no_sediment":profiles==0?"no_geology_profile":
                candidates==0?"no_candidates":sourced==0?"no_source":gold+iron+other==0?"no_mineralization":"ready";
        return String.format(java.util.Locale.ROOT,"Craton placer: chunk=%d,%d status=%s river=%d sediment=%d geology=%d candidates=%d source=%d maxGold=%.3f maxIron=%.3f predictedGold=%d predictedIron=%d predictedOther=%d existingGold=%d existingIron=%d",
                Math.floorDiv(minX,16),Math.floorDiv(minZ,16),status,river,sediment,profiles,candidates,sourced,maxGold,maxIron,gold,iron,other,existingGold,existingIron);
    }
}
