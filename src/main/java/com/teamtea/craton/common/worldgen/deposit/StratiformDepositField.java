package com.teamtea.craton.common.worldgen.deposit;

import com.teamtea.craton.common.worldgen.geology.GeologyFieldSampler;
import com.teamtea.craton.common.worldgen.noise.GeologicalNoise;

import com.teamtea.craton.api.geology.deposit.FieldDeposit;
import com.teamtea.craton.api.geology.deposit.SandstoneUraniumDeposit;
import com.teamtea.craton.api.geology.deposit.ShaleLeadZincDeposit;
import com.teamtea.craton.api.geology.deposit.StratiformCoalDeposit;
import com.teamtea.craton.api.geology.deposit.StratiformCopperDeposit;
import com.teamtea.craton.common.registry.CratonBlocks;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.*;

public class StratiformDepositField {
    private StratiformDepositField(){}

    public static BlockState applyStratiform(DepositGenerationContext ctx, BlockState host, int y) {
        BlockState placed = applyLayerBound(ctx, ctx.stratiformCoal(), host, y, 0x11);
        if (!placed.equals(host)) return placed;
        placed = applyLayerBound(ctx, ctx.stratiformCopper(), host, y, 0x29);
        if (!placed.equals(host)) return placed;
        placed = applyLayerBound(ctx, ctx.shaleLeadZinc(), host, y, 0x5A);
        if (!placed.equals(host)) return placed;
        if (ctx.sandstoneUranium().stream().anyMatch(c ->
                ctx.owners().get(c) instanceof SandstoneUraniumDeposit deposit && deposit.matchesHost(host))) {
            DepositSample uranium = sandstoneRollFront(ctx, host, y);
            if (uranium != null) {
                SandstoneUraniumDeposit deposit=(SandstoneUraniumDeposit)ctx.owners().get(uranium.candidate());
                if (deposit.matchesHost(host)) return deposit.placeSample(ctx,host,y,uranium);
            }
        }
        return host;
    }

    private static BlockState applyLayerBound(DepositGenerationContext ctx,
            List<DepositCandidateSampler.Candidate> candidates, BlockState host, int y, long variant) {
        if (candidates.isEmpty()) return host;
        List<DepositCandidateSampler.Candidate> eligible=new java.util.ArrayList<>();
        for (DepositCandidateSampler.Candidate candidate : candidates) {
            Object owner=ctx.owners().get(candidate);
            boolean allowed=owner instanceof StratiformCoalDeposit coal ? coal.matchesHost(host)
                    : owner instanceof StratiformCopperDeposit copper ? copper.matchesHost(host)
                    : owner instanceof ShaleLeadZincDeposit lead ? lead.matchesHost(host) : false;
            if (allowed) eligible.add(candidate);
        }
        if (eligible.isEmpty()) return host;
        DepositSample sample=layerBoundSample(ctx,eligible,host,y,variant);
        if (sample == null) return host;
        Object owner=ctx.owners().get(sample.candidate());
        return owner instanceof StratiformCoalDeposit coal ? coal.placeSample(ctx,host,y,sample)
                : owner instanceof StratiformCopperDeposit copper ? copper.placeSample(ctx,host,y,sample)
                : ((ShaleLeadZincDeposit)owner).placeSample(ctx,host,y,sample);
    }

    public static class DepositSample {
        public DepositSample(){}
        private double score,grade;
        private DepositCandidateSampler.Candidate candidate;
        public void reset(){score=Double.NEGATIVE_INFINITY;grade=0;candidate=null;}
        public void set(double score,double grade,DepositCandidateSampler.Candidate candidate){
            this.score=score;this.grade=grade;this.candidate=candidate;
        }
        public double score(){return score;}
        public double grade(){return grade;}
        public DepositCandidateSampler.Candidate candidate(){return candidate;}
    }

