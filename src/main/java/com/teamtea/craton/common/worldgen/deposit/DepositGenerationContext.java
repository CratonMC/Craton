package com.teamtea.craton.common.worldgen.deposit;

import com.teamtea.craton.common.worldgen.geology.GeologyFieldSampler;
import com.teamtea.craton.common.worldgen.noise.GeologicalNoise;

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
import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.*;

/**
 * Mutable context owned by one chunk buildSurface call and reused across its columns.
 * prepareColumn resets column results while retaining candidate templates and noise caches.
 * Process columns sequentially; do not share this instance across threads or concurrent columns.
 */
public class DepositGenerationContext {
    private record PreparedDefinition(Deposit deposit,long salt,DepositCandidateSampler.CellCache.PreparedQuery query,
                                      List<DepositCandidateSampler.Candidate> candidates,OrdinaryBatch batch){}
    private static class OrdinaryBatch {
        final OrdinaryDeposit definition;
        final List<DepositCandidateSampler.Candidate> candidates=new ArrayList<>();
        int placerCellX=Integer.MIN_VALUE,placerCellZ;
        DepositCandidateSampler.Candidate placerSeed;
        OrdinaryBatch(OrdinaryDeposit definition){this.definition=definition;}
    }
    private final Map<FieldDeposit,java.util.IdentityHashMap<DepositCandidateSampler.Candidate,MineralizationField>> abundanceFields=new java.util.IdentityHashMap<>();
    private int abundanceSize;
    private FieldDeposit lastAbundanceDeposit;
    private DepositCandidateSampler.Candidate lastAbundanceCandidate;
    private MineralizationField lastAbundanceField;
    public boolean enriched(FieldDeposit deposit,DepositCandidateSampler.Candidate candidate,int y){
        if(!deposit.settings().abundance().enabled())return true;
        if(lastAbundanceDeposit==deposit&&lastAbundanceCandidate==candidate)
            return lastAbundanceField.contains(x,y,z);
        return abundanceField(deposit,candidate).contains(x,y,z);
    }
    public double enrichmentScale(FieldDeposit deposit,DepositCandidateSampler.Candidate candidate){
        return deposit.settings().abundance().enabled()?abundanceField(deposit,candidate).scale():1;
    }
    private MineralizationField abundanceField(FieldDeposit deposit,DepositCandidateSampler.Candidate candidate){
        if(lastAbundanceDeposit==deposit&&lastAbundanceCandidate==candidate)
            return lastAbundanceField;
        var fields=abundanceFields.get(deposit);
        MineralizationField field=fields==null?null:fields.get(candidate);
        if(field==null){
            if(abundanceSize>=512){abundanceFields.clear();abundanceSize=0;fields=null;}
            if(fields==null){fields=new java.util.IdentityHashMap<>();abundanceFields.put(deposit,fields);}
            double orientation=columnGeometry(candidate) instanceof PreparedPegmatite peg?peg.worldAngle():angle(candidate.rotationSeed());
            field=new MineralizationField(deposit.settings().abundance(),
                    candidate.gradeSeed()^DepositCandidateSampler.salt(deposit.getType().toString()),orientation);
            fields.put(candidate,field);abundanceSize++;
        }
        lastAbundanceDeposit=deposit;lastAbundanceCandidate=candidate;lastAbundanceField=field;
        return field;
    }
    // Chunk-scoped random sources, query handles and definition batches.
    private final PositionalRandomFactory random;
    private final DepositCandidateSampler.CellCache cellCache;
    private final List<DepositCandidateSampler.Candidate> queryBuffer=new ArrayList<>();
    private final List<PreparedDefinition> preparedDefinitions;
    private final List<DepositPlan.SkarnRule> skarns;
    private final double maximumSkarnReach;
    // Retain intrusion templates across columns; rebuild the *Columns lists for each column.
    private final Map<DepositCandidateSampler.Candidate,IntrusionDeposit.Prepared> preparedIntrusions=new java.util.IdentityHashMap<>();
    private final List<IntrusionDeposit.Prepared> graniteColumns=new ArrayList<>();
    private final List<IntrusionDeposit.Prepared> dioriteColumns=new ArrayList<>();
    private final List<IntrusionDeposit.Prepared> gabbroColumns=new ArrayList<>();
    private static class PegmatiteKey {
        private IntrusionDeposit.Prepared parent;
        private HydrothermalVeinDeposit deposit;
        private long salt;
        PegmatiteKey(IntrusionDeposit.Prepared parent,HydrothermalVeinDeposit deposit,long salt){set(parent,deposit,salt);}
        void set(IntrusionDeposit.Prepared parent,HydrothermalVeinDeposit deposit,long salt){this.parent=parent;this.deposit=deposit;this.salt=salt;}
        @Override public int hashCode(){return (java.util.Objects.hashCode(parent)*31+java.util.Objects.hashCode(deposit))*31+Long.hashCode(salt);}
        @Override public boolean equals(Object other){return other instanceof PegmatiteKey key&&java.util.Objects.equals(parent,key.parent)&&java.util.Objects.equals(deposit,key.deposit)&&salt==key.salt;}
    }
    // This probe is never inserted: stored keys remain immutable throughout their lifetime.
    private final PegmatiteKey attachmentLookup=new PegmatiteKey(null,null,0);
    private final Map<PegmatiteKey,List<PreparedPegmatite.Attachment>> attachedPegmatites=new java.util.LinkedHashMap<>(64,.75f,true){
        @Override protected boolean removeEldestEntry(Map.Entry<PegmatiteKey,List<PreparedPegmatite.Attachment>> entry){return size()>256;}
    };
    // Column candidates and placement batches; reset clears contents but reuses containers.
    private final Map<DepositCandidateSampler.Candidate,Object> columnGeometry=new java.util.IdentityHashMap<>();
    private final Map<DepositCandidateSampler.Candidate,Deposit> owners=new java.util.IdentityHashMap<>();
    private final Map<Identifier,List<DepositCandidateSampler.Candidate>> candidatesByType=new HashMap<>();
    private final List<OrdinaryBatch> ordinaryBatches;
    // Derived column results and reusable output containers.
    private PlacerDeposit.SourceStrength sourceStrength;
    private Boolean vmsProvince;
    private final IntrusiveDepositField.IntrusionResult graniteResult=new IntrusiveDepositField.IntrusionResult(0);
    private final IntrusiveDepositField.IntrusionResult dioriteResult=new IntrusiveDepositField.IntrusionResult(1);
    private final IntrusiveDepositField.IntrusionResult gabbroResult=new IntrusiveDepositField.IntrusionResult(2);
    private final StratiformDepositField.ColumnCache stratiformCache=new StratiformDepositField.ColumnCache();
    private final StratiformDepositField.DepositSample stratiformSample=new StratiformDepositField.DepositSample();
    // indexedLayers persists across columns; position, host and BIF state describe the current column.
    private final Map<GeologyFieldSampler,Map<Block,List<Integer>>> indexedLayers=new java.util.IdentityHashMap<>();
    private GeologyFieldSampler geology;
    private int x,z,minY,topY,surfaceY;
    private final double maximumPlacerDepth;
    private BlockState surfaceState;
    private Holder<Biome> biome;
    private Map<Block,List<Integer>> layerIndices;
    private BandedIronFormation.BifData bif;
    // Cross-column geometry and noise caches; evicted entries are recomputed.
    private final BandedIronFormation.AnchorCache bifAnchors=new BandedIronFormation.AnchorCache();
    private final Map<DepositCandidateSampler.Candidate,PreparedVein> preparedVeins=new java.util.LinkedHashMap<>(128,.75f,true){
        @Override protected boolean removeEldestEntry(Map.Entry<DepositCandidateSampler.Candidate,PreparedVein> e){return size()>256;}
    };

