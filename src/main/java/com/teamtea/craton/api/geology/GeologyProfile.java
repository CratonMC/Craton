package com.teamtea.craton.api.geology;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamtea.craton.common.registry.CratonRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.codec.RegistryFixedCodec;

import java.util.List;

/** A datapack-defined, top-to-bottom stratigraphic sequence for a climate geology region. */
public record GeologyProfile(
        Holder<GeologyRegion> region,
        int priority,
        List<Holder<GeologyLayer>> layers
) {
    public GeologyProfile {
        layers = List.copyOf(layers);
    }

    public GeologyProfile(Holder<GeologyRegion> region, List<Holder<GeologyLayer>> layers) {
        this(region, 0, layers);
    }

    public static final Codec<GeologyProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryFixedCodec.create(CratonRegistries.GEOLOGY_REGION).fieldOf("region").forGetter(GeologyProfile::region),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(GeologyProfile::priority),
            RegistryFixedCodec.create(CratonRegistries.GEOLOGY_LAYER)
                    .listOf().fieldOf("layers").forGetter(GeologyProfile::layers)
    ).apply(instance, GeologyProfile::new));
}