    public static DepositSample layerBoundSample(DepositGenerationContext ctx,List<DepositCandidateSampler.Candidate> candidates,
                                                   BlockState targetHost,int y,long variant){
        DepositSample best=ctx.stratiformSample();
        best.reset();
        Family family=ctx.stratiformCache().family(targetHost,variant);
        Anchor winner=null;
        for(int i=0,size=candidates.size();i<size;i++){
            DepositCandidateSampler.Candidate c=candidates.get(i);
            Lens column=family.columns.get(c);
            if(column==null){
                Anchor anchor=family.anchors.get(c);
                if(anchor==null){anchor=prepareAnchor(ctx,c,targetHost,variant);family.anchors.put(c,anchor);}
                column=prepareLens(ctx,c,anchor,variant);
                family.columns.put(c,column);
            }
            if(column==INVALID_LENS) continue;
            if(column.anchor.coal&&(y+.5<column.bottom||y+.5>=column.top)) continue;
            double vertical=Math.abs(y+.5-column.center)/column.thickness;
            double maximumBody=1-column.d2-sq(vertical)+column.anchor.config.broadNoise()+column.anchor.config.detailNoise();
            boolean satellitePossible=column.pods.possible(ctx.x(),y,ctx.z());
            double absentSatellite=-9*.65-.04;
            if(best.candidate()!=null&&!satellitePossible&&maximumBody<best.score()-1E-9&&absentSatellite<=best.score()) continue;
            // Far from a lens both components are provably below the fixed absent-pod score.
            double score;
            if(!satellitePossible&&maximumBody<absentSatellite-1E-9) score=absentSatellite;
            else{
                double warp=column.anchor.warp(ctx.x(),y,ctx.z());
                double satellite=column.pods.score(ctx.x(),y,ctx.z());
                score=Math.max(1-column.d2-sq(vertical)+warp,satellite*.65-.04);
            }
            if(best.candidate()==null||score>best.score()){
                best.set(score,0,c);winner=column.anchor;
            }
        }
        if(winner==null) return null;
        best.set(best.score(),winner.grade(ctx.x(),y,ctx.z()),best.candidate());
        return best;
    }

