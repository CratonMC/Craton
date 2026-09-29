package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamtea.craton.api.geology.ore.OreType;
import com.teamtea.craton.common.registry.CratonRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.codec.RegistryFixedCodec;
import net.minecraft.resources.Identifier;

public record BandedIronFormation(
        Holder<OreType> ore,
        double length,
        double width,
        double thickness,
        double dipMin,
        double dipMax,
        double bandScale,
        double enrichmentScale,
        double frequency,
        int satelliteCount,
        double broadNoise,
        double detailNoise
) implements Deposit {

    public static final MapCodec<BandedIronFormation> CODEC =
            RecordCodecBuilder.<BandedIronFormation>mapCodec(instance -> instance.group(
                    RegistryFixedCodec.create(CratonRegistries.ORE_TYPE).fieldOf("ore")
                            .forGetter(BandedIronFormation::ore),
                    Codec.doubleRange(1,4096).fieldOf("length")
                            .forGetter(BandedIronFormation::length),
                    Codec.doubleRange(1,4096).fieldOf("width")
                            .forGetter(BandedIronFormation::width),
                    Codec.doubleRange(.1,1024).fieldOf("thickness")
                            .forGetter(BandedIronFormation::thickness),
                    Codec.DOUBLE.fieldOf("dip_min")
                            .forGetter(BandedIronFormation::dipMin),
                    Codec.DOUBLE.fieldOf("dip_max")
                            .forGetter(BandedIronFormation::dipMax),
                    Codec.DOUBLE.fieldOf("band_scale")
                            .forGetter(BandedIronFormation::bandScale),
                    Codec.DOUBLE.fieldOf("enrichment_scale")
                            .forGetter(BandedIronFormation::enrichmentScale),
                    Codec.doubleRange(0,1).optionalFieldOf("frequency",.25).forGetter(BandedIronFormation::frequency),
                    Codec.intRange(0,24).optionalFieldOf("satellite_count",8).forGetter(BandedIronFormation::satelliteCount),
                    Codec.doubleRange(0,2).optionalFieldOf("broad_noise",.32).forGetter(BandedIronFormation::broadNoise),
                    Codec.doubleRange(0,2).optionalFieldOf("detail_noise",.075).forGetter(BandedIronFormation::detailNoise)
            ).apply(instance, BandedIronFormation::new)).flatXmap(BandedIronFormation::validate,BandedIronFormation::validate);

    private static DataResult<BandedIronFormation> validate(BandedIronFormation b) {
        for(double v:new double[]{b.length,b.width,b.thickness,b.dipMin,b.dipMax,b.bandScale,b.enrichmentScale,b.frequency,b.broadNoise,b.detailNoise})
            if(!Double.isFinite(v)) return DataResult.error(() -> "BIF numbers must be finite");
        if(b.dipMin<0||b.dipMax>90||b.dipMin>b.dipMax||b.bandScale<=0||b.enrichmentScale<=0)
            return DataResult.error(() -> "Invalid BIF dip range or band/enrichment scale");
        return DataResult.success(b);
    }

    @Override
    public Identifier getType() {
        return DepositTypes.BIF;
    }

    @Override
    public Placement placement() {
        int cellSize=Math.max(64,(int)Math.ceil(Math.max(length,width)));
        double bodyReach=Math.hypot(length*.5,width*.5)
                *Math.max(1.25,Math.sqrt(1.24+broadNoise+detailNoise))+36;
        double sourceReach=Math.hypot(length*.5,width*.5)+90;
        return new Placement(cellSize,2,Math.max(bodyReach,sourceReach),frequency);
    }

    @Override
    public MapCodec<? extends Deposit> codec() {
        return CODEC;
    }
}
