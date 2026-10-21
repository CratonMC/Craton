package com.teamtea.craton.api.geology.surface;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Datapack-defined distribution profile; generation algorithms are implemented in Java. */
public record SurfaceSedimentType(Pattern pattern, int thickness) {
    public enum Pattern {
        BED("bed"), PATCH("patch");

        private final String id;
        Pattern(String id) { this.id = id; }
        public String id() { return id; }

        public static final Codec<Pattern> CODEC = Codec.STRING.comapFlatMap(
                id -> {
                    for (Pattern pattern : values()) {
                        if (pattern.id.equals(id)) return DataResult.success(pattern);
                    }
                    return DataResult.error(() -> "Unknown surface sediment pattern: " + id);
                },
                Pattern::id);
    }

    public static final Codec<SurfaceSedimentType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Pattern.CODEC.fieldOf("pattern").forGetter(SurfaceSedimentType::pattern),
            Codec.intRange(1, 16).optionalFieldOf("thickness", 3).forGetter(SurfaceSedimentType::thickness)
    ).apply(instance, SurfaceSedimentType::new));
}
