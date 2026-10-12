package com.teamtea.craton.common.datamaps;

import net.minecraft.world.item.ItemStackTemplate;

import java.util.List;

public record OreDrops(List<Entry> contents){

    public record Entry(ItemStackTemplate drop,float weight){

    }
}