    private final Map<DepositCandidateSampler.Candidate,PodEntry> podCache=new java.util.LinkedHashMap<>(64,.75f,true){
        @Override protected boolean removeEldestEntry(Map.Entry<DepositCandidateSampler.Candidate,PodEntry> e){return size()>256;}
    };
    private record PodEntry(double rx,double rz,double angle,long variant,FieldDeposit.Shape config,PreparedPods pods){}
    /** Reuse horizontal templates; update vertical centers and radii for each column. */
    public PreparedPods ordinaryPods(DepositCandidateSampler.Candidate c,double cy,double rx,double ry,double rz,
                                     double angle,long variant,FieldDeposit.Shape config){
        PodEntry entry=podCache.get(c);
        if(entry==null||entry.rx()!=rx||entry.rz()!=rz||entry.angle()!=angle||entry.variant()!=variant||entry.config()!=config){
            entry=new PodEntry(rx,rz,angle,variant,config,new PreparedPods(c,c.x(),cy,c.z(),rx,ry,rz,angle,variant,config));
            podCache.put(c,entry);
        }else entry.pods().retargetVertical(cy,ry);
        return entry.pods();
    }

    // Retain templates across columns; columnGeometry only stores current-column references.
    private final Map<DepositCandidateSampler.Candidate,Object> reusableGeometry=new java.util.LinkedHashMap<>(64,.75f,true){
        @Override protected boolean removeEldestEntry(Map.Entry<DepositCandidateSampler.Candidate,Object> e){return size()>256;}
    };
    /** Retain candidate templates across columns; callers must update all column-dependent parameters. */
    public Object reusableGeometry(DepositCandidateSampler.Candidate candidate){return reusableGeometry.get(candidate);}
    public void reusableGeometry(DepositCandidateSampler.Candidate candidate,Object value){reusableGeometry.put(candidate,value);}

