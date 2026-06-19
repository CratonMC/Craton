package com.teamtea.craton.common.registry;

import com.teamtea.craton.Craton;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CratonTab {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Craton.MODID);

    public static final Supplier<CreativeModeTab> CORE = TABS.register("core", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + Craton.MODID + ".core"))
                    .icon(() -> new ItemStack(CratonBlocks.GNEISS.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(CratonBlocks.PEGMATITE.get());
                        output.accept(CratonBlocks.GNEISS.get());
                        output.accept(CratonBlocks.RHYOLITE.get());
                        output.accept(CratonBlocks.MARBLE.get());
                        output.accept(CratonBlocks.LIMESTONE.get());
                        output.accept(CratonBlocks.GABBRO.get());
                    })
                    .build()
    );
}