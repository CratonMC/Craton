package com.teamtea.craton.common.core;

import com.teamtea.craton.api.geology.deposit.Deposit;
import com.teamtea.craton.api.geology.deposit.DepositTypes;
import com.teamtea.craton.api.geology.deposit.FieldDeposit;
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

/**
 * Deterministic single-point evaluator for datapack configured deposit families.
 * Geometry is analytic, Perlin/fBm only distorts boundaries, grade and block occupancy.
 */
public final class DepositFieldEngine {
    private DepositFieldEngine(){}

    public static ColumnContext prepare(PositionalRandomFactory random,GeologyFieldSampler geology,int x,int z,
                                        int minY,int topY,BlockState surfaceState,Holder<Biome> biome){
        Map<Identifier,List<Holder<Deposit>>> definitions=CratonContents.getFieldDeposits();
        Map<DepositCandidateSampler.Candidate,FieldDeposit> owners=new java.util.IdentityHashMap<>();
        Map<Block,List<Integer>> layerIndices=new HashMap<>();
        for(int i=0;i<geology.layers().size();i++)
            layerIndices.computeIfAbsent(geology.layers().get(i).value().blockState().getBlock(),block -> new ArrayList<>()).add(i);
        return new ColumnContext(random,geology,x,z,minY,topY,surfaceState,biome,definitions,owners,layerIndices,
                query(random,definitions,owners,DepositTypes.GRANITE,DepositRandomSequences.INTRUSIVE,x,z),
                query(random,definitions,owners,DepositTypes.DIORITE,DepositRandomSequences.INTRUSIVE,x,z),
                query(random,definitions,owners,DepositTypes.GABBRO,DepositRandomSequences.INTRUSIVE,x,z),
                query(random,definitions,owners,DepositTypes.EPITHERMAL,DepositRandomSequences.EPITHERMAL,x,z),
                query(random,definitions,owners,DepositTypes.VMS,DepositRandomSequences.VMS,x,z),
                query(random,definitions,owners,DepositTypes.VEIN,DepositRandomSequences.VEIN,x,z),
                query(random,definitions,owners,DepositTypes.WEATHERING,DepositRandomSequences.WEATHERING,x,z),
                query(random,definitions,owners,DepositTypes.KIMBERLITE,DepositRandomSequences.SPECIAL,x,z),
                query(random,definitions,owners,DepositTypes.JADEITITE,DepositRandomSequences.SPECIAL,x,z),
                query(random,definitions,owners,DepositTypes.PLACER,DepositRandomSequences.PLACER,x,z),
                query(random,definitions,owners,DepositTypes.COAL,DepositRandomSequences.STRATIFORM,x,z),
                query(random,definitions,owners,DepositTypes.COPPER,DepositRandomSequences.STRATIFORM,x,z),
                query(random,definitions,owners,DepositTypes.URANIUM,DepositRandomSequences.STRATIFORM,x,z));
    }

    private static List<DepositCandidateSampler.Candidate> query(PositionalRandomFactory random,
            Map<Identifier,List<Holder<Deposit>>> definitions,Map<DepositCandidateSampler.Candidate,FieldDeposit> owners,
            Identifier type,DepositRandomSequences sequence,int x,int z){
        List<DepositCandidateSampler.Candidate> result=new ArrayList<>();
        for(Holder<Deposit> entry:definitions.getOrDefault(type,List.of())){
            FieldDeposit field=(FieldDeposit)entry.value();
            FieldDeposit.Placement p=field.settings().placement();
            if(p.frequency()<=0||p.maxCandidates()==0) continue;
            long id=DepositCandidateSampler.salt(entry.unwrapKey().orElseThrow().identifier().toString());
            double reach=Math.max(p.reach(),field.settings().shape().horizontalReach());
            for(DepositCandidateSampler.Candidate c:DepositCandidateSampler.query(random,sequence,id,x,z,
                    p.cellSize(),reach,p.maxCandidates())){
                if(rand01(c.shapeSeed()^id)>=p.frequency()) continue;
                result.add(c);
                owners.put(c,field);
            }
        }
        return result;
    }

    private static FieldDeposit.Shape shape(ColumnContext ctx,DepositCandidateSampler.Candidate c){return ctx.owners().get(c).settings().shape();}
    private static BlockState rock(ColumnContext ctx,DepositCandidateSampler.Candidate c){return ctx.owners().get(c).settings().rock();}
    private static BlockState ore(FieldDeposit d,int index,BlockState host){
        if(index<0||index>=d.settings().ores().size()) return host;
        return d.settings().ores().get(index).value().getOreState(host);
    }
    private static BlockState ore(ColumnContext ctx,DepositCandidateSampler.Candidate c,int index,BlockState host){
        return ore(ctx.owners().get(c),index,host);
    }

    public record ColumnContext(
            PositionalRandomFactory random,GeologyFieldSampler geology,int x,int z,int minY,int topY,
            BlockState surfaceState,Holder<Biome> biome,Map<Identifier,List<Holder<Deposit>>> definitions,
            Map<DepositCandidateSampler.Candidate,FieldDeposit> owners,
            Map<Block,List<Integer>> layerIndices,
            List<DepositCandidateSampler.Candidate> granite,
            List<DepositCandidateSampler.Candidate> diorite,
            List<DepositCandidateSampler.Candidate> gabbro,
            List<DepositCandidateSampler.Candidate> epithermal,
            List<DepositCandidateSampler.Candidate> vms,
            List<DepositCandidateSampler.Candidate> veins,
            List<DepositCandidateSampler.Candidate> weathering,
            List<DepositCandidateSampler.Candidate> kimberlite,
            List<DepositCandidateSampler.Candidate> jadeitite,
            List<DepositCandidateSampler.Candidate> placer,
            List<DepositCandidateSampler.Candidate> stratiformCoal,
            List<DepositCandidateSampler.Candidate> stratiformCopper,
            List<DepositCandidateSampler.Candidate> sandstoneUranium
    ){}

