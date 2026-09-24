package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamtea.craton.api.geology.ore.OreType;
import com.teamtea.craton.common.registry.CratonRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.core.registries.codec.RegistryFixedCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviders;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.levelgen.feature.Feature;

public record BandedIronFormation(
        Holder<OreType> ore,
        double length,
        double width,
        double thickness,
        double dipMin,
        double dipMax,
        double bandScale,
        double enrichmentScale
) implements Deposit {

    public static final MapCodec<BandedIronFormation> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    RegistryFixedCodec.create(CratonRegistries.ORE_TYPE).fieldOf("ore")
                            .forGetter(BandedIronFormation::ore),
                    Codec.DOUBLE.fieldOf("length")
                            .forGetter(BandedIronFormation::length),
                    Codec.DOUBLE.fieldOf("width")
                            .forGetter(BandedIronFormation::width),
                    Codec.DOUBLE.fieldOf("thickness")
                            .forGetter(BandedIronFormation::thickness),
                    Codec.DOUBLE.fieldOf("dip_min")
                            .forGetter(BandedIronFormation::dipMin),
                    Codec.DOUBLE.fieldOf("dip_max")
                            .forGetter(BandedIronFormation::dipMax),
                    Codec.DOUBLE.fieldOf("band_scale")
                            .forGetter(BandedIronFormation::bandScale),
                    Codec.DOUBLE.fieldOf("enrichment_scale")
                            .forGetter(BandedIronFormation::enrichmentScale)
            ).apply(instance, BandedIronFormation::new));

    @Override
    public Identifier getType() {
        return DepositTypes.BIF;
    }

    @Override
    public MapCodec<? extends Deposit> codec() {
        return CODEC;
    }
}