    /** Prepared geometry for this column; clear the index before the next column. */
    public Object columnGeometry(DepositCandidateSampler.Candidate candidate){return columnGeometry.get(candidate);}
    public void columnGeometry(DepositCandidateSampler.Candidate candidate,Object value){columnGeometry.put(candidate,value);}
    public PreparedPegmatite pegmatite(DepositCandidateSampler.Candidate candidate,FieldDeposit.Shape shape){
        Object current=columnGeometry(candidate);
        if(current instanceof PreparedPegmatite prepared)return prepared;
        Object cached=reusableGeometry(candidate);
        if(!(cached instanceof PreparedPegmatite prepared)||prepared.minY()!=minY){
            PreparedPegmatite prepared=new PreparedPegmatite(candidate,minY,shape);
            reusableGeometry(candidate,prepared);
            return prepared;
        }
        return (PreparedPegmatite)cached;
    }

    public PreparedVein vein(DepositCandidateSampler.Candidate candidate,FieldDeposit.Shape shape,double frequency,int branches){
        PreparedVein prepared=preparedVeins.get(candidate);
        if(prepared==null||prepared.minY()!=minY){
            prepared=new PreparedVein(candidate,minY,shape,frequency,branches);
            preparedVeins.put(candidate,prepared);
        }
        return prepared;
    }

    public DepositGenerationContext(PositionalRandomFactory random){this(random,CratonContents.getDepositPlan());}

    public DepositGenerationContext(PositionalRandomFactory random,DepositPlan plan){
        this.random=random;this.cellCache=new DepositCandidateSampler.CellCache(random);
        maximumPlacerDepth=plan.maximumPlacerDepth;maximumSkarnReach=plan.maximumSkarnReach;skarns=plan.skarns;
        OrdinaryBatch[] byDefinition=new OrdinaryBatch[plan.definitions.size()];
        List<OrdinaryBatch> batches=new ArrayList<>();
        for(int index:plan.ordinaryOrder){
            OrdinaryBatch batch=new OrdinaryBatch((OrdinaryDeposit)plan.definitions.get(index).deposit());
            byDefinition[index]=batch;batches.add(batch);
        }
        ordinaryBatches=List.copyOf(batches);
        List<PreparedDefinition> entries=new ArrayList<>();
        for(int i=0;i<plan.definitions.size();i++){
            DepositPlan.Definition definition=plan.definitions.get(i);Deposit deposit=definition.deposit();
            Deposit.Placement placement=deposit.placement();
            if(placement.frequency()<=0||placement.maxCandidates()==0||deposit instanceof SkarnDeposit) continue;
            List<DepositCandidateSampler.Candidate> candidates=candidatesByType.computeIfAbsent(deposit.getType(),ignored -> new ArrayList<>());
            entries.add(new PreparedDefinition(deposit,definition.salt(),(deposit instanceof HydrothermalVeinDeposit||deposit instanceof PlacerDeposit)?null:
                    cellCache.prepare(deposit.sequence(),definition.salt(),placement.cellSize(),
                    definition.reach(),placement.maxCandidates(),placement.frequency()),candidates,deposit instanceof PlacerDeposit?new OrdinaryBatch((OrdinaryDeposit)deposit):byDefinition[i]));
        }
        preparedDefinitions=List.copyOf(entries);
    }

    /** Reset column state, collect candidates, then prepare intrusions and BIF. */
    void prepareColumn(GeologyFieldSampler geology,int x,int z,int minY,int topY,int surfaceY,
                       BlockState surfaceState,Holder<Biome> biome){
        prepareColumn(geology,x,z,minY,topY,surfaceY,surfaceState,biome,true);
    }

