package com.teamtea.craton.common.worldgen.surface;

import com.teamtea.craton.api.geology.GeologyLayer;
import com.teamtea.craton.api.geology.GeologyRegion;
import com.teamtea.craton.api.geology.surface.SurfaceEnvironment;
import com.teamtea.craton.api.geology.surface.SurfaceSediment;
import com.teamtea.craton.api.geology.surface.SurfaceSedimentType;
import com.teamtea.craton.common.registry.CratonContents;
import com.teamtea.craton.common.worldgen.geology.GeologyFieldSampler;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
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
                             Holder<Biome> biome, Climate.TargetPoint climate, GeologyFieldSampler geology) {
        if (rules.isEmpty() || top >= seaLevel || top <= minY + 2) return false;
        if (!column.getBlock(top + 1).is(Blocks.WATER)) return false;
        BlockState floor = column.getBlock(top);
        int layerY = Math.max(minY + 1, top - 1);
        if (!replaceable(floor) && !floor.equals(geology.sample(x, layerY, z))) return false;
        Holder<GeologyLayer> layer = geology.sampleLayer(x, layerY, z);
        return chooseRule(biome, column.getBlock(top + 1), climate, layer, x, z) != null;
    }

    /** Determine which profile wins using geological identity and spatially stable climate coverage. */
    private SurfaceSediment chooseRule(Holder<Biome> biome, BlockState water,
                                      Climate.TargetPoint climate, Holder<GeologyLayer> layer,
                                      int x, int z) {
        SurfaceSediment selected = null;
        int specificity = -1;
        for (Holder<SurfaceSediment> entry : rules) {
            SurfaceSediment rule = entry.value();
            if (!matchesEnvironment(rule.environment().value(), biome, water)) continue;
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
            int score = (rule.rocks().isEmpty() ? 0 : 2) + (rule.climate().isPresent() ? 1 : 0);
            if (score > specificity) { selected = rule; specificity = score; }
        }
        return selected;
    }

    public void apply(BlockColumn column, int x, int z, int top, int seaLevel, int minY,
                      Holder<Biome> biome, Climate.TargetPoint climate, GeologyFieldSampler geology) {
        if (rules.isEmpty() || top >= seaLevel || top <= minY + 2) return;
        if (!column.getBlock(top + 1).is(Blocks.WATER)) return;
        BlockState original = column.getBlock(top);
        int layerY = Math.max(minY + 1, top - 1);
        if (!replaceable(original) && !original.equals(geology.sample(x, layerY, z))) return;
        Holder<GeologyLayer> actualLayer = geology.sampleLayer(x, layerY, z);
        SurfaceSediment selected = chooseRule(biome, column.getBlock(top + 1), climate, actualLayer, x, z);
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
            if (!replaceable(old) && !old.equals(geology.sample(x, top - d, z))) break;
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

    private static boolean replaceable(BlockState state) {
        return state.is(Blocks.SAND) || state.is(Blocks.RED_SAND) || state.is(Blocks.GRAVEL)
                || state.is(Blocks.DIRT) || state.is(Blocks.CLAY) || state.is(Blocks.STONE)
                || state.is(Blocks.ANDESITE) || state.is(Blocks.DIORITE) || state.is(Blocks.GRANITE);
    }

    private static boolean matchesEnvironment(SurfaceEnvironment environment, Holder<Biome> biome, BlockState fluidAbove) {
        if (environment.fluid().isPresent() && fluidAbove.getFluidState().getType() != environment.fluid().get()) return false;
        String test = environment.biomes();
        Identifier id = Identifier.parse(test.startsWith("#") ? test.substring(1) : test);
        return test.startsWith("#") ? biome.is(TagKey.create(Registries.BIOME, id)) :
                biome.unwrapKey().map(key -> key.identifier().equals(id)).orElse(false);
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
