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
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Function;

/** Common immutable state for typed, continuous deposit definitions. */
public abstract class FieldDeposit extends AbstractDeposit {
    private final Identifier type;
    private final Settings settings;

    protected FieldDeposit(Identifier type, Settings settings) {
        super(resolvePlacement(settings));
        this.type=type;
        this.settings=settings;
    }

    public Identifier type() { return type; }
    public Settings settings() { return settings; }

    private static Deposit.Placement resolvePlacement(Settings settings) {
        Placement p=settings.placement();
        return new Deposit.Placement(p.cellSize(),p.maxCandidates(),
                Math.max(p.reach(),settings.shape().horizontalReach()),p.frequency());
    }
    protected static <D extends FieldDeposit> MapCodec<D> codecFor(
            int slots, boolean contact, Function<Settings,D> factory) {
        return Settings.CODEC.fieldOf("settings").flatXmap(settings -> {
            DataResult<Settings> valid=validate(settings,slots,contact);
            return valid.map(factory);
        },field -> DataResult.success(field.settings()));
    }

    protected static DataResult<Settings> validate(Settings settings,int slots,boolean contact) {
        Shape shape=settings.shape();
        if(Math.max(settings.placement().reach(),shape.horizontalReach())/settings.placement().cellSize()>8)
            return DataResult.error(() -> "Deposit reach must not exceed eight placement cells; increase cell_size");
        if(!contact&&(shape.radiusXMin()<=0||shape.radiusZMin()<=0||shape.radiusYMin()<=0))
            return DataResult.error(() -> "Non-contact deposits require positive radii");
        if(settings.ores().size()<slots)
            return DataResult.error(() -> "Deposit requires at least "+slots+" ore references");
        return DataResult.success(settings);
    }

    @Override public Identifier getType() { return type; }
    @Override public abstract MapCodec<? extends Deposit> codec();

    public record Settings(Placement placement, Shape shape, BlockState rock, List<Holder<OreType>> ores,
                           List<BlockState> alterationRocks) {
        public static final Codec<Settings> CODEC = RecordCodecBuilder.create(i -> i.group(
                Placement.CODEC.fieldOf("placement").forGetter(Settings::placement),
                Shape.CODEC.fieldOf("shape").forGetter(Settings::shape),
                BlockState.CODEC.fieldOf("rock").forGetter(Settings::rock),
                RegistryFixedCodec.create(CratonRegistries.ORE_TYPE).listOf().fieldOf("ores").forGetter(Settings::ores),
                BlockState.CODEC.listOf().optionalFieldOf("alteration_rocks",List.of()).forGetter(Settings::alterationRocks)
        ).apply(i, Settings::new));
    }

    public record Placement(int cellSize, int maxCandidates, double reach, double frequency) {
        public static final Codec<Placement> CODEC = RecordCodecBuilder.<Placement>create(i -> i.group(
                Codec.intRange(16,4096).fieldOf("cell_size").forGetter(Placement::cellSize),
                Codec.intRange(0,8).fieldOf("max_candidates").forGetter(Placement::maxCandidates),
                Codec.doubleRange(0,4096).fieldOf("reach").forGetter(Placement::reach),
                Codec.doubleRange(0,1).fieldOf("frequency").forGetter(Placement::frequency)
        ).apply(i, Placement::new)).flatXmap(Placement::validate,Placement::validate);
        private static DataResult<Placement> validate(Placement p) {
            if(!Double.isFinite(p.reach)||!Double.isFinite(p.frequency))
                return DataResult.error(() -> "Placement numbers must be finite");
            return DataResult.success(p);
        }
    }

    public record Shape(double radiusXMin, double radiusXMax, double radiusZMin, double radiusZMax,
                        double radiusYMin, double radiusYMax, double heightMin, double heightMax,
                        double broadNoise, double detailNoise, int satelliteCount, double satelliteScale) {
        public static final Codec<Shape> CODEC = RecordCodecBuilder.<Shape>create(i -> i.group(
                Codec.doubleRange(0,1024).fieldOf("radius_x_min").forGetter(Shape::radiusXMin),
                Codec.doubleRange(0,1024).fieldOf("radius_x_max").forGetter(Shape::radiusXMax),
                Codec.doubleRange(0,1024).fieldOf("radius_z_min").forGetter(Shape::radiusZMin),
                Codec.doubleRange(0,1024).fieldOf("radius_z_max").forGetter(Shape::radiusZMax),
                Codec.doubleRange(0,1024).fieldOf("radius_y_min").forGetter(Shape::radiusYMin),
                Codec.doubleRange(0,1024).fieldOf("radius_y_max").forGetter(Shape::radiusYMax),
                Codec.doubleRange(-2048,2048).fieldOf("height_min").forGetter(Shape::heightMin),
                Codec.doubleRange(-2048,2048).fieldOf("height_max").forGetter(Shape::heightMax),
                Codec.doubleRange(0,2).fieldOf("broad_noise").forGetter(Shape::broadNoise),
                Codec.doubleRange(0,2).fieldOf("detail_noise").forGetter(Shape::detailNoise),
                Codec.intRange(0,24).optionalFieldOf("satellite_count", 6).forGetter(Shape::satelliteCount),
                Codec.doubleRange(0,4).optionalFieldOf("satellite_scale", 1.0).forGetter(Shape::satelliteScale)
        ).apply(i, Shape::new)).flatXmap(Shape::validate,Shape::validate);

        private static DataResult<Shape> validate(Shape s) {
            for(double v:new double[]{s.radiusXMin,s.radiusXMax,s.radiusZMin,s.radiusZMax,s.radiusYMin,s.radiusYMax,
                    s.heightMin,s.heightMax,s.broadNoise,s.detailNoise,s.satelliteScale})
                if(!Double.isFinite(v)) return DataResult.error(() -> "Shape numbers must be finite");
            if(s.radiusXMin>s.radiusXMax||s.radiusZMin>s.radiusZMax||s.radiusYMin>s.radiusYMax||s.heightMin>s.heightMax)
                return DataResult.error(() -> "Shape minimum must not exceed maximum");
            return DataResult.success(s);
        }

        /** Conservative bound includes rotated branches, dip, domain warp and satellite pods. */
        public double horizontalReach() {
            double noise=1.0+broadNoise+detailNoise;
            double body=Math.hypot(radiusXMax*1.65,radiusZMax+radiusYMax*1.4)*noise+48;
            double pods=Math.max(radiusXMax,radiusZMax)*1.27+32*satelliteScale;
            return Math.max(body,pods);
        }

        public double x(long seed) { return radiusXMin + unit(seed) * (radiusXMax - radiusXMin); }
        public double z(long seed) { return radiusZMin + unit(seed) * (radiusZMax - radiusZMin); }
        public double y(long seed) { return radiusYMin + unit(seed) * (radiusYMax - radiusYMin); }
        public double height(int minY, long seed) { return minY + heightMin + unit(seed) * (heightMax - heightMin); }

        private static double unit(long seed) {
            long z = seed;
            z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
            z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
            z ^= z >>> 31;
            return ((z >>> 11) & ((1L << 53) - 1)) * 0x1.0p-53;
        }
    }
}
