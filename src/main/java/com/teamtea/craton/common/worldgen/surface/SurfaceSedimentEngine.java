package com.teamtea.craton.common.worldgen.surface;

import com.teamtea.craton.api.geology.GeologyLayer;
import com.teamtea.craton.api.geology.GeologyRegion;
import com.teamtea.craton.api.geology.surface.WaterEnvironment;
import com.teamtea.craton.api.geology.surface.SurfaceSediment;
import com.teamtea.craton.api.geology.surface.SurfaceSedimentType;
import com.teamtea.craton.common.registry.CratonContents;
import com.teamtea.craton.common.worldgen.geology.GeologyFieldSampler;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

import java.util.List;
import java.util.Comparator;

/** Datapack sediment profiles, sampled in world coordinates before the later placer feature. */
public final class SurfaceSedimentEngine {
    private final List<Holder<SurfaceSediment>> rules;
    private final long salt;
    /** Coordinates and verified water column, with no artificial water-type classifier. */
    public record WaterColumn(int floorY, int waterSurfaceY, boolean submerged) {
        public int depth() { return waterSurfaceY - floorY; }
    }

    public static WaterColumn classifyWater(BlockColumn column, int floorY, int upperY,
                                            int seaLevel, int minY, Holder<Biome> biome,
                                            Climate.TargetPoint climate) {
        WaterColumn dry = new WaterColumn(floorY, upperY, false);
        if (floorY < minY || upperY <= floorY) return dry;
        // Bound work without ruling out genuine deep oceans.
        if (upperY - floorY > 384) return dry;
        for (int y = floorY + 1; y <= upperY; y++) {
            if (!column.getBlock(y).getFluidState().is(net.minecraft.tags.FluidTags.WATER)) return dry;
        }
        return new WaterColumn(floorY, upperY, true);
    }

    public SurfaceSedimentEngine(PositionalRandomFactory random) {
        // Stable tie-breaking regardless of datapack registry iteration order.
        this.rules = CratonContents.getSurfaceSediments().stream()
                .sorted(Comparator.comparing(holder -> holder.unwrapKey()
                        .map(key -> key.identifier().toString()).orElse("")))
                .toList();
        this.salt = random.fromHashOf(com.teamtea.craton.Craton.rl("surface_sediments")).nextLong();
    }

    /** Exact applicability test used before disabling the legacy seabed painter. */
    public boolean appliesAt(BlockColumn column, int x, int z, int top, int seaLevel, int minY,
                             Holder<Biome> biome, Climate.TargetPoint climate, GeologyFieldSampler geology, WaterColumn water) {
        if (rules.isEmpty() || top >= seaLevel || top <= minY + 2 || !water.submerged()) return false;
        if (water.floorY() != top) return false;
        BlockState floor = column.getBlock(top);
        int layerY = Math.max(minY + 1, top - 1);
        if (!replaceableSurfaceHost(floor) && !floor.equals(geology.sample(x, layerY, z))) return false;
        Holder<GeologyLayer> layer = geology.sampleLayer(x, layerY, z);
        return chooseRule(water, biome, column.getBlock(top + 1), climate, layer, x, z) != null;
    }

    /** Determine which profile wins using geological identity and spatially stable climate coverage. */
    private SurfaceSediment chooseRule(WaterColumn waterColumn, Holder<Biome> biome, BlockState water,
                                      Climate.TargetPoint climate, Holder<GeologyLayer> layer,
                                      int x, int z) {
        SurfaceSediment selected = null;
        int specificity = -1;
        for (Holder<SurfaceSediment> entry : rules) {
            SurfaceSediment rule = entry.value();
            int environmentPriority = rule.environment().value()
                    .matchScore(biome, water, climate, waterColumn.depth());
            if (environmentPriority < 0) continue;
            if (!rule.rocks().isEmpty()) {
                boolean rockMatches = false;
                for (Holder<GeologyLayer> candidate : rule.rocks()) {
                    if (candidate.unwrapKey().equals(layer.unwrapKey())) {
                        rockMatches = true;
                        break;
                    }
                }
                if (!rockMatches) continue;
            }
            if (rule.climate().isPresent()) {
                GeologyRegion region = rule.climate().get().value();
                double coverage = region.coverage(climate, GeologyRegion.peaksAndValleys(climate.weirdness()));
                if (coverage <= noise(x, z, 48, 101)) continue;
            }
            // A sparse PATCH with no actual deposition must not suppress the
            // legacy ocean fallback just because its environment matches.
            SurfaceSedimentType pattern = rule.type().value();
            if (pattern.pattern() == SurfaceSedimentType.Pattern.PATCH
                    && patchStrength(noise(x, z, 56, 0), 0.57, 0.77) <= 0.0) continue;
            // Existing geological constraints must outrank broad climate defaults.
            int score = (rule.rocks().isEmpty() ? 0 : 16)
                    + (rule.climate().isPresent() ? 8 : 0) + environmentPriority * 12;
            if (score > specificity) { selected = rule; specificity = score; }
        }
        return selected;
    }

