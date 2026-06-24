package com.teamtea.craton.data;

import com.teamtea.craton.Craton;
import com.teamtea.craton.data.datapack.DatapackRegistryGenerator;
import com.teamtea.craton.data.lang.Lang_EN;
import com.teamtea.craton.data.lang.Lang_ZH;
import com.teamtea.craton.data.loot.CLootTableProvider;
import com.teamtea.craton.data.model.CModelProvider;
import com.teamtea.craton.data.recipe.CratonRecipeProvider;
import com.teamtea.craton.data.tag.CBlockTagProvider;
import com.teamtea.craton.data.tag.CItemTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;

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
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        var esb = new CBlockTagProvider(packOutput, lookupProvider, MODID);
        generator.addProvider(true, esb);
        generator.addProvider(true, new CItemTagProvider(packOutput, lookupProvider));
        generator.addProvider(true, new CLootTableProvider(packOutput, lookupProvider));
        generator.addProvider(true, new DatapackRegistryGenerator(packOutput, lookupProvider));
        generator.addProvider(true, new CratonRecipeProvider.Runner(packOutput, lookupProvider));

    }
}
