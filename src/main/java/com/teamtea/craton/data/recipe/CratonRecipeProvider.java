package com.teamtea.craton.data.recipe;

import com.google.common.collect.ImmutableMap;
import com.teamtea.craton.Craton;
import com.teamtea.craton.common.block.StoneCollection;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CookingBookCategory;
import com.teamtea.craton.common.registry.CratonItems;
import net.minecraft.world.item.crafting.Ingredient;
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
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC,
                        CratonItems.SURVEY_CHART.value())
                .requires(Items.MAP)
                .requires(Items.AMETHYST_SHARD)
                .requires(Items.RAW_COPPER)
                .unlockedBy("has_map", has(Items.MAP))
                .save(output);
        generateMineralSmelting();
        generateOreBlockSmelting();
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

    /** Datapack recipes for mineral products. Output is generated under data/craton/recipe/. */
    private void generateMineralSmelting() {
        // Independent hematite/magnetite grains recover an iron nugget, not an ingot.
        cooking("hematite_nugget", CratonItems.HEMATITE_NUGGET.value(), Items.IRON_NUGGET, 1, 0.1f);
        cooking("magnetite_nugget", CratonItems.MAGNETITE_NUGGET.value(), Items.IRON_NUGGET, 1, 0.1f);
        cooking("raw_hematite", CratonItems.RAW_HEMATITE.value(), Items.IRON_INGOT, 1, 0.7f);
        cooking("raw_magnetite", CratonItems.RAW_MAGNETITE.value(), Items.IRON_INGOT, 1, 0.7f);
        cooking("raw_iron_sulfide", CratonItems.RAW_IRON_SULFIDE.value(), Items.IRON_INGOT, 1, 0.7f);
        cooking("raw_copper_oxide", CratonItems.RAW_COPPER_OXIDE.value(), Items.COPPER_INGOT, 1, 0.7f);
        cooking("raw_copper_sulfide", CratonItems.RAW_COPPER_SULFIDE.value(), Items.COPPER_INGOT, 1, 0.7f);
        cooking("raw_gold", CratonItems.RAW_GOLD.value(), Items.GOLD_INGOT, 1, 1.0f);
        // A placer block represents diluted gold-bearing sediment, not a full ingot.
        cooking("auriferous_gravel", CratonBlocks.auriferous_gravel.value(), Items.GOLD_NUGGET, 3, 4, 0.2f);
        cooking("auriferous_sand", CratonBlocks.auriferous_sand.value(), Items.GOLD_NUGGET, 2, 3, 0.2f);
        cooking("ferriferous_gravel", CratonBlocks.ferriferous_gravel.value(), Items.IRON_NUGGET, 1, 2, 0.2f);
        cooking("ferriferous_sand", CratonBlocks.ferriferous_sand.value(), Items.IRON_NUGGET, 1, 2, 0.2f);
        cooking("ferriferous_red_sand", CratonBlocks.ferriferous_red_sand.value(), Items.IRON_NUGGET, 1, 2, 0.2f);
    }

    /** Direct block smelting follows vanilla ore behavior; placer sediments are configured separately. */
    private void generateOreBlockSmelting() {
        cooking("ore_banded_iron_gneiss_ore", CratonBlocks.banded_iron_gneiss_ore.value(), Items.IRON_INGOT, 1, 0.7f);
        cooking("ore_limestone_coal_ore", CratonBlocks.limestone_coal_ore.value(), Items.COAL, 1, 0.1f);
        cooking("ore_sedimentary_copper_ore", CratonBlocks.sedimentary_copper_ore.value(), Items.COPPER_INGOT, 1, 0.7f);
        cooking("ore_metasomatic_lapis_ore", CratonBlocks.metasomatic_lapis_ore.value(), Items.LAPIS_LAZULI, 1, 0.2f);
        cooking("ore_magmatic_pge_ore", CratonBlocks.magmatic_pge_ore.value(), CratonItems.RAW_OSMIUM.value(), 1, 0.7f);
        dualCooking("ore_magmatic_ni_cu_ore", CratonBlocks.magmatic_ni_cu_ore.value(), CratonItems.RAW_NICKEL.value(), Items.COPPER_INGOT, 1, 0.7f);
        cooking("ore_pegmatite_quartz_ore", CratonBlocks.pegmatite_quartz_ore.value(), Items.QUARTZ, 1, 0.2f);
        cooking("ore_pegmatite_emerald_ore", CratonBlocks.pegmatite_emerald_ore.value(), Items.EMERALD, 1, 1.0f);
        cooking("ore_quartz_vein_gold_ore", CratonBlocks.quartz_vein_gold_ore.value(), Items.GOLD_INGOT, 1, 1.0f);
        cooking("ore_quartz_vein_pyrite_ore", CratonBlocks.quartz_vein_pyrite_ore.value(), Items.IRON_INGOT, 1, 0.7f);
        cooking("ore_fluorite_vein_ore", CratonBlocks.fluorite_vein_ore.value(), CratonItems.FLUORITE.value(), 1, 0.2f);
        cooking("ore_sedimentary_uranium_ore", CratonBlocks.sedimentary_uranium_ore.value(), CratonItems.RAW_URANIUM.value(), 1, 0.7f);
        dualCooking("ore_sedimentary_pb_zn_ore", CratonBlocks.sedimentary_pb_zn_ore.value(), CratonItems.RAW_ZINC.value(), CratonItems.RAW_LEAD.value(), 1, 0.7f);
        cooking("ore_shale_coal_ore", CratonBlocks.shale_coal_ore.value(), Items.COAL, 1, 0.1f);
        cooking("ore_deep_hydrothermal_redstone_ore", CratonBlocks.deep_hydrothermal_redstone_ore.value(), Items.REDSTONE, 1, 0.7f);
        cooking("ore_granite_cassiterite_ore", CratonBlocks.granite_cassiterite_ore.value(), CratonItems.RAW_TIN.value(), 1, 0.7f);
        cooking("ore_granite_wolframite_ore", CratonBlocks.granite_wolframite_ore.value(), CratonItems.RAW_TUNGSTEN.value(), 1, 0.7f);
        cooking("ore_porphyry_gold_ore", CratonBlocks.porphyry_gold_ore.value(), Items.GOLD_INGOT, 1, 1.0f);
        cooking("ore_porphyry_copper_ore", CratonBlocks.porphyry_copper_ore.value(), Items.COPPER_INGOT, 1, 0.7f);
        cooking("ore_porphyry_sulfide_ore", CratonBlocks.porphyry_sulfide_ore.value(), Items.IRON_INGOT, 1, 0.7f);
        dualCooking("ore_epithermal_au_ag_ore", CratonBlocks.epithermal_au_ag_ore.value(), Items.GOLD_INGOT, CratonItems.RAW_SILVER.value(), 1, 1.0f);
        cooking("ore_vms_copper_ore", CratonBlocks.vms_copper_ore.value(), Items.COPPER_INGOT, 1, 0.7f);
        cooking("ore_vms_zinc_ore", CratonBlocks.vms_zinc_ore.value(), CratonItems.RAW_ZINC.value(), 1, 0.7f);
        dualCooking("ore_vms_pb_ag_ore", CratonBlocks.vms_pb_ag_ore.value(), CratonItems.RAW_LEAD.value(), CratonItems.RAW_SILVER.value(), 1, 0.7f);
        cooking("ore_bauxite_ore", CratonBlocks.bauxite_ore.value(), CratonItems.BAUXITE.value(), 1, 0.7f);
        cooking("ore_diamond_bearing_kimberlite", CratonBlocks.diamond_bearing_kimberlite.value(), Items.DIAMOND, 1, 1.0f);
        cooking("ore_diamond_rich_kimberlite", CratonBlocks.diamond_rich_kimberlite.value(), Items.DIAMOND, 1, 1.0f);
        cooking("ore_skarn_iron_ore", CratonBlocks.skarn_iron_ore.value(), Items.IRON_INGOT, 1, 0.7f);
        cooking("ore_skarn_copper_ore", CratonBlocks.skarn_copper_ore.value(), Items.COPPER_INGOT, 1, 0.7f);
        dualCooking("ore_skarn_w_sn_ore", CratonBlocks.skarn_w_sn_ore.value(), CratonItems.RAW_TIN.value(), CratonItems.RAW_TUNGSTEN.value(), 1, 0.7f);
        dualCooking("ore_skarn_pb_zn_ore", CratonBlocks.skarn_pb_zn_ore.value(), CratonItems.RAW_ZINC.value(), CratonItems.RAW_LEAD.value(), 1, 0.7f);
        cooking("ore_skarn_gold_ore", CratonBlocks.skarn_gold_ore.value(), Items.GOLD_INGOT, 1, 1.0f);
        cooking("ore_meteoric_iron_ore", CratonBlocks.meteoric_iron_ore.value(), Items.IRON_INGOT, 1, 0.7f);
    }

    /**
     * Publish both metal recovery routes as independent datapack recipes.
     * A furnace or blast furnace may choose only one matching recipe; external
     * processing mods and datapacks can use these declarations independently.
     */
    private void dualCooking(String name, ItemLike input, ItemLike firstResult,
                             ItemLike secondResult, int count, float xp) {
        String[] metals = switch (name) {
            case "ore_magmatic_ni_cu_ore" -> new String[]{"nickel", "copper"};
            case "ore_epithermal_au_ag_ore" -> new String[]{"gold", "silver"};
            case "ore_vms_pb_ag_ore" -> new String[]{"lead", "silver"};
            case "ore_sedimentary_pb_zn_ore", "ore_skarn_pb_zn_ore" -> new String[]{"zinc", "lead"};
            case "ore_skarn_w_sn_ore" -> new String[]{"tin", "tungsten"};
            default -> throw new IllegalArgumentException("Unknown polymetallic ore: " + name);
        };
        cookingWithTagCondition(name + "_primary", input, firstResult, count, xp, metals[0]);
        cookingWithTagCondition(name + "_secondary", input, secondResult, count, xp, metals[1]);
    }

    /** Keep both direct-smelting routes, conditional on the corresponding metal's ore tag. */
    private void cookingWithTagCondition(String name, ItemLike input, ItemLike result,
                                         int count, float xp, String metal) {
        var condition = new net.neoforged.neoforge.common.conditions.NotCondition(
                new net.neoforged.neoforge.common.conditions.TagEmptyCondition(
                        net.minecraft.tags.TagKey.create(Registries.ITEM,
                                net.minecraft.resources.Identifier.parse("c:ores/" + metal))));
        var conditionedOutput = output.withConditions(condition);
        var ingredient = Ingredient.of(input);
        var resultStack = new ItemStackTemplate(result.asItem(), count);
        SimpleCookingRecipeBuilder.smelting(ingredient, RecipeCategory.MISC,
                        CookingBookCategory.MISC, resultStack, xp, 200)
                .unlockedBy("has_" + name, has(input))
                .save(conditionedOutput, net.minecraft.resources.ResourceKey.create(
                        Registries.RECIPE, Craton.rl("smelting/" + name)));
        SimpleCookingRecipeBuilder.blasting(ingredient, RecipeCategory.MISC,
                        CookingBookCategory.MISC, resultStack, xp, 200)
                .unlockedBy("has_" + name, has(input))
                .save(conditionedOutput, net.minecraft.resources.ResourceKey.create(
                        Registries.RECIPE, Craton.rl("blasting/" + name)));
    }

    private void cooking(String name, ItemLike input, ItemLike result, int count, float xp) {
        cooking(name, input, result, count, count, xp);
    }

    private void cooking(String name, ItemLike input, ItemLike result, int count, int blastCount, float xp) {
        var ingredient = Ingredient.of(input);
        var outputStack = new ItemStackTemplate(result.asItem(), count);
        // Only placer sediments receive an additional recovery bonus in the blast furnace.
        // Primary raw ores keep the same output in both furnaces.
        var blastOutputStack = new ItemStackTemplate(result.asItem(), blastCount);
        SimpleCookingRecipeBuilder.smelting(ingredient, RecipeCategory.MISC, CookingBookCategory.MISC, outputStack, xp, 200)
                .unlockedBy("has_" + name, has(input))
                .save(output, net.minecraft.resources.ResourceKey.create(Registries.RECIPE, Craton.rl("smelting/" + name)));
        // In 26.3 the blast furnace gets its speedup from fuel: cookingtime stays 200.
        SimpleCookingRecipeBuilder.blasting(ingredient, RecipeCategory.MISC, CookingBookCategory.MISC, blastOutputStack, xp, 200)
                .unlockedBy("has_" + name, has(input))
                .save(output, net.minecraft.resources.ResourceKey.create(Registries.RECIPE, Craton.rl("blasting/" + name)));
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

    protected void stonecutterResultFromBase(RecipeCategory category, ItemLike result, ItemLike base, int count) {
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(base), category, result, count)
                .unlockedBy(getHasName(base), this.has(base))
                .save(this.output, Craton.rl("stonecutting/" + getConversionRecipeName(result, base)).toString());
    }
