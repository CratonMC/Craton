package com.teamtea.craton.api.geology;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.biome.Climate;

import java.util.Optional;

/** Datapack-defined horizontal geology region; depth is deliberately excluded. */
public record GeologyRegion(
        int priority,
        Range continentalness,
        Range erosion,
        Range temperature,
        Range humidity,
        Range peaksAndValleys,
        float transitionWidth
) {
    /** Existing bootstrap calls inherit a small, datapack-configurable climate transition. */
    public GeologyRegion(int priority,Range continentalness,Range erosion,Range temperature,
                         Range humidity,Range peaksAndValleys) {
        this(priority,continentalness,erosion,temperature,humidity,peaksAndValleys,0.02F);
    }

    public static final Codec<GeologyRegion> CODEC = RecordCodecBuilder.<GeologyRegion>create(instance -> instance.group(
            Codec.INT.optionalFieldOf("priority", 0).forGetter(GeologyRegion::priority),
            Range.CODEC.optionalFieldOf("continentalness", Range.ANY).forGetter(GeologyRegion::continentalness),
            Range.CODEC.optionalFieldOf("erosion", Range.ANY).forGetter(GeologyRegion::erosion),
            Range.CODEC.optionalFieldOf("temperature", Range.ANY).forGetter(GeologyRegion::temperature),
            Range.CODEC.optionalFieldOf("humidity", Range.ANY).forGetter(GeologyRegion::humidity),
            Range.CODEC.optionalFieldOf("peaks_and_valleys", Range.ANY).forGetter(GeologyRegion::peaksAndValleys),
            Codec.floatRange(0F,1F).optionalFieldOf("transition_width",0.02F).forGetter(GeologyRegion::transitionWidth)
    ).apply(instance, GeologyRegion::new)).validate(region -> Float.isFinite(region.transitionWidth())
            ?DataResult.success(region):DataResult.error(() -> "Geology transition width must be finite"));

    public boolean matches(Climate.TargetPoint sample) {
        return matches(sample, peaksAndValleys(sample.weirdness()));
    }

    /** Allows the classifier to calculate PV once per column. */
    public boolean matches(Climate.TargetPoint sample, long pv) {
        return continentalness.contains(sample.continentalness())
                && erosion.contains(sample.erosion())
                && temperature.contains(sample.temperature())
                && humidity.contains(sample.humidity())
                && peaksAndValleys.contains(pv);
    }

    /** Ordered alpha coverage: priority still determines which overlapping rule owns the area. */
    public double coverage(Climate.TargetPoint sample,long pv) {
        long width=Climate.quantizeCoord(transitionWidth);
        if(width<=0) return matches(sample,pv)?1:0;
        return continentalness.coverage(sample.continentalness(),width)
                *erosion.coverage(sample.erosion(),width)
                *temperature.coverage(sample.temperature(),width)
                *humidity.coverage(sample.humidity(),width)
                *peaksAndValleys.coverage(pv,width);
    }

    public static long peaksAndValleys(long weirdness) {
        return -3L * (Math.abs(Math.abs(weirdness) - Climate.quantizeCoord(2F / 3F))
                - Climate.quantizeCoord(1F / 3F));
    }

    /** Bounds use the same quantized coordinates as Climate.TargetPoint. */
    public record Range(Optional<Float> min, Optional<Float> max,
                        boolean minInclusive, boolean maxInclusive) {
        public static final Range ANY = new Range(Optional.empty(), Optional.empty(), true, false);

        public static final Codec<Range> CODEC = RecordCodecBuilder.<Range>create(instance -> instance.group(
                Codec.FLOAT.optionalFieldOf("min").forGetter(Range::min),
                Codec.FLOAT.optionalFieldOf("max").forGetter(Range::max),
                Codec.BOOL.optionalFieldOf("min_inclusive", true).forGetter(Range::minInclusive),
                Codec.BOOL.optionalFieldOf("max_inclusive", false).forGetter(Range::maxInclusive)
        ).apply(instance, Range::new)).validate(Range::validate);

        private DataResult<Range> validate() {
            if (min.filter(value -> !Float.isFinite(value)).isPresent()
                    || max.filter(value -> !Float.isFinite(value)).isPresent()) {
                return DataResult.error(() -> "Geology region bounds must be finite");
            }
            if (min.isPresent() && max.isPresent()) {
                long lower = Climate.quantizeCoord(min.get());
                long upper = Climate.quantizeCoord(max.get());
                if (lower > upper || (lower == upper && !(minInclusive && maxInclusive))) {
                    return DataResult.error(() -> "Geology region range is empty after quantization");
                }
            }
            return DataResult.success(this);
        }

        public static Range atLeast(float min) {
            return new Range(Optional.of(min), Optional.empty(), true, false);
        }

        public static Range greaterThan(float min) {
            return new Range(Optional.of(min), Optional.empty(), false, false);
        }

        public static Range below(float max) {
            return new Range(Optional.empty(), Optional.of(max), true, false);
        }

        private double coverage(long value,long width) {
            double result=1;
            if(min.isPresent()) result*=smooth(((double)value-Climate.quantizeCoord(min.get()))/width);
            if(max.isPresent()) result*=smooth((Climate.quantizeCoord(max.get())-(double)value)/width);
            return result;
        }

        private static double smooth(double distance) {
            double t=Math.max(0,Math.min(1,(distance+1)*.5));
            return t*t*(3-2*t);
        }

        public boolean contains(long value) {
            if (min.isPresent()) {
                long lower = Climate.quantizeCoord(min.get());
                if (value < lower || (value == lower && !minInclusive)) return false;
            }
            if (max.isPresent()) {
                long upper = Climate.quantizeCoord(max.get());
                if (value > upper || (value == upper && !maxInclusive)) return false;
            }
            return true;
        }
    }
}
