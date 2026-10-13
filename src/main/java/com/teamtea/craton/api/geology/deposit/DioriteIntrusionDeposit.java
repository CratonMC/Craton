package com.teamtea.craton.api.geology.deposit;

import com.teamtea.craton.common.worldgen.deposit.DepositCandidateSampler;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamtea.craton.api.geology.ore.OreType;
import com.teamtea.craton.common.registry.CratonRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.codec.RegistryFixedCodec;
import net.minecraft.world.level.block.state.BlockState;
import com.teamtea.craton.common.worldgen.deposit.DepositGenerationContext;
import com.teamtea.craton.common.worldgen.deposit.IntrusiveDepositField.IntrusionResult;

import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.*;
import java.util.List;

public class DioriteIntrusionDeposit extends IntrusionDeposit {
    public static final MapCodec<DioriteIntrusionDeposit> CODEC =
            Configuration.CODEC.fieldOf("settings").flatXmap(config -> validate(config.base(),3,false)
                            .map(valid -> new DioriteIntrusionDeposit(valid,config.mineralization())),
                    deposit -> DataResult.success(new Configuration(deposit.settings(),deposit.mineralization)));

    /** Chance applies once per intrusion, not independently to each block. */
    public record Mineralization(double fertileChance,double radiusFraction,double heightFraction,double oreDensity,boolean coherentOre,int clusterMin,int clusterMax,double clusterGap) {
        public Mineralization {
            if(clusterMin<1||clusterMin>8||clusterMax<1||clusterMax>8||!Double.isFinite(clusterGap)||clusterGap<2||clusterGap>32)
                throw new IllegalArgumentException("Invalid porphyry cluster count or gap");
        }
        public Mineralization(double fertileChance,double radiusFraction,double heightFraction,double oreDensity,boolean coherentOre){
            this(fertileChance,radiusFraction,heightFraction,oreDensity,coherentOre,1,1,3);
        }
        public Mineralization(double fertileChance,double radiusFraction,double heightFraction,double oreDensity){
            this(fertileChance,radiusFraction,heightFraction,oreDensity,false);
        }
        public static final Mineralization UNRESTRICTED=new Mineralization(1,0,0,1);
        public static final Codec<Mineralization> CODEC=RecordCodecBuilder.<Mineralization>create(i -> i.group(
                Codec.doubleRange(0,1).fieldOf("fertile_chance").forGetter(Mineralization::fertileChance),
                Codec.doubleRange(0,2).fieldOf("radius_fraction").forGetter(Mineralization::radiusFraction),
                Codec.doubleRange(0,2).fieldOf("height_fraction").forGetter(Mineralization::heightFraction),
                Codec.doubleRange(0,1).fieldOf("ore_density").forGetter(Mineralization::oreDensity),
                Codec.BOOL.optionalFieldOf("coherent_ore",false).forGetter(Mineralization::coherentOre),
                Codec.intRange(1,8).optionalFieldOf("cluster_min",1).forGetter(Mineralization::clusterMin),
                Codec.intRange(1,8).optionalFieldOf("cluster_max",1).forGetter(Mineralization::clusterMax),
                Codec.doubleRange(2,32).optionalFieldOf("cluster_gap",3.0).forGetter(Mineralization::clusterGap)
        ).apply(i,Mineralization::new)).flatXmap(m -> m.clusterMin()>m.clusterMax()
                        ?DataResult.error(() -> "cluster_min exceeds cluster_max")
                        :(m.radiusFraction()==0)!=(m.heightFraction()==0)
                        ?DataResult.error(() -> "Mineralization radius and height must both be zero or both positive")
                        :DataResult.success(m),DataResult::success);
    }

