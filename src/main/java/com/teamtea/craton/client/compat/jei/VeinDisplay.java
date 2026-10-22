package com.teamtea.craton.client.compat.jei;

import net.minecraft.world.item.ItemStack;
import java.util.List;

/** JEI projection of registered deposit parameters, not a generated-world map. */
public record VeinDisplay(String id, String type, List<ItemStack> ores,
        int cellSize, int candidates, double reach, double frequency,
        String geometry, double radiusX, double radiusZ, double radiusY,
        double heightMin, double heightMax, double dip) {}
