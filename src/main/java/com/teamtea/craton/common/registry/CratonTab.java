package com.teamtea.craton.common.registry;

import com.teamtea.craton.Craton;
import com.teamtea.craton.common.core.StoneCollection;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamily;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CratonTab {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Craton.MODID);

    public static final Supplier<CreativeModeTab> CORE = TABS.register("core", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + Craton.MODID + ".core"))
                    .icon(() -> new ItemStack(CratonBlocks.GNEISS.origin().get().getBaseBlock()))
                    .displayItems((parameters, output) -> {
                        for (StoneCollection collection : CratonBlocks.STONE_COLLECTIONS) {
                            for (BlockFamily blockFamily : collection.getAll()) {
                                acceptFamily(output, blockFamily);
                            }
                        }
                    })
                    .build()
    );

    private static void acceptFamily(CreativeModeTab.Output output, BlockFamily family) {
        output.accept(family.getBaseBlock());
        for (Block value : family.getVariants().values()) {
            output.accept(value);
        }
        // output.accept(family.getBaseBlock());
        // output.accept(family.get(BlockFamily.Variant.STAIRS));
        // output.accept(family.get(BlockFamily.Variant.SLAB));
        // output.accept(family.get(BlockFamily.Variant.WALL));
    }
}