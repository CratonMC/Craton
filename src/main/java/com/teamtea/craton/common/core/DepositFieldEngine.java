package com.teamtea.craton.common.core;

import com.teamtea.craton.api.geology.deposit.Deposit;
import com.teamtea.craton.api.geology.deposit.BandedIronFormation;
import com.teamtea.craton.api.geology.deposit.DepositTypes;
import com.teamtea.craton.api.geology.deposit.FieldDeposit;
import com.teamtea.craton.api.geology.deposit.*;
import com.teamtea.craton.common.registry.CratonBlocks;
import com.teamtea.craton.common.registry.CratonContents;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import net.minecraft.resources.Identifier;
import static com.teamtea.craton.common.core.DepositFieldSupport.*;

/**
 * Deterministic single-point evaluator for datapack configured deposit families.
 * Geometry is analytic, Perlin/fBm only distorts boundaries, grade and block occupancy.
 */
public final class DepositFieldEngine {
    private DepositFieldEngine(){}

    public static ColumnContext prepare(ColumnContext context,GeologyFieldSampler geology,int x,int z,
                                        int minY,int topY,BlockState surfaceState,Holder<Biome> biome){
        context.reset(geology,x,z,minY,topY,surfaceState,biome);
        Map<Identifier,List<Holder<Deposit>>> definitions=context.definitions();
        Map<DepositCandidateSampler.Candidate,Deposit> owners=context.owners();
        DepositCandidateSampler.CellCache cells=context.cellCache();
        context.granite=query(cells,definitions,owners,DepositTypes.GRANITE,DepositRandomSequences.INTRUSIVE,x,z);
        context.diorite=query(cells,definitions,owners,DepositTypes.DIORITE,DepositRandomSequences.INTRUSIVE,x,z);
        context.gabbro=query(cells,definitions,owners,DepositTypes.GABBRO,DepositRandomSequences.INTRUSIVE,x,z);
        context.epithermal=query(cells,definitions,owners,DepositTypes.EPITHERMAL,DepositRandomSequences.EPITHERMAL,x,z);
        context.vms=query(cells,definitions,owners,DepositTypes.VMS,DepositRandomSequences.VMS,x,z);
        context.veins=query(cells,definitions,owners,DepositTypes.VEIN,DepositRandomSequences.VEIN,x,z);
        context.weathering=query(cells,definitions,owners,DepositTypes.WEATHERING,DepositRandomSequences.WEATHERING,x,z);
        context.kimberlite=query(cells,definitions,owners,DepositTypes.KIMBERLITE,DepositRandomSequences.SPECIAL,x,z);
        context.jadeitite=query(cells,definitions,owners,DepositTypes.JADEITITE,DepositRandomSequences.SPECIAL,x,z);
        context.placer=query(cells,definitions,owners,DepositTypes.PLACER,DepositRandomSequences.PLACER,x,z);
        context.stratiformCoal=query(cells,definitions,owners,DepositTypes.COAL,DepositRandomSequences.STRATIFORM,x,z);
        context.stratiformCopper=query(cells,definitions,owners,DepositTypes.COPPER,DepositRandomSequences.STRATIFORM,x,z);
        context.sandstoneUranium=query(cells,definitions,owners,DepositTypes.URANIUM,DepositRandomSequences.STRATIFORM,x,z);
        context.bif=BandedIronFormation.prepareBif(query(cells,definitions,owners,DepositTypes.BIF,DepositRandomSequences.STRATIFORM,x,z),
                owners,geology,x,z);
        return context;
    }

    private static List<DepositCandidateSampler.Candidate> query(DepositCandidateSampler.CellCache cells,
            Map<Identifier,List<Holder<Deposit>>> definitions,Map<DepositCandidateSampler.Candidate,Deposit> owners,
            Identifier type,DepositRandomSequences sequence,int x,int z){
        List<DepositCandidateSampler.Candidate> result=new ArrayList<>();
        for(Holder<Deposit> entry:definitions.getOrDefault(type,List.of())){
            Deposit deposit=entry.value();
            Deposit.Placement p=deposit.placement();
            if(p.frequency()<=0||p.maxCandidates()==0) continue;
            long id=DepositCandidateSampler.salt(entry.unwrapKey().orElseThrow().identifier().toString());
            for(DepositCandidateSampler.Candidate c:cells.query(sequence,id,x,z,
                    p.cellSize(),p.reach(),p.maxCandidates())){
                if(rand01(c.shapeSeed()^id)>=p.frequency()) continue;
                result.add(c);
                owners.put(c,deposit);
            }
        }
        return result;
    }

    public static final class ColumnContext {
        private final PositionalRandomFactory random;
        private final DepositCandidateSampler.CellCache cellCache;
        private final Map<Identifier,List<Holder<Deposit>>> definitions;
        private final Map<DepositCandidateSampler.Candidate,Deposit> owners=new java.util.IdentityHashMap<>();
        private final Map<Integer,PlacerDeposit.SourceStrength> sourceCache=new HashMap<>();
        private final IntrusiveDepositField.IntrusionResult graniteResult=new IntrusiveDepositField.IntrusionResult(0);
        private final IntrusiveDepositField.IntrusionResult dioriteResult=new IntrusiveDepositField.IntrusionResult(1);
        private final IntrusiveDepositField.IntrusionResult gabbroResult=new IntrusiveDepositField.IntrusionResult(2);
        private final StratiformDepositField.DepositSample stratiformSample=new StratiformDepositField.DepositSample();
        private final Map<GeologyFieldSampler,Map<Block,List<Integer>>> indexedLayers=new java.util.IdentityHashMap<>();
        private GeologyFieldSampler geology;
        private int x,z,minY,topY;
        private BlockState surfaceState;
        private Holder<Biome> biome;
        private Map<Block,List<Integer>> layerIndices;
        private List<DepositCandidateSampler.Candidate> granite,diorite,gabbro,epithermal,vms,veins,
                weathering,kimberlite,jadeitite,placer,stratiformCoal,stratiformCopper,sandstoneUranium;
        private BandedIronFormation.BifData bif;

