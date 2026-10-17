package com.teamtea.craton.common.survey;

import com.teamtea.craton.common.worldgen.deposit.DepositCandidateSampler;
import com.teamtea.craton.common.worldgen.deposit.DepositPlan;
import com.teamtea.craton.common.worldgen.deposit.DepositRandomSequences;
import com.teamtea.craton.common.worldgen.geology.GeologyFieldSampler;
import com.teamtea.craton.common.worldgen.noise.GeologicalNoise;

import com.teamtea.craton.api.geology.deposit.*;
import com.teamtea.craton.common.registry.CratonBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import java.util.ArrayList;
import java.util.List;
import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.*;
import static com.teamtea.craton.common.survey.SurveyMapCanvas.Group.*;

/** Candidate reconnaissance: shared placement seeds and preliminary host gates, without a voxel scan. */
public class DepositSurvey {
    public interface Environment {
        boolean river(int x,int z);
        boolean ocean(int x,int z);
        double surfaceY(int x,int z);
    }
    private record Source(int x,int z,double radius){}
    private record PendingPlacer(PlacerDeposit deposit,DepositCandidateSampler.Candidate candidate,PlacerDeposit.Anchor anchor){}
    private final DepositPlan plan;
    private final GeologyFieldSampler geology;
    private final DepositCandidateSampler.CellCache cache;
    private final SurveyMapCanvas canvas;
    private final Environment environment;
    private final java.util.function.LongPredicate placerRiver;
    private final int minY;
    private final List<Source> placerSources=new ArrayList<>();
    private final List<PendingPlacer> placers=new ArrayList<>();
    private int definitionIndex=-1,cellX,cellZ,minCellZ,maxCellX,maxCellZ,candidateIndex,placerIndex;
    private List<DepositCandidateSampler.Candidate> candidates=List.of();
    private DepositPlan.Definition definition;
    private boolean exhausted,rasterStarted;
    private long visitedCells,visitedCandidates;

