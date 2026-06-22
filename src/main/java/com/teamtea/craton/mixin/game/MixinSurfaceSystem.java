package com.teamtea.craton.mixin.game;


import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.teamtea.craton.common.core.WorldSetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.world.level.levelgen.SurfaceSystem.class)
public abstract class MixinSurfaceSystem {
    @Shadow
    @Final
    private PositionalRandomFactory noiseRandom;

    // @Inject(at = {@At(value = "INVOKE",
    //         target = "Lnet/minecraft/world/level/chunk/BlockColumn;setBlock(ILnet/minecraft/world/level/block/state/BlockState;)V")},
    //         method = {"buildSurface"})
    // public void ssdsd$canUse(RandomState randomState,
    //                          BiomeManager biomeManager,
    //                          Registry<Biome> biomes,
    //                          boolean useLegacyRandom,
    //                          WorldGenerationContext generationContext,
    //                          ChunkAccess protoChunk,
    //                          NoiseChunk noiseChunk,
    //                          SurfaceRules.RuleSource ruleSource,
    //                          CallbackInfo ci,
    //                          @Local(name = "surfaceBiome") Holder<Biome> surfaceBiome,
    //                          @Local(name = "column") BlockColumn column,
    //                          @Local(name = "blockX") int blockX,
    //                          @Local(name = "blockZ") int blockZ,
    //                          @Local(name = "startingHeight") int startingHeight,
    //                          @Local(name = "blockPos") BlockPos.MutableBlockPos blockPos,
    //                          @Share("lowSuface") LocalIntRef localIntRef) {
    //     WorldSetter.rebuildCloumnExtension(column,blockPos, blockX, blockZ, startingHeight, protoChunk,surfaceBiome, noiseRandom);
    // }
    @Inject(at = {@At(value = "INVOKE", target = "Lnet/minecraft/core/Holder;is(Lnet/minecraft/resources/ResourceKey;)Z", ordinal = 1)}, method = {"buildSurface"})
    public void ssdsd$canUse(RandomState randomState,
                             BiomeManager biomeManager,
                             Registry<Biome> biomes,
                             boolean useLegacyRandom,
                             WorldGenerationContext generationContext,
                             ChunkAccess protoChunk,
                             NoiseChunk noiseChunk,
                             SurfaceRules.RuleSource ruleSource,
                             CallbackInfo ci,
                             @Local(name = "surfaceBiome") Holder<Biome> surfaceBiome,
                             @Local(name = "column") BlockColumn column,
                             @Local(name = "blockX") int blockX,
                             @Local(name = "blockZ") int blockZ,
                             @Local(name = "startingHeight") int startingHeight,
                             @Local(name = "blockPos") BlockPos.MutableBlockPos blockPos) {
        WorldSetter.rebuildCloumnExtension(column,blockPos, blockX, blockZ, startingHeight, protoChunk,surfaceBiome,noiseRandom);
    }


}
