package com.teamtea.craton.common.core;

import com.teamtea.craton.api.geology.deposit.*;
import com.teamtea.craton.common.registry.CratonBlocks;
import com.teamtea.craton.common.registry.CratonContents;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

import net.minecraft.resources.Identifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        for(PreparedDefinition entry:context.preparedDefinitions){
            Deposit deposit=entry.deposit();
            Deposit.Placement p=deposit.placement();
            if(p.frequency()<=0||p.maxCandidates()==0) continue;
            context.cellCache.queryInto(deposit.sequence(),entry.salt(),x,z,
                    p.cellSize(),entry.reach(),p.maxCandidates(),context.queryBuffer);
            for(DepositCandidateSampler.Candidate candidate:context.queryBuffer){
                if(rand01(candidate.shapeSeed()^entry.salt())>=p.frequency()) continue;
                context.owners.put(candidate,deposit);
                context.candidatesByType.computeIfAbsent(deposit.getType(),ignored -> new ArrayList<>()).add(candidate);
                if(deposit instanceof OrdinaryDeposit ordinary)
                    context.ordinaryCandidates.computeIfAbsent(ordinary.order(),ignored -> new ArrayList<>()).add(candidate);
            }
        }
        context.prepareIntrusions();
        context.bif=BandedIronFormation.prepareBif(context.bifCandidates(),context.owners(),geology,x,z);
        return context;
    }

    private record PreparedDefinition(Deposit deposit,long salt,double reach){}
    private record SkarnRule(SkarnDeposit deposit,long salt){}

    public static final class ColumnContext {
        private final PositionalRandomFactory random;
        private final DepositCandidateSampler.CellCache cellCache;
        private final List<DepositCandidateSampler.Candidate> queryBuffer=new ArrayList<>();
        private final List<PreparedDefinition> preparedDefinitions;
        private final List<SkarnRule> skarns;
        private final double maximumSkarnReach;
        private final Map<DepositCandidateSampler.Candidate,IntrusionDeposit.Prepared> preparedIntrusions=new java.util.IdentityHashMap<>();
        private final List<IntrusionDeposit.Prepared> graniteColumns=new ArrayList<>();
        private final List<IntrusionDeposit.Prepared> dioriteColumns=new ArrayList<>();
        private final List<IntrusionDeposit.Prepared> gabbroColumns=new ArrayList<>();
        private final Map<DepositCandidateSampler.Candidate,Deposit> owners=new java.util.IdentityHashMap<>();
        private final Map<Identifier,List<DepositCandidateSampler.Candidate>> candidatesByType=new HashMap<>();
        private final java.util.NavigableMap<Integer,List<DepositCandidateSampler.Candidate>> ordinaryCandidates=new java.util.TreeMap<>();
        private PlacerDeposit.SourceStrength sourceStrength;
        private Boolean vmsProvince;
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
        private BandedIronFormation.BifData bif;

        public ColumnContext(PositionalRandomFactory random){
            this.random=random;
            this.cellCache=new DepositCandidateSampler.CellCache(random);
            List<PreparedDefinition> entries=new ArrayList<>();
            List<SkarnRule> contacts=new ArrayList<>();
            for(Holder<Deposit> holder:CratonContents.getDeposits()){
                Deposit deposit=holder.value();
                long salt=DepositCandidateSampler.salt(holder.unwrapKey().orElseThrow().identifier().toString());
                entries.add(new PreparedDefinition(deposit,salt,deposit.placement().reach()));
                if(deposit instanceof SkarnDeposit skarn) contacts.add(new SkarnRule(skarn,salt));
            }
            this.skarns=List.copyOf(contacts);
            double reach=0;
            for(SkarnRule rule:contacts)
                reach=Math.max(reach,rule.deposit().settings().shape().radiusYMax());
            this.maximumSkarnReach=reach;
            List<PreparedDefinition> resolved=new ArrayList<>(entries.size());
            for(PreparedDefinition entry:entries){
                Deposit deposit=entry.deposit();
                double candidateReach=deposit instanceof IntrusionDeposit intrusion
                        ?Math.max(entry.reach(),intrusion.contactReach(reach)):entry.reach();
                resolved.add(new PreparedDefinition(deposit,entry.salt(),candidateReach));
            }
            this.preparedDefinitions=List.copyOf(resolved);
        }

        private void reset(GeologyFieldSampler geology,int x,int z,int minY,int topY,
                           BlockState surfaceState,Holder<Biome> biome){
            this.geology=geology;
            this.x=x;this.z=z;this.minY=minY;this.topY=topY;
            this.surfaceState=surfaceState;this.biome=biome;
            owners.clear();
            queryBuffer.clear();
            candidatesByType.values().forEach(List::clear);
            ordinaryCandidates.values().forEach(List::clear);
            sourceStrength=null;
            vmsProvince=null;
            layerIndices=indexedLayers.computeIfAbsent(geology,sampler -> {
                Map<Block,List<Integer>> result=new HashMap<>();
                for(int i=0;i<sampler.layers().size();i++)
                    result.computeIfAbsent(sampler.layers().get(i).value().blockState().getBlock(),key -> new ArrayList<>()).add(i);
                return result;
            });
        }

        private void collectIntrusions(List<DepositCandidateSampler.Candidate> candidates,
                                       List<IntrusionDeposit.Prepared> columns){
            columns.clear();
            for(DepositCandidateSampler.Candidate candidate:candidates){
                IntrusionDeposit.Prepared prepared=preparedIntrusions.get(candidate);
                if(prepared==null||prepared.originMinY!=minY){
                    prepared=((IntrusionDeposit)owners.get(candidate)).prepare(candidate,minY);
                    preparedIntrusions.put(candidate,prepared);
                }
                columns.add(prepared);
            }
        }

        private void prepareIntrusions(){
            collectIntrusions(granite(),graniteColumns);
            collectIntrusions(diorite(),dioriteColumns);
            collectIntrusions(gabbro(),gabbroColumns);
            // All candidates use the same cutoff: an irrelevant, higher-score
            // body must still suppress a lower-score contact halo as before.
            double cutoff=-.08;
            for(IntrusionDeposit.Prepared p:graniteColumns) cutoff=Math.min(cutoff,-maximumSkarnReach/p.minRadius);
            for(IntrusionDeposit.Prepared p:dioriteColumns) cutoff=Math.min(cutoff,-maximumSkarnReach/p.minRadius);
            for(IntrusionDeposit.Prepared p:gabbroColumns) cutoff=Math.min(cutoff,-maximumSkarnReach/p.minRadius);
            for(IntrusionDeposit.Prepared p:graniteColumns) p.deposit.prepareColumn(this,p,cutoff);
            for(IntrusionDeposit.Prepared p:dioriteColumns) p.deposit.prepareColumn(this,p,cutoff);
            for(IntrusionDeposit.Prepared p:gabbroColumns) p.deposit.prepareColumn(this,p,cutoff);
        }

        public List<IntrusionDeposit.Prepared> graniteColumns(){return graniteColumns;}
        public List<IntrusionDeposit.Prepared> dioriteColumns(){return dioriteColumns;}
        public List<IntrusionDeposit.Prepared> gabbroColumns(){return gabbroColumns;}

        public PositionalRandomFactory random(){return random;}
        public DepositCandidateSampler.CellCache cellCache(){return cellCache;}
        public Map<DepositCandidateSampler.Candidate,Deposit> owners(){return owners;}
        public PlacerDeposit.SourceStrength sourceStrength(PlacerDeposit deposit){
            if(sourceStrength==null) sourceStrength=deposit.primarySourceStrength(this);
            return sourceStrength;
        }
        public boolean vmsProvince(){
            if(vmsProvince==null)
                vmsProvince=.5+.5*GeologicalNoise.fbm2(x,z,DepositRandomSequences.VMS.salt()^0x564D5350L,.0019,4)>=.46;
            return vmsProvince;
        }
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
        public List<DepositCandidateSampler.Candidate> candidates(Identifier type){return candidatesByType.getOrDefault(type,List.of());}
        public List<DepositCandidateSampler.Candidate> granite(){return candidates(DepositTypes.GRANITE);}
        public List<DepositCandidateSampler.Candidate> diorite(){return candidates(DepositTypes.DIORITE);}
        public List<DepositCandidateSampler.Candidate> gabbro(){return candidates(DepositTypes.GABBRO);}
        public List<DepositCandidateSampler.Candidate> epithermal(){return candidates(DepositTypes.EPITHERMAL);}
        public List<DepositCandidateSampler.Candidate> vms(){return candidates(DepositTypes.VMS);}
        public List<DepositCandidateSampler.Candidate> veins(){return candidates(DepositTypes.VEIN);}
        public List<DepositCandidateSampler.Candidate> weathering(){return candidates(DepositTypes.WEATHERING);}
        public List<DepositCandidateSampler.Candidate> kimberlite(){return candidates(DepositTypes.KIMBERLITE);}
        public List<DepositCandidateSampler.Candidate> jadeitite(){return candidates(DepositTypes.JADEITITE);}
        public List<DepositCandidateSampler.Candidate> placer(){return candidates(DepositTypes.PLACER);}
        public List<DepositCandidateSampler.Candidate> stratiformCoal(){return candidates(DepositTypes.COAL);}
        public List<DepositCandidateSampler.Candidate> stratiformCopper(){return candidates(DepositTypes.COPPER);}
        public List<DepositCandidateSampler.Candidate> sandstoneUranium(){return candidates(DepositTypes.URANIUM);}
        public List<DepositCandidateSampler.Candidate> shaleLeadZinc(){return candidates(DepositTypes.SHALE_PB_ZN);}
        public List<DepositCandidateSampler.Candidate> bifCandidates(){return candidates(DepositTypes.BIF);}
        public BandedIronFormation.BifData bif(){return bif;}

        public BlockState applyCandidates(List<DepositCandidateSampler.Candidate> candidates,
                                          BlockState originalHost,BlockState state,int y){
            for(DepositCandidateSampler.Candidate candidate:candidates){
                OrdinaryDeposit definition=(OrdinaryDeposit)owners.get(candidate);
                BlockState placed=definition.place(this,candidate,originalHost,state,y);
                if(placed!=null) return placed;
            }
            return state;
        }

        public BlockState applySkarn(BlockState host,BlockState state,int y,IntrusiveDepositField.IntrusionResult intrusion){
            if(!isCarbonate(host)) return state;
            for(SkarnRule rule:skarns)
                state=rule.deposit().placeContact(this,host,state,y,intrusion,rule.salt());
            return state;
        }

        public BlockState applyBif(BlockState host,int y){
            if(!host.is(CratonBlocks.GNEISS.getOrigin().getBaseBlock())) return host;
            for(BandedIronFormation.BifColumnHit hit:bif.hits()){
                BlockState placed=hit.deposit().placeHit(hit,host,x,y,z);
                if(placed!=null) return placed;
            }
            return host;
        }
    }

    public static BlockState apply(ColumnContext ctx,BlockState host,BlockState initialState,int y){
        BlockState state=initialState;
        if(state.equals(host)) state=ctx.applyBif(host,y);
        if(state.equals(host)) state=StratiformDepositField.applyStratiform(ctx,state,y);

        IntrusiveDepositField.IntrusionResult intrusion=IntrusiveDepositField.sampleIntrusions(ctx,y);
        if(intrusion.candidate()!=null&&IntrusiveDepositField.intrusionOccupies(ctx,y,intrusion)){
            state=rock(ctx,intrusion.candidate());
            state=IntrusiveDepositField.applyIntrusiveOre(ctx,state,y,intrusion);
        }else if(state.equals(host)){
            state=IntrusiveDepositField.applyIntrusiveSatellite(ctx,state,y);
        }

        if(intrusion.candidate()!=null&&intrusion.score()<=.12) state=ctx.applySkarn(host,state,y,intrusion);
        for(List<DepositCandidateSampler.Candidate> candidates:ctx.ordinaryCandidates.values())
            if(!candidates.isEmpty()) state=ctx.applyCandidates(candidates,host,state,y);
        return state;
    }



}
