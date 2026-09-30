package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.Craton;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class DepositTypes {
    public static final Map<Identifier, MapCodec<? extends Deposit>> DEPOSITS =
            new HashMap<>();
    private static final Map<Identifier,Function<FieldDeposit.Settings,? extends FieldDeposit>> FIELD_FACTORIES =
            new HashMap<>();

    public static final Identifier BIF = Craton.rl("bif");
    public static final Identifier GRANITE = Craton.rl("granite_intrusion"),
            DIORITE = Craton.rl("diorite_intrusion"),
            GABBRO = Craton.rl("gabbro_intrusion"),
            EPITHERMAL = Craton.rl("epithermal"),
            VMS = Craton.rl("vms"),
            SKARN = Craton.rl("skarn"),
            VEIN = Craton.rl("hydrothermal_vein"),
            WEATHERING = Craton.rl("weathering"),
            KIMBERLITE = Craton.rl("kimberlite"),
            JADEITITE = Craton.rl("jadeitite"),
            PLACER = Craton.rl("placer"),
            COAL = Craton.rl("stratiform_coal"),
            COPPER = Craton.rl("stratiform_copper"),
            URANIUM = Craton.rl("sandstone_uranium");

    public static void register(
            Identifier id,
            MapCodec<? extends Deposit> codec
    ) {
        DEPOSITS.put(id, codec);
    }

    private static void registerField(Identifier id,MapCodec<? extends FieldDeposit> codec,
                                      Function<FieldDeposit.Settings,? extends FieldDeposit> factory) {
        register(id,codec);
        FIELD_FACTORIES.put(id,factory);
    }

    public static FieldDeposit createField(Identifier id,FieldDeposit.Settings settings) {
        var factory=FIELD_FACTORIES.get(id);
        if(factory==null) throw new IllegalArgumentException("No field deposit factory for "+id);
        return factory.apply(settings);
    }

    static {
        register(BIF, BandedIronFormation.CODEC);
        registerField(GRANITE,GraniteIntrusionDeposit.CODEC,GraniteIntrusionDeposit::new);
        registerField(DIORITE,DioriteIntrusionDeposit.CODEC,DioriteIntrusionDeposit::new);
        registerField(GABBRO,GabbroIntrusionDeposit.CODEC,GabbroIntrusionDeposit::new);
        registerField(EPITHERMAL,EpithermalDeposit.CODEC,EpithermalDeposit::new);
        registerField(VMS,VmsDeposit.CODEC,VmsDeposit::new);
        registerField(SKARN,SkarnDeposit.CODEC,SkarnDeposit::new);
        registerField(VEIN,HydrothermalVeinDeposit.CODEC,HydrothermalVeinDeposit::new);
        registerField(WEATHERING,WeatheringDeposit.CODEC,WeatheringDeposit::new);
        register(KIMBERLITE,KimberliteDeposit.CODEC);
        registerField(JADEITITE,JadeititeDeposit.CODEC,JadeititeDeposit::new);
        registerField(PLACER,PlacerDeposit.CODEC,PlacerDeposit::new);
        registerField(COAL,StratiformCoalDeposit.CODEC,StratiformCoalDeposit::new);
        registerField(COPPER,StratiformCopperDeposit.CODEC,StratiformCopperDeposit::new);
        registerField(URANIUM,SandstoneUraniumDeposit.CODEC,SandstoneUraniumDeposit::new);
    }
}
