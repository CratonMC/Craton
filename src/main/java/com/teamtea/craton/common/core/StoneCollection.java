package com.teamtea.craton.common.core;

import com.teamtea.craton.api.geology.block.ExtendedBlockFamily;
import com.teamtea.craton.common.block.VerticalSlabBlock;
import net.minecraft.data.BlockFamily;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public record StoneCollection(
        Supplier<BlockFamily> origin,
        Supplier<BlockFamily> polished,
        Supplier<BlockFamily> brick,
        Supplier<BlockFamily> mossyBrick
) {
    public BlockFamily getOrigin() {
        return origin.get();
    }

    public BlockFamily getPolished() {
        return polished.get();
    }

    public BlockFamily getBrick() {
        return brick.get();
    }

    public BlockFamily getMossyBrick() {
        return mossyBrick.get();
    }

    public List<BlockFamily> getAll() {
        return List.of(getOrigin(), getPolished(),
                getBrick(), getMossyBrick());
    }

    public @Nullable VerticalSlabBlock getVerticalSlab(BlockFamily blockFamily) {
        return (VerticalSlabBlock) ExtendedBlockFamily.getVerticalSlab(blockFamily);
    }
}