    void prepareUndergroundColumn(GeologyFieldSampler geology,int x,int z,int minY,int topY,int surfaceY,
                                  BlockState surfaceState,Holder<Biome> biome){
        prepareColumn(geology,x,z,minY,topY,surfaceY,surfaceState,biome,false);
    }

    private void prepareColumn(GeologyFieldSampler geology,int x,int z,int minY,int topY,int surfaceY,
                               BlockState surfaceState,Holder<Biome> biome,boolean includePlacers){
        reset(geology,x,z,minY,topY,surfaceY,surfaceState,biome);
        collectColumnCandidates(x,z,includePlacers);
        prepareIntrusions();
        preparePegmatites();
        bif=BandedIronFormation.prepareBif(bifCandidates(),owners(),geology,x,z,bifAnchors);
    }

    private void preparePegmatites(){
        for(int definition=0;definition<preparedDefinitions.size();definition++){
            PreparedDefinition entry=preparedDefinitions.get(definition);
            if(!(entry.deposit() instanceof HydrothermalVeinDeposit vein))continue;
            for(int i=0;i<graniteColumns.size();i++){
                IntrusionDeposit.Prepared parent=graniteColumns.get(i);
                attachmentLookup.set(parent,vein,entry.salt());
                List<PreparedPegmatite.Attachment> attachments=attachedPegmatites.get(attachmentLookup);
                if(attachments==null){
                    attachments=vein.attachedTo(parent,entry.salt());
                    attachedPegmatites.put(new PegmatiteKey(parent,vein,entry.salt()),attachments);
                }
                for(int a=0;a<attachments.size();a++){
                    PreparedPegmatite.Attachment attachment=attachments.get(a);
                    if(!attachment.geometry().affectsColumn(x,z))continue;
                    var candidate=attachment.candidate();
                    owners.put(candidate,vein);
                    entry.candidates().add(candidate);
                    if(entry.batch()!=null)entry.batch().candidates.add(candidate);
                    columnGeometry(candidate,attachment.geometry());
                }
            }
        }
    }

    private void collectColumnCandidates(int x,int z,boolean includePlacers){
        for(PreparedDefinition entry:preparedDefinitions){
            if(entry.deposit() instanceof PlacerDeposit){
                if(!includePlacers)continue;
                var placement=entry.deposit().placement();
                int cx=Math.floorDiv(x,placement.cellSize()),cz=Math.floorDiv(z,placement.cellSize());
                var batch=entry.batch();
                if(batch.placerSeed==null||batch.placerCellX!=cx||batch.placerCellZ!=cz){
                    batch.placerSeed=cellCache.regionalSeed(entry.deposit().sequence(),entry.salt(),cx,cz,placement.cellSize());
                    batch.placerCellX=cx;batch.placerCellZ=cz;
                }
                owners.put(batch.placerSeed,entry.deposit());
                entry.candidates().add(batch.placerSeed);
                continue;
            }
            if(entry.query()==null)continue;
            if(entry.deposit() instanceof PeatDeposit&&(!biome.is(net.neoforged.neoforge.common.Tags.Biomes.IS_SWAMP)||biome.is(net.neoforged.neoforge.common.Tags.Biomes.IS_OCEAN)))continue;
            entry.query().queryInto(x,z,queryBuffer);
            for(DepositCandidateSampler.Candidate candidate:queryBuffer){
                owners.put(candidate,entry.deposit());
                entry.candidates().add(candidate);
                if(entry.batch()!=null) entry.batch().candidates.add(candidate);
            }
        }
    }

    /** Clear column state without discarding candidate caches. */
    private void reset(GeologyFieldSampler geology,int x,int z,int minY,int topY,int surfaceY,
                       BlockState surfaceState,Holder<Biome> biome){
        this.geology=geology;
        stratiformCache.reset(geology);
        this.x=x;this.z=z;this.minY=minY;this.topY=topY;this.surfaceY=surfaceY;
        this.surfaceState=surfaceState;this.biome=biome;
        owners.clear();
        columnGeometry.clear();
        queryBuffer.clear();
        candidatesByType.values().forEach(List::clear);
        for(OrdinaryBatch batch:ordinaryBatches) batch.candidates.clear();
        sourceStrength=null;
        vmsProvince=null;
        layerIndices=null;
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
        for(IntrusionDeposit.Prepared p:graniteColumns){
            p.deposit.prepareColumn(this,p,cutoff);
        }
        for(IntrusionDeposit.Prepared p:dioriteColumns){
            p.deposit.prepareColumn(this,p,cutoff);
        }
        for(IntrusionDeposit.Prepared p:gabbroColumns){
            p.deposit.prepareColumn(this,p,cutoff);
        }
    }