        public ColumnContext(PositionalRandomFactory random){
            this.random=random;
            this.cellCache=new DepositCandidateSampler.CellCache(random);
            this.definitions=CratonContents.getDepositsByType();
        }

        private void reset(GeologyFieldSampler geology,int x,int z,int minY,int topY,
                           BlockState surfaceState,Holder<Biome> biome){
            this.geology=geology;
            this.x=x;this.z=z;this.minY=minY;this.topY=topY;
            this.surfaceState=surfaceState;this.biome=biome;
            owners.clear();
            sourceCache.clear();
            layerIndices=indexedLayers.computeIfAbsent(geology,sampler -> {
                Map<Block,List<Integer>> result=new HashMap<>();
                for(int i=0;i<sampler.layers().size();i++)
                    result.computeIfAbsent(sampler.layers().get(i).value().blockState().getBlock(),key -> new ArrayList<>()).add(i);
                return result;
            });
        }

        public PositionalRandomFactory random(){return random;}
        public DepositCandidateSampler.CellCache cellCache(){return cellCache;}
        public Map<Identifier,List<Holder<Deposit>>> definitions(){return definitions;}
        public Map<DepositCandidateSampler.Candidate,Deposit> owners(){return owners;}
        public Map<Integer,PlacerDeposit.SourceStrength> sourceCache(){return sourceCache;}
        public IntrusiveDepositField.IntrusionResult graniteResult(){return graniteResult;}
        public IntrusiveDepositField.IntrusionResult dioriteResult(){return dioriteResult;}
        public IntrusiveDepositField.IntrusionResult gabbroResult(){return gabbroResult;}
        public StratiformDepositField.DepositSample stratiformSample(){return stratiformSample;}
        public GeologyFieldSampler geology(){return geology;}
        public int x(){return x;}
        public int z(){return z;}
        public int minY(){return minY;}
        public int topY(){return topY;}
        public BlockState surfaceState(){return surfaceState;}
        public Holder<Biome> biome(){return biome;}
        public Map<Block,List<Integer>> layerIndices(){return layerIndices;}
        public List<DepositCandidateSampler.Candidate> granite(){return granite;}
        public List<DepositCandidateSampler.Candidate> diorite(){return diorite;}
        public List<DepositCandidateSampler.Candidate> gabbro(){return gabbro;}
        public List<DepositCandidateSampler.Candidate> epithermal(){return epithermal;}
        public List<DepositCandidateSampler.Candidate> vms(){return vms;}
        public List<DepositCandidateSampler.Candidate> veins(){return veins;}
        public List<DepositCandidateSampler.Candidate> weathering(){return weathering;}
        public List<DepositCandidateSampler.Candidate> kimberlite(){return kimberlite;}
        public List<DepositCandidateSampler.Candidate> jadeitite(){return jadeitite;}
        public List<DepositCandidateSampler.Candidate> placer(){return placer;}
        public List<DepositCandidateSampler.Candidate> stratiformCoal(){return stratiformCoal;}
        public List<DepositCandidateSampler.Candidate> stratiformCopper(){return stratiformCopper;}
        public List<DepositCandidateSampler.Candidate> sandstoneUranium(){return sandstoneUranium;}
        public BandedIronFormation.BifData bif(){return bif;}
    }

    public static BlockState apply(ColumnContext ctx,BlockState host,BlockState initialState,int y){
        BlockState state=initialState;
        if(state.equals(host)) state=BandedIronFormation.applyBandedIronFormations(ctx.bif().hits(),host,ctx.x(),y,ctx.z());
        if(state.equals(host)) state=StratiformDepositField.applyStratiform(ctx,state,y);

        IntrusiveDepositField.IntrusionResult intrusion=IntrusiveDepositField.sampleIntrusions(ctx,y);
        if(intrusion.candidate()!=null&&IntrusiveDepositField.intrusionOccupies(ctx,y,intrusion)){
            state=rock(ctx,intrusion.candidate());
            state=IntrusiveDepositField.applyIntrusiveOre(ctx,state,y,intrusion);
        }else if(state.equals(host)){
            state=IntrusiveDepositField.applyIntrusiveSatellite(ctx,state,y);
        }

        if(intrusion.candidate()!=null&&intrusion.score()<=.12) state=SkarnDeposit.applySkarn(ctx,host,state,y,intrusion);
        state=VmsDeposit.applyVms(ctx,host,state,y);
        state=EpithermalDeposit.applyEpithermal(ctx,host,state,y);
        state=HydrothermalVeinDeposit.applyVeins(ctx,host,state,y);
        state=WeatheringDeposit.applyWeathering(ctx,host,state,y);
        state=KimberliteDeposit.applyKimberlite(ctx,state,y);
        state=JadeititeDeposit.applyJadeitite(ctx,host,state,y);
        state=PlacerDeposit.applyPlacer(ctx,host,state,y);
        return state;
    }



}
