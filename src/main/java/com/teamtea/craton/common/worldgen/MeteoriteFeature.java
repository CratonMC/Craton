package com.teamtea.craton.common.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamtea.craton.api.geology.ore.OreType;
import com.teamtea.craton.common.core.GeologicalNoise;
import com.teamtea.craton.common.registry.CratonRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.codec.RegistryFixedCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

/** An isolated impact site, generated once by placement modifiers rather than a continuous field. */
public record MeteoriteFeature(Holder<OreType> ore, BlockState rock, int minRadius, int maxRadius, float oreChance) implements Feature {
    public static final MapCodec<MeteoriteFeature> CODEC = RecordCodecBuilder.<MeteoriteFeature>mapCodec(i -> i.group(
            RegistryFixedCodec.create(CratonRegistries.ORE_TYPE).fieldOf("ore").forGetter(MeteoriteFeature::ore),
            BlockState.CODEC.fieldOf("rock").forGetter(MeteoriteFeature::rock),
            com.mojang.serialization.Codec.intRange(1,7).fieldOf("min_radius").forGetter(MeteoriteFeature::minRadius),
            com.mojang.serialization.Codec.intRange(1,7).fieldOf("max_radius").forGetter(MeteoriteFeature::maxRadius),
            com.mojang.serialization.Codec.floatRange(0,1).optionalFieldOf("ore_chance",.13f).forGetter(MeteoriteFeature::oreChance)
    ).apply(i, MeteoriteFeature::new)).flatXmap(MeteoriteFeature::validate,MeteoriteFeature::validate);

    private static com.mojang.serialization.DataResult<MeteoriteFeature> validate(MeteoriteFeature feature) {
        if(feature.minRadius>feature.maxRadius||!Float.isFinite(feature.oreChance))
            return com.mojang.serialization.DataResult.error(() -> "Invalid meteorite radius range or ore chance");
        return com.mojang.serialization.DataResult.success(feature);
    }

    @Override public java.util.stream.Stream<Holder<Feature>> getSubFeatures() { return java.util.stream.Stream.empty(); }

    @Override public MapCodec<MeteoriteFeature> codec() { return CODEC; }

    @Override public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (minRadius < 1 || maxRadius < minRadius || maxRadius > 7) return false;
        int radius = minRadius + random.nextInt(maxRadius - minRadius + 1);
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ()) - 1;
        if (surface <= level.getMinY() + 4) return false;
        if (!level.getFluidState(new BlockPos(origin.getX(), surface, origin.getZ())).isEmpty()) return false;
        int craterRadius = radius * 2 + 2;
        int craterDepth = Math.max(2, (int)Math.round(radius * .85));
        int centerY = surface - craterDepth + Math.max(1, radius / 3);
        long shapeSeed = random.nextLong();
        int changed = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        // Cut the bowl first. Each column follows its own ground level so the
        // impact remains a depression instead of a flat disk on uneven terrain.
        for (int dx = -craterRadius-1; dx <= craterRadius+1; dx++) for (int dz = -craterRadius-1; dz <= craterRadius+1; dz++) {
            double distance = Math.hypot(dx, dz);
            if (distance > craterRadius + 1) continue;
            int localSurface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX()+dx, origin.getZ()+dz)-1;
            if (Math.abs(localSurface-surface)>5) continue;
            double normalized = distance / craterRadius;
            double bowl = Math.max(0, 1-normalized*normalized);
            int cut = (int)Math.round(craterDepth*bowl*bowl);
            for (int depth = 0; depth < cut; depth++) {
                pos.set(origin.getX()+dx, localSurface-depth, origin.getZ()+dz);
                BlockState old = level.getBlockState(pos);
                if (naturalGround(old) && level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2)) changed++;
            }
            // A discontinuous, low ejecta rim leaves the bowl easy to read.
            if (normalized > .81 && normalized < 1.08 && random.nextFloat() < .44f) {
                pos.set(origin.getX()+dx, localSurface, origin.getZ()+dz);
                if (naturalGround(level.getBlockState(pos))) {
                    pos.setY(localSurface+1);
                    if (level.getBlockState(pos).isAir() && level.setBlock(pos, Blocks.COBBLESTONE.defaultBlockState(), 2)) changed++;
                }
            }
        }
        // A coherent sphere lies partly buried in the floor, with richer iron
        // toward its center. Noise roughens its surface without fragmenting it.
        for (int dx = -radius-1; dx <= radius+1; dx++) for (int dy = -radius-1; dy <= radius+1; dy++)
            for (int dz = -radius-1; dz <= radius+1; dz++) {
                int x=origin.getX()+dx, y=centerY+dy, z=origin.getZ()+dz;
                double distance=Math.sqrt(dx*dx+dy*dy+dz*dz);
                double rough=GeologicalNoise.fbm(x,y,z,shapeSeed,.18,2)*.10;
                if (distance > radius*(1+rough)) continue;
                pos.set(x,y,z);
                BlockState old=level.getBlockState(pos);
                if (!old.isAir() && !naturalGround(old)) continue;
                float chance=Math.min(.8f,oreChance*(float)(1+.7*Math.max(0,1-distance/radius)));
                BlockState state=random.nextFloat()<chance ? ore.value().getOreState(rock) : rock;
                if (level.setBlock(pos,state,2)) changed++;
            }
        return changed > 0;
    }

    private static boolean naturalGround(BlockState state) {
        return !state.hasBlockEntity() && (state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(BlockTags.STONE_ORE_REPLACEABLES)
                || state.is(BlockTags.DIRT) || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)
                || state.is(Blocks.GRAVEL) || state.is(Blocks.BASALT)
                || state.is(Blocks.TUFF) || state.is(Blocks.SNOW_BLOCK));
    }
}