    public void apply(BlockColumn column, int x, int z, int top, int seaLevel, int minY,
                      Holder<Biome> biome, Climate.TargetPoint climate, GeologyFieldSampler geology, WaterColumn water) {
        if (rules.isEmpty() || top >= seaLevel || top <= minY + 2 || !water.submerged()) return;
        if (water.floorY() != top) return;
        BlockState original = column.getBlock(top);
        int layerY = Math.max(minY + 1, top - 1);
        if (!replaceableSurfaceHost(original) && !original.equals(geology.sample(x, layerY, z))) return;
        Holder<GeologyLayer> actualLayer = geology.sampleLayer(x, layerY, z);
        SurfaceSediment selected = chooseRule(water, biome, column.getBlock(top + 1), climate, actualLayer, x, z);
        if (selected == null) return;
        SurfaceSedimentType distribution = selected.type().value();
        // Both thickness and presence are sampled from continuous world-space noise.
        // A BED covers the river floor; a PATCH is a coherent, localized body.
        double broad = noise(x, z, 56, 0);
        double mainStrength = distribution.pattern() == SurfaceSedimentType.Pattern.PATCH
                ? patchStrength(broad, 0.57, 0.77) : 1.0;
        if (mainStrength <= 0.0) return;
        int mainThickness = thickness(distribution.thickness(), broad, mainStrength);
        if (mainThickness == 0) return;

        // Write the primary sediment first. Secondary deposits are overlays, not
        // replacements for the full primary column, and retain their own thickness.
        // Record the actually written depth so an obstacle cannot be bypassed.
        int written = 0;
        BlockState main = selected.mainBlock().defaultBlockState();
        for (int d = 0; d < mainThickness && top - d > minY; d++) {
            BlockState old = column.getBlock(top - d);
            if (!(d == 0 ? replaceableSurfaceHost(old) : replaceable(old))
                    && !old.equals(geology.sample(x, top - d, z))) break;
            column.setBlock(top - d, main);
            written++;
        }
        if (written == 0) return;

        int channel = 1;
        for (SurfaceSediment.Secondary secondary : selected.secondary()) {
            SurfaceSedimentType secondaryType = secondary.type().value();
            boolean patch = secondaryType.pattern() == SurfaceSedimentType.Pattern.PATCH;
            // A larger regional signal dominates the smaller detail signal;
            // both use world coordinates, so no chunk-local seams are introduced.
            double regional = noise(x, z, patch ? 42 : 72, channel);
            double detail = noise(x, z, patch ? 20 : 40, channel + 1000);
            double field = 0.82 * regional + 0.18 * detail;
            channel++;
            double strength = patch ? patchStrength(field, 0.51, 0.70)
                    : patchStrength(field, 0.58, 0.79);
            if (strength <= 0.0) continue;
            int secondaryThickness = Math.min(written,
                    thickness(secondaryType.thickness(), field, strength));
            // The first matching overlay wins at a column. This avoids later
            // secondaries overwriting previously placed clay/gravel bodies.
            BlockState overlay = secondary.block().defaultBlockState();
            for (int d = 0; d < secondaryThickness; d++) {
                column.setBlock(top - d, overlay);
            }
            break;
        }
    }

    /** Resolve the actual sediment of a nearby submerged column. Bank material must
     *  come from this selection, never from a separate dry-bank environment rule.
     *  Callers supply a snapshot of the water column and its original climate/geology.
     */
    public SurfaceSediment selectSubmergedRule(BlockColumn column, int x, int z, int top,
                                                Holder<Biome> biome, Climate.TargetPoint climate,
                                                GeologyFieldSampler geology, WaterColumn water, int minY) {
        if (!water.submerged() || water.floorY() != top || top <= minY + 2) return null;
        BlockState floor = column.getBlock(top);
        int layerY = Math.max(minY + 1, top - 1);
        if (!replaceableSurfaceHost(floor) && !floor.equals(geology.sample(x, layerY, z))) return null;
        return chooseRule(water, biome, column.getBlock(top + 1), climate,
                geology.sampleLayer(x, layerY, z), x, z);
    }

