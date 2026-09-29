package com.teamtea.craton.common.core;

import com.teamtea.craton.api.geology.GeologyLayer;
import com.teamtea.craton.api.geology.GeologyProfile;
import com.teamtea.craton.common.registry.CratonContents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.IdentityHashMap;

public final class WorldSetter {

    public static final class ChunkContext {
        private final PositionalRandomFactory random;
        private final Map<Holder<GeologyProfile>,GeologyFieldSampler> geology=new IdentityHashMap<>();
        private final DepositFieldEngine.ColumnContext deposits;

        public ChunkContext(PositionalRandomFactory random){
            this.random=random;
            this.deposits=new DepositFieldEngine.ColumnContext(random);
        }

        private GeologyFieldSampler geology(Holder<GeologyProfile> profile,int minY){
            return geology.computeIfAbsent(profile,key -> new GeologyFieldSampler(key.value().layers(),minY,random));
        }
    }

    public static void rebuildCloumnExtension(BlockColumn column,BlockPos.MutableBlockPos pos,int x,int z,
                                              int startingHeight,ChunkAccess chunk,Holder<Biome> biome,
                                              PositionalRandomFactory random){
        rebuildCloumnExtension(column,pos,x,z,startingHeight,chunk,biome,new ChunkContext(random));
    }

    public static void rebuildCloumnExtension(BlockColumn column,BlockPos.MutableBlockPos pos,int x,int z,
                                              int startingHeight,ChunkAccess chunk,Holder<Biome> biome,
                                              ChunkContext context){
        boolean debugmode=false;
        Optional<Holder<GeologyProfile>> optional=CratonContents.getGeologyProfile(biome);
        if(optional.isEmpty()) return;
        List<Holder<GeologyLayer>> layers=optional.get().value().layers();
        if(layers.isEmpty()) return;

        LevelHeightAccessor height=chunk.getHeightAccessorForGeneration();
        int minY=height.getMinY();
        startingHeight=chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG,x,z);
        BlockState surfaceState=column.getBlock(startingHeight);
        int topY=startingHeight-getSurfaceCut(surfaceState,context.random,pos.setY(startingHeight));
       if(debugmode)
        for (int i = topY; i <= startingHeight; i++) {
            column.setBlock(i,Blocks.AIR.defaultBlockState());
        }
        if(topY<=minY) return;

        GeologyFieldSampler geology=context.geology(optional.get(),minY);
        DepositFieldEngine.ColumnContext depositContext=DepositFieldEngine.prepare(
                context.deposits,geology,x,z,minY,topY,surfaceState,biome);

        for(int y=minY;y<=topY;y++){
            BlockState current=chunk.getBlockState(pos.setY(y));
            if(!shouldReplace(current)) {
                if(debugmode)
                column.setBlock(y,Blocks.AIR.defaultBlockState());
                continue;
            }
            BlockState host=geology.sample(x,y,z);
            BlockState state=DepositFieldEngine.apply(depositContext,host,host,y);
            if(debugmode)
                if(state==host||state==current){
                column.setBlock(y,Blocks.AIR.defaultBlockState());
                continue;
            }
            column.setBlock(y,state);
        }
    }

    private static int getSurfaceCut(BlockState state,PositionalRandomFactory random,BlockPos.MutableBlockPos pos){
        if(state.is(BlockTags.DIRT)) return random.at(pos).nextInt(1,2);
        if(state.is(BlockTags.SAND)) return random.at(pos).nextInt(3,5);
        if(state.is(Blocks.GRASS_BLOCK)) return random.at(pos).nextInt(2,3);
        return random.at(pos).nextInt(0,1);
    }

    private static boolean shouldReplace(BlockState state){
        return state.is(BlockTags.BASE_STONE_OVERWORLD)||state.is(BlockTags.DIRT)
                ||state.is(Blocks.GRASS_BLOCK)||state.is(BlockTags.SAND)||state.is(Blocks.GRAVEL);
    }

}
