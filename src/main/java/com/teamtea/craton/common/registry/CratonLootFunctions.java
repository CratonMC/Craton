package com.teamtea.craton.common.registry;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.Craton;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;

public final class CratonLootFunctions {
    public static final DeferredRegister<MapCodec<? extends LootItemFunction>> TYPES =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, Craton.MODID);
    public static final OreProduct PRODUCT = OreProduct.INSTANCE;
    static { TYPES.register("ore_product", () -> OreProduct.CODEC); }

    public static final class OreProduct implements LootItemFunction {
        public static final OreProduct INSTANCE = new OreProduct();
        public static final MapCodec<OreProduct> CODEC = MapCodec.unit(INSTANCE);

        private OreProduct() {}

        @Override public MapCodec<? extends LootItemFunction> codec() { return CODEC; }

        @Override
        public Set<ContextKey<?>> getReferencedContextParams() {
            return Set.of(LootContextParams.BLOCK_STATE);
        }

        @Override public ItemStack apply(ItemStack stack, LootContext context) {
            BlockState state = context.getOptional(LootContextParams.BLOCK_STATE);
            if (state == null) return stack;
            CratonDataMaps.OreDrops data = state.getBlock().builtInRegistryHolder().getData(CratonDataMaps.ORE_DROPS);
            if (data == null || data.drops().isEmpty()) return stack;
            int total = 0;
            for (CratonDataMaps.Product drop : data.drops()) total += drop.weight();
            int roll = context.getRandom().nextInt(total);
            for (CratonDataMaps.Product drop : data.drops()) {
                roll -= drop.weight();
                if (roll < 0) {
                    int count = drop.minCount() + context.getRandom().nextInt(
                            Math.max(1, drop.maxCount() - drop.minCount() + 1));
                    return new ItemStack(drop.item(), count);
                }
            }
            return stack;
        }
    }

    private CratonLootFunctions() {}
}