    /** Extend the same underwater sediment mixture onto a nearby exposed shore.
     *  Only a one-block veneer, on natural loose/soil hosts; ores and terracotta
     *  above water remain untouched. No independent climate/material selection.
     */
    public void applyBank(BlockColumn column, int x, int z, int top, int minY,
                          SurfaceSediment source) {
        if (source == null || top <= minY + 2) return;
        BlockState original = column.getBlock(top);
        if (!(original.is(Blocks.GRASS_BLOCK) || original.is(Blocks.DIRT)
                || original.is(Blocks.COARSE_DIRT) || original.is(Blocks.ROOTED_DIRT)
                || original.is(Blocks.SAND) || original.is(Blocks.RED_SAND)
                || original.is(Blocks.GRAVEL) || original.is(Blocks.CLAY))) return;
        if (!column.getBlock(top + 1).getFluidState().isEmpty()
                || !column.getBlock(top + 1).isAir()) return;
        // Use the bank position to continue the same deterministic surface fields.
        SurfaceSedimentType primary = source.type().value();
        if (primary.pattern() == SurfaceSedimentType.Pattern.PATCH
                && patchStrength(noise(x, z, 56, 0), 0.57, 0.77) <= 0) return;
        BlockState material = source.mainBlock().defaultBlockState();
        int channel = 1;
        for (SurfaceSediment.Secondary second : source.secondary()) {
            SurfaceSedimentType secondaryType = second.type().value();
            boolean patch = secondaryType.pattern() == SurfaceSedimentType.Pattern.PATCH;
            double regionNoise = noise(x, z, patch ? 42 : 72, channel);
            double detailNoise = noise(x, z, patch ? 20 : 40, channel + 1000);
            channel++;
            double field = 0.82 * regionNoise + 0.18 * detailNoise;
            double strength = patch ? patchStrength(field, 0.51, 0.70)
                    : patchStrength(field, 0.58, 0.79);
            if (strength > 0) {
                material = second.block().defaultBlockState();
                break;
            }
        }
        column.setBlock(top, material);
    }

    private static double patchStrength(double value, double edge, double full) {
        if (value <= edge) return 0.0;
        if (value >= full) return 1.0;
        return smooth((value - edge) / (full - edge));
    }

    private static int thickness(int maxThickness, double field, double strength) {
        // Region noise varies the local depth, while low-strength patch edges
        // taper to a single block rather than abruptly exposing a thick wall.
        int body = Math.max(1, Math.min(maxThickness,
                (int) Math.round(1.0 + field * (maxThickness - 1))));
        return Math.max(1, Math.min(maxThickness, (int) Math.ceil(body * strength)));
    }

    /** Vanilla badlands terracotta may form the exposed floor beneath water.
     *  This exception is permitted ONLY at the verified submerged surface (d == 0).
     *  Deeper terracotta, ores and other non-natural blocks are not added to the
     *  generic replaceable set. Caller must first verify the full water column.
     */
    private static boolean replaceableSurfaceHost(BlockState state) {
        return replaceable(state) || state.is(BlockTags.TERRACOTTA);
    }

    private static boolean replaceable(BlockState state) {
        return state.is(Blocks.SAND) || state.is(Blocks.RED_SAND) || state.is(Blocks.GRAVEL)
                || state.is(Blocks.DIRT) || state.is(Blocks.CLAY) || state.is(Blocks.STONE)
                || state.is(Blocks.ANDESITE) || state.is(Blocks.DIORITE) || state.is(Blocks.GRANITE);
    }

    private double noise(int x, int z, int size, int index) {
        double fx = x / (double) size, fz = z / (double) size;
        int ix = (int)Math.floor(fx), iz = (int)Math.floor(fz);
        double u = smooth(fx - ix), v = smooth(fz - iz);
        double a = mix(hash(ix, iz, index), hash(ix + 1, iz, index), u);
        double b = mix(hash(ix, iz + 1, index), hash(ix + 1, iz + 1, index), u);
        return mix(a, b, v);
    }
    private double hash(int x, int z, int index) {
        long v = salt ^ (x * 0x9E3779B97F4A7C15L) ^ (z * 0xC2B2AE3D27D4EB4FL)
                ^ (index * 0x165667B19E3779F9L);
        v = (v ^ (v >>> 30)) * 0xBF58476D1CE4E5B9L;
        v = (v ^ (v >>> 27)) * 0x94D049BB133111EBL;
        return ((v ^ (v >>> 31)) >>> 11) * 0x1.0p-53;
    }
    private static double smooth(double t) { return t*t*(3-2*t); }
    private static double mix(double a, double b, double t) { return a + (b-a)*t; }
}
