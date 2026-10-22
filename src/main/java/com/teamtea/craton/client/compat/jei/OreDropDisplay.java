package com.teamtea.craton.client.compat.jei;

import net.minecraft.world.item.ItemStack;

/** JEI projection of the active OreDrops DataMap; never a second drop table. */
public record OreDropDisplay(ItemStack ore, ItemStack result, boolean bonus,
                             double baseChance, int minCount, int maxCount,
                             double fortuneScale, double fortuneChanceScale) {
}
