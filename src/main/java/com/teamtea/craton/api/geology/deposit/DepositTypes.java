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
    public static final Identifier GRANITE = Craton.rl("granite_intrusion"), DIORITE = Craton.rl("diorite_intrusion"),
            GABBRO = Craton.rl("gabbro_intrusion"), EPITHERMAL = Craton.rl("epithermal"), VMS = Craton.rl("vms"),
            SKARN = Craton.rl("skarn"), VEIN = Craton.rl("hydrothermal_vein"), WEATHERING = Craton.rl("weathering"),
            KIMBERLITE = Craton.rl("kimberlite"), JADEITITE = Craton.rl("jadeitite"), PLACER = Craton.rl("placer"),
            COAL = Craton.rl("stratiform_coal"), COPPER = Craton.rl("stratiform_copper"), URANIUM = Craton.rl("sandstone_uranium");

    public static void register(
            Identifier id,
            MapCodec<? extends Deposit> codec
    ) {
        DEPOSITS.put(id, codec);
    }

    static {
        register(BIF, BandedIronFormation.CODEC);
        for (Identifier id : new Identifier[]{GRANITE, DIORITE, GABBRO, EPITHERMAL, VMS, SKARN, VEIN,
                WEATHERING, KIMBERLITE, JADEITITE, PLACER, COAL, COPPER, URANIUM})
            register(id, FieldDeposit.codecFor(id));
    }
}
