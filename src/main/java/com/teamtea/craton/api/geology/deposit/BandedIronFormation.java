package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.registry.CratonBlocks;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.teamtea.craton.common.core.*;
import static com.teamtea.craton.common.core.DepositFieldSupport.*;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamtea.craton.api.geology.ore.OreType;
import com.teamtea.craton.common.registry.CratonRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.codec.RegistryFixedCodec;
import net.minecraft.resources.Identifier;

public class BandedIronFormation extends AbstractDeposit {
    private final Holder<OreType> ore;
    private final double length,width,thickness,dipMin,dipMax,bandScale,enrichmentScale,frequency,broadNoise,detailNoise;
    private final int satelliteCount;

    public BandedIronFormation(Holder<OreType> ore,double length,double width,double thickness,
                               double dipMin,double dipMax,double bandScale,double enrichmentScale,
                               double frequency,int satelliteCount,double broadNoise,double detailNoise) {
        super(resolvePlacement(length,width,frequency,broadNoise,detailNoise));
        this.ore=ore;
        this.length=length;
        this.width=width;
        this.thickness=thickness;
        this.dipMin=dipMin;
        this.dipMax=dipMax;
        this.bandScale=bandScale;
        this.enrichmentScale=enrichmentScale;
        this.frequency=frequency;
        this.satelliteCount=satelliteCount;
        this.broadNoise=broadNoise;
        this.detailNoise=detailNoise;
    }

