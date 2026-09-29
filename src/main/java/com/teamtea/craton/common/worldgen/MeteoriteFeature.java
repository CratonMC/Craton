package com.teamtea.craton.common.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamtea.craton.api.geology.ore.OreType;
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
        int changed = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) for (int dz = -radius; dz <= radius; dz++) {
            double r = Math.hypot(dx, dz);
            if (r > radius + .25) continue;
            int localSurface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX()+dx, origin.getZ()+dz)-1;
            if (Math.abs(localSurface-surface)>3) continue;
            int crater = (int)Math.round(Math.max(0, 1-r/radius)*2);
            for (int dy = -crater; dy <= 0; dy++) {
                pos.set(origin.getX()+dx, localSurface+dy, origin.getZ()+dz);
                BlockState old = level.getBlockState(pos);
                if (old.hasBlockEntity() || !(old.is(BlockTags.BASE_STONE_OVERWORLD)
                        || old.is(Blocks.DIRT) || old.is(Blocks.GRASS_BLOCK) || old.is(Blocks.COARSE_DIRT)
                        || old.is(Blocks.SAND) || old.is(Blocks.RED_SAND) || old.is(Blocks.GRAVEL)
                        || old.is(Blocks.BASALT) || old.is(Blocks.TUFF))) continue;
                BlockState state = random.nextFloat()<oreChance ? ore.value().getOreState(rock) : rock;
                if (level.setBlock(pos, state, 2)) changed++;
            }
        }
        return changed > 0;
    }
}