//    @FunctionalInterface
//    private interface FamilyStonecutterRecipeProvider {
//        void create(CratonRecipeProvider context, ItemLike result, ItemLike base);
//    }
//
//    private static final Map<BlockFamily.Variant, FamilyStonecutterRecipeProvider> STONECUTTER_RECIPE_BUILDERS = ImmutableMap.<BlockFamily.Variant, FamilyStonecutterRecipeProvider>builder()
//            .put(BlockFamily.Variant.SLAB, (context, result, base) -> context.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, result, base, 2))
//            .put(BlockFamily.Variant.STAIRS, (context, result, base) -> context.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, result, base, 1))
//            .put(BlockFamily.Variant.BRICKS, (context, result, base) -> context.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, result, base, 1))
//            .put(BlockFamily.Variant.WALL, (context, result, base) -> context.stonecutterResultFromBase(RecipeCategory.DECORATIONS, result, base, 1))
//            .put(BlockFamily.Variant.CHISELED, (context, result, base) -> context.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, result, base, 1))
//            .put(BlockFamily.Variant.POLISHED, (context, result, base) -> context.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, result, base, 1))
//            .put(BlockFamily.Variant.CUT, (context, result, base) -> context.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, result, base, 1))
//            .put(BlockFamily.Variant.TILES, (context, result, base) -> context.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, result, base, 1))
//            .put(BlockFamily.Variant.COBBLED, (context, result, base) -> context.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, result, base, 1))
//            .build();
//
//    private void generateStonecutterRecipe(BlockFamily family, BlockFamily.Variant variant, Block base) {
//        FamilyStonecutterRecipeProvider recipeFunction = STONECUTTER_RECIPE_BUILDERS.get(variant);
//        if (recipeFunction != null) {
//            recipeFunction.create(this, family.get(variant), base);
//        }
//
//        if (variant == BlockFamily.Variant.POLISHED
//                || variant == BlockFamily.Variant.CUT
//                || variant == BlockFamily.Variant.BRICKS
//                || variant == BlockFamily.Variant.TILES
//                || variant == BlockFamily.Variant.COBBLED) {
//            BlockFamily childVariantFamily = BlockFamilies.getFamily(family.get(variant));
//            if (childVariantFamily != null) {
//                childVariantFamily.getVariants().forEach((childVariant, r) -> this.generateStonecutterRecipe(childVariantFamily, childVariant, base));
//            }
//        }
//    }

}
