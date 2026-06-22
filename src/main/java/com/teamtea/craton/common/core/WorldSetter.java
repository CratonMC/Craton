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
import java.util.Optional;

public final class WorldSetter {
    private static final boolean DEBUG_EXPOSE_GEOLOGY = false;

    public static void rebuildCloumnExtension(BlockColumn column, BlockPos.MutableBlockPos mutablePos, int x, int z, int startingHeight, ChunkAccess chunk, Holder<Biome> surfaceBiome, PositionalRandomFactory noiseRandom) {
        // if (FMLEnvironment.isProduction()) {
        //     return;
        // }


        // int surfaceY = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
        // if (surfaceY <= minY || surfaceY >= maxY) {
        //     return;
        // }

        Optional<Holder<GeologyProfile>> profileOptional = CratonContents.getGeologyProfile(surfaceBiome);

        if (profileOptional.isEmpty()) {
            // System.out.println("==============");
            // Registry<Biome> biomes = ServerLifecycleHooks.getCurrentServer().registryAccess().lookupOrThrow(Registries.BIOME);
            // for (Holder.Reference<Biome> biomeReference : biomes.listElements().toList()) {
            //     if(CratonContents.getGeologyProfile(biomeReference).isEmpty())
            //         System.out.println(biomeReference.getKey().identifier());
            // }
            // System.out.println("==============");
            return;
        }

        GeologyProfile profile = profileOptional.get().value();
        List<Holder<GeologyLayer>> layers = profile.layers();

        if (layers.isEmpty()) {
            return;
        }
        LevelHeightAccessor heightAccessor = chunk.getHeightAccessorForGeneration();
        int minY = heightAccessor.getMinY();
        int maxY = heightAccessor.getMaxY();

        startingHeight = startingHeight - 1;
        startingHeight = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);

        BlockState surfaceState = column.getBlock(startingHeight);
        int cut = getSurfaceCut(surfaceState, noiseRandom, mutablePos.setY(startingHeight));
        int topY = startingHeight - cut;

        if (topY <= minY) {
            return;
        }

        for (int y = minY; y <= topY; y++) {
            BlockState current = chunk.getBlockState(mutablePos.setY(y));

            if (!shouldReplace(current)) {
                continue;
            }

            column.setBlock(y, getGeologyState(layers, y, minY, startingHeight, x, z));
        }
    }

    private static int getSurfaceCut(BlockState state, PositionalRandomFactory noiseRandom, BlockPos.MutableBlockPos mutableBlockPos) {
        // if(true)return 0;

        if (state.is(BlockTags.DIRT)) {
            return noiseRandom.at(mutableBlockPos).nextInt(1, 2);
        }

        if (state.is(BlockTags.SAND)) {
            return noiseRandom.at(mutableBlockPos).nextInt(3, 5);
        }
        if (state.is(Blocks.GRASS_BLOCK)) {
            return noiseRandom.at(mutableBlockPos).nextInt(2, 3);
        }

        return noiseRandom.at(mutableBlockPos).nextInt(0, 1);
    }

    private static boolean shouldReplace(BlockState state) {
        if (state.is(BlockTags.BASE_STONE_OVERWORLD)) {
            return true;
        }


        // if (!DEBUG_EXPOSE_GEOLOGY) {
        //     return false;
        // }

        return state.is(BlockTags.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(BlockTags.SAND);
    }

    private static BlockState getGeologyState(
            List<Holder<GeologyLayer>> layers,
            int worldY,
            int minY,
            int surfaceY,
            int x,
            int z
    ) {
        if (layers.isEmpty()) {
            throw new IllegalArgumentException("Geology profile has no layers");
        }

        double depthFromBottom = worldY - minY + getRegionalWarp(x, z);

        double boundary = 0.0;

        for (int i = layers.size() - 1; i >= 0; i--) {
            GeologyLayer layer = layers.get(i).value();

            double thickness = Math.max(1.0, layer.thickness());
            double offset = getLayerBoundaryOffset(layer, x, z);

            boundary += thickness;

            double effectiveBoundary = Math.max(1.0, boundary + offset);

            if (depthFromBottom < effectiveBoundary) {
                return layer.blockState();
            }
        }

        return layers.getFirst().value().blockState();
    }

    private static double getRegionalWarp(int x, int z) {
        return Math.sin(x * 0.003 + z * 0.001) * 18.0
                + Math.cos(x * 0.002 - z * 0.004) * 14.0;
    }

    private static double getLayerBoundaryOffset(GeologyLayer layer, int x, int z) {
        int seed = layer.seed();
        double amplitude = layer.amplitude();
        double freqX = layer.freqX();
        double freqZ = layer.freqZ();

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