    private record Configuration(Placement placement,Shape shape,BlockState rock,
                                 List<Holder<OreType>> ores,List<BlockState> alterationRocks,Abundance abundance,Mineralization mineralization){
        private Configuration(Settings base,Mineralization mineralization){
            this(base.placement(),base.shape(),base.rock(),base.ores(),base.alterationRocks(),base.abundance(),mineralization);
        }
        private Settings base(){return new Settings(placement,shape,rock,ores,alterationRocks,abundance);}
        private static final Codec<Configuration> CODEC=RecordCodecBuilder.create(i -> i.group(
                Placement.CODEC.fieldOf("placement").forGetter(Configuration::placement),
                Shape.CODEC.fieldOf("shape").forGetter(Configuration::shape),
                BlockState.CODEC.fieldOf("rock").forGetter(Configuration::rock),
                RegistryFixedCodec.create(CratonRegistries.ORE_TYPE).listOf().fieldOf("ores").forGetter(Configuration::ores),
                BlockState.CODEC.listOf().optionalFieldOf("alteration_rocks",List.of()).forGetter(Configuration::alterationRocks),
                Abundance.CODEC.optionalFieldOf("abundance",Abundance.NONE).forGetter(Configuration::abundance),
                Mineralization.CODEC.optionalFieldOf("mineralization",Mineralization.UNRESTRICTED)
                        .forGetter(Configuration::mineralization)
        ).apply(i,Configuration::new));
    }

    private final Mineralization mineralization;
    private final double maximumClusterRadial;

    public DioriteIntrusionDeposit(Settings settings) {
        this(settings,Mineralization.UNRESTRICTED);
    }

    public DioriteIntrusionDeposit(Settings settings,Mineralization mineralization) {
        super(DepositTypes.DIORITE,settings);
        if(mineralization.clusterMin()>mineralization.clusterMax())throw new IllegalArgumentException("cluster_min exceeds cluster_max");
        this.mineralization=mineralization;
        double minimumHorizontal=Math.min(settings.shape().radiusXMin(),settings.shape().radiusZMin());
        double base=.10+.14*mineralization.oreDensity();
        double radius=mineralization.radiusFraction()>0?mineralization.radiusFraction():1;
        double height=mineralization.heightFraction()>0?mineralization.heightFraction():1;
        double vertical=Math.max(2/settings.shape().radiusYMin(),height*base);
        double horizontal=Math.max(1.6/minimumHorizontal,.319*radius*base);
        // Stock-axis derivatives, jitter, voxel snapping and the maximum ellipsoid support.
        // Both stock half-widths are at least .25 in model coordinates.
        maximumClusterRadial=4*(.26*vertical+.043+.708/minimumHorizontal+horizontal)+1e-8;
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }

    @Override
    public Prepared prepare(DepositCandidateSampler.Candidate c,int minY){
        return prepare(c,minY,17,31,.013,0x91,0xD1,.010,.046);
    }

    @Override
    public void prepareColumn(DepositGenerationContext ctx,Prepared prepared,double scoreCutoff){
        prepared.geometry.prepareColumn(ctx,prepared,scoreCutoff);
    }

    @Override
    public void sample(DepositGenerationContext ctx,Prepared prepared,int y,IntrusionResult best){
        if(prepared.outside(y)) return;
        double upper=prepared.geometry.upperBound(prepared,y);
        if(prepared.cannotImprove(upper,best)) return;
        double score=prepared.geometry.sample(prepared,ctx.x(),y,ctx.z());
        if(score>best.score()) best.set(score,score*prepared.minRadius,prepared.candidate,
                prepared.geometry.normalizedY(prepared,y),prepared.geometry.radial(prepared,y));
    }

    public boolean isFertile(DepositCandidateSampler.Candidate candidate){
        return mineralization.oreDensity()>0
                &&rand01(candidate.alterationSeed()^0xD10C0A7L)<mineralization.fertileChance();
    }

