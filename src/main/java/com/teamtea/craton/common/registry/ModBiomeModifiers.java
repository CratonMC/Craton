package com.teamtea.craton.common.registry;

import com.teamtea.craton.Craton;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
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
    public static final ResourceKey<BiomeModifier> REMOVE_ORES = createKey("remove_ores");
    public static final ResourceKey<BiomeModifier> ADD_METEORITE = createKey("add_meteorite");

    private static ResourceKey<BiomeModifier> createKey(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, Craton.rl(name));
    }

    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> holderGetter = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placedFeatureHolderGetter = context.lookup(Registries.PLACED_FEATURE);
        context.register(ADD_METEORITE, new BiomeModifiers.AddFeaturesBiomeModifier(
                holderGetter.getOrThrow(Tags.Biomes.IS_OVERWORLD),
                HolderSet.direct(placedFeatureHolderGetter.getOrThrow(MeteoriteFeatures.PLACED_METEORITE)),
                GenerationStep.Decoration.SURFACE_STRUCTURES));
        // Craton owns Overworld deposits; remove only explicitly listed vanilla ore features.
        // Handle Nether ores, other mods and existing stone-blob removal rules separately.
        context.register(REMOVE_ORES, new BiomeModifiers.RemoveFeaturesBiomeModifier(
                holderGetter.getOrThrow(Tags.Biomes.IS_OVERWORLD),
                HolderSet.direct(
                        placedFeatureHolderGetter.getOrThrow(OrePlacements.ORE_COAL_UPPER),
                        placedFeatureHolderGetter.getOrThrow(OrePlacements.ORE_COAL_LOWER),
                        placedFeatureHolderGetter.getOrThrow(OrePlacements.ORE_IRON_UPPER),
                        placedFeatureHolderGetter.getOrThrow(OrePlacements.ORE_IRON_MIDDLE),
                        placedFeatureHolderGetter.getOrThrow(OrePlacements.ORE_IRON_SMALL),
                        placedFeatureHolderGetter.getOrThrow(OrePlacements.ORE_GOLD_EXTRA),
                        placedFeatureHolderGetter.getOrThrow(OrePlacements.ORE_GOLD),
                        placedFeatureHolderGetter.getOrThrow(OrePlacements.ORE_GOLD_LOWER),
                        placedFeatureHolderGetter.getOrThrow(OrePlacements.ORE_REDSTONE),
                        placedFeatureHolderGetter.getOrThrow(OrePlacements.ORE_REDSTONE_LOWER),
                        placedFeatureHolderGetter.getOrThrow(OrePlacements.ORE_DIAMOND),
                        placedFeatureHolderGetter.getOrThrow(OrePlacements.ORE_DIAMOND_MEDIUM),
                        placedFeatureHolderGetter.getOrThrow(OrePlacements.ORE_DIAMOND_LARGE),
                        placedFeatureHolderGetter.getOrThrow(OrePlacements.ORE_DIAMOND_BURIED),
                        placedFeatureHolderGetter.getOrThrow(OrePlacements.ORE_LAPIS),
                        placedFeatureHolderGetter.getOrThrow(OrePlacements.ORE_LAPIS_BURIED),
                        placedFeatureHolderGetter.getOrThrow(OrePlacements.ORE_EMERALD),
                        placedFeatureHolderGetter.getOrThrow(OrePlacements.ORE_COPPER),
                        placedFeatureHolderGetter.getOrThrow(OrePlacements.ORE_COPPER_LARGE)),
                Set.of(GenerationStep.Decoration.values())));
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