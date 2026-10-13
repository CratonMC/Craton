package com.teamtea.craton.mixin.game;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.teamtea.craton.common.worldgen.surface.WorldSetter;
import com.teamtea.craton.common.worldgen.geology.GeologyClimateSampler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.densityfunction.DensityBufferPool;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.material.MaterialSystem;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(MaterialSystem.class)
public abstract class MixinSurfaceSystem {
    @Shadow
    @Final
    private PositionalRandomFactory noiseRandom;

    @Shadow
    public abstract int getSeaLevel();

    @WrapMethod(method = "buildSurface")
    private void craton$withGenerationContext(
            RandomState randomState,
            BiomeManager biomeManager,
            WorldGenerationContext generationContext,
            ChunkAccess protoChunk,
            NoiseChunk noiseChunk,
            MaterialRule ruleSource,
            @Nullable Set<Holder<Biome>> possibleBiomes,
            Operation<Void> original,
            @Share(value = "chunkContext", namespace = "craton")
            LocalRef<WorldSetter.ChunkContext> chunkContext,
            @Share(value = "sampler", namespace = "craton")
            LocalRef<GeologyClimateSampler> sampler
    ) {
        DensityBufferPool pool = randomState.acquireDensityBufferPool();
        try {
            try {
                SamplerContext context = SamplerContext.builder()
                        .enableCaches().useBufferArena(pool).build();
                sampler.set(new GeologyClimateSampler(randomState.createClimateSampler(context), getSeaLevel()));
            } catch (RuntimeException exception) {
                // Do not return this pool twice if fallback creation also fails.
                DensityBufferPool failedPool = pool;
                pool = null;
                randomState.releaseDensityBufferPool(failedPool);
                sampler.set(new GeologyClimateSampler(
                        randomState.createClimateSampler(SamplerContext.EMPTY_UNCACHED), getSeaLevel()));
            }
            chunkContext.set(new WorldSetter.ChunkContext(noiseRandom, sampler.get()));
            original.call(randomState, biomeManager, generationContext, protoChunk, noiseChunk,
                    ruleSource, possibleBiomes);
            chunkContext.get().finishSurface(protoChunk,getSeaLevel());
        } finally {
            // Covers vanilla failures, geology failures and another mixin cancelling the method.
            sampler.set(null);
            chunkContext.set(null);
            if (pool != null) randomState.releaseDensityBufferPool(pool);
        }
    }

    @Inject(
            method = "buildSurface",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/Holder;is(Lnet/minecraft/resources/ResourceKey;)Z",
                    ordinal = 1
            )
    )
    private void craton$applyGeology(
            RandomState randomState,
            BiomeManager biomeManager,
            WorldGenerationContext generationContext,
            ChunkAccess protoChunk,
            NoiseChunk noiseChunk,
            MaterialRule ruleSource,
            @Nullable Set<Holder<Biome>> possibleBiomes,
            CallbackInfo ci,
            @Local(name = "surfaceBiome") Holder<Biome> surfaceBiome,
            @Local(name = "blockX") int blockX,
            @Local(name = "blockZ") int blockZ,
            @Share(value = "chunkContext", namespace = "craton")
            LocalRef<WorldSetter.ChunkContext> chunkContext
    ) {
        // Capture coordinates/biome only. Geology waits until vanilla finishes its surface rules.
        chunkContext.get().captureColumn(blockX,blockZ,surfaceBiome);
    }

}
