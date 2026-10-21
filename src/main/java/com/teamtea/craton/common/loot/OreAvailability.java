package com.teamtea.craton.common.loot;

import com.teamtea.craton.common.block.OreBlock;
import com.teamtea.craton.common.registry.CratonBlocks;
import com.teamtea.craton.common.registry.CratonDataMaps.OreDrops;
import com.teamtea.craton.common.registry.CratonDataMaps.Product;
import com.teamtea.craton.api.geology.ore.OreType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.registries.datamaps.DataMapsUpdatedEvent;
import java.util.ArrayList;

/** Availability is data-driven; registration alone never enables a metal. */
@EventBusSubscriber
public class OreAvailability {
    private static volatile Object version = new Object();
    public static Object version() { return version; }

    private static boolean available(Product product) {
        return product.enabled() && product.requiredIngotTag().map(tag ->
                OreType.hasIngot(tag)).orElse(true);
    }

    public static OreDrops resolve(OreDrops data) {
        if (data == null) return null;
        var main = new ArrayList<Product>();
        var bonus = new ArrayList<Product>();
        for (var product : data.drops()) if (available(product)) main.add(product);
        for (var product : data.bonusDrops())
            if (product.chance() > 0 && available(product)) bonus.add(product);
        // Missing main products must not promote rare bonuses to guaranteed drops.
        // Retain independent bonus probabilities even when no primary is available.
        return new OreDrops(main, bonus);
    }

    public static boolean available(Item item) {
        for (var holder : CratonBlocks.ORE_BLOCKS) {
            if (!OreType.available(holder.value()) || !(holder.value() instanceof OreBlock ore)) continue;
            var drops = ore.availableDrops();
            if (drops == null) continue;
            for (var product : drops.drops()) if (product.item() == item) return true;
            for (var product : drops.bonusDrops()) if (product.item() == item) return true;
        }
        return false;
    }

    @SubscribeEvent public static void tagsUpdated(TagsUpdatedEvent event) { version = new Object(); }
    @SubscribeEvent public static void dataUpdated(DataMapsUpdatedEvent event) {
        if (event.getRegistryKey().equals(Registries.BLOCK)) version = new Object();
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { version = new Object(); }
    private OreAvailability() {}
}