    /** Candidate anchors survive between columns; local thickness/center never do. */
    public static class ColumnCache {
        private GeologyFieldSampler geology;
        private final java.util.Map<net.minecraft.world.level.block.Block,java.util.Map<Long,Family>> families=new java.util.HashMap<>();
        public void reset(GeologyFieldSampler sampler){
            if(geology!=sampler){families.clear();geology=sampler;}
            else for(var variants:families.values()) for(Family family:variants.values()) family.columns.clear();
        }
        private Family family(BlockState host,long variant){
            return families.computeIfAbsent(host.getBlock(),ignored -> new java.util.HashMap<>())
                    .computeIfAbsent(variant,ignored -> new Family());
        }
    }
    private static class Family {
        final java.util.Map<DepositCandidateSampler.Candidate,Lens> columns=new java.util.IdentityHashMap<>();
        final java.util.Map<DepositCandidateSampler.Candidate,Anchor> anchors=new java.util.LinkedHashMap<>(32,.75f,true){
            @Override protected boolean removeEldestEntry(java.util.Map.Entry<DepositCandidateSampler.Candidate,Anchor> e){return size()>256;}
        };
    }
    private static class Anchor {
        final int layer;
        final boolean coal;
        final double angle,cos,sin,rx,rz,requestedThickness,offsetRandom;
        final FieldDeposit.Shape config;
        final DepositCandidateSampler.Candidate candidate;
        final PreparedPods pods;
        final Lens lens;
        GeologicalNoise.CachedFbm broad,detail,grade;
        Anchor(int layer,DepositCandidateSampler.Candidate c,FieldDeposit.Shape config,long variant,boolean coal){
            this.coal=coal;
            this.layer=layer;this.candidate=c;this.config=config;
            angle=angle(c.rotationSeed());cos=Math.cos(angle);sin=Math.sin(angle);
            rx=config.x(c.shapeSeed()^variant);rz=config.z(c.shapeSeed()^(variant*31));
            requestedThickness=config.y(c.gradeSeed()^variant);offsetRandom=rand01(c.verticalSeed()^0x7788L);
            pods=layer<0?null:new PreparedPods(c,c.x(),0,c.z(),rx,1,rz,angle,variant,config);
            lens=layer<0?null:new Lens(this,pods);
        }
        double warp(int x,int y,int z){
            if(broad==null){broad=new GeologicalNoise.CachedFbm(candidate.shapeSeed(),.011,3);detail=new GeologicalNoise.CachedFbm(candidate.shapeSeed()^0xA4B1C2D3L,.054,2);}
            return broad.sample(x,y,z)*config.broadNoise()+detail.sample(x,y,z)*config.detailNoise();
        }
        double grade(int x,int y,int z){
            if(grade==null) grade=new GeologicalNoise.CachedFbm(candidate.gradeSeed(),.065,3);
            return .5+.5*grade.sample(x,y,z);
        }
    }
    private static class Lens {
        final Anchor anchor;
        final PreparedPods pods;
        double center,thickness,d2,bottom,top;
        Lens(Anchor anchor,PreparedPods pods){this.anchor=anchor;this.pods=pods;}
    }
    private static final Lens INVALID_LENS=new Lens(null,null);
    private static Anchor prepareAnchor(DepositGenerationContext ctx,DepositCandidateSampler.Candidate c,BlockState host,long variant){
        FieldDeposit deposit=(FieldDeposit)ctx.owners().get(c);
        java.util.function.Predicate<BlockState> eligibleHost = deposit instanceof StratiformCoalDeposit coalDeposit
                ? coalDeposit::matchesHost
                : deposit instanceof StratiformCopperDeposit copper ? copper::matchesHost
                : deposit instanceof ShaleLeadZincDeposit lead ? lead::matchesHost
                : state -> false;
        List<Integer> eligible=matchingHostLayers(ctx,eligibleHost,c.x(),c.z());
        // One selected horizon per deposit candidate, including coal with multiple hosts.
        int selected=eligible.isEmpty()?-1:eligible.get(index(c.verticalSeed()^variant,eligible.size()));
        int layer=selected>=0&&ctx.geology().layers().get(selected).value().blockState().is(host.getBlock()) ? selected : -1;
        boolean coal=deposit instanceof StratiformCoalDeposit;
        return new Anchor(layer,c,shape(ctx,c),variant,coal);
    }
    private static Lens prepareLens(DepositGenerationContext ctx,DepositCandidateSampler.Candidate c,Anchor anchor,long variant){
        if(anchor.layer<0) return INVALID_LENS;
        double layerThickness=ctx.geology().layerThickness(anchor.layer,ctx.x(),ctx.z());
        if(layerThickness<=1E-9) return INVALID_LENS;
        double thickness=Math.min(anchor.requestedThickness,layerThickness*.45);
        double offset=Math.min(layerThickness-thickness,thickness+2+anchor.offsetRandom*layerThickness*.15);
        double top=ctx.geology().boundaryY(anchor.layer,ctx.x(),ctx.z());
        double center=top-offset;
        double dx=ctx.x()+.5-c.x(),dz=ctx.z()+.5-c.z();
        double along=dx*anchor.cos+dz*anchor.sin,across=-dx*anchor.sin+dz*anchor.cos;
        double d2=sq(along/anchor.rx)+sq(across/anchor.rz);
        anchor.pods.retargetVertical(center,thickness*1.6);
        Lens lens=anchor.lens;
        lens.center=center;lens.thickness=thickness;lens.d2=d2;
        lens.top=top;
        lens.bottom=lens.top-layerThickness;
        return lens;
    }

    private static List<Integer> matchingHostLayers(DepositGenerationContext ctx,
            java.util.function.Predicate<BlockState> predicate,int x,int z) {
        List<Integer> matches=new java.util.ArrayList<>();
        for (int i=0;i<ctx.geology().layers().size();i++)
            if (ctx.geology().layerThickness(i,x,z)>=1.0
                    && predicate.test(ctx.geology().layers().get(i).value().blockState())) matches.add(i);
        return matches;
    }

    private static DepositSample sandstoneRollFront(DepositGenerationContext ctx,BlockState host,int y){
        DepositSample best=ctx.stratiformSample();
        best.reset();
        List<DepositCandidateSampler.Candidate> candidates=ctx.sandstoneUranium();
        for(int i=0,size=candidates.size();i<size;i++){
            DepositCandidateSampler.Candidate candidate=candidates.get(i);
            SandstoneUraniumDeposit deposit=(SandstoneUraniumDeposit)ctx.owners().get(candidate);
            if (!deposit.matchesHost(host)) continue;
            List<Integer> sandstoneLayers=matchingHostLayers(ctx,deposit::matchesHost,candidate.x(),candidate.z());
            if(sandstoneLayers.isEmpty()) continue;
            ((SandstoneUraniumDeposit)ctx.owners().get(candidate))
                    .sampleRollFront(ctx,candidate,y,sandstoneLayers,best);
        }
        return best.candidate()==null?null:best;
    }

}
