package com.teamtea.craton.common.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamtea.craton.common.block.OreBlock;
import com.teamtea.craton.common.registry.CratonDataMaps;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.SingleEntryContainerBase;
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
            roll(ore.availableDrops(), context.getRandom(), output, fortuneLevel(context), context.getLuck());
    }
    public static Builder<?> products() {
        return simpleBuilder((weight, quality, condition, modifier) -> new OreProductsEntry(condition, modifier));
    }
    /** Used by non-mining interactions such as brushing, which have no enchanted mining tool. */
    public static void roll(CratonDataMaps.OreDrops data, RandomSource random, Consumer<ItemStack> output) {
        roll(data, random, output, 0, 0.0f);
    }

    /** Backward-compatible API; non-player sources have no Luck bonus. */
    public static void roll(CratonDataMaps.OreDrops data, RandomSource random, Consumer<ItemStack> output, int fortuneLevel) {
        roll(data, random, output, fortuneLevel, 0.0f);
    }

    /** Fortune/Luck modify amounts; Fortune can separately increase bonus trigger chance per product. */
    public static void roll(CratonDataMaps.OreDrops data, RandomSource random, Consumer<ItemStack> output,
                            int fortuneLevel, float luck) {
        if (data == null) return;
        int total = 0;
        for (var drop : data.drops()) if (drop.enabled()) total += drop.weight();
        if (total > 0) {
            int selected = random.nextInt(total);
            for (var drop : data.drops()) {
                if (!drop.enabled()) continue;
                selected -= drop.weight();
                if (selected < 0) {
                    // A selected main product may be barren; omitted chance defaults to 1.0.
                    // Fortune changes the amount, not this base occurrence chance.
                    if (random.nextDouble() < drop.chance())
                        emit(drop, random, output, fortuneLevel, luck);
                    break;
                }
            }
        }
        for (var drop : data.bonusDrops())
            if (drop.enabled() && random.nextDouble() < bonusChance(drop, fortuneLevel))
                emit(drop, random, output, fortuneLevel, luck);
    }
    /** Diminishing returns, no hard Fortune-level cap; applies only to bonus products. */
    private static double bonusChance(CratonDataMaps.Product drop, int fortuneLevel) {
        if (fortuneLevel <= 0 || drop.fortuneChanceScale() <= 0) return drop.chance();
        return Math.min(1.0, drop.chance() *
                (1.0 + drop.fortuneChanceScale() * Math.log1p((double) fortuneLevel)));
    }

    private static int fortuneLevel(LootContext context) {
        ItemStack tool = context.getOptional(LootContextParams.TOOL);
        if (tool == null || tool.isEmpty()) return 0;
        var fortune = context.getLevel().registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.FORTUNE);
        return Math.max(0, tool.getEnchantmentLevel(fortune));
    }

    private static void emit(CratonDataMaps.Product drop, RandomSource random,
                             Consumer<ItemStack> output, int fortuneLevel, float luck) {
        int base = drop.minCount() + random.nextInt(drop.maxCount() - drop.minCount() + 1);
        // Vanilla distribution for ANY non-negative Fortune level (no level III ceiling).
        // Bound nextInt to protect against overflow from invalid/maliciously high levels.
        int extraRoll = fortuneLevel > 0
                ? Math.max(0, random.nextInt(Math.min(fortuneLevel, Integer.MAX_VALUE - 2) + 2) - 1)
                : 0;
        // Configurable per-product scales. Luck is opt-in and independent from Fortune.
        double multiplier = 1.0 + extraRoll * drop.fortuneScale()
                + Math.max(0.0f, luck) * drop.luckScale();
        double amount = base * multiplier;
        // Stochastic rounding avoids systematic bias from fractional scales.
        long count = (long) Math.floor(Math.min(amount, Integer.MAX_VALUE));
        if (count < Integer.MAX_VALUE && random.nextDouble() < amount - count) count++;
        // Split into bounded stacks rather than overflowing ItemStack counts.
        int maxStack = new ItemStack(drop.item()).getMaxStackSize();
        while (count > 0) {
            int batch = (int) Math.min(count, maxStack);
            output.accept(new ItemStack(drop.item(), batch));
            count -= batch;
        }
    }
}