    public static BlockState apply(ColumnContext ctx,BlockState host,BlockState initialState,int y){
        BlockState state=initialState;
        if(state.equals(host)) state=applyStratiform(ctx,state,y);

        IntrusionResult intrusion=sampleIntrusions(ctx,y);
        if(intrusion.candidate()!=null&&intrusionOccupies(ctx,y,intrusion)){
            state=rock(ctx,intrusion.candidate());
            state=applyIntrusiveOre(ctx,state,y,intrusion);
        }else if(state.equals(host)){
            state=applyIntrusiveSatellite(ctx,state,y);
        }

        if(intrusion.candidate()!=null&&intrusion.score()<=.12) state=applySkarn(ctx,host,state,y,intrusion);
        state=applyVms(ctx,host,state,y);
        state=applyEpithermal(ctx,host,state,y);
        state=applyVeins(ctx,host,state,y);
        state=applyWeathering(ctx,host,state,y);
        state=applySpecial(ctx,host,state,y);
        state=applyPlacer(ctx,host,state,y);
        return state;
    }

    /* ---------------- 1. stratiform / stratabound ---------------- */

    private static BlockState applyStratiform(ColumnContext ctx,BlockState host,int y){
        if(isLimestone(host)){
            DepositSample coal=layerBoundSample(ctx,ctx.stratiformCoal(),CratonBlocks.LIMESTONE.getOrigin().getBaseBlock().defaultBlockState(),y,0x11);
            if(coal!=null&&occupies(coal.score(),coal.grade(),coal.candidate(),ctx.x(),y,ctx.z(),.10,.82))
                return ore(ctx,coal.candidate(),0,host);

            DepositSample copper=layerBoundSample(ctx,ctx.stratiformCopper(),CratonBlocks.LIMESTONE.getOrigin().getBaseBlock().defaultBlockState(),y,0x29);
            if(copper!=null&&occupies(copper.score(),copper.grade(),copper.candidate(),ctx.x(),y,ctx.z(),.14,.72))
                return ore(ctx,copper.candidate(),0,host);
        }
        if(host.is(Blocks.SANDSTONE)||host.is(Blocks.RED_SANDSTONE)){
            DepositSample u=sandstoneRollFront(ctx,ctx.sandstoneUranium(),y);
            if(u!=null&&occupies(u.score(),u.grade(),u.candidate(),ctx.x(),y,ctx.z(),.12,.62))
                return ore(ctx,u.candidate(),0,host);
        }
        return host;
    }

    private record DepositSample(double score,double grade,DepositCandidateSampler.Candidate candidate){}

