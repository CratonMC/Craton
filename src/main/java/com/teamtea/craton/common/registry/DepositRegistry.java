package com.teamtea.craton.common.registry;

import com.teamtea.craton.Craton;
import com.teamtea.craton.api.geology.deposit.BandedIronFormation;
import com.teamtea.craton.api.geology.deposit.Deposit;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.material.rule.OreVeinRule;

public final class DepositRegistry {

    public static final ResourceKey<Deposit> BIF = createKey("bif");
    public static final ResourceKey<Deposit> BIF2 = createKey("bif2");
    public static final ResourceKey<Deposit> BIF3 = createKey("bif3");

    private static ResourceKey<Deposit> createKey(String name) {
        return ResourceKey.create(
                CratonRegistries.DEPOSIT,
                Craton.rl(name)
        );
    }

    public static void bootstrap(BootstrapContext<Deposit> context) {
        var oreTypes = context.lookup(CratonRegistries.ORE_TYPE);
        context.register(
                BIF,
                new BandedIronFormation(
                        oreTypes.getOrThrow(OreTypeRegistry.IRON),
                        800.0,
                        300.0,
                        3,
                        0.0,
                        30.0,
                        4.0,
                        120.0
                )
        );

        context.register(
                BIF2,
                new BandedIronFormation(
                        oreTypes.getOrThrow(OreTypeRegistry.COPPER),
                        800.0,
                        300.0,
                        6,
                        0.0,
                        30.0,
                        4.0,
                        120.0
                )
        );

        context.register(
                BIF3,
                new BandedIronFormation(
                        oreTypes.getOrThrow(OreTypeRegistry.GOLD),
                        800.0,
                        300.0,
                        5,
                        0.0,
                        30.0,
                        4.0,
                        120.0
                )
        );
    }
}