package com.teamtea.craton.common.worldgen;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.registry.CratonBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.common.Tags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

import java.util.function.Function;

/** Badlands surface placer: independent of underground ore bodies and river PlacerFeature. */
public record BadlandsRedSandPlacerFeature() implements Feature {
    public static final MapCodec<BadlandsRedSandPlacerFeature> CODEC =
            MapCodec.unit(BadlandsRedSandPlacerFeature::new);
    private static final int CELL_SIZE = 8;
    private static final long SALT = 0x6C2D819A4F17B5E3L;

    @Override public MapCodec<BadlandsRedSandPlacerFeature> codec() { return CODEC; }
    @Override public java.util.stream.Stream<Holder<Feature>> getSubFeatures() { return java.util.stream.Stream.empty(); }

    @Override public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource ignored, BlockPos origin) {
        // One pass per chunk; all writes are constrained to this chunk, like PlacerFeature.
        var chunk = level.getChunk(origin);
        int minX = Math.floorDiv(origin.getX(), 16) * 16;
        int minZ = Math.floorDiv(origin.getZ(), 16) * 16;
        long seed = level.getLevel().getChunkSource().randomState().seed();
        return placeChunk(chunk, minX, minZ, seed, level::getBiome) > 0;
    }

    public static int placeChunk(ChunkAccess chunk, int minX, int minZ, long seed,
                                 Function<BlockPos, Holder<Biome>> biomes) {
        int minY = chunk.getHeightAccessorForGeneration().getMinY();
        int maxY = chunk.getHeightAccessorForGeneration().getMaxY(); // exclusive
        int changed = 0;
        var pos = new BlockPos.MutableBlockPos();
        for (int z = minZ; z < minZ + 16; z++) for (int x = minX; x < minX + 16; x++) {
            // Heightmap Y interpretation can vary by generation phase.
            // Accept the exposed red sand at the heightmap position or one below,
            // rather than unconditionally subtracting one from the height.
            int heightY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x & 15, z & 15);
            int surfaceY = -1;
            for (int offset = 0; offset <= 1; offset++) {
                int candidateY = heightY - offset;
                if (candidateY < minY || candidateY + 1 >= maxY) continue;
                pos.set(x, candidateY, z);
                if (!chunk.getBlockState(pos).is(Blocks.RED_SAND)) continue;
                if (!chunk.getBlockState(pos.setY(candidateY + 1)).isAir()) continue;
                surfaceY = candidateY;
                break;
            }
            if (surfaceY < minY) continue;
            pos.set(x, surfaceY, z);
            if (!biomes.apply(pos).is(Tags.Biomes.IS_BADLANDS)) continue;
            if (!isScatteredDeposit(seed, x, z)) continue;
            chunk.setBlockState(pos, CratonBlocks.ferriferous_red_sand.value().defaultBlockState(),
                    Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS);
            changed++;
        }
        return changed;
    }

    /** Sparse isolated iron-rich grains on existing exposed red sand. */
    private static boolean isScatteredDeposit(long seed, int x, int z) {
        // Coarse cells modulate local density; no chunk-relative randomness or cross-chunk writes.
        long cellX = Math.floorDiv(x, CELL_SIZE);
        long cellZ = Math.floorDiv(z, CELL_SIZE);
        long zone = mix(seed ^ SALT ^ (cellX * 0x9E3779B97F4A7C15L)
                ^ (cellZ * 0xC2B2AE3D27D4EB4FL));
        // Some cells contain no mineralization at all.
        if (Long.remainderUnsigned(zone, 5) >= 2) return false;

        // Individual grains are deterministic by world coordinate, deliberately dispersed.
        long grain = mix(seed ^ (SALT + 0x517cc1b727220a95L)
                ^ ((long) x * 0xD6E8FEB86659FD93L)
                ^ ((long) z * 0xA5A3564E27F88619L));
        // Variable local density, without smooth disks or large artificial blobs.
        int threshold = Long.remainderUnsigned(zone >>> 13, 2) == 0 ? 3 : 2;
        return Long.remainderUnsigned(grain, 100) < threshold;
    }

    private static long mix(long v) {
        v ^= v >>> 30; v *= 0xbf58476d1ce4e5b9L;
        v ^= v >>> 27; v *= 0x94d049bb133111ebL;
        return v ^ (v >>> 31);
    }
}
