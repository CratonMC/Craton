package com.teamtea.craton.common.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

public class WorldTester {

    public static void extracted(BlockState blockState, ChunkAccess val$chunk, BlockPos.MutableBlockPos val$blockpos$mutableblockpos) {
        LevelHeightAccessor levelheightaccessor = val$chunk.getHeightAccessorForGeneration();
        int minBuildHeight = levelheightaccessor.getMinY();
        int height = val$chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, val$blockpos$mutableblockpos.getX(), val$blockpos$mutableblockpos.getZ());
        BlockState blockState1 = val$chunk.getBlockState(val$blockpos$mutableblockpos.setY(height));
        int cut = blockState1.is(BlockTags.DIRT) ? 3 : blockState1.is(BlockTags.SAND) ? 5 : 0;
        for (int i = minBuildHeight; i <= height - cut && height < levelheightaccessor.getMaxY(); i++) {
            val$chunk.setBlockState(val$blockpos$mutableblockpos.setY(i),
                    WorldTester.esdsa$getP190009(i - minBuildHeight, blockState, height - minBuildHeight, val$blockpos$mutableblockpos));
        }
    }

    public static BlockState esdsa$getP190009(int y, BlockState p_190009_, int yMax, BlockPos blockpos) {
        double noise = Math.sin(blockpos.getX() * 0.1) * 0.33 + Math.cos(blockpos.getZ() * 0.1) * 0.33 + Math.sin(blockpos.getZ() * 0.2) * 0.33;

        // **保持 noise 允许负值，平滑调整**
        noise = Mth.clamp(noise, -0.5, 0.5);

        // **动态调整高度**
        int heightRange = y + (int) (noise * 10);  // 让 noise 只影响 `-2 ~ 2`，减少剧烈波动

        // **按照整数区间进行分层**
        // if (y == yMax) return Blocks.GRASS_BLOCK.defaultBlockState();
        // if (y >= yMax - 2) return Blocks.DIRT.defaultBlockState();
        // if (y >= yMax+ (int) (noise * 10) - 2) return null;
        heightRange = heightRange - 64;
        // if (heightRange > 120)
        //     return BuiltInRegistries.BLOCK.get(Identifier.parse("create:limestone")).defaultBlockState();
        // if (heightRange > 105)
        //     return BuiltInRegistries.BLOCK.get(Identifier.parse("create:scoria")).defaultBlockState();
        // if (heightRange > 90)
        //     return BuiltInRegistries.BLOCK.get(Identifier.parse("create:scorchia")).defaultBlockState();
        // if (heightRange > 75)
        //     return BuiltInRegistries.BLOCK.get(Identifier.parse("create:veridium")).defaultBlockState();
        // if (heightRange > 60)
        //     return BuiltInRegistries.BLOCK.get(Identifier.parse("create:ochrum")).defaultBlockState();
        // if (heightRange > 45)
        //     return BuiltInRegistries.BLOCK.get(Identifier.parse("create:crimsite")).defaultBlockState();
        // if (heightRange >= -100)
        //     return BuiltInRegistries.BLOCK.get(Identifier.parse("create:asurine")).defaultBlockState();

        if (heightRange > 192) return Blocks.LIME_TERRACOTTA.defaultBlockState();
        if (heightRange > 160) return Blocks.GREEN_TERRACOTTA.defaultBlockState();
        if (heightRange > 128) return Blocks.RED_TERRACOTTA.defaultBlockState();
        if (heightRange > 96) return Blocks.BLUE_TERRACOTTA.defaultBlockState();
        if (heightRange > 64) return Blocks.BROWN_TERRACOTTA.defaultBlockState();
        if (heightRange > 32) return Blocks.PINK_TERRACOTTA.defaultBlockState();
        if (heightRange >= 0) return Blocks.WHITE_TERRACOTTA.defaultBlockState();
        return Blocks.GRASS_BLOCK.defaultBlockState();
    }
}
