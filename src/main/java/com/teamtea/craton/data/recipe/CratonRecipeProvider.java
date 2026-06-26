package com.teamtea.craton.data.recipe;


import com.google.common.collect.ImmutableMap;
import com.teamtea.craton.common.core.StoneCollection;
import com.teamtea.craton.common.registry.CratonBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamilies;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.data.recipes.packs.VanillaRecipeProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CratonRecipeProvider extends VanillaRecipeProvider {

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected @NonNull RecipeProvider createRecipeProvider(HolderLookup.@NonNull Provider registries, RecipeOutput output) {
            return new CratonRecipeProvider(registries, output);
        }

        @Override
        public @NonNull String getName() {
            return "Craton Recipes";
        }
    }

    public CratonRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        HolderLookup.RegistryLookup<Item> items = registries.lookupOrThrow(Registries.ITEM);

        for (StoneCollection stoneCollection : CratonBlocks.STONE_COLLECTIONS) {
            for (BlockFamily blockFamily : stoneCollection.getAll()) {
                generateRecipes(blockFamily, FeatureFlagSet.of(FeatureFlags.VANILLA));
                if (blockFamily != stoneCollection.getOrigin()) {
                    stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS,
                            blockFamily.getBaseBlock(),
                            stoneCollection.getOrigin().getBaseBlock(),
                           1);
                }
                for (Block value : blockFamily.getVariants().values()) {
                    // stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS,
                    //         value,
                    //         stoneCollection.getOrigin().getBaseBlock(),
                    //         1);
                    stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS,
                            value,
                            blockFamily.getBaseBlock(),
                            1);
                }
            }
        }
    }

    @FunctionalInterface
    private interface FamilyStonecutterRecipeProvider {
        void create(CratonRecipeProvider context, ItemLike result, ItemLike base);
    }

    private static final Map<BlockFamily.Variant, FamilyStonecutterRecipeProvider> STONECUTTER_RECIPE_BUILDERS = ImmutableMap.<BlockFamily.Variant, FamilyStonecutterRecipeProvider>builder()
            .put(BlockFamily.Variant.SLAB, (context, result, base) -> context.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, result, base, 2))
            .put(BlockFamily.Variant.STAIRS, (context, result, base) -> context.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, result, base, 1))
            .put(BlockFamily.Variant.BRICKS, (context, result, base) -> context.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, result, base, 1))
            .put(BlockFamily.Variant.WALL, (context, result, base) -> context.stonecutterResultFromBase(RecipeCategory.DECORATIONS, result, base, 1))
            .put(BlockFamily.Variant.CHISELED, (context, result, base) -> context.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, result, base, 1))
            .put(BlockFamily.Variant.POLISHED, (context, result, base) -> context.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, result, base, 1))
            .put(BlockFamily.Variant.CUT, (context, result, base) -> context.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, result, base, 1))
            .put(BlockFamily.Variant.TILES, (context, result, base) -> context.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, result, base, 1))
            .put(BlockFamily.Variant.COBBLED, (context, result, base) -> context.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, result, base, 1))
            .build();

    private void generateStonecutterRecipe(BlockFamily family, BlockFamily.Variant variant, Block base) {
        FamilyStonecutterRecipeProvider recipeFunction = STONECUTTER_RECIPE_BUILDERS.get(variant);
        if (recipeFunction != null) {
            recipeFunction.create(this, family.get(variant), base);
        }

        if (variant == BlockFamily.Variant.POLISHED
                || variant == BlockFamily.Variant.CUT
                || variant == BlockFamily.Variant.BRICKS
                || variant == BlockFamily.Variant.TILES
                || variant == BlockFamily.Variant.COBBLED) {
            BlockFamily childVariantFamily = BlockFamilies.getFamily(family.get(variant));
            if (childVariantFamily != null) {
                childVariantFamily.getVariants().forEach((childVariant, r) -> this.generateStonecutterRecipe(childVariantFamily, childVariant, base));
            }
        }
    }

}
