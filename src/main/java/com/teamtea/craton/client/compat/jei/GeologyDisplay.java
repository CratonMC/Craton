package com.teamtea.craton.client.compat.jei;

import net.minecraft.world.item.ItemStack;
import java.util.List;

/** Live registry projection: profiles are ordered top to bottom; ore-host lookups are not strata. */
public record GeologyDisplay(String id, String region, int priority,
                             List<ItemStack> strata, List<String> layerInfo,
                             List<Double> thickness, boolean hostLookup, int page, int pages) {}
