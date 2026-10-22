
package com.teamtea.craton.data.datamaps;

import com.teamtea.craton.common.registry.CratonBlocks;
import com.teamtea.craton.common.registry.CratonDataMaps;
import com.teamtea.craton.common.registry.CratonDataMaps.OreDrops;
import com.teamtea.craton.common.registry.CratonDataMaps.Product;
import com.teamtea.craton.common.registry.CratonItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.DataMapProvider;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DataMapPro extends DataMapProvider {

    public DataMapPro(
            PackOutput packOutput,
            CompletableFuture<HolderLookup.Provider> lookupProvider
    ) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        gatherOreHosts(provider);

        add(CratonBlocks.banded_iron_gneiss_ore,
                List.of(product(CratonItems.RAW_HEMATITE.value(), "c:ingots/iron")),
                List.of());

        add(CratonBlocks.skarn_iron_ore,
                List.of(product(CratonItems.RAW_MAGNETITE.value(), "c:ingots/iron")),
                List.of());

        add(CratonBlocks.sedimentary_copper_ore,
                List.of(product(CratonItems.RAW_COPPER_OXIDE.value(), "c:ingots/copper")),
                List.of());

        add(CratonBlocks.limestone_coal_ore,
                List.of(product(Items.COAL)),
                List.of());

        add(CratonBlocks.shale_coal_ore,
                List.of(product(Items.COAL)),
                List.of());

        add(CratonBlocks.metasomatic_lapis_ore,
                List.of(counts(product(Items.LAPIS_LAZULI), 4, 9)),
                List.of());

        add(CratonBlocks.pegmatite_quartz_ore,
                List.of(product(Items.QUARTZ)),
                List.of());

        add(CratonBlocks.pegmatite_emerald_ore,
                List.of(tuned(product(Items.EMERALD), 0.50, 0.0)),
                List.of());

        add(CratonBlocks.fluorite_vein_ore,
                List.of(product(CratonItems.FLUORITE.value(), "c:gems/fluorite")),
                List.of());

        add(CratonBlocks.deep_hydrothermal_redstone_ore,
                List.of(counts(product(Items.REDSTONE), 4, 5)),
                List.of());

        add(CratonBlocks.bauxite_ore,
                List.of(tuned(product(CratonItems.BAUXITE.value(), "c:ingots/aluminum"), 0.75, 0.0)),
                List.of());

        add(CratonBlocks.peat,
                List.of(product(CratonBlocks.peat.value().asItem())),
                List.of());

        add(CratonBlocks.sedimentary_uranium_ore,
                List.of(tuned(product(CratonItems.RAW_URANIUM.value(), "c:ingots/uranium"), 0.75, 0.0)),
                List.of());

        add(CratonBlocks.granite_cassiterite_ore,
                List.of(tuned(product(CratonItems.RAW_TIN.value(), "c:ingots/tin"), 0.75, 0.0)),
                List.of());

        add(CratonBlocks.granite_wolframite_ore,
                List.of(tuned(product(CratonItems.RAW_TUNGSTEN.value(), "c:ingots/tungsten"), 0.75, 0.0)),
                List.of());

        add(CratonBlocks.auriferous_gravel,
                List.of(counts(tuned(product(Items.GOLD_NUGGET, "c:ingots/gold"), 0.65, 0.0), 2, 4)),
                List.of());

        add(CratonBlocks.auriferous_sand,
                List.of(counts(tuned(product(Items.GOLD_NUGGET, "c:ingots/gold"), 0.65, 0.0), 2, 4)),
                List.of());

        add(CratonBlocks.ferriferous_gravel,
                List.of(counts(product(Items.IRON_NUGGET, "c:ingots/iron"), 2, 4)),
                List.of());

        add(CratonBlocks.ferriferous_sand,
                List.of(counts(product(Items.IRON_NUGGET, "c:ingots/iron"), 2, 4)),
                List.of());

        add(CratonBlocks.ferriferous_red_sand,
                List.of(counts(product(Items.IRON_NUGGET, "c:ingots/iron"), 2, 4)),
                List.of());

        add(CratonBlocks.diamond_bearing_kimberlite,
                List.of(tuned(new Product(Items.DIAMOND, 1, 1, 1, 0.35, true, Optional.empty()), 0.50, 0.0)),
                List.of());

        add(CratonBlocks.diamond_rich_kimberlite,
                List.of(tuned(new Product(Items.DIAMOND, 1, 1, 1, 0.85, true, Optional.empty()), 0.65, 0.0)),
                List.of());

        add(CratonBlocks.meteoric_iron_ore,
                List.of(tuned(product(CratonItems.METEORIC_IRON.value(), "c:ingots/iron"), 0.35, 0.0)),
                List.of());

        add(CratonBlocks.quartz_vein_gold_ore,
                List.of(tuned(product(CratonItems.RAW_GOLD.value(), "c:ingots/gold"), 0.65, 0.0)),
                List.of(
                        bonus(Items.QUARTZ, 0.25)
                ));

        add(CratonBlocks.quartz_vein_pyrite_ore,
                List.of(product(CratonItems.RAW_IRON_SULFIDE.value(), "c:ingots/iron")),
                List.of(
                        bonusChance(tuned(bonus(CratonItems.RAW_GOLD.value(), 0.10, "c:ingots/gold"), 0.65, 0.0), 0.3),
                        bonus(Items.QUARTZ, 0.25)
                ));

        add(CratonBlocks.porphyry_sulfide_ore,
                List.of(product(CratonItems.RAW_IRON_SULFIDE.value(), "c:ingots/iron")),
                List.of(
                        bonus(CratonItems.RAW_COPPER_SULFIDE.value(), 0.20, "c:ingots/copper"),
                        bonusChance(tuned(bonus(CratonItems.RAW_GOLD.value(), 0.10, "c:ingots/gold"), 0.65, 0.0), 0.3)
                ));

        add(CratonBlocks.porphyry_copper_ore,
                List.of(product(CratonItems.RAW_COPPER_SULFIDE.value(), "c:ingots/copper")),
                List.of(
                        bonusChance(tuned(bonus(CratonItems.RAW_GOLD.value(), 0.15, "c:ingots/gold"), 0.65, 0.0), 0.3)
                ));

        add(CratonBlocks.vms_copper_ore,
                List.of(product(CratonItems.RAW_COPPER_SULFIDE.value(), "c:ingots/copper")),
                List.of(
                        bonusChance(tuned(bonus(CratonItems.RAW_ZINC.value(), 0.20, "c:ingots/zinc"), 1.00, 0.0), 0.50)
                ));

        add(CratonBlocks.skarn_copper_ore,
                List.of(product(CratonItems.RAW_COPPER_SULFIDE.value(), "c:ingots/copper")),
                List.of(
                        bonusChance(tuned(bonus(CratonItems.RAW_GOLD.value(), 0.10, "c:ingots/gold"), 0.65, 0.0), 0.3)
                ));

        add(CratonBlocks.magmatic_ni_cu_ore,
                List.of(product(CratonItems.RAW_NICKEL.value(), "c:ingots/nickel")),
                List.of(
                        bonus(CratonItems.RAW_COPPER_SULFIDE.value(), 0.25, "c:ingots/copper")
                ));

        add(CratonBlocks.porphyry_gold_ore,
                List.of(tuned(product(CratonItems.RAW_GOLD.value(), "c:ingots/gold"), 0.65, 0.0)),
                List.of(
                        bonus(CratonItems.RAW_COPPER_SULFIDE.value(), 0.20, "c:ingots/copper")
                ));

        add(CratonBlocks.skarn_gold_ore,
                List.of(tuned(product(CratonItems.RAW_GOLD.value(), "c:ingots/gold"), 0.65, 0.0)),
                List.of());

        add(CratonBlocks.epithermal_au_ag_ore,
                List.of(
                        tuned(product(CratonItems.RAW_GOLD.value(), 40, "c:ingots/gold"), 0.65, 0.0),
                        tuned(product(CratonItems.RAW_SILVER.value(), 60, "c:ingots/silver"), 0.75, 0.0)
                ),
                List.of());

        add(CratonBlocks.vms_pb_ag_ore,
                List.of(product(CratonItems.RAW_LEAD.value(), "c:ingots/lead")),
                List.of(
                        bonusChance(tuned(bonus(CratonItems.RAW_SILVER.value(), 0.20, "c:ingots/silver"), 0.75, 0.0), 0.40)
                ));

        add(CratonBlocks.vms_zinc_ore,
                List.of(product(CratonItems.RAW_ZINC.value(), "c:ingots/zinc")),
                List.of(
                        bonusChance(tuned(bonus(CratonItems.RAW_LEAD.value(), 0.20, "c:ingots/lead"), 1.00, 0.0), 0.50)
                ));

        add(CratonBlocks.magmatic_pge_ore,
                List.of(tuned(product(CratonItems.RAW_OSMIUM.value(), "c:ingots/osmium"), 0.35, 0.0)),
                List.of(
                        bonus(CratonItems.RAW_NICKEL.value(), 0.20, "c:ingots/nickel")
                ));

        add(CratonBlocks.sedimentary_pb_zn_ore,
                List.of(
                        product(CratonItems.RAW_LEAD.value(), 40, "c:ingots/lead"),
                        product(CratonItems.RAW_ZINC.value(), 60, "c:ingots/zinc")
                ),
                List.of());

        add(CratonBlocks.skarn_pb_zn_ore,
                List.of(
                        product(CratonItems.RAW_LEAD.value(), 40, "c:ingots/lead"),
                        product(CratonItems.RAW_ZINC.value(), 60, "c:ingots/zinc")
                ),
                List.of(
                        bonusChance(tuned(bonus(CratonItems.RAW_SILVER.value(), 0.15, "c:ingots/silver"), 0.75, 0.0), 0.40)
                ));

        add(CratonBlocks.skarn_w_sn_ore,
                List.of(
                        tuned(product(CratonItems.RAW_TIN.value(), 50, "c:ingots/tin"), 0.75, 0.0),
                        tuned(product(CratonItems.RAW_TUNGSTEN.value(), 50, "c:ingots/tungsten"), 0.75, 0.0)
                ),
                List.of());

        add(CratonBlocks.suspicious_jadeitite,
                List.of(
                        tuned(product(CratonItems.GREEN_JADE.value()), 0.50, 0.0),
                        tuned(product(CratonItems.WHITE_JADE.value()), 0.50, 0.0),
                        tuned(product(CratonItems.LAVENDER_JADE.value()), 0.50, 0.0)
                ),
                List.of());
    }

    /** Base drops per successful product roll (before Fortune), with optional per-product tuning. */
    private Product counts(Product product, int minCount, int maxCount) {
        return new Product(product.item(), product.weight(), minCount, maxCount,
                product.chance(), product.enabled(), product.requiredIngotTag(),
                product.fortuneScale(), product.luckScale(), product.fortuneChanceScale());
    }

    /** Per-product amount scaling: 1.0 = vanilla Fortune; 0.0 = no Fortune bonus.
     *  luckScale = bonus amount per positive Luck point; 0.0 disables Luck for this item.
     */
    private Product tuned(Product product, double fortuneScale, double luckScale) {
        return new Product(product.item(), product.weight(), product.minCount(), product.maxCount(),
                product.chance(), product.enabled(), product.requiredIngotTag(), fortuneScale, luckScale,
                product.fortuneChanceScale());
    }

    /** Adjust bonus trigger Fortune sensitivity per Product; 0 disables it. */
    private Product bonusChance(Product product, double chanceScale) {
        return new Product(product.item(), product.weight(), product.minCount(), product.maxCount(),
                product.chance(), product.enabled(), product.requiredIngotTag(),
                product.fortuneScale(), product.luckScale(), chanceScale);
    }

    /** Derive the host index from actual OreType variants rather than duplicating host lists. */
    private void gatherOreHosts(HolderLookup.Provider provider) {
        java.util.Map<Block, java.util.Set<Block>> hostsByOre = new java.util.LinkedHashMap<>();
        provider.lookup(com.teamtea.craton.common.registry.CratonRegistries.ORE_TYPE)
                .ifPresent(types -> types.listElements().forEach(typeHolder -> {
                    var type = typeHolder.value();
                    for (var variant : type.variants()) {
                        java.util.Set<Block> hosts = hostsByOre.computeIfAbsent(
                                variant.result().getBlock(), ignored -> new java.util.LinkedHashSet<>());
                        variant.hosts().forEach(holder -> hosts.add(holder.value()));
                    }
                }));
        hostsByOre.forEach((ore, hosts) -> builder(CratonDataMaps.ORE_HOSTS).add(
                net.minecraft.resources.ResourceKey.create(Registries.BLOCK,
                        net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(ore)),
                new CratonDataMaps.OreHosts(List.copyOf(hosts)), false));
    }

    private void add(
            Holder<Block> block,
            List<Product> main,
            List<Product> bonus
    ) {
        builder(CratonDataMaps.ORE_DROPS).add(
                block.unwrapKey().orElseThrow(),
                new OreDrops(main, bonus),
                false
        );
    }

    private Product product(Item item) {
        return new Product(
                item, 1, 1, 1, 1.0, true, Optional.empty()
        );
    }

    private Product product(Item item, String requiredIngotTag) {
        return product(item, 1, requiredIngotTag);
    }

    private Product product(
            Item item,
            int weight,
            String requiredIngotTag
    ) {
        return new Product(
                item, weight, 1, 1, 1.0, true,
                Optional.of(
                        TagKey.create(
                                Registries.ITEM,
                                Identifier.parse(requiredIngotTag)
                        )
                )
        );
    }

    private Product bonus(Item item, double chance) {
        return new Product(
                item, 1, 1, 1, chance, true, Optional.empty(), 1.0, 0.0, 0.5
        );
    }

    private Product bonus(
            Item item,
            double chance,
            String requiredIngotTag
    ) {
        return bonus(item, chance, true, requiredIngotTag);
    }

    private Product bonus(
            Item item,
            double chance,
            boolean enabled,
            String requiredIngotTag
    ) {
        return new Product(
                item, 1, 1, 1, chance, enabled,
                Optional.of(
                        TagKey.create(
                                Registries.ITEM,
                                Identifier.parse(requiredIngotTag)
                        )
                ), 1.0, 0.0, 0.5
        );
    }
}
