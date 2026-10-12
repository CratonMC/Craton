package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamtea.craton.api.geology.ore.OreType;
import com.teamtea.craton.common.registry.CratonRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.codec.RegistryFixedCodec;
import net.minecraft.world.level.block.state.BlockState;
import com.teamtea.craton.common.core.DepositFieldEngine.ColumnContext;
import com.teamtea.craton.common.core.IntrusiveDepositField.IntrusionResult;
import com.teamtea.craton.common.core.*;
import static com.teamtea.craton.common.core.DepositFieldSupport.*;
import java.util.List;


public final class DioriteIntrusionDeposit extends IntrusionDeposit {
    public static final MapCodec<DioriteIntrusionDeposit> CODEC =
            Configuration.CODEC.fieldOf("settings").flatXmap(config -> validate(config.base(),3,false)
                            .map(valid -> new DioriteIntrusionDeposit(valid,config.mineralization())),
                    deposit -> DataResult.success(new Configuration(deposit.settings(),deposit.mineralization)));

    /** Chance applies once per intrusion, not independently to each block. */
    public record Mineralization(double fertileChance,double radiusFraction,double heightFraction,double oreDensity) {
        public static final Mineralization UNRESTRICTED=new Mineralization(1,0,0,1);
        public static final Codec<Mineralization> CODEC=RecordCodecBuilder.<Mineralization>create(i -> i.group(
                Codec.doubleRange(0,1).fieldOf("fertile_chance").forGetter(Mineralization::fertileChance),
                Codec.doubleRange(0,2).fieldOf("radius_fraction").forGetter(Mineralization::radiusFraction),
                Codec.doubleRange(0,2).fieldOf("height_fraction").forGetter(Mineralization::heightFraction),
                Codec.doubleRange(0,1).fieldOf("ore_density").forGetter(Mineralization::oreDensity)
        ).apply(i,Mineralization::new)).flatXmap(m -> (m.radiusFraction()==0)!=(m.heightFraction()==0)
                        ?DataResult.error(() -> "Mineralization radius and height must both be zero or both positive")
                        :DataResult.success(m),DataResult::success);
    }

    private record Configuration(Placement placement,Shape shape,BlockState rock,
                                 List<Holder<OreType>> ores,List<BlockState> alterationRocks,Mineralization mineralization){
        private Configuration(Settings base,Mineralization mineralization){
            this(base.placement(),base.shape(),base.rock(),base.ores(),base.alterationRocks(),mineralization);
        }
        private Settings base(){return new Settings(placement,shape,rock,ores,alterationRocks);}
        private static final Codec<Configuration> CODEC=RecordCodecBuilder.create(i -> i.group(
                Placement.CODEC.fieldOf("placement").forGetter(Configuration::placement),
                Shape.CODEC.fieldOf("shape").forGetter(Configuration::shape),
                BlockState.CODEC.fieldOf("rock").forGetter(Configuration::rock),
                RegistryFixedCodec.create(CratonRegistries.ORE_TYPE).listOf().fieldOf("ores").forGetter(Configuration::ores),
                BlockState.CODEC.listOf().optionalFieldOf("alteration_rocks",List.of()).forGetter(Configuration::alterationRocks),
                Mineralization.CODEC.optionalFieldOf("mineralization",Mineralization.UNRESTRICTED)
                        .forGetter(Configuration::mineralization)
        ).apply(i,Configuration::new));
    }

    private final Mineralization mineralization;

    public DioriteIntrusionDeposit(Settings settings) {
        this(settings,Mineralization.UNRESTRICTED);
    }

    public DioriteIntrusionDeposit(Settings settings,Mineralization mineralization) {
        super(DepositTypes.DIORITE,settings);
        this.mineralization=mineralization;
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }



    @Override
    public Prepared prepare(DepositCandidateSampler.Candidate c,int minY){
        return prepare(c,minY,17,31);
    }

    @Override
    public void prepareColumn(ColumnContext ctx,Prepared prepared,double scoreCutoff){
        prepared.prepareEllipsoidColumn(ctx.x(),ctx.z(),scoreCutoff,7,false);
    }

    @Override
    public void sample(ColumnContext ctx,Prepared prepared,int y,IntrusionResult best){
        if(prepared.outside(y)) return;
        Shape config=settings().shape();
        DepositCandidateSampler.Candidate c=prepared.candidate;
        double rx=prepared.rx,rz=prepared.rz,ry=prepared.ry,cy=prepared.cy;
        double ny=(y+.5-cy)/ry;
        double radialLower=prepared.radialLower;
        double upper=1-Math.sqrt(radialLower*radialLower+ny*ny)+prepared.noiseAmplitude;
        if(prepared.cannotImprove(upper,best)) return;
        double wx=ctx.x()+.5+GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.shapeSeed(),.013,3)*7-c.x();
        double wz=ctx.z()+.5+GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.shapeSeed()^0x91,.013,3)*7-c.z();
        double rotDx=wx,rotDz=wz;
        double rotCos=prepared.rotCos,rotSin=prepared.rotSin;
        double rotAlong=rotDx*rotCos+rotDz*rotSin,rotAcross=-rotDx*rotSin+rotDz*rotCos;
        double rad=Math.sqrt(sq(rotAlong/rx)+sq(rotAcross/rz));
        double score=1-Math.sqrt(rad*rad+ny*ny)
                +shapeNoise(ctx.x(),y,ctx.z(),c.shapeSeed()^0xD1,.010,.046,config.broadNoise(),config.detailNoise());
        double dist=score*Math.min(Math.min(rx,rz),ry);
        if(score>best.score()) best.set(score,dist,c,ny,rad);
    }

    @Override
    public BlockState placeOre(ColumnContext ctx,BlockState host,int y,IntrusionResult intrusion){
        DepositCandidateSampler.Candidate c=intrusion.candidate();
        if(mineralization.oreDensity()<=0||rand01(c.alterationSeed()^0xD10C0A7L)>=mineralization.fertileChance()) return host;
        if(mineralization.radiusFraction()>0&&mineralization.heightFraction()>0){
            double zone=sq(intrusion.radial()/mineralization.radiusFraction())
                    +sq((intrusion.normalizedY()+.05)/mineralization.heightFraction());
            if(zone>1+.12*intrusion.score()) return host;
        }
        double hydro=(1-Math.abs(intrusion.normalizedY()+.05))*.65+intrusion.score()*.55;
        if(hydro<=.48) return host;
        double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.050,3);
        if(occupies(hydro-.35,grade,c,ctx.x(),y,ctx.z(),
                .12,.58,mineralization.oreDensity())){
            double mix=GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.oreSeed(),.105);
            // The sulfide halo is the outer part of the mineralized zone, not
            // the outer part of the whole intrusion. A small radius_fraction
            // must not squeeze the halo into an almost invisible rim.
            double mineralizedRadius=mineralization.radiusFraction()>0
                    ?mineralization.radiusFraction():1.0;
            if(intrusion.radial()>mineralizedRadius*.58&&mix>.43) return ore(this,2,host);
            if(grade>.66&&mix>.64) return ore(this,1,host);
            return ore(this,0,host);
        }
        return host;
    }


}
