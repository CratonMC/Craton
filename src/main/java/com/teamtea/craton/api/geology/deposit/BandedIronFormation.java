package com.teamtea.craton.api.geology.deposit;

import com.teamtea.craton.common.worldgen.deposit.DepositCandidateSampler;
import com.teamtea.craton.common.worldgen.deposit.DepositRandomSequences;
import com.teamtea.craton.common.worldgen.deposit.MineralizationField;
import com.teamtea.craton.common.worldgen.geology.GeologyFieldSampler;
import com.teamtea.craton.common.worldgen.noise.GeologicalNoise;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.registry.CratonBlocks;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.*;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamtea.craton.api.geology.ore.OreType;
import com.teamtea.craton.common.registry.CratonRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.codec.RegistryFixedCodec;
import net.minecraft.resources.Identifier;

public class BandedIronFormation extends AbstractDeposit {
    private final Holder<OreType> ore;
    private final double length,width,thickness,dipMin,dipMax,bandScale,enrichmentScale,oreDensity,frequency,broadNoise,detailNoise;
    private final int satelliteCount;
    private final Abundance abundance;

    public BandedIronFormation(Holder<OreType> ore,double length,double width,double thickness,
                               double dipMin,double dipMax,double bandScale,double enrichmentScale,double oreDensity,
                               double frequency,int satelliteCount,double broadNoise,double detailNoise) {
        this(ore,length,width,thickness,dipMin,dipMax,bandScale,enrichmentScale,oreDensity,frequency,satelliteCount,broadNoise,detailNoise,Abundance.BANDED);
    }
    public BandedIronFormation(Holder<OreType> ore,double length,double width,double thickness,
                               double dipMin,double dipMax,double bandScale,double enrichmentScale,double oreDensity,
                               double frequency,int satelliteCount,double broadNoise,double detailNoise,Abundance abundance) {
        super(resolvePlacement(length,width,frequency,broadNoise,detailNoise));
        this.abundance=abundance;
        this.ore=ore;
        this.length=length;
        this.width=width;
        this.thickness=thickness;
        this.dipMin=dipMin;
        this.dipMax=dipMax;
        this.bandScale=bandScale;
        this.enrichmentScale=enrichmentScale;
        this.oreDensity=oreDensity;
        this.frequency=frequency;
        this.satelliteCount=satelliteCount;
        this.broadNoise=broadNoise;
        this.detailNoise=detailNoise;
    }

