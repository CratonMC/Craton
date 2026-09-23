package com.teamtea.craton.common.registry;

import com.teamtea.craton.Craton;
import com.teamtea.craton.api.geology.ore.OreType;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public final class OreTypeRegistry {

    public static final ResourceKey<OreType> IRON = createKey("iron");
    public static final ResourceKey<OreType> GOLD = createKey("gold");
    public static final ResourceKey<OreType> COPPER = createKey("copper");

    private static ResourceKey<OreType> createKey(String name) {
        return ResourceKey.create(
                CratonRegistries.ORE_TYPE,
                Craton.rl(name)
        );
    }

    public static void bootstrap(BootstrapContext<OreType> context) {
        var blocks = context.lookup(Registries.BLOCK);

        var stoneReplaceables = blocks.getOrThrow(
                BlockTags.STONE_ORE_REPLACEABLES
        );

        var deepslateReplaceables = blocks.getOrThrow(
                BlockTags.DEEPSLATE_ORE_REPLACEABLES
        );

        context.register(
                IRON,
                new OreType(List.of(
                        new OreType.Variant(
                                stoneReplaceables,
                                Blocks.IRON_ORE.defaultBlockState()
                        ),
                        new OreType.Variant(
                                deepslateReplaceables,
                                Blocks.DEEPSLATE_IRON_ORE.defaultBlockState()
                        )
                ))
        );

        context.register(
                GOLD,
                new OreType(List.of(
                        new OreType.Variant(
                                stoneReplaceables,
                                Blocks.GOLD_ORE.defaultBlockState()
                        ),
                        new OreType.Variant(
                                deepslateReplaceables,
                                Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState()
                        )
                ))
        );

        context.register(
                COPPER,
                new OreType(List.of(
                        new OreType.Variant(
                                stoneReplaceables,
                                Blocks.COPPER_ORE.defaultBlockState()
                        ),
                        new OreType.Variant(
                                deepslateReplaceables,
                                Blocks.DEEPSLATE_COPPER_ORE.defaultBlockState()
                        )
                ))
        );
    }
}