package com.teamtea.craton.common.registry;

import com.teamtea.craton.Craton;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.OrePlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Set;

public class ModBiomeModifiers {

    public static final ResourceKey<BiomeModifier> REMOVE_STONE = createKey("remove_stone");

    private static ResourceKey<BiomeModifier> createKey(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, Craton.rl(name));
    }

    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> holderGetter = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placedFeatureHolderGetter = context.lookup(Registries.PLACED_FEATURE);
        context.register(REMOVE_STONE, new BiomeModifiers.RemoveFeaturesBiomeModifier(
                holderGetter.getOrThrow(Tags.Biomes.IS_OVERWORLD),
                HolderSet.direct(
                        placedFeatureHolderGetter.getOrThrow(OrePlacements.ORE_DIRT),
                        placedFeatureHolderGetter.getOrThrow(OrePlacements.ORE_GRANITE_LOWER),
                        placedFeatureHolderGetter.getOrThrow(OrePlacements.ORE_GRANITE_UPPER),
                        placedFeatureHolderGetter.getOrThrow(OrePlacements.ORE_DIORITE_LOWER),
                        placedFeatureHolderGetter.getOrThrow(OrePlacements.ORE_DIORITE_UPPER),
                        placedFeatureHolderGetter.getOrThrow(OrePlacements.ORE_ANDESITE_LOWER),
                        placedFeatureHolderGetter.getOrThrow(OrePlacements.ORE_ANDESITE_UPPER),
                        placedFeatureHolderGetter.getOrThrow(OrePlacements.ORE_TUFF),
                        placedFeatureHolderGetter.getOrThrow(OrePlacements.ORE_GRAVEL)),
                Set.of(GenerationStep.Decoration.values())));
    }
}