    public Abundance abundance(){return abundance;}
    public Holder<OreType> ore() { return ore; }
    public double length() { return length; }
    public double width() { return width; }
    public double thickness() { return thickness; }
    public double dipMin() { return dipMin; }
    public double dipMax() { return dipMax; }
    public double bandScale() { return bandScale; }
    public double enrichmentScale() { return enrichmentScale; }
    public double oreDensity() { return oreDensity; }
    public double frequency() { return frequency; }
    public int satelliteCount() { return satelliteCount; }
    public double broadNoise() { return broadNoise; }
    public double detailNoise() { return detailNoise; }

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
                    Codec.doubleRange(0,1).optionalFieldOf("ore_density",1.0)
                            .forGetter(BandedIronFormation::oreDensity),
                    Codec.doubleRange(0,1).optionalFieldOf("frequency",.25).forGetter(BandedIronFormation::frequency),
                    Codec.intRange(0,24).optionalFieldOf("satellite_count",8).forGetter(BandedIronFormation::satelliteCount),
                    Codec.doubleRange(0,2).optionalFieldOf("broad_noise",.32).forGetter(BandedIronFormation::broadNoise),
                    Codec.doubleRange(0,2).optionalFieldOf("detail_noise",.075).forGetter(BandedIronFormation::detailNoise),
                    Abundance.CODEC.optionalFieldOf("abundance",Abundance.BANDED).forGetter(BandedIronFormation::abundance)
            ).apply(instance, BandedIronFormation::new)).flatXmap(BandedIronFormation::validate,BandedIronFormation::validate);

    private static DataResult<BandedIronFormation> validate(BandedIronFormation b) {
        for(double v:new double[]{b.length,b.width,b.thickness,b.dipMin,b.dipMax,b.bandScale,b.enrichmentScale,b.oreDensity,b.frequency,b.broadNoise,b.detailNoise})
            if(!Double.isFinite(v)) return DataResult.error(() -> "BIF numbers must be finite");
        if(b.dipMin<0||b.dipMax>90||b.dipMin>b.dipMax||b.bandScale<=0||b.enrichmentScale<=0)
            return DataResult.error(() -> "Invalid BIF dip range or band/enrichment scale");
        return DataResult.success(b);
    }

    @Override
    public Identifier getType() {
        return DepositTypes.BIF;
    }

    @Override public DepositRandomSequences sequence(){return DepositRandomSequences.STRATIFORM;}

    private static Placement resolvePlacement(double length,double width,double frequency,
                                              double broadNoise,double detailNoise) {
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

    public record BifColumnHit(BandedIronFormation deposit,double centerY,double horizontalScore,
                                double satelliteScore,long gradeSeed,long occupancySeed,double halfThickness,GradeCache noise){
        public BifColumnHit(BandedIronFormation deposit,double centerY,double horizontalScore,double satelliteScore,
                            long gradeSeed,long occupancySeed,double halfThickness){
            this(deposit,centerY,horizontalScore,satelliteScore,gradeSeed,occupancySeed,halfThickness,
                    new GradeCache(gradeSeed,occupancySeed));
        }
    }

    /** Exact 3-D caches plus lazy 2-D grade; no grade work until a voxel reaches the body. */
    public static class GradeCache {
        private final long seed;
        private final GeologicalNoise.CachedFbm banding;
        private final GeologicalNoise.CachedOccupancy occupancy;
        private boolean initialized;
        private int columnX,columnZ;
        private double enrichment;
        private BandedIronFormation definition;
        private MineralizationField abundanceField;
        private Abundance abundanceConfig;
        private boolean enriched(BandedIronFormation deposit,int x,int y,int z){
            if(abundanceField==null||abundanceConfig!=deposit.abundance()){
                abundanceConfig=deposit.abundance();
                abundanceField=new MineralizationField(abundanceConfig,seed,seed^0xB1F23L);
            }
            return abundanceField.contains(x,y,z);
        }
        private GradeCache(long gradeSeed,long occupancySeed){
            seed=gradeSeed;
            banding=new GeologicalNoise.CachedFbm(gradeSeed,.075,2);
            occupancy=new GeologicalNoise.CachedOccupancy(occupancySeed,.16);
        }
        private double enrichment(BandedIronFormation deposit,int x,int z){
            if(!initialized||columnX!=x||columnZ!=z||definition!=deposit){
                enrichment=.5+.5*GeologicalNoise.fbm2(x,z,seed^0xB1F0L,1/Math.max(16,deposit.enrichmentScale()),3);
                initialized=true;columnX=x;columnZ=z;definition=deposit;
            }
            return enrichment;
        }
    }

    public record BifData(List<DepositCandidateSampler.Candidate> candidates,List<BifColumnHit> hits){}

    private static final BifData EMPTY_COLUMN=new BifData(List.of(),List.of());

    /** One bounded cache per generation context; invalid anchors are cached as well. */
    public static class AnchorCache {
        private final Map<DepositCandidateSampler.Candidate,Anchor> anchors=new java.util.LinkedHashMap<>(128,.75f,true){
            @Override protected boolean removeEldestEntry(Map.Entry<DepositCandidateSampler.Candidate,Anchor> e){return size()>1024;}
        };
        private GeologyFieldSampler geology;
        private Anchor get(BandedIronFormation bif,GeologyFieldSampler sampler,DepositCandidateSampler.Candidate c){
            if(geology!=sampler){anchors.clear();geology=sampler;}
            return anchors.computeIfAbsent(c,key -> bif.prepareAnchor(sampler,key));
        }
    }
    private record Pod(long seed,double along,double across,double radiusAlong,double radiusAcross){}
    private record Anchor(int layer,double strikeX,double strikeZ,Pod[] pods,GradeCache noise){}
    private static final Anchor INVALID_ANCHOR=new Anchor(-1,1,0,new Pod[0],null);

    private Anchor prepareAnchor(GeologyFieldSampler geology,DepositCandidateSampler.Candidate candidate){
        List<Integer> eligible=geology.matchingLayersAt(
                CratonBlocks.GNEISS.getOrigin().getBaseBlock().defaultBlockState(),candidate.x(),candidate.z());
        if(eligible.isEmpty()) return INVALID_ANCHOR;
        int layer=eligible.get((int)Math.floor(rand01(candidate.verticalSeed())*eligible.size())%eligible.size());
        double rawGx=geology.boundaryY(layer,candidate.x()+4,candidate.z())-geology.boundaryY(layer,candidate.x()-4,candidate.z());
        double rawGz=geology.boundaryY(layer,candidate.x(),candidate.z()+4)-geology.boundaryY(layer,candidate.x(),candidate.z()-4);
        double dip=Math.toDegrees(Math.atan(Math.hypot(rawGx/8.0,rawGz/8.0)));
        if(dip<dipMin()||dip>dipMax()) return INVALID_ANCHOR;
        double sx=-rawGz,sz=rawGx,len=Math.sqrt(sx*sx+sz*sz);
        if(len<1E-6){sx=1;sz=0;}else{sx/=len;sz/=len;}
        Pod[] pods=new Pod[Math.min(24,satelliteCount())];
        double a=Math.max(1,length()*.5),b=Math.max(1,width()*.5);
        for(int i=0;i<pods.length;i++){
            long seed=DepositCandidateSampler.mix(candidate.shapeSeed()^(0x9E3779B97F4A7C15L*(i+1)));
            double theta=rand01(seed)*Math.PI*2,radial=1.04+rand01(seed^0xA17L)*.20;
            pods[i]=new Pod(seed,a*radial*Math.cos(theta),b*radial*Math.sin(theta),
                    8+rand01(seed^0xB31L)*13,5+rand01(seed^0xC49L)*9);
        }
        return new Anchor(layer,sx,sz,pods,new GradeCache(candidate.gradeSeed(),candidate.occupancySeed()));
    }

    public static BifData prepareBif(List<DepositCandidateSampler.Candidate> candidates,
            Map<DepositCandidateSampler.Candidate,Deposit> owners,GeologyFieldSampler geology,int x,int z){
        return prepareBif(candidates,owners,geology,x,z,new AnchorCache());
    }

    public static BifData prepareBif(List<DepositCandidateSampler.Candidate> candidates,
            Map<DepositCandidateSampler.Candidate,Deposit> owners,GeologyFieldSampler geology,int x,int z,AnchorCache cache){
        if(candidates.isEmpty()) return EMPTY_COLUMN;
        return new BifData(candidates,prepareBandedIronFormations(candidates,owners,geology,x,z,cache));
    }

    private static List<BifColumnHit> prepareBandedIronFormations(List<DepositCandidateSampler.Candidate> candidates,
            Map<DepositCandidateSampler.Candidate,Deposit> owners,GeologyFieldSampler geology,int x,int z,AnchorCache cache){
        List<BifColumnHit> hits=new ArrayList<>();
        for(DepositCandidateSampler.Candidate candidate:candidates){
            BandedIronFormation bif=(BandedIronFormation)owners.get(candidate);
            Anchor anchor=cache.get(bif,geology,candidate);
            if(anchor.layer()<0) continue;
            int targetLayer=anchor.layer();
            double horizontalScore=horizontalScore(bif,anchor,x,z,candidate);
            double satelliteScore=satelliteScore(bif,anchor,x,z,candidate);
                if(horizontalScore<-.24&&satelliteScore<-.20) continue;
                double topBoundary=geology.boundaryY(targetLayer,x,z);
                double bottomBoundary=targetLayer+1<geology.layers().size()
                        ?geology.boundaryY(targetLayer+1,x,z):geology.minY();
                double available=topBoundary-bottomBoundary;
                if(available<=1E-9) continue;
                double halfThickness=Math.min(bif.thickness()*.5,available*.45);
                double verticalOffset=GeologicalNoise.fbm2(x,z,candidate.shapeSeed(),.035,2)
                        *Math.min(Math.min(halfThickness*.22,1.5),available*.05);
                // Keep the ore-bearing core below the mixed contact with the overlying rock.
                double inset=Math.min(available*.5,Math.max(4.0,bif.thickness()*.5+2.0));
                double centerY=topBoundary-inset+verticalOffset;
                hits.add(new BifColumnHit(bif,centerY,horizontalScore,satelliteScore,
                        candidate.gradeSeed(),candidate.occupancySeed(),halfThickness,anchor.noise()));
        }
        return hits;
    }

    public BlockState placeHit(BifColumnHit hit,BlockState host,int x,int y,int z){
            if(!hit.noise().enriched(this,x,y,z))return null;
            double halfThickness=hit.halfThickness();
            double dy=y+.5-hit.centerY();
            // Ellipsoidal section: the iron lens thins out at both ends.
            double body=hit.horizontalScore()-sq(dy/halfThickness);
            double satellite=hit.satelliteScore()-sq(dy/Math.min(2.2,halfThickness));
            if(body<-.20&&satellite<-.16) return null;

            double envelope=GeologicalNoise.smoothstep(-.18,.48,body);
            double phase=(y+.5-hit.centerY())/Math.max(2,bandScale());
            double banding=GeologicalNoise.clamp(.5+.35*Math.sin(phase*Math.PI*2)
                    +.15*hit.noise().banding.sample(x,y,z),0,1);
            double enrichment=hit.noise().enrichment(this,x,z);
            double probability=oreDensity()*(.08+.74*envelope)*(.35+.35*banding+.30*enrichment);
            double occupancy=hit.noise().occupancy.sample(x,y,z);
            if(body>-.20&&occupancy<probability) return ore().value().getOreState(host);
            double satelliteChance=oreDensity()*GeologicalNoise.smoothstep(-.16,.42,satellite)*(.24+.35*banding);
            if(satellite>-.16&&occupancy<satelliteChance) return ore().value().getOreState(host);
        return null;
    }

    private static double horizontalScore(BandedIronFormation bif,Anchor anchor,
                                          int x,int z,DepositCandidateSampler.Candidate candidate){
        int anchorX=candidate.x(),anchorZ=candidate.z();
        
        double dx=x+.5-anchorX,dz=z+.5-anchorZ,along=dx*anchor.strikeX()+dz*anchor.strikeZ();
        double across=-dx*anchor.strikeZ()+dz*anchor.strikeX();
        double a=Math.max(1,bif.length()*.5),b=Math.max(1,bif.width()*.5);
        double normalized=sq(along/a)+sq(across/b);
        double warp=GeologicalNoise.fbm2(x,z,candidate.shapeSeed(),.006,3)*bif.broadNoise()
                +GeologicalNoise.fbm2(x,z,candidate.shapeSeed()^0x5DEECE66DL,.052,2)*bif.detailNoise();
        return 1-normalized+warp;
    }

    private static double satelliteScore(BandedIronFormation bif,Anchor anchor,
                                         int x,int z,DepositCandidateSampler.Candidate candidate){
        
        double dx=x+.5-candidate.x(),dz=z+.5-candidate.z();
        double along=dx*anchor.strikeX()+dz*anchor.strikeZ();
        double across=-dx*anchor.strikeZ()+dz*anchor.strikeX();
        double a=Math.max(1,bif.length()*.5),b=Math.max(1,bif.width()*.5);
        if(Math.abs(along)>a*1.25+25||Math.abs(across)>b*1.25+25) return -9;
        double best=-9;
        for(Pod pod:anchor.pods()){
            long seed=pod.seed();
            double localAlong=along-pod.along(),localAcross=across-pod.across();
            if(Math.abs(localAlong)>25||Math.abs(localAcross)>19) continue;
            double radiusAlong=pod.radiusAlong(),radiusAcross=pod.radiusAcross();
            double broad=GeologicalNoise.fbm2(x,z,seed,.026,3)*.23;
            double detail=GeologicalNoise.fbm2(x,z,seed^0xD52L,.085,2)*.09;
            best=Math.max(best,1-sq(localAlong/radiusAlong)-sq(localAcross/radiusAcross)+broad+detail);
        }
        return best;
    }

}
