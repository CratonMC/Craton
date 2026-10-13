package com.teamtea.craton.api.geology.deposit;

import com.teamtea.craton.common.worldgen.deposit.DepositCandidateSampler;
import com.teamtea.craton.common.worldgen.noise.GeologicalNoise;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.worldgen.deposit.DepositRandomSequences;

import net.minecraft.world.level.block.state.BlockState;
import com.teamtea.craton.common.worldgen.deposit.DepositGenerationContext;

import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.*;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamtea.craton.api.geology.ore.OreType;
import com.teamtea.craton.common.registry.CratonRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.codec.RegistryFixedCodec;

import java.util.List;

public class KimberliteDeposit extends OrdinaryDeposit {
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
                                 List<Holder<OreType>> ores,List<BlockState> alterationRocks,Abundance abundance,RichBody richBody) {
        private Configuration(Settings base,RichBody richBody) {
            this(base.placement(),base.shape(),base.rock(),base.ores(),base.alterationRocks(),base.abundance(),richBody);
        }

        private Settings base() { return new Settings(placement,shape,rock,ores,alterationRocks,abundance); }

        private static final Codec<Configuration> CODEC=RecordCodecBuilder.create(i -> i.group(
                Placement.CODEC.fieldOf("placement").forGetter(Configuration::placement),
                Shape.CODEC.fieldOf("shape").forGetter(Configuration::shape),
                BlockState.CODEC.fieldOf("rock").forGetter(Configuration::rock),
                RegistryFixedCodec.create(CratonRegistries.ORE_TYPE).listOf().fieldOf("ores").forGetter(Configuration::ores),
                BlockState.CODEC.listOf().optionalFieldOf("alteration_rocks",List.of()).forGetter(Configuration::alterationRocks),
                Abundance.CODEC.optionalFieldOf("abundance",Abundance.NONE).forGetter(Configuration::abundance),
                RichBody.CODEC.optionalFieldOf("rich_body",RichBody.NONE).forGetter(Configuration::richBody)
        ).apply(i,Configuration::new));
    }

    /** Candidate-fixed values and exact noise caches, owned by the chunk context. */
    private static class Prepared {
        final KimberliteDeposit owner;
        final int minY;
        final double bottom,top,minimumRadius,radiusDelta,coreY,coreX,coreZ,coreSize;
        final boolean rich;
        private boolean columnReady;
        private int columnX,columnZ;
        private double minimumDistance;
        double minimumDistance(DepositCandidateSampler.Candidate c,int x,int z){
            if(!columnReady||columnX!=x||columnZ!=z){
                // Each warp component is bounded by 5; triangle inequality bounds distance.
                minimumDistance=Math.max(0,Math.hypot(x+.5-c.x(),z+.5-c.z())-Math.sqrt(50));
                columnX=x;columnZ=z;columnReady=true;
            }
            return minimumDistance;
        }
        GeologicalNoise.CachedFbm warpX,warpZ,broad,detail;
        GeologicalNoise.CachedFbm coreBroad,coreDetail;
        GeologicalNoise.CachedOccupancy occupancy,oreOccupancy;
        Prepared(KimberliteDeposit owner,DepositCandidateSampler.Candidate c,int minY){
            this.owner=owner;this.minY=minY;
            Shape config=owner.settings().shape();RichBody body=owner.richBody();
            double cy=config.height(minY,c.verticalSeed());
            bottom=cy-config.y(c.shapeSeed());top=cy+config.radiusYMax();
            minimumRadius=config.radiusXMin();radiusDelta=config.x(c.shapeSeed())-minimumRadius;
            rich=rand01(c.oreSeed()^0xD1A0L)<body.chance();
            if(rich){
                coreY=bottom+(top-bottom)*(.42+.18*rand01(c.shapeSeed()^0xC0DEL));
                double coreT=(coreY-bottom)/(top-bottom);
                double coreRadius=minimumRadius+radiusDelta*Math.pow(coreT,.78);
                coreX=c.x()+(rand01(c.shapeSeed()^0x51L)-.5)*coreRadius*.40;
                coreZ=c.z()+(rand01(c.shapeSeed()^0xA7L)-.5)*coreRadius*.40;
                coreSize=(body.radiusMin()+(body.radiusMax()-body.radiusMin())*rand01(c.shapeSeed()^0xD1L))*config.satelliteScale();
            }else{coreY=coreX=coreZ=coreSize=0;}

        }
    }

    @Override
    public BlockState place(DepositGenerationContext ctx,DepositCandidateSampler.Candidate c,
                            BlockState originalHost,BlockState state,int y){
        Shape config=settings().shape();
        Prepared prepared=(Prepared)ctx.columnGeometry(c);
        if(prepared==null){
            prepared=(Prepared)ctx.reusableGeometry(c);
            if(prepared==null||prepared.owner!=this||prepared.minY!=ctx.minY()){
                double cy=config.height(ctx.minY(),c.verticalSeed());
                if(y<cy-config.y(c.shapeSeed())||y>cy+config.radiusYMax()) return null;
                prepared=new Prepared(this,c,ctx.minY());ctx.reusableGeometry(c,prepared);
            }
            ctx.columnGeometry(c,prepared);
        }
        double bottom=prepared.bottom,top=prepared.top;
        if(y<bottom||y>top) return null;
        double t=(y-bottom)/(top-bottom);
        double radius=prepared.minimumRadius+prepared.radiusDelta*Math.pow(t,.78);
        double maximumNoise=Math.abs(config.broadNoise())+Math.abs(config.detailNoise());
        if(radius>0&&prepared.minimumDistance(c,ctx.x(),ctx.z())/radius>1+maximumNoise+.12+1E-9) return null;
        if(prepared.warpX==null){
            prepared.warpX=new GeologicalNoise.CachedFbm(c.shapeSeed(),.020,3);
            prepared.warpZ=new GeologicalNoise.CachedFbm(c.shapeSeed()^71,.020,3);
            prepared.broad=new GeologicalNoise.CachedFbm(c.shapeSeed()^0xB64L,.011,3);
            prepared.detail=new GeologicalNoise.CachedFbm(c.shapeSeed()^0xB64L^0xA4B1C2D3L,.060,2);
        }
        double dx=ctx.x()+.5-c.x()+prepared.warpX.sample(ctx.x(),y,ctx.z())*5;
        double dz=ctx.z()+.5-c.z()+prepared.warpZ.sample(ctx.x(),y,ctx.z())*5;
        double score=1-Math.hypot(dx,dz)/radius
                +(prepared.broad.sample(ctx.x(),y,ctx.z())*config.broadNoise()
                +prepared.detail.sample(ctx.x(),y,ctx.z())*config.detailNoise());
        if(score<-.12) return null;
        double p=GeologicalNoise.smoothstep(-.12,.25,score);
        if(prepared.occupancy==null) prepared.occupancy=new GeologicalNoise.CachedOccupancy(c.occupancySeed(),.16);
        if(prepared.occupancy.sample(ctx.x(),y,ctx.z())>p) return null;
        BlockState pipe=settings().rock();
        if(prepared.rich){
            double coreY=prepared.coreY,coreX=prepared.coreX,coreZ=prepared.coreZ,size=prepared.coreSize;
            if(prepared.coreBroad==null){
                prepared.coreBroad=new GeologicalNoise.CachedFbm(c.shapeSeed()^0xD1A0L,.035,3);
                prepared.coreDetail=new GeologicalNoise.CachedFbm(c.shapeSeed()^0xD1A0L^0xA4B1C2D3L,.11,2);
            }
            double body=1-sq((ctx.x()+.5-coreX)/size)-sq((y+.5-coreY)/(size*.80))
                    -sq((ctx.z()+.5-coreZ)/size)
                    +(prepared.coreBroad.sample(ctx.x(),y,ctx.z())*.12+prepared.coreDetail.sample(ctx.x(),y,ctx.z())*.07);
            if(body>0) return ore(this,1,pipe);
        }
        if(prepared.oreOccupancy==null) prepared.oreOccupancy=new GeologicalNoise.CachedOccupancy(c.oreSeed(),.10);
        if(prepared.oreOccupancy.sample(ctx.x(),y,ctx.z())<.10+.18*p)
            return abundantOre(this,ctx,c,y,0,pipe);
        return pipe;
    }

}
