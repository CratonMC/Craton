package com.teamtea.craton.data.datapack;



import com.teamtea.craton.Craton;
import com.teamtea.craton.common.registry.CratonRegistries;
import com.teamtea.craton.common.registry.GeologyLayerRegistry;
import com.teamtea.craton.common.registry.GeologyProfileRegistry;
import com.teamtea.craton.common.registry.ModBiomeModifiers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class DatapackRegistryGenerator extends DatapackBuiltinEntriesProvider {

    public static final RegistrySetBuilder REGISTRY_SET_BUILDER = new RegistrySetBuilder()
            .add(CratonRegistries.GEOLOGY_LAYER, GeologyLayerRegistry::bootstrap)
            .add(CratonRegistries.GEOLOGY_PROFILE, GeologyProfileRegistry::bootstrap)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModBiomeModifiers::bootstrap)
            ;

    public DatapackRegistryGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, REGISTRY_SET_BUILDER, Set.of(Craton.MODID));
    }


}