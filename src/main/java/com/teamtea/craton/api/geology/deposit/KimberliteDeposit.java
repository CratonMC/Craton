package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.core.DepositRandomSequences;

import net.minecraft.world.level.block.state.BlockState;
import com.teamtea.craton.common.core.DepositFieldEngine.ColumnContext;
import com.teamtea.craton.common.core.*;
import static com.teamtea.craton.common.core.DepositFieldSupport.*;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamtea.craton.api.geology.ore.OreType;
import com.teamtea.craton.common.registry.CratonRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.codec.RegistryFixedCodec;

import java.util.List;

public final class KimberliteDeposit extends OrdinaryDeposit {
    public static final MapCodec<KimberliteDeposit> CODEC=Configuration.CODEC.fieldOf("settings")
            .flatXmap(config -> validate(config.base(),2,false)
                    .map(valid -> new KimberliteDeposit(valid,config.richBody())),
                    deposit -> DataResult.success(new Configuration(deposit.settings(),deposit.richBody())));

    private final RichBody richBody;

    public KimberliteDeposit(Settings settings,RichBody richBody) {
        super(DepositTypes.KIMBERLITE,settings,DepositRandomSequences.SPECIAL,50);
        this.richBody=richBody;
    }

    public RichBody richBody() { return richBody; }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }

    public record RichBody(double chance,double radiusMin,double radiusMax) {
        public static final RichBody NONE=new RichBody(0,2.2,4.2);
        public static final Codec<RichBody> CODEC=RecordCodecBuilder.<RichBody>create(i -> i.group(
                Codec.doubleRange(0,1).fieldOf("chance").forGetter(RichBody::chance),
                Codec.doubleRange(0,32).fieldOf("radius_min").forGetter(RichBody::radiusMin),
                Codec.doubleRange(0,32).fieldOf("radius_max").forGetter(RichBody::radiusMax)
        ).apply(i,RichBody::new)).flatXmap(r -> r.radiusMin()>r.radiusMax()||r.chance()>0&&r.radiusMin()<=0
                ?DataResult.error(() -> "Rich body radius range must be positive and ordered when chance is nonzero")
                :DataResult.success(r),DataResult::success);
    }

    private record Configuration(Placement placement,Shape shape,BlockState rock,
                                 List<Holder<OreType>> ores,List<BlockState> alterationRocks,RichBody richBody) {
        private Configuration(Settings base,RichBody richBody) {
            this(base.placement(),base.shape(),base.rock(),base.ores(),base.alterationRocks(),richBody);
        }

        private Settings base() { return new Settings(placement,shape,rock,ores,alterationRocks); }

        private static final Codec<Configuration> CODEC=RecordCodecBuilder.create(i -> i.group(
                Placement.CODEC.fieldOf("placement").forGetter(Configuration::placement),
                Shape.CODEC.fieldOf("shape").forGetter(Configuration::shape),
                BlockState.CODEC.fieldOf("rock").forGetter(Configuration::rock),
                RegistryFixedCodec.create(CratonRegistries.ORE_TYPE).listOf().fieldOf("ores").forGetter(Configuration::ores),
                BlockState.CODEC.listOf().optionalFieldOf("alteration_rocks",List.of()).forGetter(Configuration::alterationRocks),
                RichBody.CODEC.optionalFieldOf("rich_body",RichBody.NONE).forGetter(Configuration::richBody)
        ).apply(i,Configuration::new));
    }



    @Override
    public BlockState place(ColumnContext ctx,DepositCandidateSampler.Candidate c,
                            BlockState originalHost,BlockState state,int y){
        Shape config=settings().shape();
        double cy=config.height(ctx.minY(),c.verticalSeed());
        double bottom=cy-config.y(c.shapeSeed()),top=cy+config.radiusYMax();
        if(y<bottom||y>top) return null;
        double t=(y-bottom)/(top-bottom);
        double radius=config.radiusXMin()+(config.x(c.shapeSeed())-config.radiusXMin())*Math.pow(t,.78);
        double dx=ctx.x()+.5-c.x()+GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.shapeSeed(),.020,3)*5;
        double dz=ctx.z()+.5-c.z()+GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.shapeSeed()^71,.020,3)*5;
        double score=1-Math.hypot(dx,dz)/radius
                +shapeNoise(ctx.x(),y,ctx.z(),c.shapeSeed()^0xB64L,.011,.060,config.broadNoise(),config.detailNoise());
        if(score<-.12) return null;
        double p=GeologicalNoise.smoothstep(-.12,.25,score);
        if(GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.occupancySeed(),.16)>p) return null;
        BlockState pipe=settings().rock();
        RichBody rich=richBody();
        if(rand01(c.oreSeed()^0xD1A0L)<rich.chance()){
            double coreY=bottom+(top-bottom)*(.42+.18*rand01(c.shapeSeed()^0xC0DEL));
            double coreT=(coreY-bottom)/(top-bottom);
            double coreRadius=config.radiusXMin()+(config.x(c.shapeSeed())-config.radiusXMin())*Math.pow(coreT,.78);
            double coreX=c.x()+(rand01(c.shapeSeed()^0x51L)-.5)*coreRadius*.40;
            double coreZ=c.z()+(rand01(c.shapeSeed()^0xA7L)-.5)*coreRadius*.40;
            double size=(rich.radiusMin()+(rich.radiusMax()-rich.radiusMin())*rand01(c.shapeSeed()^0xD1L))
                    *config.satelliteScale();
            double body=1-sq((ctx.x()+.5-coreX)/size)-sq((y+.5-coreY)/(size*.80))
                    -sq((ctx.z()+.5-coreZ)/size)
                    +shapeNoise(ctx.x(),y,ctx.z(),c.shapeSeed()^0xD1A0L,.035,.11,.12,.07);
            if(body>0) return ore(this,1,pipe);
        }
        return pipe;
    }

}
