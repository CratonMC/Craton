package com.teamtea.craton.common.core;

import com.teamtea.craton.common.registry.CratonBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.fml.loading.FMLEnvironment;

public final class WorldSetterOldv2 {

    private static final boolean DEBUG_EXPOSE_GEOLOGY = false;

    private static final double BASE_LAYER_THICKNESS = 24.0;

    private WorldSetterOldv2() {
    }

    public static void extracted(
            BlockState originalState,
            ChunkAccess chunk,
            BlockPos.MutableBlockPos mutablePos
    ) {
        replaceChunkStone(chunk, mutablePos);
    }

    public static void replaceChunkStone(
            ChunkAccess chunk,
            BlockPos.MutableBlockPos mutablePos
    ) {
        if (FMLEnvironment.isProduction()) {
            return;
        }

        LevelHeightAccessor heightAccessor = chunk.getHeightAccessorForGeneration();
        int minY = heightAccessor.getMinY();
        int maxY = heightAccessor.getMaxY();

        int x = mutablePos.getX();
        int z = mutablePos.getZ();

        int surfaceY = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
        if (surfaceY <= minY || surfaceY >= maxY) {
            return;
        }

        BlockState surfaceState = chunk.getBlockState(mutablePos.setY(surfaceY));
        int cut = DEBUG_EXPOSE_GEOLOGY ? 0 : getSurfaceCut(surfaceState);
        int topY = surfaceY - cut;

        for (int y = minY; y <= topY; y++) {
            BlockState current = chunk.getBlockState(mutablePos.setY(y));

            if (!shouldReplace(current)) {
                continue;
            }

            chunk.setBlockState(
                    mutablePos,
                    getGeologyState(y, minY, surfaceY, x, z)
            );
        }
    }

    private static int getSurfaceCut(BlockState state) {
        if (state.is(BlockTags.DIRT)) {
            return 3;
        }

        if (state.is(BlockTags.SAND)) {
            return 5;
        }

        return 0;
    }

    private static boolean shouldReplace(BlockState state) {
        if (state.is(BlockTags.BASE_STONE_OVERWORLD)) {
            return true;
        }

        if (!DEBUG_EXPOSE_GEOLOGY) {
            return false;
        }

        return state.is(BlockTags.DIRT) || state.is(BlockTags.SAND);
    }

    private static BlockState getGeologyState(
            int worldY,
            int minY,
            int surfaceY,
            int x,
            int z
    ) {
        double depth01 = getDepth01(worldY, minY, surfaceY);

        /*
         * 暂时保留接口。
         * 以后伟晶岩可以放在这里，作为切穿地层的脉体或晶洞。
         */
        // if (isPegmatiteVein(worldY, x, z, depth01)) {
        //     return CratonBlocks.PEGMATITE.getOrigin().getBaseBlock().defaultBlockState();
        // }

        double y = worldY - minY;

        /*
         * 每个边界都有自己的“种子/形态场”。
         * 这样层面不会完全平行，不会像彩虹蛋糕。
         */
        double b1 = 1.0 * BASE_LAYER_THICKNESS + boundaryOffset(x, z, 101, 10.0, 0.010, 0.006);
        double b2 = 2.0 * BASE_LAYER_THICKNESS + boundaryOffset(x, z, 202, 16.0, 0.008, 0.011);
        double b3 = 3.0 * BASE_LAYER_THICKNESS + boundaryOffset(x, z, 303, 22.0, 0.006, 0.014);
        double b4 = 4.0 * BASE_LAYER_THICKNESS + boundaryOffset(x, z, 404, 14.0, 0.012, 0.007);

        /*
         * 整体抬升/沉降。
         * 所有层共享一个大尺度构造趋势，但每个层界仍然有自己的局部变化。
         */
        double regionalWarp = regionalWarp(x, z);
        y += regionalWarp;

        /*
         * 避免边界交叉太严重。
         */
        b2 = Math.max(b2, b1 + 8.0);
        b3 = Math.max(b3, b2 + 8.0);
        b4 = Math.max(b4, b3 + 8.0);

        double cycle = 5.0 * BASE_LAYER_THICKNESS;
        double yy = positiveModulo(y, cycle);

        if (yy < b1) {
            return CratonBlocks.GABBRO.getOrigin().getBaseBlock().defaultBlockState();
        }

        if (yy < b2) {
            return CratonBlocks.GNEISS.getOrigin().getBaseBlock().defaultBlockState();
        }

        if (yy < b3) {
            return CratonBlocks.LIMESTONE.getOrigin().getBaseBlock().defaultBlockState();
        }

        if (yy < b4) {
            return CratonBlocks.MARBLE.getOrigin().getBaseBlock().defaultBlockState();
        }

        return CratonBlocks.RHYOLITE.getOrigin().getBaseBlock().defaultBlockState();
    }

    private static double getDepth01(int worldY, int minY, int surfaceY) {
        return Mth.clamp(
                (double) (worldY - minY) / Math.max(1, surfaceY - minY),
                0.0,
                1.0
        );
    }

    private static double regionalWarp(int x, int z) {
        return Math.sin(x * 0.003 + z * 0.001) * 18.0
                + Math.cos(x * 0.002 - z * 0.004) * 14.0;
    }

    private static double boundaryOffset(
            int x,
            int z,
            int seed,
            double amplitude,
            double freqX,
            double freqZ
    ) {
        double sx = x + seed * 37.17;
        double sz = z - seed * 19.31;

        double large = Math.sin(sx * freqX + sz * freqZ) * amplitude;
        double medium = Math.cos(sx * freqZ * 1.7 - sz * freqX * 1.3) * amplitude * 0.45;
        double small = Math.sin((sx + sz) * 0.035) * amplitude * 0.18;

        return large + medium + small;
    }

    private static double positiveModulo(double value, double mod) {
        double result = value % mod;
        return result < 0.0 ? result + mod : result;
    }
}