    @Override
    public BlockState placeOre(DepositGenerationContext ctx,BlockState host,int y,IntrusionResult intrusion){
        DepositCandidateSampler.Candidate c=intrusion.candidate();
        if(!isFertile(c)) return host;
        if(mineralization.coherentOre())return mineralization.clusterMax()>1
                ?placeClusterOre(ctx,host,y,intrusion):placeCoherentOre(ctx,host,y,intrusion);
        if(mineralization.radiusFraction()>0&&mineralization.heightFraction()>0){
            double zone=sq(intrusion.radial()/mineralization.radiusFraction())
                    +sq((intrusion.normalizedY()+.05)/mineralization.heightFraction());
            if(zone>1+.12*intrusion.score()) return host;
        }
        double hydro=(1-Math.abs(intrusion.normalizedY()+.05))*.65+intrusion.score()*.55;
        if(hydro<=.48) return host;
        if(!ctx.enriched(this,c,y))return host;
        Prepared prepared=ctx.preparedIntrusion(c);
        double grade=prepared.noise().grade(ctx.x(),y,ctx.z());
        if(prepared.occupies(hydro-.35,grade,ctx.x(),y,ctx.z(),
                .12,.58,mineralization.oreDensity())){
            double mix=prepared.noise().mix(ctx.x(),y,ctx.z());
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

    /** One bounded ore body; richness changes its extent instead of cutting it into packets. */
    private BlockState placeCoherentOre(DepositGenerationContext ctx,BlockState host,int y,
                                        IntrusionResult intrusion){
        double radius=mineralization.radiusFraction()>0?mineralization.radiusFraction():1;
        double height=mineralization.heightFraction()>0?mineralization.heightFraction():1;
        double baseExtent=.10+.14*mineralization.oreDensity();
        double radial=intrusion.radial()/radius,vertical=(intrusion.normalizedY()+.05)/height;
        double distance=sq(radial)+sq(vertical);
        double maximumScale=settings().abundance().enabled()?settings().abundance().richScale():1;
        if(distance>=1.12*sq(baseExtent*maximumScale))return host;
        double extent=baseExtent*ctx.enrichmentScale(this,intrusion.candidate());
        double zone=distance/sq(extent);
        // Grade lies in [0,1], so no noise query can admit a point past this bound.
        if(zone>=1.12)return host;
        Prepared prepared=ctx.preparedIntrusion(intrusion.candidate());
        double grade=prepared.noise().grade(ctx.x(),y,ctx.z());
        double support=1-zone+.24*(grade-.5);
        if(support<=0)return host;
        // Preserve a connected interior and allow patchier, gently fading margins.
        if(support<.45&&grade<.43-.18*support)return host;
        double mix=prepared.noise().mix(ctx.x(),y,ctx.z());
        double localRadius=intrusion.radial()/(radius*extent);
        if(localRadius>.58&&mix>.43)return ore(this,2,host);
        if(grade>.66&&mix>.64)return ore(this,1,host);
        return ore(this,0,host);
    }

    private BlockState placeClusterOre(DepositGenerationContext ctx,BlockState host,int y,IntrusionResult intrusion){
        if(intrusion.normalizedY()<-.8||intrusion.normalizedY()>.6||intrusion.radial()>maximumClusterRadial)return host;
        Prepared prepared=ctx.preparedIntrusion(intrusion.candidate());
        var clusters=prepared.porphyry(mineralization);
        double zone=clusters.sample(ctx.x(),y,ctx.z(),1);
        if(zone>=1)return host;
        double scale=ctx.enrichmentScale(this,intrusion.candidate());
        zone/=scale*scale;
        if(zone>=1)return host;
        double grade=prepared.noise().grade(ctx.x(),y,ctx.z());
        if(zone>=.90+.16*(grade-.5))return host;
        double mix=prepared.noise().mix(ctx.x(),y,ctx.z());
        if(clusters.radial()/scale>.58&&mix>.43)return ore(this,2,host);
        if(grade>.66&&mix>.64)return ore(this,1,host);
        return ore(this,0,host);
    }

}
