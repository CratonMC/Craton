package com.teamtea.craton.common.core;

import com.teamtea.craton.api.geology.GeologyLayer;
import com.teamtea.craton.api.geology.GeologyProfile;
import com.teamtea.craton.common.registry.CratonRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.List;
import java.util.Optional;

public final class WorldSetterOldv3 {
    private static final boolean DEBUG_EXPOSE_GEOLOGY = false;

    private WorldSetterOldv3() {
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

        Optional<GeologyProfile> profileOptional = findProfile(chunk, mutablePos, x, surfaceY, z);
        if (profileOptional.isEmpty()) {
            return;
        }

        GeologyProfile profile = profileOptional.get();
        List<Holder<GeologyLayer>> layers = profile.layers();

        if (layers.isEmpty()) {
            return;
        }

        BlockState surfaceState = chunk.getBlockState(mutablePos.setY(surfaceY));
        int cut = DEBUG_EXPOSE_GEOLOGY ? 0 : getSurfaceCut(surfaceState);
        int topY = surfaceY - cut;

        if (topY <= minY) {
            return;
        }

        for (int y = minY; y <= topY; y++) {
            BlockState current = chunk.getBlockState(mutablePos.setY(y));

            if (!shouldReplace(current) && y + cut > topY) {
                continue;
            }

            chunk.setBlockState(
                    mutablePos,
                    getGeologyState(layers, y, minY, surfaceY, x, z)
            );
        }
    }

    private static Optional<GeologyProfile> findProfile(
            ChunkAccess chunk,
            BlockPos.MutableBlockPos mutablePos,
            int x,
            int surfaceY,
            int z
    ) {
        Optional<Registry<GeologyProfile>> registryOptional =
                ServerLifecycleHooks.getCurrentServer().registryAccess().lookup(CratonRegistries.GEOLOGY_PROFILE);

        if (registryOptional.isEmpty()) {
            return Optional.empty();
        }

        Holder<Biome> biome = chunk.getNoiseBiome(
                QuartPos.fromBlock(x),
                QuartPos.fromBlock(surfaceY),
                QuartPos.fromBlock(z)
        );

        for (Holder.Reference<GeologyProfile> profileHolder : registryOptional.get().listElements().toList()) {
            GeologyProfile profile = profileHolder.value();

            if (profile.biomes().contains(biome)) {
                return Optional.of(profile);
            }
        }

        return Optional.empty();
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

        double y = worldY - minY;

        double totalThickness = 0.0;
        for (Holder<GeologyLayer> holder : layers) {
            totalThickness += Math.max(1.0, holder.value().thickness());
        }

        double regionalWarp = getRegionalWarp(x, z);
        double yy = positiveModulo(y + regionalWarp, totalThickness);

        double boundary = 0.0;

        for (int i = 0; i < layers.size(); i++) {
            GeologyLayer layer = layers.get(i).value();
            double thickness = Math.max(1.0, layer.thickness());

            boundary += thickness;

            double offset = getLayerBoundaryOffset(layer, x, z);
            double effectiveBoundary = boundary + offset;

            if (yy < effectiveBoundary) {
                return layer.blockState();
            }
        }

        return layers.get(layers.size() - 1).value().blockState();
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