    public Holder<OreType> ore() { return ore; }
    public double length() { return length; }
    public double width() { return width; }
    public double thickness() { return thickness; }
    public double dipMin() { return dipMin; }
    public double dipMax() { return dipMax; }
    public double bandScale() { return bandScale; }
    public double enrichmentScale() { return enrichmentScale; }
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
                                double satelliteScore,long gradeSeed,long occupancySeed){}

    public record BifData(List<DepositCandidateSampler.Candidate> candidates,List<BifColumnHit> hits){}

    private static final BifData EMPTY_COLUMN=new BifData(List.of(),List.of());

    public static BifData prepareBif(List<DepositCandidateSampler.Candidate> candidates,
            Map<DepositCandidateSampler.Candidate,Deposit> owners,GeologyFieldSampler geology,int x,int z){
        if(candidates.isEmpty()) return EMPTY_COLUMN;
        return new BifData(candidates,prepareBandedIronFormations(candidates,owners,geology,x,z));
    }

    private static List<BifColumnHit> prepareBandedIronFormations(List<DepositCandidateSampler.Candidate> candidates,
            Map<DepositCandidateSampler.Candidate,Deposit> owners,GeologyFieldSampler geology,int x,int z){
        List<Integer> gneissLayers=new ArrayList<>();
        for(int i=0;i<geology.layers().size();i++)
            if(geology.layers().get(i).value().blockState().is(CratonBlocks.GNEISS.getOrigin().getBaseBlock())) gneissLayers.add(i);
        if(gneissLayers.isEmpty()) return List.of();

        List<BifColumnHit> hits=new ArrayList<>();
        for(DepositCandidateSampler.Candidate candidate:candidates){
            BandedIronFormation bif=(BandedIronFormation)owners.get(candidate);
                // A BIF instance binds to exactly one eligible gneiss horizon, not every gneiss band in the profile.
                int targetLayer=gneissLayers.get((int)Math.floor(rand01(candidate.verticalSeed())*gneissLayers.size())%gneissLayers.size());
                double gx=(geology.boundaryY(targetLayer,candidate.x()+4,candidate.z())
                        -geology.boundaryY(targetLayer,candidate.x()-4,candidate.z()))/8.0;
                double gz=(geology.boundaryY(targetLayer,candidate.x(),candidate.z()+4)
                        -geology.boundaryY(targetLayer,candidate.x(),candidate.z()-4))/8.0;
                double hostDip=Math.toDegrees(Math.atan(Math.hypot(gx,gz)));
                if(hostDip<bif.dipMin()||hostDip>bif.dipMax()) continue;
                double horizontalScore=horizontalScore(bif,geology,targetLayer,x,z,candidate);
                double satelliteScore=satelliteScore(bif,geology,targetLayer,x,z,candidate);
                if(horizontalScore<-.24&&satelliteScore<-.20) continue;
                double halfThickness=bif.thickness()*.5;
                double verticalOffset=GeologicalNoise.fbm2(x,z,candidate.shapeSeed(),.035,2)*Math.min(halfThickness*.22,1.5);
                double topBoundary=geology.boundaryY(targetLayer,x,z);
                double bottomBoundary=targetLayer+1<geology.layers().size()
                        ?geology.boundaryY(targetLayer+1,x,z):geology.minY();
                double available=Math.max(1,topBoundary-bottomBoundary);
                double inset=Math.min(available*.5,Math.max(2.0,bif.thickness()*.5+1.0));
                double centerY=topBoundary-inset+verticalOffset;
                hits.add(new BifColumnHit(bif,centerY,horizontalScore,satelliteScore,
                        candidate.gradeSeed(),candidate.occupancySeed()));
        }
        return hits;
    }

    public BlockState placeHit(BifColumnHit hit,BlockState host,int x,int y,int z){
            double halfThickness=Math.max(.75,thickness()*.5);
            double verticalScore=1-Math.abs(y+.5-hit.centerY())/halfThickness;
            double body=Math.min(hit.horizontalScore(),verticalScore);
            double satellite=Math.min(hit.satelliteScore(),1-Math.abs(y+.5-hit.centerY())/Math.min(2.2,halfThickness));
            if(body<-.20&&satellite<-.16) return null;

            double envelope=GeologicalNoise.smoothstep(-.18,.48,body);
            double phase=(y+.5-hit.centerY())/Math.max(2,bandScale());
            double banding=GeologicalNoise.clamp(.5+.35*Math.sin(phase*Math.PI*2)
                    +.15*GeologicalNoise.fbm(x,y,z,hit.gradeSeed(),.075,2),0,1);
            double enrichment=.5+.5*GeologicalNoise.fbm2(x,z,hit.gradeSeed()^0xB1F0L,
                    1/Math.max(16,enrichmentScale()),3);
            double probability=(.08+.74*envelope)*(.35+.35*banding+.30*enrichment);
            double occupancy=GeologicalNoise.occupancy(x,y,z,hit.occupancySeed(),.16);
            if(body>-.20&&occupancy<probability) return ore().value().getOreState(host);
            double satelliteChance=GeologicalNoise.smoothstep(-.16,.42,satellite)*(.24+.35*banding);
            if(satellite>-.16&&occupancy<satelliteChance) return ore().value().getOreState(host);
        return null;
    }

    private static double horizontalScore(BandedIronFormation bif,GeologyFieldSampler geology,int layerIndex,
                                          int x,int z,DepositCandidateSampler.Candidate candidate){
        int anchorX=candidate.x(),anchorZ=candidate.z();
        double[] strike=geology.strike(layerIndex,anchorX,anchorZ);
        double dx=x+.5-anchorX,dz=z+.5-anchorZ,along=dx*strike[0]+dz*strike[1];
        double across=-dx*strike[1]+dz*strike[0];
        double a=Math.max(1,bif.length()*.5),b=Math.max(1,bif.width()*.5);
        double normalized=sq(along/a)+sq(across/b);
        double warp=GeologicalNoise.fbm2(x,z,candidate.shapeSeed(),.006,3)*bif.broadNoise()
                +GeologicalNoise.fbm2(x,z,candidate.shapeSeed()^0x5DEECE66DL,.052,2)*bif.detailNoise();
        return 1-normalized+warp;
    }

    private static double satelliteScore(BandedIronFormation bif,GeologyFieldSampler geology,int layerIndex,
                                         int x,int z,DepositCandidateSampler.Candidate candidate){
        double[] strike=geology.strike(layerIndex,candidate.x(),candidate.z());
        double dx=x+.5-candidate.x(),dz=z+.5-candidate.z();
        double along=dx*strike[0]+dz*strike[1];
        double across=-dx*strike[1]+dz*strike[0];
        double a=Math.max(1,bif.length()*.5),b=Math.max(1,bif.width()*.5);
        if(Math.abs(along)>a*1.25+25||Math.abs(across)>b*1.25+25) return -9;
        double best=-9;
        for(int i=0;i<Math.min(24,bif.satelliteCount());i++){
            long seed=DepositCandidateSampler.mix(candidate.shapeSeed()^(0x9E3779B97F4A7C15L*(i+1)));
            double theta=rand01(seed)*Math.PI*2;
            double radial=1.04+rand01(seed^0xA17L)*.20;
            double ca=Math.cos(theta),sa=Math.sin(theta);
            double podAlong=a*radial*ca,podAcross=b*radial*sa;
            double localAlong=along-podAlong,localAcross=across-podAcross;
            if(Math.abs(localAlong)>25||Math.abs(localAcross)>19) continue;
            double radiusAlong=8+rand01(seed^0xB31L)*13;
            double radiusAcross=5+rand01(seed^0xC49L)*9;
            double broad=GeologicalNoise.fbm2(x,z,seed,.026,3)*.23;
            double detail=GeologicalNoise.fbm2(x,z,seed^0xD52L,.085,2)*.09;
            best=Math.max(best,1-sq(localAlong/radiusAlong)-sq(localAcross/radiusAcross)+broad+detail);
        }
        return best;
    }

}