    private static DepositSample layerBoundSample(ColumnContext ctx,List<DepositCandidateSampler.Candidate> candidates,
                                                   BlockState targetHost,int y,long variant){
        List<Integer> targetLayers=matchingLayers(ctx,targetHost);
        if(targetLayers.isEmpty()) return null;
        DepositSample best=null;
        for(DepositCandidateSampler.Candidate c:candidates){
            int layer=targetLayers.get(index(c.verticalSeed()^variant,targetLayers.size()));
            double angle=angle(c.rotationSeed());
            double[] p=rotate(ctx.x()+.5-c.x(),ctx.z()+.5-c.z(),angle);
            FieldDeposit.Shape config=shape(ctx,c);
            double rx=config.x(c.shapeSeed()^variant);
            double rz=config.z(c.shapeSeed()^(variant*31));
            double d2=sq(p[0]/rx)+sq(p[1]/rz);
            double boundary=ctx.geology().boundaryY(layer,ctx.x(),ctx.z());
            double thickness=config.y(c.gradeSeed()^variant);
            double layerThickness=ctx.geology().thickness(layer);
            double offset=Math.min(layerThickness*.65,thickness+2+rand01(c.verticalSeed()^0x7788L)*layerThickness*.15);
            double vertical=Math.abs(y+.5-(boundary-offset))/thickness;
            double warp=shapeNoise(ctx.x(),y,ctx.z(),c.shapeSeed(),.011,.054,config.broadNoise(),config.detailNoise());
            double center=boundary-offset;
            double satellite=satellitePods(c,ctx.x(),y,ctx.z(),c.x(),center,c.z(),rx,thickness*1.6,rz,angle,variant,config);
            double score=Math.max(1-d2-vertical*.72+warp,satellite*.65-.04);
            double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.065,3);
            if(best==null||score>best.score()) best=new DepositSample(score,grade,c);
        }
        return best;
    }

    private static DepositSample sandstoneRollFront(ColumnContext ctx,List<DepositCandidateSampler.Candidate> candidates,int y){
        List<Integer> sandstoneLayers=matchingLayers(ctx,Blocks.SANDSTONE.defaultBlockState());
        if(sandstoneLayers.isEmpty()) return null;
        DepositSample best=null;
        for(DepositCandidateSampler.Candidate c:candidates){
            int layer=sandstoneLayers.get(index(c.verticalSeed(),sandstoneLayers.size()));
            double angle=angle(c.rotationSeed());
            double[] p=rotate(ctx.x()+.5-c.x(),ctx.z()+.5-c.z(),angle);
            FieldDeposit.Shape config=shape(ctx,c);
            double rx=config.x(c.shapeSeed()),rz=config.z(c.shapeSeed()^91);
            double sandstoneTop=ctx.geology().boundaryY(layer,ctx.x(),ctx.z());
            double thickness=ctx.geology().thickness(layer);
            double cy=sandstoneTop-thickness*.52;
            double dy=(y+.5-cy)/Math.max(5,thickness*.42);
            double front=p[1]-rz*.32*(1-dy*dy);
            double band=config.y(c.gradeSeed());
            double body=Math.min(1-Math.abs(p[0])/rx,1-Math.abs(front)/band)
                    +shapeNoise(ctx.x(),y,ctx.z(),c.shapeSeed()^0x55,.010,.055,config.broadNoise(),config.detailNoise());
            double satellite=satellitePods(c,ctx.x(),y,ctx.z(),c.x(),cy,c.z(),rx,thickness*.38,rz,angle,0x55,config);
            double score=Math.max(body,satellite*.65-.04);
            double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.07,3);
            if(best==null||score>best.score()) best=new DepositSample(score,grade,c);
        }
        return best;
    }

    /* ---------------- 2. intrusive-related ---------------- */

    private record IntrusionResult(double score,double surfaceDistance,int kind,DepositCandidateSampler.Candidate candidate,
                                   double normalizedY,double radial){}

    private static IntrusionResult sampleIntrusions(ColumnContext ctx,int y){
        IntrusionResult best=new IntrusionResult(-99,-999,0,null,0,0);
        best=better(best,sampleGranite(ctx,y));
        best=better(best,sampleDiorite(ctx,y));
        best=better(best,sampleGabbro(ctx,y));
        return best;
    }

    private static IntrusionResult better(IntrusionResult a,IntrusionResult b){return b.score()>a.score()?b:a;}

    private static IntrusionResult sampleGranite(ColumnContext ctx,int y){
        IntrusionResult best=new IntrusionResult(-99,-999,0,null,0,0);
        if(ctx.granite().isEmpty()) return best;
        for(DepositCandidateSampler.Candidate c:ctx.granite()){
            FieldDeposit.Shape config=shape(ctx,c);
            double angle=angle(c.rotationSeed());
            double wx=ctx.x()+.5+GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.shapeSeed(),.009,3)*8-c.x();
            double wz=ctx.z()+.5+GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.shapeSeed()^0x51,.009,3)*8-c.z();
            double[] p=rotate(wx,wz,angle);
            double rx=config.x(c.shapeSeed()),rz=config.z(c.shapeSeed()^0xA7),ry=config.y(c.shapeSeed()^0xC3);
            double cy=config.height(ctx.minY(),c.verticalSeed());
            double ny=(y+.5-cy)/ry,rad=Math.sqrt(sq(p[0]/rx)+sq(p[1]/rz));
            double chamber=1-Math.sqrt(rad*rad+ny*ny);
            double cupolaCy=cy+ry*.62, cupola=1-Math.sqrt(sq(p[0]/(rx*.46))+sq(p[1]/(rz*.46))+sq((y+.5-cupolaCy)/(ry*.42)));
            double score=Math.max(chamber,cupola)
                    +shapeNoise(ctx.x(),y,ctx.z(),c.shapeSeed()^0xB5297A4DL,.008,.041,config.broadNoise(),config.detailNoise());
            double dist=score*Math.min(Math.min(rx,rz),ry);
            if(score>best.score()) best=new IntrusionResult(score,dist,0,c,ny,rad);
        }
        return best;
    }

    private static IntrusionResult sampleDiorite(ColumnContext ctx,int y){
        IntrusionResult best=new IntrusionResult(-99,-999,1,null,0,0);
        if(ctx.diorite().isEmpty()) return best;
        for(DepositCandidateSampler.Candidate c:ctx.diorite()){
            FieldDeposit.Shape config=shape(ctx,c);
            double angle=angle(c.rotationSeed());
            double wx=ctx.x()+.5+GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.shapeSeed(),.013,3)*7-c.x();
            double wz=ctx.z()+.5+GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.shapeSeed()^0x91,.013,3)*7-c.z();
            double[] p=rotate(wx,wz,angle);
            double rx=config.x(c.shapeSeed()),rz=config.z(c.shapeSeed()^17),ry=config.y(c.shapeSeed()^31);
            double cy=config.height(ctx.minY(),c.verticalSeed());
            double ny=(y+.5-cy)/ry,rad=Math.sqrt(sq(p[0]/rx)+sq(p[1]/rz));
            double score=1-Math.sqrt(rad*rad+ny*ny)
                    +shapeNoise(ctx.x(),y,ctx.z(),c.shapeSeed()^0xD1,.010,.046,config.broadNoise(),config.detailNoise());
            double dist=score*Math.min(Math.min(rx,rz),ry);
            if(score>best.score()) best=new IntrusionResult(score,dist,1,c,ny,rad);
        }
        return best;
    }

    private static IntrusionResult sampleGabbro(ColumnContext ctx,int y){
        IntrusionResult best=new IntrusionResult(-99,-999,2,null,0,0);
        if(ctx.gabbro().isEmpty()) return best;
        for(DepositCandidateSampler.Candidate c:ctx.gabbro()){
            FieldDeposit.Shape config=shape(ctx,c);
            double angle=angle(c.rotationSeed());
            double[] p=rotate(ctx.x()+.5-c.x(),ctx.z()+.5-c.z(),angle);
            double rx=config.x(c.shapeSeed()),rz=config.z(c.shapeSeed()^17);
            double cy=config.height(ctx.minY(),c.verticalSeed());
            double radial=Math.sqrt(sq(p[0]/rx)+sq(p[1]/rz));
            double bowl=cy+18*radial*radial+GeologicalNoise.fbm2(ctx.x(),ctx.z(),c.shapeSeed(),.014,3)*5;
            double half=config.y(c.shapeSeed()^71);
            double vertical=Math.abs(y+.5-bowl)/half;
            double score=1-radial-vertical*.78
                    +shapeNoise(ctx.x(),y,ctx.z(),c.shapeSeed()^0x77,.009,.050,config.broadNoise(),config.detailNoise());
            double dist=score*Math.min(Math.min(rx,rz),half);
            double ny=(y+.5-bowl)/half;
            if(score>best.score()) best=new IntrusionResult(score,dist,2,c,ny,radial);
        }
        return best;
    }

    private static boolean intrusionOccupies(ColumnContext ctx,int y,IntrusionResult r){
        double p=GeologicalNoise.smoothstep(-.08,.20,r.score());
        if(r.score()>.28) p=.985;
        double occ=GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),r.candidate().occupancySeed(),.12);
        return occ<p;
    }

    private static BlockState applyIntrusiveOre(ColumnContext ctx,BlockState host,int y,IntrusionResult r){
        DepositCandidateSampler.Candidate c=r.candidate();
        double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.050,3);
        double mix=GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.oreSeed(),.105);
        if(r.kind()==2){
            double floor=(-r.normalizedY()-.12)+GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.alterationSeed(),.033,3)*.20;
            if(r.score()>.02&&floor>.18&&occupies(r.score()+floor*.35,grade,c,ctx.x(),y,ctx.z(),.05,.70)){
                if(mix>.78) return ore(ctx,c,1,host);
                return ore(ctx,c,0,host);
            }
        }else if(r.kind()==1){
            double hydro=(1-Math.abs(r.normalizedY()+.05))*.65+r.score()*.55;
            if(hydro>.48&&occupies(hydro-.35,grade,c,ctx.x(),y,ctx.z(),.12,.58))
                return ore(ctx,c,0,host);
        }else{
            double cupola=r.normalizedY()*.55+r.score()*.55;
            if(cupola>.32&&occupies(cupola-.25,grade,c,ctx.x(),y,ctx.z(),.10,.52))
                return ore(ctx,c,0,host);
        }
        return host;
    }

    private static BlockState applyIntrusiveSatellite(ColumnContext ctx,BlockState host,int y){
        BlockState state=host;
        for(DepositCandidateSampler.Candidate c:ctx.granite()){
            state=intrusivePod(ctx,state,y,c,0);
            if(!state.equals(host)) return state;
        }
        for(DepositCandidateSampler.Candidate c:ctx.diorite()){
            state=intrusivePod(ctx,state,y,c,1);
            if(!state.equals(host)) return state;
        }
        for(DepositCandidateSampler.Candidate c:ctx.gabbro()){
            state=intrusivePod(ctx,state,y,c,2);
            if(!state.equals(host)) return state;
        }
        return host;
    }

    private static BlockState intrusivePod(ColumnContext ctx,BlockState host,int y,DepositCandidateSampler.Candidate c,int kind){
        double cy,rx,ry,rz;
        if(kind==0){
            FieldDeposit.Shape config=shape(ctx,c);
            cy=config.height(ctx.minY(),c.verticalSeed());
            rx=config.x(c.shapeSeed());
            rz=config.z(c.shapeSeed()^0xA7);
            ry=config.y(c.shapeSeed()^0xC3);
        }else if(kind==1){
            FieldDeposit.Shape config=shape(ctx,c);
            cy=config.height(ctx.minY(),c.verticalSeed());
            rx=config.x(c.shapeSeed());
            rz=config.z(c.shapeSeed()^17);
            ry=config.y(c.shapeSeed()^31);
        }else{
            FieldDeposit.Shape config=shape(ctx,c);
            cy=config.height(ctx.minY(),c.verticalSeed());
            rx=config.x(c.shapeSeed());
            rz=config.z(c.shapeSeed()^17);
            ry=config.y(c.shapeSeed()^71);
        }
        double pod=satellitePods(c,ctx.x(),y,ctx.z(),c.x(),cy,c.z(),rx,ry,rz,angle(c.rotationSeed()),0x1A7L,shape(ctx,c));
        if(pod<-.12) return host;
        double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.057,3);
        if(!occupies(pod*.62,grade,c,ctx.x(),y,ctx.z(),.06,.50)) return host;
        return rock(ctx,c);
    }

    /* ---------------- 3. epithermal ---------------- */

    private static BlockState applyEpithermal(ColumnContext ctx,BlockState originalHost,BlockState state,int y){
        if(!originalHost.is(Blocks.ANDESITE)&&!isRhyolite(originalHost)) return state;
        for(DepositCandidateSampler.Candidate c:ctx.epithermal()){
            FieldDeposit.Shape config=shape(ctx,c);
            double score=veinSystemScore(c,ctx.x(),y,ctx.z(),ctx.minY(),config.x(c.shapeSeed()),config.z(c.shapeSeed()),.021,2,config);
            if(score<-.14) continue;
            double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.065,3);
            double p=GeologicalNoise.smoothstep(-.12,.42,score)*(.28+.62*grade);
            double occ=GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.occupancySeed(),.17);
            if(occ<p){
                if(grade>.67&&GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.oreSeed(),.11)<.34)
                    return ore(ctx,c,0,state);
                return rock(ctx,c);
            }
        }
        return state;
    }

    /* ---------------- 4. VMS ---------------- */

    private static BlockState applyVms(ColumnContext ctx,BlockState originalHost,BlockState state,int y){
        boolean cap=originalHost.is(Blocks.TUFF)||originalHost.is(Blocks.BASALT);
        boolean footwall=originalHost.is(Blocks.ANDESITE)||originalHost.is(CratonBlocks.GABBRO.getOrigin().getBaseBlock());
        if(!cap&&!footwall) return state;
        List<Integer> tuffLayers=matchingLayers(ctx,Blocks.TUFF.defaultBlockState());
        if(tuffLayers.isEmpty()) return state;

        double province=.5+.5*GeologicalNoise.fbm2(ctx.x(),ctx.z(),DepositRandomSequences.VMS.salt()^0x564D5350L,.0019,4);
        if(province<.46) return state;

        for(DepositCandidateSampler.Candidate c:ctx.vms()){
            int layer=tuffLayers.get(index(c.verticalSeed(),tuffLayers.size()));
            if(layer==0||layer+1>=ctx.geology().layers().size()
                    ||!ctx.geology().layers().get(layer-1).value().blockState().is(Blocks.BASALT)
                    ||!ctx.geology().layers().get(layer+1).value().blockState().is(CratonBlocks.GABBRO.getOrigin().getBaseBlock())) continue;
            double cy=ctx.geology().boundaryY(layer,ctx.x(),ctx.z())-2.0;
            double angle=angle(c.rotationSeed());
            double[] p=rotate(ctx.x()+.5-c.x(),ctx.z()+.5-c.z(),angle);
            FieldDeposit.Shape config=shape(ctx,c);
            double rx=config.x(c.shapeSeed()),rz=config.z(c.shapeSeed()^91),ry=config.y(c.shapeSeed()^173);
            double broad=GeologicalNoise.fbm2(ctx.x(),ctx.z(),c.shapeSeed()^0x46A7L,.009,3);
            double detail=GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.shapeSeed(),.042,3);
            double upper=y+.5-cy;
            double vertical=upper>0?upper/(ry*.55):upper/(ry*1.30);
            double lens=1-sq(p[0]/rx)-sq(p[1]/rz)-sq(vertical)+broad*config.broadNoise()+detail*config.detailNoise();

            double feeder=feederScore(c,ctx.x(),y,ctx.z(),cy,Math.min(rx,rz)*.45,30+rand01(c.shapeSeed()^0x99)*25);
            double satellites=satellitePods(c,ctx.x(),y,ctx.z(),c.x(),cy,c.z(),rx,ry,rz,angle,0x564D53L,config);
            if(!cap) lens=-9;
            double score=Math.max(Math.max(lens,feeder*.70),cap?satellites*.65-.04:-9);
            if(score<-.15) continue;

            double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.075,3);
            if(!occupies(score,grade,c,ctx.x(),y,ctx.z(),.10,.78)) continue;
            double mix=GeologicalNoise.occupancy(ctx.x()+13,y,ctx.z()-7,c.oreSeed(),.12);
            if(feeder*.70>lens){
                if(mix<.51) return ore(ctx,c,0,state);
                return ore(ctx,c,3,state);
            }
            if(mix<.43) return ore(ctx,c,0,state);
            if(mix<.52) return ore(ctx,c,1,state);
            if(mix<.57) return ore(ctx,c,2,state);
            return ore(ctx,c,3,state);
        }
        return state;
    }

    private static double feederScore(DepositCandidateSampler.Candidate c,int x,int y,int z,double top,double radius,double depth){
        if(y>top+2||y<top-depth) return -9;
        double t=(top-y)/depth;
        double warpX=GeologicalNoise.fbm(x,y,z,c.shapeSeed()^0x321,.035,3)*8;
        double warpZ=GeologicalNoise.fbm(x,y,z,c.shapeSeed()^0x765,.035,3)*8;
        double dx=x+.5-c.x()+warpX,dz=z+.5-c.z()+warpZ;
        double widening=radius*(.22+.52*(1-t));
        double d=Math.hypot(dx,dz)/Math.max(1,widening);
        double stringer=.5+.5*GeologicalNoise.fbm(x*1.35,y,z*1.35,c.alterationSeed(),.095,3);
        return 1-d+(stringer-.5)*.55;
    }

    /* ---------------- 5. skarn ---------------- */

    private static BlockState applySkarn(ColumnContext ctx,BlockState host,BlockState state,int y,IntrusionResult intrusion){
        for(Holder<Deposit> entry:ctx.definitions().getOrDefault(DepositTypes.SKARN,List.of())){
            long salt=DepositCandidateSampler.salt(entry.unwrapKey().orElseThrow().identifier().toString());
            state=applySkarnInstance(ctx,host,state,y,intrusion,(FieldDeposit)entry.value(),salt);
        }
        return state;
    }

    private static BlockState applySkarnInstance(ColumnContext ctx,BlockState originalHost,BlockState state,int y,IntrusionResult intrusion,FieldDeposit definition,long salt){
        if(!isCarbonate(originalHost)||intrusion.candidate()==null) return state;
        if(rand01(intrusion.candidate().alterationSeed()^salt)>=definition.settings().placement().frequency()) return state;
        double reach=definition.settings().shape().y(intrusion.candidate().shapeSeed());
        if(intrusion.surfaceDistance()>0||intrusion.surfaceDistance()<-reach) return state;

        DepositCandidateSampler.Candidate c=intrusion.candidate();
        FieldDeposit.Shape config=definition.settings().shape();
        double broad=GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.alterationSeed()^salt,.012,3);
        double detail=GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.shapeSeed()^0x5CA1L^salt,.052,2);
        double patch=.23+broad*config.broadNoise()+detail*config.detailNoise();
        double metasomatism=GeologicalNoise.smoothstep(-config.y(c.shapeSeed()),0,intrusion.surfaceDistance());
        double grade=.5+.5*GeologicalNoise.fbm(ctx.x()-41,y+19,ctx.z()+67,c.gradeSeed()^salt,.065,3);
        double score=Math.min(patch,metasomatism);
        double alterP=GeologicalNoise.smoothstep(-.15,.45,score)*(.42+.42*grade);
        if(GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.alterationSeed()^salt,.15)>alterP) return state;

        BlockState altered=definition.settings().rock();
        if(grade<.62) return altered;
        double mix=GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.oreSeed()^salt,.115);
        return switch(intrusion.kind()){
            case 0 -> mix<.50
                    ?ore(definition,0,altered)
                    :ore(definition,1,altered);
            case 1 -> mix<.51
                    ?ore(definition,2,altered)
                    :ore(definition,3,altered);
            default -> mix<.58
                    ?ore(definition,4,altered)
                    :ore(definition,2,altered);
        };
    }

    /* ---------------- 6. quartz / late hydrothermal veins ---------------- */

    private static BlockState applyVeins(ColumnContext ctx,BlockState originalHost,BlockState state,int y){
        if(!originalHost.is(Blocks.GRANITE)&&!isPegmatite(originalHost)) return state;
        for(DepositCandidateSampler.Candidate c:ctx.veins()){
            FieldDeposit.Shape config=shape(ctx,c);
            double score=veinSystemScore(c,ctx.x(),y,ctx.z(),ctx.minY(),config.x(c.shapeSeed()),config.z(c.shapeSeed()),.013,3,config);
            if(score<-.15) continue;
            double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.075,3);
            double p=GeologicalNoise.smoothstep(-.14,.38,score)*(.34+.58*grade);
            if(GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.occupancySeed(),.18)>p) continue;
            double mix=GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.oreSeed(),.105);
            if(grade>.73&&mix<.24) return ore(ctx,c,0,state);
            if(grade>.60&&mix<.52) return ore(ctx,c,1,state);
            if(mix>.78) return ore(ctx,c,2,state);
            return rock(ctx,c);
        }
        return state;
    }

    /* ---------------- 7. weathering residual ---------------- */

    private static BlockState applyWeathering(ColumnContext ctx,BlockState originalHost,BlockState state,int y){
        if(!originalHost.is(Blocks.BASALT)||ctx.biome().is(Tags.Biomes.IS_OCEAN)
                ||!ctx.definitions().containsKey(DepositTypes.WEATHERING)) return state;
        int depth=ctx.topY()-y;
        if(depth<1) return state;
        for(DepositCandidateSampler.Candidate c:ctx.weathering()){
            FieldDeposit.Shape config=shape(ctx,c);
            if(depth>config.y(c.shapeSeed())) continue;
            double dx=ctx.x()+.5-c.x(),dz=ctx.z()+.5-c.z();
            double r=config.x(c.shapeSeed()),rz=config.z(c.shapeSeed()^91);
            double footprint=1-sq(dx/r)-sq(dz/rz)
                    +shapeNoise2(ctx.x(),ctx.z(),c.shapeSeed(),.010,.052,config.broadNoise(),config.detailNoise());
            double satellite=satellitePods(c,ctx.x(),y,ctx.z(),c.x(),ctx.topY()-config.y(c.shapeSeed())*.5,c.z(),r,config.y(c.shapeSeed())*.38,rz,0,0xBA6817L,config);
            footprint=Math.max(footprint,satellite*.65-.04);
            if(footprint<-.10) continue;
            double edgeP=GeologicalNoise.smoothstep(-.10,.38,footprint);
            if(GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.occupancySeed(),.15)>edgeP) continue;

            List<BlockState> alteration=ctx.owners().get(c).settings().alterationRocks();
            double thickness=config.y(c.shapeSeed());
            if(depth<=thickness*3/13) return alteration.size()>0?alteration.get(0):rock(ctx,c);
            if(depth<=thickness*9/13){
                double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.075,3);
                if(grade>.42) return ore(ctx,c,0,state);
                return alteration.size()>1?alteration.get(1):rock(ctx,c);
            }
            return alteration.size()>2?alteration.get(2):rock(ctx,c);
        }
        return state;
    }

    /* ---------------- 8. special bodies ---------------- */

    private static BlockState applySpecial(ColumnContext ctx,BlockState originalHost,BlockState state,int y){
        state=applyKimberlite(ctx,state,y);
        state=applyJadeitite(ctx,originalHost,state,y);
        return state;
    }

    private static BlockState applyKimberlite(ColumnContext ctx,BlockState state,int y){
        for(DepositCandidateSampler.Candidate c:ctx.kimberlite()){
            FieldDeposit.Shape config=shape(ctx,c);
            double cy=config.height(ctx.minY(),c.verticalSeed());
            double bottom=cy-config.y(c.shapeSeed()),top=cy+config.radiusYMax();
            if(y<bottom||y>top) continue;
            double t=(y-bottom)/(top-bottom);
            double radius=config.radiusXMin()+(config.x(c.shapeSeed())-config.radiusXMin())*Math.pow(t,.78);
            double dx=ctx.x()+.5-c.x()+GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.shapeSeed(),.020,3)*5;
            double dz=ctx.z()+.5-c.z()+GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.shapeSeed()^71,.020,3)*5;
            double score=1-Math.hypot(dx,dz)/radius
                    +shapeNoise(ctx.x(),y,ctx.z(),c.shapeSeed()^0xB64L,.011,.060,config.broadNoise(),config.detailNoise());
            if(score<-.12) continue;
            double p=GeologicalNoise.smoothstep(-.12,.25,score);
            if(GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.occupancySeed(),.16)>p) continue;
            double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.09,3);
            if(grade>.88&&GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.oreSeed(),.13)<.18)
                return ore(ctx,c,0,state);
            return rock(ctx,c);
        }
        return state;
    }

    private static BlockState applyJadeitite(ColumnContext ctx,BlockState originalHost,BlockState state,int y){
        // Current rock registry has no eclogite/schist yet; gneiss/deepslate are conservative HP-background proxies.
        if(!originalHost.is(CratonBlocks.GNEISS.getOrigin().getBaseBlock())&&!originalHost.is(Blocks.DEEPSLATE)) return state;
        for(DepositCandidateSampler.Candidate c:ctx.jadeitite()){
            double angle=angle(c.rotationSeed());
            double[] p=rotate(ctx.x()+.5-c.x(),ctx.z()+.5-c.z(),angle);
            FieldDeposit.Shape config=shape(ctx,c);
            double cy=config.height(ctx.minY(),c.verticalSeed());
            double rx=config.x(c.shapeSeed()),rz=config.z(c.shapeSeed()),ry=config.y(c.shapeSeed());
            double body=1-sq(p[0]/rx)-sq(p[1]/rz)-sq((y+.5-cy)/ry)
                    +shapeNoise(ctx.x(),y,ctx.z(),c.shapeSeed(),.013,.065,config.broadNoise(),config.detailNoise());
            double pods=satellitePods(c,ctx.x(),y,ctx.z(),c.x(),cy,c.z(),rx,ry,rz,angle,0x6ADEL,config);
            double score=Math.max(body,pods*.65-.04);
            if(score<-.12) continue;
            double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.085,3);
            if(occupies(score,grade,c,ctx.x(),y,ctx.z(),.08,.74))
                return ore(ctx,c,0,state);
        }
        return state;
    }

    /* ---------------- 9. placer ---------------- */

    private record SourceStrength(double gold,double iron){}

    private static BlockState applyPlacer(ColumnContext ctx,BlockState originalHost,BlockState state,int y){
        if(ctx.topY()-y<0||!ctx.biome().is(Tags.Biomes.IS_RIVER)) return state;
        boolean sediment=ctx.surfaceState().is(Blocks.SAND)||ctx.surfaceState().is(Blocks.RED_SAND)||ctx.surfaceState().is(Blocks.GRAVEL);
        if(!sediment) return state;
        SourceStrength source=primarySourceStrength(ctx);
        if(source.gold()<=0&&source.iron()<=0) return state;

        for(DepositCandidateSampler.Candidate c:ctx.placer()){
            double dx=ctx.x()+.5-c.x(),dz=ctx.z()+.5-c.z();
            FieldDeposit.Shape config=shape(ctx,c);
            if(ctx.topY()-y>config.y(c.shapeSeed())) continue;
            double r=config.x(c.shapeSeed()),rz=config.z(c.shapeSeed()^91);
            double score=1-sq(dx/r)-sq(dz/rz)
                    +shapeNoise2(ctx.x(),ctx.z(),c.shapeSeed(),.015,.075,config.broadNoise(),config.detailNoise());
            if(score<-.10) continue;
            double placerGrade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.11,3);
            double p=GeologicalNoise.smoothstep(-.10,.35,score)*(.30+.55*placerGrade);
            if(GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.occupancySeed(),.18)>p) continue;
            if(source.gold()>=source.iron()&&source.gold()*placerGrade>.42)
                return ore(ctx,c,0,state);
            if(source.iron()*placerGrade>.38)
                return ore(ctx,c,1,state);
        }
        return state;
    }

    private static SourceStrength primarySourceStrength(ColumnContext ctx){
        double gold=0,iron=0;
        for(DepositCandidateSampler.Candidate c:ctx.epithermal()){
            double cy=shape(ctx,c).height(ctx.minY(),c.verticalSeed());
            BlockState h=ctx.geology().sampleRaw(c.x(),(int)Math.round(cy),c.z());
            if(h.is(Blocks.ANDESITE)||isRhyolite(h)) gold=Math.max(gold,proximity(ctx.x(),ctx.z(),c,120));
        }
        for(DepositCandidateSampler.Candidate c:ctx.veins()){
            double cy=shape(ctx,c).height(ctx.minY(),c.verticalSeed());
            BlockState h=ctx.geology().sampleRaw(c.x(),(int)Math.round(cy),c.z());
            if(h.is(Blocks.GRANITE)||isPegmatite(h)) gold=Math.max(gold,proximity(ctx.x(),ctx.z(),c,135));
        }
        for(DepositCandidateSampler.Candidate c:ctx.diorite()) gold=Math.max(gold,proximity(ctx.x(),ctx.z(),c,145)*.75);
        List<Integer> tuffLayers=matchingLayers(ctx,Blocks.TUFF.defaultBlockState());
        if(!tuffLayers.isEmpty()){
            for(DepositCandidateSampler.Candidate c:ctx.vms()){
                double province=.5+.5*GeologicalNoise.fbm2(c.x(),c.z(),DepositRandomSequences.VMS.salt()^0x564D5350L,.0019,4);
                if(province<.46) continue;
                int layer=tuffLayers.get(index(c.verticalSeed(),tuffLayers.size()));
                if(layer==0||layer+1>=ctx.geology().layers().size()
                        ||!ctx.geology().layers().get(layer-1).value().blockState().is(Blocks.BASALT)
                        ||!ctx.geology().layers().get(layer+1).value().blockState().is(CratonBlocks.GABBRO.getOrigin().getBaseBlock())) continue;
                double cy=ctx.geology().boundaryY(layer,c.x(),c.z())-2.0;
                BlockState h=ctx.geology().sampleRaw(c.x(),(int)Math.round(cy),c.z());
                if(h.is(Blocks.TUFF)) iron=Math.max(iron,proximity(ctx.x(),ctx.z(),c,105)*.55);
            }
        }
        // Skarn candidates are intentionally not treated as placer sources unless a real
        // carbonate/intrusion contact can be reconstructed; candidate proximity alone is insufficient.
        return new SourceStrength(gold,iron);
    }

    /* ---------------- shared geometry ---------------- */

    /** Small correlated pods follow the parent body's orientation and stay near its outer envelope. */
    private static double satellitePods(DepositCandidateSampler.Candidate c,int x,int y,int z,
                                        double cx,double cy,double cz,double rx,double ry,double rz,
                                        double angle,long variant,FieldDeposit.Shape config){
        if(config.satelliteCount()<=0||config.satelliteScale()<=0) return -9;
        double[] p=rotate(x+.5-cx,z+.5-cz,angle);
        if(Math.abs(p[0])>rx*1.27+24*config.satelliteScale()||Math.abs(p[1])>rz*1.27+20*config.satelliteScale()
                ||Math.abs(y+.5-cy)>ry*.72+7*config.satelliteScale()) return -9;
        double best=-9;
        for(int i=0;i<Math.min(24,config.satelliteCount());i++){
            long seed=DepositCandidateSampler.mix(c.shapeSeed()^variant^(0x9E3779B97F4A7C15L*(i+1)));
            double theta=rand01(seed)*Math.PI*2;
            double latitude=(rand01(seed^0xA71L)-.5)*1.12;
            double shell=1.04+rand01(seed^0xB52L)*.22;
            double horizontal=Math.sqrt(1-latitude*latitude)*shell;
            double px=rx*horizontal*Math.cos(theta),pz=rz*horizontal*Math.sin(theta);
            double py=ry*latitude*shell;
            double sx=(7+rand01(seed^0xC63L)*10)*config.satelliteScale();
            double sz=(5+rand01(seed^0xD84L)*9)*config.satelliteScale();
            double sy=Math.max(1.5,Math.min(5,ry*.38))*config.satelliteScale();
            double ax=(p[0]-px)/sx,az=(p[1]-pz)/sz,ay=(y+.5-cy-py)/sy;
            if(Math.abs(ax)>1.4||Math.abs(az)>1.4||Math.abs(ay)>1.4) continue;
            double broad=GeologicalNoise.fbm(x,y,z,seed,.040,3)*.22;
            double detail=GeologicalNoise.fbm(x,y,z,seed^0xE95L,.115,2)*.07;
            best=Math.max(best,1-sq(ax)-sq(az)-sq(ay)+broad+detail);
        }
        return best;
    }

    private static double veinSystemScore(DepositCandidateSampler.Candidate c,int x,int y,int z,int minY,
                                          double halfLength,double thickness,double curveFreq,int branches,FieldDeposit.Shape config){
        double warpX=GeologicalNoise.fbm(x,y,z,c.shapeSeed()^0x111,.010,3)*45*config.broadNoise();
        double warpY=GeologicalNoise.fbm(x,y,z,c.shapeSeed()^0x222,.012,3)*35*config.broadNoise();
        double warpZ=GeologicalNoise.fbm(x,y,z,c.shapeSeed()^0x333,.010,3)*45*config.broadNoise();
        double wx=x+.5+warpX,wz=z+.5+warpZ,wy=y+.5+warpY;
        double cy=config.height(minY,c.verticalSeed());
        double mainAngle=angle(c.rotationSeed());
        double best=singleVeinScore(c,wx,wy,wz,cy,mainAngle,halfLength,thickness,curveFreq,0,config.y(c.shapeSeed()));
        for(int i=0;i<branches;i++){
            long s=DepositCandidateSampler.mix(c.shapeSeed()^(0x9E3779B97F4A7C15L*(i+1)));
            double da=(rand01(s)-.5)*1.15;
            double branchLength=halfLength*(.42+.30*rand01(s^0x51));
            double branchThickness=thickness*(.50+.28*rand01(s^0x91));
            double along=(rand01(s^0xA1)-.5)*halfLength*1.1;
            double lateral=veinCurve(c,along,curveFreq,0);
            double bx=c.x()+along*Math.cos(mainAngle)-lateral*Math.sin(mainAngle);
            double bz=c.z()+along*Math.sin(mainAngle)+lateral*Math.cos(mainAngle);
            double bcy=cy+veinRise(c,along,0);
            DepositCandidateSampler.Candidate fake=new DepositCandidateSampler.Candidate((int)Math.round(bx),(int)Math.round(bz),s,c.gradeSeed(),c.oreSeed(),c.verticalSeed(),s^0x44,c.alterationSeed(),c.occupancySeed());
            best=Math.max(best,singleVeinScore(fake,wx,wy,wz,bcy,mainAngle+da,branchLength,branchThickness,curveFreq*1.18,i+1,config.y(c.shapeSeed())*.78));
        }
        double body=best+GeologicalNoise.fbm(x,y,z,c.shapeSeed()^0x555,.075,2)*config.detailNoise();
        double pods=satellitePods(c,x,y,z,c.x(),cy,c.z(),halfLength,config.y(c.shapeSeed()),thickness,mainAngle,0x6E11L,config);
        return Math.max(body,pods*.65-.04);
    }

    private static double singleVeinScore(DepositCandidateSampler.Candidate c,double x,double y,double z,double cy,double angle,
                                          double halfLength,double thickness,double curveFreq,int branch,double halfHeight){
        double[] p=rotate(x-c.x(),z-c.z(),angle);
        double curve=veinCurve(c,p[0],curveFreq,branch);
        double rise=veinRise(c,p[0],branch);
        double dip=49+rand01(c.rotationSeed()^0xD1)*29;
        double plane=p[1]-curve-(y-cy-rise)/Math.tan(Math.toRadians(dip));
        double lengthFade=1-Math.abs(p[0])/halfLength;
        if(lengthFade<-.15) return -9;
        double verticalFade=1-Math.abs(y-cy)/halfHeight;
        if(verticalFade<-.15) return -9;
        double localThickness=thickness*(.58+.42*Math.max(0,Math.min(lengthFade,verticalFade)));
        return Math.min(lengthFade,verticalFade)-Math.abs(plane)/localThickness;
    }

    private static double veinCurve(DepositCandidateSampler.Candidate c,double along,double frequency,int branch){
        return GeologicalNoise.fbm(along,0,0,c.shapeSeed()^branch,.010+frequency*.20,3)*12;
    }

    private static double veinRise(DepositCandidateSampler.Candidate c,double along,int branch){
        return (rand01(c.rotationSeed()^0x51)-.5)*.20*along
                +GeologicalNoise.fbm(along,0,0,c.shapeSeed()^0x71^branch,.014,3)*5;
    }

    private static boolean occupies(double body,double grade,DepositCandidateSampler.Candidate c,int x,int y,int z,
                                    double outer,double innerProbability){
        double envelope=GeologicalNoise.smoothstep(-.18,.42,body);
        double p=envelope*(outer+(innerProbability-outer)*GeologicalNoise.clamp(grade,0,1));
        if(body>.55) p=Math.max(p,Math.min(.96,innerProbability+.12));
        return GeologicalNoise.occupancy(x,y,z,c.occupancySeed(),.16)<p;
    }

    private static List<Integer> matchingLayers(ColumnContext ctx,BlockState target){
        return ctx.layerIndices().getOrDefault(target.getBlock(),List.of());
    }

    private static double shapeNoise(int x,int y,int z,long seed,double broadFrequency,double detailFrequency,
                                     double broadAmplitude,double detailAmplitude){
        return GeologicalNoise.fbm(x,y,z,seed,broadFrequency,3)*broadAmplitude
                +GeologicalNoise.fbm(x,y,z,seed^0xA4B1C2D3L,detailFrequency,2)*detailAmplitude;
    }

    private static double shapeNoise2(int x,int z,long seed,double broadFrequency,double detailFrequency,
                                      double broadAmplitude,double detailAmplitude){
        return GeologicalNoise.fbm2(x,z,seed,broadFrequency,3)*broadAmplitude
                +GeologicalNoise.fbm2(x,z,seed^0xA4B1C2D3L,detailFrequency,2)*detailAmplitude;
    }

    private static int index(long seed,int size){return Math.min(size-1,(int)(rand01(seed)*size));}
    private static double[] rotate(double dx,double dz,double angle){double ca=Math.cos(angle),sa=Math.sin(angle);return new double[]{dx*ca+dz*sa,-dx*sa+dz*ca};}
    private static double angle(long seed){return rand01(seed)*Math.PI*2;}
    private static double rand01(long seed){long z=DepositCandidateSampler.mix(seed);return ((z>>>11)&((1L<<53)-1))*0x1.0p-53;}
    private static double proximity(int x,int z,DepositCandidateSampler.Candidate c,double radius){return GeologicalNoise.clamp(1-Math.hypot(x+.5-c.x(),z+.5-c.z())/radius,0,1);}
    private static long salt(String id){return DepositCandidateSampler.salt(id);}

    private static boolean isCarbonate(BlockState state){return isLimestone(state)||isMarble(state);}
    private static boolean isLimestone(BlockState state){return state.is(CratonBlocks.LIMESTONE.getOrigin().getBaseBlock());}
    private static boolean isMarble(BlockState state){return state.is(CratonBlocks.MARBLE.getOrigin().getBaseBlock());}
    private static boolean isRhyolite(BlockState state){return state.is(CratonBlocks.RHYOLITE.getOrigin().getBaseBlock());}
    private static boolean isPegmatite(BlockState state){return state.is(CratonBlocks.PEGMATITE.getOrigin().getBaseBlock());}

    private static double sq(double x){return x*x;}
}
