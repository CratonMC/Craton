package com.teamtea.craton.common.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamtea.craton.Craton;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

import java.util.List;

public final class CratonDataMaps {
    /** One weighted product is selected per ordinary break. Overrides are reloadable. */
    public record Product(Item item, int weight, int minCount, int maxCount) {
        public static final Codec<Product> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(Product::item),
                Codec.intRange(1, 10000).fieldOf("weight").forGetter(Product::weight),
                Codec.intRange(1, 64).fieldOf("min_count").forGetter(Product::minCount),
                Codec.intRange(1, 64).fieldOf("max_count").forGetter(Product::maxCount)
        ).apply(instance, Product::new));
    }

    public record OreDrops(List<Product> drops) {
        public static final Codec<OreDrops> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Product.CODEC.listOf().fieldOf("drops").forGetter(OreDrops::drops)
        ).apply(instance, OreDrops::new));
    }

    public static final DataMapType<Block, OreDrops> ORE_DROPS =
            DataMapType.builder(Craton.rl("ore_drops"), Registries.BLOCK, OreDrops.CODEC).build();

    public static void register(RegisterDataMapTypesEvent event) {
        event.register(ORE_DROPS);
    }

    private CratonDataMaps() {}
}
