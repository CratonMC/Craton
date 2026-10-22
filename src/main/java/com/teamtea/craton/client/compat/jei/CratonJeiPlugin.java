package com.teamtea.craton.client.compat.jei;

import com.teamtea.craton.Craton;
import com.teamtea.craton.common.block.OreBlock;
import com.teamtea.craton.common.registry.CratonDataMaps;
import com.teamtea.craton.common.registry.CratonRegistries;
import com.teamtea.craton.api.geology.deposit.Deposit;
import com.teamtea.craton.api.geology.deposit.FieldDeposit;
import com.teamtea.craton.api.geology.deposit.BandedIronFormation;
import com.teamtea.craton.api.geology.ore.OreType;
import net.minecraft.client.Minecraft;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import java.util.ArrayList;
import java.util.List;

/** Optional client JEI integration; requires only compile-time JEI API. */
@JeiPlugin
public final class CratonJeiPlugin implements IModPlugin {
    public static final IRecipeType<OreDropDisplay> ORE_DROPS =
            IRecipeType.create(Craton.MODID, "ore_drops", OreDropDisplay.class);

    public static final IRecipeType<VeinDisplay> VEINS =
            IRecipeType.create(Craton.MODID, "veins", VeinDisplay.class);
    public static final IRecipeType<GeologyDisplay> GEOLOGY =
            IRecipeType.create(Craton.MODID, "geology", GeologyDisplay.class);

    @Override public Identifier getPluginUid() { return Craton.rl("jei_plugin"); }

