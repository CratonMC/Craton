package com.teamtea.craton.common.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamtea.craton.common.block.OreBlock;
import com.teamtea.craton.common.registry.CratonDataMaps;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.SingleEntryContainerBase;
import net.minecraft.world.level.storage.loot.entries.UniformContainerBase;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import java.util.Optional;
import java.util.function.Consumer;

/** Emits all data-map products so ordinary loot modifiers apply to every resulting stack. */
public class OreProductsEntry extends SingleEntryContainerBase {
    public static final MapCodec<OreProductsEntry> CODEC = RecordCodecBuilder.mapCodec(instance ->
            commonFields(instance).apply(instance, OreProductsEntry::new));

    public OreProductsEntry(Optional<Holder<LootItemCondition>> condition, Optional<Holder<LootItemFunction>> modifier) {
        super(1, 0, condition, modifier);
    }
    @Override public MapCodec<OreProductsEntry> codec() { return CODEC; }


    @Override public void createItemStack(Consumer<ItemStack> output, LootContext context) {
        var state = context.getOptional(LootContextParams.BLOCK_STATE);
        if (state != null && state.getBlock() instanceof OreBlock ore)
            roll(ore.availableDrops(), context.getRandom(), output);
    }
    public static Builder<?> products() {
        return simpleBuilder((weight, quality, condition, modifier) -> new OreProductsEntry(condition, modifier));
    }
    public static void roll(CratonDataMaps.OreDrops data, RandomSource random, Consumer<ItemStack> output) {
        if (data == null) return;
        int total = 0;
        for (var drop : data.drops()) if (drop.enabled()) total += drop.weight();
        if (total > 0) {
            int selected = random.nextInt(total);
            for (var drop : data.drops()) {
                if (!drop.enabled()) continue;
                selected -= drop.weight();
                if (selected < 0) { emit(drop, random, output); break; }
            }
        }
        for (var drop : data.bonusDrops())
            if (drop.enabled() && (drop.chance() >= 1 || random.nextDouble() < drop.chance()))
                emit(drop, random, output);
    }
    private static void emit(CratonDataMaps.Product drop, RandomSource random, Consumer<ItemStack> output) {
        output.accept(new ItemStack(drop.item(), drop.minCount() + random.nextInt(drop.maxCount() - drop.minCount() + 1)));
    }
}
