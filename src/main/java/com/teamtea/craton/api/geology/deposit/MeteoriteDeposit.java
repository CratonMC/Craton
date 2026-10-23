package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamtea.craton.api.geology.ore.OreType;
import com.teamtea.craton.common.worldgen.deposit.DepositRandomSequences;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.codec.RegistryFixedCodec;
import net.minecraft.world.level.block.state.BlockState;
import com.teamtea.craton.common.registry.CratonRegistries;
import net.minecraft.resources.Identifier;

import com.teamtea.craton.common.worldgen.noise.GeologicalNoise;
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

/** Meteorite structure and mineralization, independent of placement frequency and biome selection. */
public record MeteoriteDeposit(Holder<OreType> ore, BlockState rock,
                              int minRadius, int maxRadius, float oreChance,
                              int rarity, HolderSet<Block> replaceable, float craterDepthFactor, float rimChance, BlockState rimBlock) implements Deposit {
    public MeteoriteDeposit(Holder<OreType> ore, BlockState rock, int minRadius, int maxRadius, float oreChance, int rarity, HolderSet<Block> replaceable) {
        this(ore, rock, minRadius, maxRadius, oreChance, rarity, replaceable, .85f, .44f, Blocks.COBBLESTONE.defaultBlockState());
    }
    public MeteoriteDeposit(Holder<OreType> ore, BlockState rock, int minRadius, int maxRadius, float oreChance) {
        this(ore, rock, minRadius, maxRadius, oreChance, 128,
                HolderSet.direct(Blocks.GRASS_BLOCK.builtInRegistryHolder(), Blocks.SAND.builtInRegistryHolder(),
                        Blocks.RED_SAND.builtInRegistryHolder(), Blocks.GRAVEL.builtInRegistryHolder(),
                        Blocks.BASALT.builtInRegistryHolder(), Blocks.TUFF.builtInRegistryHolder(),
                        Blocks.SNOW_BLOCK.builtInRegistryHolder()));
    }
    public static final MapCodec<MeteoriteDeposit> CODEC = RecordCodecBuilder.<MeteoriteDeposit>mapCodec(i -> i.group(
            RegistryFixedCodec.create(CratonRegistries.ORE_TYPE).fieldOf("ore").forGetter(MeteoriteDeposit::ore),
            BlockState.CODEC.fieldOf("rock").forGetter(MeteoriteDeposit::rock),
            Codec.intRange(1, 7).fieldOf("min_radius").forGetter(MeteoriteDeposit::minRadius),
            Codec.intRange(1, 7).fieldOf("max_radius").forGetter(MeteoriteDeposit::maxRadius),
            Codec.floatRange(0, 1).optionalFieldOf("ore_chance", .13f).forGetter(MeteoriteDeposit::oreChance),
            Codec.intRange(1, 1000000).optionalFieldOf("rarity", 128).forGetter(MeteoriteDeposit::rarity),
            RegistryCodecs.holderSet(Registries.BLOCK).fieldOf("replaceable").forGetter(MeteoriteDeposit::replaceable),
            Codec.floatRange(.1f, 3f).optionalFieldOf("crater_depth_factor", .85f).forGetter(MeteoriteDeposit::craterDepthFactor),
            Codec.floatRange(0f, 1f).optionalFieldOf("rim_chance", .44f).forGetter(MeteoriteDeposit::rimChance),
            BlockState.CODEC.optionalFieldOf("rim_block", Blocks.COBBLESTONE.defaultBlockState()).forGetter(MeteoriteDeposit::rimBlock)
    ).apply(i, MeteoriteDeposit::new)).flatXmap(MeteoriteDeposit::validate, MeteoriteDeposit::validate);

    private static com.mojang.serialization.DataResult<MeteoriteDeposit> validate(MeteoriteDeposit d) {
        return d.minRadius() > d.maxRadius()
                ? com.mojang.serialization.DataResult.error(() -> "Meteorite min_radius exceeds max_radius")
                : com.mojang.serialization.DataResult.success(d);
    }
    @Override public Identifier getType() { return DepositTypes.METEORITE; }
    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }
    // Feature-placement events, not field candidates, control meteorite rarity.
    @Override public Placement placement() { return new Placement(16, 0, 0, 1); }
    @Override public DepositRandomSequences sequence() { return DepositRandomSequences.SPECIAL; }
    public boolean generateFeature(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (random.nextInt(rarity()) != 0) return false;
        var ore = ore();
        var rock = rock();
        int minRadius = minRadius(), maxRadius = maxRadius();
        float oreChance = oreChance();
        if (minRadius < 1 || maxRadius < minRadius || maxRadius > 7) return false;
        int radius = minRadius + random.nextInt(maxRadius - minRadius + 1);
        // An impact is atomic: do not begin excavation unless the entire
        // horizontal footprint can be written by this worldgen region.
        // A partially clipped crater would leave seams at chunk boundaries.
        int footprint = radius * 2 + 3;
        for (int dx : new int[]{-footprint, footprint}) {
            for (int dz : new int[]{-footprint, footprint}) {
                if (!level.ensureCanWrite(origin.offset(dx, 0, dz))) return false;
            }
        }
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ()) - 1;
        if (surface <= level.getMinY() + 4) return false;
        if (!level.getFluidState(new BlockPos(origin.getX(), surface, origin.getZ())).isEmpty()) return false;
        int craterRadius = radius * 2 + 2;
        int craterDepth = Math.max(2, (int)Math.round(radius * craterDepthFactor()));
        int centerY = surface - craterDepth + Math.max(1, radius / 3);
        if (surface + 1 >= level.getMaxY()
                || centerY - radius - 1 < level.getMinY()
                || centerY + radius + 1 >= level.getMaxY()) return false;
        long shapeSeed = random.nextLong();
        int changed = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        // Cut the bowl first. Each column follows its own ground level so the
        // impact remains a depression instead of a flat disk on uneven terrain.
        for (int dx = -craterRadius-1; dx <= craterRadius+1; dx++) for (int dz = -craterRadius-1; dz <= craterRadius+1; dz++) {
            double distance = Math.hypot(dx, dz);
            if (distance > craterRadius + 1) continue;
            if (!level.ensureCanWrite(origin.offset(dx, 0, dz))) return false;
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
            if (normalized > .81 && normalized < 1.08 && random.nextFloat() < rimChance()) {
                pos.set(origin.getX()+dx, localSurface, origin.getZ()+dz);
                if (naturalGround(level.getBlockState(pos))) {
                    pos.setY(localSurface+1);
                    if (level.getBlockState(pos).isAir() && level.setBlock(pos, rimBlock(), 2)) changed++;
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
                if (!level.ensureCanWrite(pos)) continue;
                BlockState old=level.getBlockState(pos);
                if (!old.isAir() && !naturalGround(old)) continue;
                float chance=Math.min(.8f,oreChance*(float)(1+.7*Math.max(0,1-distance/radius)));
                BlockState state=random.nextFloat()<chance ? ore.value().getOreState(rock) : rock;
                if (level.setBlock(pos,state,2)) changed++;
            }
        return changed > 0;
    }

    private boolean naturalGround(BlockState state) {
        if (state.hasBlockEntity()) return false;
        return replaceable.contains(state.getBlock().builtInRegistryHolder());
    }
}
