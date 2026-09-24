package com.teamtea.craton.common.core;

import com.teamtea.craton.api.geology.GeologyLayer;
import com.teamtea.craton.api.geology.GeologyProfile;
import com.teamtea.craton.api.geology.deposit.BandedIronFormation;
import com.teamtea.craton.api.geology.deposit.Deposit;
import com.teamtea.craton.common.registry.CratonContents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
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

    public static void rebuildCloumnExtension(
            BlockColumn column,
            BlockPos.MutableBlockPos mutablePos,
            int x,
            int z,
            int startingHeight,
            ChunkAccess chunk,
            Holder<Biome> surfaceBiome,
            PositionalRandomFactory noiseRandom
    ) {
        Optional<Holder<GeologyProfile>> profileOptional =
                CratonContents.getGeologyProfile(surfaceBiome);

        if (profileOptional.isEmpty()) {
            return;
        }

        GeologyProfile profile = profileOptional.get().value();
        List<Holder<GeologyLayer>> layers = profile.layers();

        if (layers.isEmpty()) {
            return;
        }

        LevelHeightAccessor heightAccessor =
                chunk.getHeightAccessorForGeneration();

        int minY = heightAccessor.getMinY();

        startingHeight = chunk.getHeight(
                Heightmap.Types.OCEAN_FLOOR_WG,
                x,
                z
        );

        BlockState surfaceState =
                column.getBlock(startingHeight);

        int cut = getSurfaceCut(
                surfaceState,
                noiseRandom,
                mutablePos.setY(startingHeight)
        );

        int topY = startingHeight - cut;

        if (topY <= minY) {
            return;
        }

        /*
         * Registry contents are obtained once for the whole column,
         * not once for every Y.
         */
        List<Holder<Deposit>> deposits =
                CratonContents.getDeposits();

        for (int y = minY; y <= topY; y++) {
            BlockState current =
                    chunk.getBlockState(
                            mutablePos.setY(y)
                    );

            if (!shouldReplace(current)) {
                continue;
            }

            /*
             * Resolve the host geology first.
             */
            BlockState state = getGeologyState(
                    layers,
                    y,
                    minY,
                    x,
                    z
            );

            /*
             * Deposits overprint the host geology.
             */
            for (Holder<Deposit> depositHolder : deposits) {
                Deposit deposit = depositHolder.value();

                if (deposit instanceof BandedIronFormation bif) {
                    state = applyBandedIronFormation(
                            bif,
                            layers,
                            state,
                            y,
                            minY,
                            x,
                            z,
                            noiseRandom
                    );
                }
            }

            column.setBlock(y, state);
        }
    }

    private static int getSurfaceCut(
            BlockState state,
            PositionalRandomFactory noiseRandom,
            BlockPos.MutableBlockPos mutableBlockPos
    ) {
        if (state.is(BlockTags.DIRT)) {
            return noiseRandom
                    .at(mutableBlockPos)
                    .nextInt(1, 2);
        }

        if (state.is(BlockTags.SAND)) {
            return noiseRandom
                    .at(mutableBlockPos)
                    .nextInt(3, 5);
        }

        if (state.is(Blocks.GRASS_BLOCK)) {
            return noiseRandom
                    .at(mutableBlockPos)
                    .nextInt(2, 3);
        }

        return noiseRandom
                .at(mutableBlockPos)
                .nextInt(0, 1);
    }

    private static boolean shouldReplace(BlockState state) {
        if (state.is(BlockTags.BASE_STONE_OVERWORLD)) {
            return true;
        }

        return state.is(BlockTags.DIRT)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(BlockTags.SAND);
    }

    private static BlockState getGeologyState(
            List<Holder<GeologyLayer>> layers,
            int worldY,
            int minY,
            int x,
            int z
    ) {
        if (layers.isEmpty()) {
            throw new IllegalArgumentException(
                    "Geology profile has no layers"
            );
        }

        double depthFromBottom =
                worldY
                        - minY
                        + getRegionalWarp(x, z);

        double boundary = 0.0;

        for (int i = layers.size() - 1; i >= 0; i--) {
            GeologyLayer layer =
                    layers.get(i).value();

            double thickness =
                    Math.max(
                            1.0,
                            layer.thickness()
                    );

            double offset =
                    getLayerBoundaryOffset(
                            layer,
                            x,
                            z
                    );

            boundary += thickness;

            double effectiveBoundary =
                    Math.max(
                            1.0,
                            boundary + offset
                    );

            if (depthFromBottom < effectiveBoundary) {
                return layer.blockState();
            }
        }

        return layers.getFirst()
                .value()
                .blockState();
    }

    private static BlockState applyBandedIronFormation(
            BandedIronFormation bif,
            List<Holder<GeologyLayer>> layers,
            BlockState hostState,
            int worldY,
            int minY,
            int x,
            int z,
            PositionalRandomFactory noiseRandom
    ) {
        if (layers.isEmpty()) {
            return hostState;
        }

        /*
         * Temporary stratigraphic selection:
         *
         * We currently do not have a field in BIF describing which
         * stratigraphic boundary it belongs to.
         *
         * Therefore we test every internal layer boundary instead of
         * hard-coding "layers.size() - 3".
         */
        for (int layerIndex = 0;
             layerIndex < layers.size();
             layerIndex++) {

            BlockState result =
                    applyBandedIronFormationAtLayer(
                            bif,
                            layers,
                            layerIndex,
                            hostState,
                            worldY,
                            minY,
                            x,
                            z,
                            noiseRandom
                    );

            if (result != hostState) {
                return result;
            }
        }

        return hostState;
    }

    private static BlockState applyBandedIronFormationAtLayer(
            BandedIronFormation bif,
            List<Holder<GeologyLayer>> layers,
            int layerIndex,
            BlockState hostState,
            int worldY,
            int minY,
            int x,
            int z,
            PositionalRandomFactory noiseRandom
    ) {
        /*
         * Prototype placement scale.
         *
         * No global fixed spacing is used here. The scale is derived from
         * the actual deposit definition so larger BIF definitions naturally
         * use larger placement regions.
         *
         * This is still placement prototype code and can later be replaced
         * by proper data-driven placement parameters.
         */
        int placementSize =
                Math.max(
                        16,
                        (int) Math.ceil(bif.length())
                );

        int regionX =
                Math.floorDiv(
                        x,
                        placementSize
                );

        int regionZ =
                Math.floorDiv(
                        z,
                        placementSize
                );

        /*
         * A BIF can extend out of the region containing its anchor.
         * Check the surrounding candidate regions.
         */
        for (int rx = regionX - 1;
             rx <= regionX + 1;
             rx++) {

            for (int rz = regionZ - 1;
                 rz <= regionZ + 1;
                 rz++) {

                BlockPos anchor =
                        getDepositAnchor(
                                bif,
                                layerIndex,
                                rx,
                                rz,
                                placementSize,
                                noiseRandom
                        );

                if (!isInsideHorizontalFootprint(
                        bif,
                        layers,
                        layerIndex,
                        minY,
                        x,
                        z,
                        anchor.getX(),
                        anchor.getZ()
                )) {
                    continue;
                }

                double boundaryY =
                        getLayerBoundaryY(
                                layers,
                                layerIndex,
                                minY,
                                x,
                                z
                        );

                double distance =
                        worldY + 0.5
                                - boundaryY;

                if (Math.abs(distance)
                        > bif.thickness() * 0.5) {
                    continue;
                }

                /*
                 * The BIF does not know or care which concrete block
                 * represents iron in this host rock.
                 *
                 * OreType resolves:
                 *
                 * host rock -> corresponding ore block
                 */
                return bif.ore()
                        .value()
                        .getOreState(hostState);
            }
        }

        return hostState;
    }

    private static BlockPos getDepositAnchor(
            BandedIronFormation bif,
            int layerIndex,
            int regionX,
            int regionZ,
            int placementSize,
            PositionalRandomFactory noiseRandom
    ) {
        int baseX =
                regionX * placementSize;

        int baseZ =
                regionZ * placementSize;

        /*
         * Include the layer index in the positional seed so different
         * stratigraphic boundaries do not receive exactly the same anchors.
         *
         * bandScale/enrichmentScale are deliberately NOT used for placement.
         */
        BlockPos seedPos =
                new BlockPos(
                        baseX,
                        layerIndex,
                        baseZ
                );

        RandomSource random =
                noiseRandom.at(seedPos);

        int anchorX =
                baseX
                        + random.nextInt(
                        placementSize
                );

        int anchorZ =
                baseZ
                        + random.nextInt(
                        placementSize
                );

        return new BlockPos(
                anchorX,
                0,
                anchorZ
        );
    }

    private static boolean isInsideHorizontalFootprint(
            BandedIronFormation bif,
            List<Holder<GeologyLayer>> layers,
            int layerIndex,
            int minY,
            int x,
            int z,
            int anchorX,
            int anchorZ
    ) {
        double[] strike =
                getLayerStrike(
                        layers,
                        layerIndex,
                        minY,
                        anchorX,
                        anchorZ
                );

        double strikeX = strike[0];
        double strikeZ = strike[1];

        double dx =
                x + 0.5
                        - anchorX;

        double dz =
                z + 0.5
                        - anchorZ;

        double along =
                dx * strikeX
                        + dz * strikeZ;

        if (Math.abs(along)
                > bif.length() * 0.5) {
            return false;
        }

        double across =
                -dx * strikeZ
                        + dz * strikeX;

        return Math.abs(across)
                <= bif.width() * 0.5;
    }

    /*
     * Returns the world-space Y coordinate of the selected
     * stratigraphic boundary.
     *
     * getGeologyState uses:
     *
     * worldY - minY + regionalWarp
     *
     * and compares it against:
     *
     * cumulativeThickness + layerOffset
     *
     * therefore:
     *
     * boundaryY =
     *     minY
     *     + cumulativeThickness
     *     + layerOffset
     *     - regionalWarp
     */
    private static double getLayerBoundaryY(
            List<Holder<GeologyLayer>> layers,
            int layerIndex,
            int minY,
            int x,
            int z
    ) {
        double boundary = 0.0;

        for (int i = layers.size() - 1;
             i >= layerIndex;
             i--) {

            GeologyLayer layer =
                    layers.get(i).value();

            boundary += Math.max(
                    1.0,
                    layer.thickness()
            );
        }

        GeologyLayer layer =
                layers.get(layerIndex)
                        .value();

        double offset =
                getLayerBoundaryOffset(
                        layer,
                        x,
                        z
                );

        return minY
                + boundary
                + offset
                - getRegionalWarp(
                x,
                z
        );
    }

    /*
     * Estimates local strike from the boundary gradient.
     *
     * gradient = (dY/dX, dY/dZ)
     *
     * strike = (-dY/dZ, dY/dX)
     */
    private static double[] getLayerStrike(
            List<Holder<GeologyLayer>> layers,
            int layerIndex,
            int minY,
            int x,
            int z
    ) {
        double gradientX =
                getLayerBoundaryY(
                        layers,
                        layerIndex,
                        minY,
                        x + 4,
                        z
                )
                        - getLayerBoundaryY(
                        layers,
                        layerIndex,
                        minY,
                        x - 4,
                        z
                );

        double gradientZ =
                getLayerBoundaryY(
                        layers,
                        layerIndex,
                        minY,
                        x,
                        z + 4
                )
                        - getLayerBoundaryY(
                        layers,
                        layerIndex,
                        minY,
                        x,
                        z - 4
                );

        double strikeX =
                -gradientZ;

        double strikeZ =
                gradientX;

        double length =
                Math.sqrt(
                        strikeX * strikeX
                                + strikeZ * strikeZ
                );

        if (length < 1.0E-6) {
            return new double[]{
                    1.0,
                    0.0
            };
        }

        return new double[]{
                strikeX / length,
                strikeZ / length
        };
    }

    private static double getRegionalWarp(
            int x,
            int z
    ) {
        return Math.sin(
                x * 0.003
                        + z * 0.001
        ) * 18.0
                + Math.cos(
                x * 0.002
                        - z * 0.004
        ) * 14.0;
    }

    private static double getLayerBoundaryOffset(
            GeologyLayer layer,
            int x,
            int z
    ) {
        int seed =
                layer.seed();

        double amplitude =
                layer.amplitude();

        double freqX =
                layer.freqX();

        double freqZ =
                layer.freqZ();

        double sx =
                x + seed * 37.17;

        double sz =
                z - seed * 19.31;

        double large =
                Math.sin(
                        sx * freqX
                                + sz * freqZ
                ) * amplitude;

        double medium =
                Math.cos(
                        sx * freqZ * 1.7
                                - sz * freqX * 1.3
                ) * amplitude * 0.45;

        double small =
                Math.sin(
                        (sx + sz) * 0.035
                ) * amplitude * 0.18;

        return large
                + medium
                + small;
    }
}