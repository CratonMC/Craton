package com.teamtea.craton.common.core;

import net.minecraft.data.BlockFamily;

import java.util.List;
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
}
