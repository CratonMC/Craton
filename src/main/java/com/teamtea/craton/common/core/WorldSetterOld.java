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

public final class WorldSetterOld {

    private static final boolean DEBUG_EXPOSE_GEOLOGY = false;

    private static final double LAYER_THICKNESS = 20.0;

    private WorldSetterOld() {
    }

    public static void extracted(
            BlockState originalState,
            ChunkAccess chunk,
            BlockPos.MutableBlockPos mutablePos
    ) {
        replaceChunkStone(originalState, chunk, mutablePos);
    }

    public static void replaceChunkStone(
            BlockState originalState,
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
        int surfaceCut = DEBUG_EXPOSE_GEOLOGY ? 0 : getSurfaceCut(surfaceState);
        int replaceTopY = surfaceY - surfaceCut;

        if (replaceTopY <= minY) {
            return;
        }

        for (int y = minY; y <= replaceTopY; y++) {
            BlockState currentState = chunk.getBlockState(mutablePos.setY(y));

            if (!shouldReplace(currentState)) {
                continue;
            }

            chunk.setBlockState(
                    mutablePos,
                    getGeologyState(y, minY, surfaceY, x, z)
            );
        }
    }

    private static int getSurfaceCut(BlockState surfaceState) {
        if (surfaceState.is(BlockTags.DIRT)) {
            return 3;
        }

        if (surfaceState.is(BlockTags.SAND)) {
            return 5;
        }

        return 0;
    }

    private static boolean shouldReplace(BlockState state) {
        if (state.is(BlockTags.BASE_STONE_OVERWORLD)) {
            return true;
        }

        // if(true)return true;

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

        if (isPegmatiteVein(worldY, x, z, depth01)) {
            return CratonBlocks.PEGMATITE.getOrigin().getBaseBlock().defaultBlockState();
        }

        double warpedY = getWarpedLayerY(worldY, x, z);
        int layerIndex = Math.floorMod((int) Math.floor(warpedY / LAYER_THICKNESS), 5);

        return switch (layerIndex) {
            case 0 -> CratonBlocks.LIMESTONE.getOrigin().getBaseBlock().defaultBlockState();
            case 1 -> CratonBlocks.MARBLE.getOrigin().getBaseBlock().defaultBlockState();
            case 2 -> CratonBlocks.GNEISS.getOrigin().getBaseBlock().defaultBlockState();
            case 3 -> CratonBlocks.RHYOLITE.getOrigin().getBaseBlock().defaultBlockState();
            default -> CratonBlocks.GABBRO.getOrigin().getBaseBlock().defaultBlockState();
        };
    }

    private static double getDepth01(int worldY, int minY, int surfaceY) {
        return Mth.clamp(
                (double) (worldY - minY) / Math.max(1, surfaceY - minY),
                0.0,
                1.0
        );
    }

    private static double getWarpedLayerY(int worldY, int x, int z) {
        double largeFold =
                Math.sin(x * 0.010 + z * 0.004) * 24.0
                        + Math.cos(x * 0.004 - z * 0.011) * 18.0;

        double mediumFold =
                Math.sin((x + z) * 0.018) * 8.0
                        + Math.cos((x - z) * 0.016) * 6.0;

        double smallRipple =
                Math.sin(x * 0.055 + z * 0.041) * 3.0;

        return worldY + largeFold + mediumFold + smallRipple;
    }

    private static boolean isPegmatiteVein(int worldY, int x, int z, double depth01) {
        if (depth01 < 0.18 || depth01 > 0.88) {
            return false;
        }

        double vein =
                Math.abs(Math.sin(x * 0.075 + worldY * 0.160 + z * 0.045))
                        + Math.abs(Math.cos(x * 0.033 - worldY * 0.120 + z * 0.060)) * 0.35;

        return vein > 1.31;
    }
}