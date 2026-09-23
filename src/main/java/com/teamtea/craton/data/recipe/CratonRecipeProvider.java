package com.teamtea.craton.data.recipe;


import com.google.common.collect.ImmutableMap;
import com.teamtea.craton.common.core.StoneCollection;
import com.teamtea.craton.common.registry.CratonBlocks;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamilies;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.data.recipes.packs.VanillaRecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CratonRecipeProvider extends VanillaRecipeProvider {

    HolderGetter<Item> items;

    public CratonRecipeProvider(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput) {
        super(recipeOutput, advancementOutput);
        items = recipeOutput.lookup(Registries.ITEM);
    }

    @Override
    protected void buildRecipes() {
        for (StoneCollection stoneCollection : CratonBlocks.STONE_COLLECTIONS) {
            for (BlockFamily blockFamily : stoneCollection.getAll()) {
                generateRecipes(blockFamily, FeatureFlagSet.of(FeatureFlags.VANILLA));
                if (blockFamily != stoneCollection.getOrigin()) {
                    stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS,
                            blockFamily.getBaseBlock(),
                            stoneCollection.getOrigin().getBaseBlock(),
                           1);
                }
                // for (Block value : blockFamily.getVariants().values()) {
                //     // stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS,
                //     //         value,
                //     //         stoneCollection.getOrigin().getBaseBlock(),
                //     //         1);
                //     stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS,
                //             value,
                //             blockFamily.getBaseBlock(),
                //             1);
                // }
                verticalSlabRecipes(
                        stoneCollection.getVerticalSlab(blockFamily),
                        blockFamily.getBaseBlock()
                );
            }
        }
    }

    private void verticalSlabRecipes(ItemLike verticalSlab, ItemLike baseBlock) {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.BUILDING_BLOCKS, verticalSlab, 6)
                .define('#', baseBlock)
                .pattern("#")
                .pattern("#")
                .pattern("#")
                .unlockedBy("has_" + getItemName(baseBlock), has(baseBlock))
                .save(output);

        stonecutterResultFromBase(
                RecipeCategory.BUILDING_BLOCKS,
                verticalSlab,
                baseBlock,
                2
        );
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