    @Override public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new OreDropCategory(registration.getJeiHelpers().getGuiHelper()),
                new VeinCategory(registration.getJeiHelpers().getGuiHelper()),
                new GeologyCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override public void registerRecipes(IRecipeRegistration registration) {
        List<OreDropDisplay> displays = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (!(block instanceof OreBlock ore)) continue;
            // Reads active/synced DataMap values, including ingot-tag availability filtering.
            CratonDataMaps.OreDrops data = ore.availableDrops();
            if (data == null) continue;
            long weightTotal = data.drops().stream().filter(CratonDataMaps.Product::enabled)
                    .mapToLong(CratonDataMaps.Product::weight).sum();
            for (var product : data.drops()) {
                if (!product.enabled() || weightTotal == 0) continue;
                double chance = product.chance() * product.weight() / weightTotal;
                displays.add(display(ore, product, false, chance));
            }
            for (var product : data.bonusDrops()) {
                if (!product.enabled()) continue;
                displays.add(display(ore, product, true, product.chance()));
            }
        }
        registration.addRecipes(ORE_DROPS, displays);
        var client = Minecraft.getInstance();
        if (client.level == null) return;
        var access = client.level.registryAccess();
        access.lookup(CratonRegistries.DEPOSIT).ifPresent(registry -> {
            List<VeinDisplay> veins = new ArrayList<>();
            registry.listElements().forEach(holder -> {
                Deposit deposit = holder.value();
                Set<Block> blocks = new LinkedHashSet<>();
                if (deposit instanceof FieldDeposit field) {
                    for (var type : field.settings().ores()) collectOres(type.value(), blocks);
                } else if (deposit instanceof BandedIronFormation banded) {
                    collectOres(banded.ore().value(), blocks);
                }
                if (blocks.isEmpty()) return;
                var placement = deposit.placement();
                String geometry = "body";
                double rx = 30, rz = 30, ry = 18, minHeight = Double.NaN, maxHeight = Double.NaN, dip = 0;
                if (deposit instanceof BandedIronFormation bif) {
                    geometry = "band";
                    rx = bif.length() * 0.5;
                    rz = bif.width() * 0.5;
                    ry = bif.thickness() * 0.5;
                    dip = (bif.dipMin() + bif.dipMax()) * 0.5;
                } else if (deposit instanceof FieldDeposit field) {
                    var shape = field.settings().shape();
                    rx = (shape.radiusXMin() + shape.radiusXMax()) * 0.5;
                    rz = (shape.radiusZMin() + shape.radiusZMax()) * 0.5;
                    ry = (shape.radiusYMin() + shape.radiusYMax()) * 0.5;
                    minHeight = shape.heightMin();
                    maxHeight = shape.heightMax();
                    // Distinguish the actual generation families, rather than collapsing
                    // pipes, feeder systems, contact aureoles and weathering into ellipses.
                    if (deposit instanceof com.teamtea.craton.api.geology.deposit.PlacerDeposit) geometry = "placer";
                    else if (deposit instanceof com.teamtea.craton.api.geology.deposit.PeatDeposit) geometry = "peat";
                    else if (deposit instanceof com.teamtea.craton.api.geology.deposit.WeatheringDeposit) geometry = "weathering";
                    else if (deposit instanceof com.teamtea.craton.api.geology.deposit.KimberliteDeposit) geometry = "pipe";
                    else if (deposit instanceof com.teamtea.craton.api.geology.deposit.VmsDeposit) geometry = "vms";
                    else if (deposit instanceof com.teamtea.craton.api.geology.deposit.SkarnDeposit) geometry = "contact";
                    else if (deposit instanceof com.teamtea.craton.api.geology.deposit.HydrothermalVeinDeposit) geometry = "pegmatite";
                    else if (deposit instanceof com.teamtea.craton.api.geology.deposit.EpithermalDeposit) geometry = "epithermal";
                    else if (deposit instanceof com.teamtea.craton.api.geology.deposit.DeepHydrothermalRedstoneDeposit) geometry = "redstone";
                    else if (deposit instanceof com.teamtea.craton.api.geology.deposit.SandstoneUraniumDeposit) geometry = "rollfront";
                    else if (deposit instanceof com.teamtea.craton.api.geology.deposit.StratiformCoalDeposit
                            || deposit instanceof com.teamtea.craton.api.geology.deposit.StratiformCopperDeposit
                            || deposit instanceof com.teamtea.craton.api.geology.deposit.ShaleLeadZincDeposit) geometry = "stratiform";
                    else if (deposit instanceof com.teamtea.craton.api.geology.deposit.GraniteIntrusionDeposit
                            || deposit instanceof com.teamtea.craton.api.geology.deposit.DioriteIntrusionDeposit
                            || deposit instanceof com.teamtea.craton.api.geology.deposit.GabbroIntrusionDeposit) geometry = "intrusion";
                    else if (deposit instanceof com.teamtea.craton.api.geology.deposit.JadeititeDeposit) geometry = "pods";
                    else if (deposit instanceof com.teamtea.craton.api.geology.deposit.OrdinaryDeposit) geometry = "ellipsoid";
                    // A kimberlite pipe extends from centre - sampled Y radius to centre + max Y radius.
                    if (deposit instanceof com.teamtea.craton.api.geology.deposit.KimberliteDeposit) {
                        ry = (shape.radiusYMax() + ry) * 0.5;
                    }
                }
                veins.add(new VeinDisplay(holder.unwrapKey().orElseThrow().identifier().toString(),
                        deposit.getType().toString(), blocks.stream().map(ItemStack::new).toList(),
                        placement.cellSize(), placement.maxCandidates(), placement.reach(), placement.frequency(),
                        geometry, rx, rz, ry, minHeight, maxHeight, dip));
            });
            registration.addRecipes(VEINS, veins);
        });
        access.lookup(CratonRegistries.GEOLOGY_PROFILE).ifPresent(registry -> {
            List<GeologyDisplay> profiles = new ArrayList<>();
            registry.listElements().forEach(holder -> {
                var profile = holder.value();
                List<ItemStack> rocks = new ArrayList<>();
                List<String> info = new ArrayList<>();
                List<Double> thickness = new ArrayList<>();
                profile.layers().forEach(layer -> {
                    rocks.add(new ItemStack(layer.value().blockState().getBlock()));
                    info.add("Thickness " + layer.value().thickness() + " | variation " + layer.value().amplitude());
                    thickness.add(layer.value().thickness());
                });
                addGeologyPages(profiles, holder.unwrapKey().orElseThrow().identifier().toString(),
                        profile.region().unwrapKey().map(k -> k.identifier().toString()).orElse("unknown"),
                        profile.priority(), rocks, info, thickness, false);
            });
            // Only real GeologyProfile entries belong in stratigraphy.
            // Ore-host compatibility is accessed through ore information, not as fake strata.
            registration.addRecipes(GEOLOGY, profiles);
        });
    }

    /** Keep one JEI entry per geological profile: never split its layers into recipes. */
    private static void addGeologyPages(List<GeologyDisplay> out, String id, String region, int priority,
                                        List<ItemStack> rocks, List<String> info,
                                        List<Double> thickness, boolean hosts) {
        out.add(new GeologyDisplay(id, region, priority,
                List.copyOf(rocks), List.copyOf(info),
                hosts ? List.of() : List.copyOf(thickness), hosts, 1, 1));
    }

    private static void collectOres(OreType type, Set<Block> blocks) {
        if (!type.available()) return;
        for (var variant : type.variants()) blocks.add(variant.result().getBlock());
    }

    private static OreDropDisplay display(OreBlock ore, CratonDataMaps.Product product,
                                           boolean bonus, double chance) {
        return new OreDropDisplay(new ItemStack(ore), new ItemStack(product.item()), bonus,
                chance, product.minCount(), product.maxCount(),
                product.fortuneScale(), product.fortuneChanceScale());
    }
}