    public DepositSurvey(DepositPlan plan,GeologyFieldSampler geology,PositionalRandomFactory random,
                         int minY,SurveyMapCanvas canvas,Environment environment){
        this.plan=plan;this.geology=geology;this.cache=new DepositCandidateSampler.CellCache(random);
        this.minY=minY;this.canvas=canvas;this.environment=environment;
        this.placerRiver=key->environment.river((int)(key>>32),(int)key);
        long cells=0;
        for(var entry:plan.definitions()){
            Deposit d=entry.deposit();if(!independent(d))continue;
            double reach=entry.reach()+(d instanceof PlacerDeposit?PlacerDeposit.RIVER_SEARCH_RADIUS:0);int size=d.placement().cellSize();
            long nx=(long)Math.floor((canvas.maximumX()+reach)/size)-(long)Math.floor((canvas.minimumX()-reach)/size)+1;
            long nz=(long)Math.floor((canvas.maximumZ()+reach)/size)-(long)Math.floor((canvas.minimumZ()-reach)/size)+1;
            cells+=nx*nz;
            if(cells>32768)throw new IllegalArgumentException("Survey placement configuration is too dense");
        }
        nextDefinition();
    }
    private static boolean independent(Deposit deposit){
        return !(deposit instanceof PeatDeposit)&&!(deposit instanceof SkarnDeposit)&&!(deposit instanceof HydrothermalVeinDeposit)
                &&deposit.placement().maxCandidates()>0&&deposit.placement().frequency()>0;
    }
    private void nextDefinition(){
        definition=null;
        while(++definitionIndex<plan.definitions().size()){
            var entry=plan.definitions().get(definitionIndex);if(!independent(entry.deposit()))continue;
            definition=entry;int size=entry.deposit().placement().cellSize();
            double reach=entry.reach()+(entry.deposit() instanceof PlacerDeposit?PlacerDeposit.RIVER_SEARCH_RADIUS:0);
            cellX=(int)Math.floor((canvas.minimumX()-reach)/size);
            maxCellX=(int)Math.floor((canvas.maximumX()+reach)/size);
            cellZ=minCellZ=(int)Math.floor((canvas.minimumZ()-reach)/size);
            maxCellZ=(int)Math.floor((canvas.maximumZ()+reach)/size);return;
        }
        exhausted=true;
    }
    /** One candidate, one cell lookup, or one raster row; the caller owns the tick budget. */
    public boolean step(){
        if(candidateIndex<candidates.size()){
            if(++visitedCandidates>65536)throw new IllegalStateException("Too many survey candidates");
            inspect(definition.deposit(),definition.salt(),candidates.get(candidateIndex++));return false;
        }
        if(!exhausted){
            if(cellX>maxCellX)nextDefinition();
            if(!exhausted){
                var p=definition.deposit().placement();
                candidates=definition.deposit() instanceof PlacerDeposit
                        ?List.of(cache.regionalSeed(definition.deposit().sequence(),definition.salt(),cellX,cellZ,p.cellSize()))
                        :cache.queryCell(definition.deposit().sequence(),definition.salt(),cellX,cellZ,
                        p.cellSize(),p.maxCandidates(),p.frequency());candidateIndex=0;visitedCells++;
                if(++cellZ>maxCellZ){cellZ=minCellZ;cellX++;}
                return false;
            }
        }
        if(placerIndex<placers.size()){
            var pending=placers.get(placerIndex++);var c=pending.candidate();
            for(Source source:placerSources)if(Math.hypot(pending.anchor().x()-source.x(),pending.anchor().z()-source.z())<source.radius()){
                var shape=pending.deposit().settings().shape();
                canvas.add(new SurveyMapCanvas.Mark(pending.anchor().x(),pending.anchor().z(),shape.x(c.shapeSeed()),
                        shape.z(c.shapeSeed()^91),angle(c.rotationSeed()),GOLD,c.shapeSeed()));break;
            }
            return false;
        }
        rasterStarted=true;return canvas.drawRow();
    }
    public long visitedCells(){return visitedCells;}
    public long visitedCandidates(){return visitedCandidates;}
    public boolean rasterStarted(){return rasterStarted;}
    private boolean hasLayer(DepositCandidateSampler.Candidate c,Block block){
        return !geology.matchingLayersAt(block.defaultBlockState(),c.x(),c.z()).isEmpty();
    }
    private boolean atCenter(FieldDeposit deposit,DepositCandidateSampler.Candidate c,Block... rocks){
        int y=(int)Math.round(deposit.settings().shape().height(minY,c.verticalSeed()));
        var state=geology.sampleRaw(c.x(),y,c.z());
        for(Block rock:rocks)if(state.is(rock))return true;
        return false;
    }
    private void inspect(Deposit deposit,long salt,DepositCandidateSampler.Candidate c){
        if(deposit instanceof BandedIronFormation bif){
            var host=CratonBlocks.GNEISS.getOrigin().getBaseBlock();var layers=geology.matchingLayersAt(host.defaultBlockState(),c.x(),c.z());
            if(layers.isEmpty()||bif.oreDensity()<=0)return;
            int layer=layers.get(index(c.verticalSeed(),layers.size()));
            double gx=(geology.boundaryY(layer,c.x()+4,c.z())-geology.boundaryY(layer,c.x()-4,c.z()))/8;
            double gz=(geology.boundaryY(layer,c.x(),c.z()+4)-geology.boundaryY(layer,c.x(),c.z()-4))/8;
            double dip=Math.toDegrees(Math.atan(Math.hypot(gx,gz)));
            if(dip<bif.dipMin()||dip>bif.dipMax())return;
            double[] strike=geology.strike(layer,c.x(),c.z());
            canvas.add(new SurveyMapCanvas.Mark(c.x(),c.z(),bif.length()*.5,bif.width()*.5,
                    Math.atan2(strike[1],strike[0]),IRON,c.shapeSeed()));
            placerSources.add(new Source(c.x(),c.z(),Math.max(bif.length(),bif.width())*.5+60));return;
        }
        if(!(deposit instanceof FieldDeposit field))return;
        if(deposit instanceof IntrusionDeposit intrusion){
            if(intrusion instanceof DioriteIntrusionDeposit diorite){
                placerSources.add(new Source(c.x(),c.z(),145));
            }
            var parent=intrusion.prepare(c,minY);
            if(!(intrusion instanceof DioriteIntrusionDeposit diorite)||diorite.isFertile(c))
                canvas.add(new SurveyMapCanvas.Mark(c.x(),c.z(),parent.rx,parent.rz,parent.angle,
                        intrusion instanceof GraniteIntrusionDeposit?TIN_TUNGSTEN:intrusion instanceof GabbroIntrusionDeposit?NICKEL:COPPER,c.shapeSeed()));
            if(intrusion instanceof GraniteIntrusionDeposit)
                for(var entry:plan.definitions())if(entry.deposit() instanceof HydrothermalVeinDeposit vein)
                    for(var child:vein.attachedTo(parent,entry.salt())){
                        var shape=vein.settings().shape();
                        canvas.add(new SurveyMapCanvas.Mark(child.candidate().x(),child.candidate().z(),
                                child.geometry().projectedHalfLength(),child.geometry().projectedHalfWidth(),
                                child.geometry().worldAngle(),PEGMATITE,child.candidate().shapeSeed()));
                        placerSources.add(new Source(child.candidate().x(),child.candidate().z(),135));
                    }
            for(var rule:plan.skarns()){
                if(rand01(c.alterationSeed()^rule.salt())>=rule.deposit().placement().frequency())continue;
                if(!hasLayer(c,CratonBlocks.LIMESTONE.getOrigin().getBaseBlock())&&!hasLayer(c,CratonBlocks.MARBLE.getOrigin().getBaseBlock()))continue;
                double reach=rule.deposit().settings().shape().y(c.shapeSeed());
                canvas.add(new SurveyMapCanvas.Mark(c.x(),c.z(),parent.rx+reach,parent.rz+reach,parent.angle,SKARN,c.alterationSeed()^rule.salt()));
            }
        }else if(deposit instanceof StratiformCoalDeposit){
            if(hasLayer(c,CratonBlocks.LIMESTONE.getOrigin().getBaseBlock())||hasLayer(c,CratonBlocks.SHALE.getOrigin().getBaseBlock()))add(field,c,COAL);
        }else if(deposit instanceof StratiformCopperDeposit){
            if(hasLayer(c,CratonBlocks.LIMESTONE.getOrigin().getBaseBlock()))add(field,c,COPPER);
        }else if(deposit instanceof ShaleLeadZincDeposit){
            if(hasLayer(c,CratonBlocks.SHALE.getOrigin().getBaseBlock()))add(field,c,LEAD_ZINC);
        }else if(deposit instanceof SandstoneUraniumDeposit){
            if(hasLayer(c,CratonBlocks.ARKOSE_SANDSTONE.getOrigin().getBaseBlock()))add(field,c,URANIUM);
        }else if(deposit instanceof VmsDeposit){
            var layers=geology.matchingLayersAt(Blocks.TUFF.defaultBlockState(),c.x(),c.z());
            if(layers.isEmpty()||.5+.5*GeologicalNoise.fbm2(c.x(),c.z(),DepositRandomSequences.VMS.salt()^0x564D5350L,.0019,4)<.46)return;
            int layer=layers.get(index(c.verticalSeed(),layers.size()));
            if(!VmsDeposit.isVolcanicHorizon(geology,layer,c.x(),c.z()))return;
            add(field,c,COPPER);placerSources.add(new Source(c.x(),c.z(),105));
        }else if(deposit instanceof EpithermalDeposit){
            if(atCenter(field,c,Blocks.ANDESITE,CratonBlocks.RHYOLITE.getOrigin().getBaseBlock())){
                add(field,c,GOLD);placerSources.add(new Source(c.x(),c.z(),120));
            }
        }else if(deposit instanceof JadeititeDeposit){
            if(atCenter(field,c,CratonBlocks.ECLOGITE.getOrigin().getBaseBlock(),CratonBlocks.SCHIST.getOrigin().getBaseBlock()))add(field,c,GEM);
        }else if(deposit instanceof DeepHydrothermalRedstoneDeposit){
            if(hasLayer(c,CratonBlocks.ECLOGITE.getOrigin().getBaseBlock()))add(field,c,REDSTONE);
        }else if(deposit instanceof KimberliteDeposit){add(field,c,GEM);
        }else if(deposit instanceof WeatheringDeposit){
            if(!environment.ocean(c.x(),c.z())&&geology.sampleRaw(c.x(),(int)Math.floor(environment.surfaceY(c.x(),c.z()))-4,c.z()).is(Blocks.BASALT))add(field,c,BAUXITE);
        }else if(deposit instanceof PlacerDeposit placer){
            var anchor=PlacerDeposit.riverAnchor(c,placerRiver);
            if(!anchor.found())return;
            if(placers.size()>=2048)throw new IllegalStateException("Too many placer candidates");
            placers.add(new PendingPlacer(placer,c,anchor));
        }
    }
    private void add(FieldDeposit deposit,DepositCandidateSampler.Candidate c,SurveyMapCanvas.Group group){
        var shape=deposit.settings().shape();
        canvas.add(new SurveyMapCanvas.Mark(c.x(),c.z(),shape.x(c.shapeSeed()),shape.z(c.shapeSeed()^91),angle(c.rotationSeed()),group,c.shapeSeed()));
    }
}