    public IntrusionDeposit.Prepared preparedIntrusion(DepositCandidateSampler.Candidate candidate){return preparedIntrusions.get(candidate);}

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
    public StratiformDepositField.ColumnCache stratiformCache(){return stratiformCache;}
    public StratiformDepositField.DepositSample stratiformSample(){return stratiformSample;}
    public GeologyFieldSampler geology(){return geology;}
    public int x(){return x;}
    public int z(){return z;}
    public int minY(){return minY;}
    public int topY(){return topY;}
    public int surfaceY(){return surfaceY;}
    public double maximumPlacerDepth(){return maximumPlacerDepth;}
    public BlockState surfaceState(){return surfaceState;}
    public Holder<Biome> biome(){return biome;}
    public Map<Block,List<Integer>> layerIndices(){
        if(layerIndices==null){
            layerIndices=indexedLayers.computeIfAbsent(geology,sampler -> {
            Map<Block,List<Integer>> result=new HashMap<>();
            for(int i=0;i<sampler.layers().size();i++)
                result.computeIfAbsent(sampler.layers().get(i).value().blockState().getBlock(),key -> new ArrayList<>()).add(i);
            return result;
        });
        }
        return layerIndices;
    }
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
    public List<DepositCandidateSampler.Candidate> peat(){return candidates(DepositTypes.PEAT);}
    public List<DepositCandidateSampler.Candidate> placer(){return candidates(DepositTypes.PLACER);}
    public List<DepositCandidateSampler.Candidate> stratiformCoal(){return candidates(DepositTypes.COAL);}
    public List<DepositCandidateSampler.Candidate> stratiformCopper(){return candidates(DepositTypes.COPPER);}
    public List<DepositCandidateSampler.Candidate> sandstoneUranium(){return candidates(DepositTypes.URANIUM);}
    public List<DepositCandidateSampler.Candidate> shaleLeadZinc(){return candidates(DepositTypes.SHALE_PB_ZN);}
    public List<DepositCandidateSampler.Candidate> bifCandidates(){return candidates(DepositTypes.BIF);}
    public BandedIronFormation.BifData bif(){return bif;}

    public BlockState applyCandidates(List<DepositCandidateSampler.Candidate> candidates,
                                      BlockState originalHost,BlockState state,int y){
        for(int i=0,size=candidates.size();i<size;i++){
            DepositCandidateSampler.Candidate candidate=candidates.get(i);
            OrdinaryDeposit definition=(OrdinaryDeposit)owners.get(candidate);
            BlockState placed=definition.place(this,candidate,originalHost,state,y);
            if(placed!=null && !placed.equals(state)) return placed;
        }
        return state;
    }

    BlockState applyOrdinary(BlockState host,BlockState state,int y){
        int previousOrder=0;
        boolean haveOrder=false,placedInOrder=false;
        for(int b=0;b<ordinaryBatches.size();b++){
            OrdinaryBatch batch=ordinaryBatches.get(b);
            int order=batch.definition.order();
            if(!haveOrder||order!=previousOrder){previousOrder=order;haveOrder=true;placedInOrder=false;}
            if(placedInOrder||batch.candidates.isEmpty()||!batch.definition.canPlace(this,host,state,y)) continue;
            for(int i=0;i<batch.candidates.size();i++){
                BlockState placed=batch.definition.place(this,batch.candidates.get(i),host,state,y);
                if(placed!=null&&!placed.equals(state)){state=placed;placedInOrder=true;break;}
            }
        }
        return state;
    }

    public BlockState applySkarn(BlockState host,BlockState state,int y,IntrusiveDepositField.IntrusionResult intrusion){
        if(!isCarbonate(host)) return state;
        for(int i=0,size=skarns.size();i<size;i++){
            DepositPlan.SkarnRule rule=skarns.get(i);
            state=rule.deposit().placeContact(this,host,state,y,intrusion,rule.salt());
        }
        return state;
    }

    public BlockState applyBif(BlockState host,int y){
        if(!host.is(CratonBlocks.GNEISS.getOrigin().getBaseBlock())) return host;
        List<BandedIronFormation.BifColumnHit> hits=bif.hits();
        for(int i=0,size=hits.size();i<size;i++){
            BandedIronFormation.BifColumnHit hit=hits.get(i);
            BlockState placed=hit.deposit().placeHit(hit,host,x,y,z);
            if(placed!=null && !placed.equals(host)) return placed;
        }
        return host;
    }
}
