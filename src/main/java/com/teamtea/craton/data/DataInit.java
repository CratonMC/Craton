package com.teamtea.craton.data;

import com.teamtea.craton.Craton;
import com.teamtea.craton.common.registry.*;
import com.teamtea.craton.data.lang.Lang_EN;
import com.teamtea.craton.data.lang.Lang_ZH;
import com.teamtea.craton.data.loot.CLootTableProvider;
import com.teamtea.craton.data.model.CModelProvider;
import com.teamtea.craton.data.recipe.CratonRecipeProvider;
import com.teamtea.craton.data.tag.CBlockTagProvider;
import com.teamtea.craton.data.tag.CItemTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.concurrent.CompletableFuture;

public class DataInit {
    public final static String MODID = Craton.MODID;

    public static void dataGen(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        generator.addProvider(true, new Lang_EN(packOutput));
        generator.addProvider(true, new Lang_ZH(packOutput));
        generator.addProvider(true, new CModelProvider(packOutput, MODID));
    }

    public static void dataGenServer(GatherDataEvent.Server event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getReloadableLookupProvider();
        var esb = new CBlockTagProvider(packOutput, lookupProvider, MODID);
        generator.addProvider(true, esb);
        generator.addProvider(true, new CItemTagProvider(packOutput, lookupProvider));
        event.createReloadableRegistryObjects(
                new RegistrySetBuilder()
                        .add(RecipeProvider.asBootstrap(CratonRecipeProvider::new))
                        .add(Registries.LOOT_TABLE, new CLootTableProvider())
        );
        event.createWorldRegistryObjects(
                new RegistrySetBuilder()
                        .add(CratonRegistries.GEOLOGY_LAYER, GeologyLayerRegistry::bootstrap)
                        .add(CratonRegistries.GEOLOGY_PROFILE, GeologyProfileRegistry::bootstrap)
                        .add(CratonRegistries.ORE_TYPE, OreTypeRegistry::bootstrap)
                        .add(CratonRegistries.DEPOSIT, DepositRegistry::bootstrap)
                        .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModBiomeModifiers::bootstrap)
        );
    }
}
