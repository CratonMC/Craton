package com.teamtea.scientificgeology.mixin.game;


import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.teamtea.scientificgeology.common.misc.WorldTester;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.level.levelgen.SurfaceSystem$1")
public abstract class MixinSurface {

    @Shadow
    @Final
    ChunkAccess val$protoChunk;

    @Shadow
    @Final
    BlockPos.MutableBlockPos val$columnPos;

    @Shadow
    @Final
    ChunkPos val$chunkPos;

    @Inject(at = {@At("HEAD")}, method = {"setBlock"})
    public void ccccs$canUse2(int p_190008_,
                              BlockState p_190009_,
                              CallbackInfo ci,
                              @Local(argsOnly = true) LocalRef<BlockState> blockStateLocalRef) {
        if (blockStateLocalRef.get().is(Blocks.SNOW)) {
            blockStateLocalRef.set(Blocks.AIR.defaultBlockState());
        }
    }

    @Inject(at = {@At("RETURN")}, method = {"setBlock"})
    public void ssdsd$canUse(int p_190008_,
                             BlockState blockState,
                             CallbackInfo ci,
                             @Local(argsOnly = true) LocalRef<BlockState> blockStateLocalRef) {
        WorldTester.extracted(blockState, val$protoChunk, val$columnPos);
    }


}
