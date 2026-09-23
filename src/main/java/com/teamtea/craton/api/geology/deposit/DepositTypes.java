package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.Craton;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public class DepositTypes {
    public static final Map<Identifier, MapCodec<? extends Deposit>> DEPOSITS =
            new HashMap<>();

    public static final Identifier BIF = Craton.rl("bif");

    public static void register(
            Identifier id,
            MapCodec<? extends Deposit> codec
    ) {
        DEPOSITS.put(id, codec);
    }

    static {
        register(BIF, BandedIronFormation.CODEC);
    }
}
