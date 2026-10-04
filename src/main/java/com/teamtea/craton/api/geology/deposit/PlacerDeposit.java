package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.core.DepositRandomSequences;

import com.teamtea.craton.common.registry.CratonBlocks;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import java.util.List;
import com.teamtea.craton.common.core.DepositFieldEngine.ColumnContext;
import com.teamtea.craton.common.core.*;
import static com.teamtea.craton.common.core.DepositFieldSupport.*;


public final class PlacerDeposit extends OrdinaryDeposit {
    public static final MapCodec<PlacerDeposit> CODEC =
            codecFor(2, false, PlacerDeposit::new);

    public PlacerDeposit(Settings settings) {
        super(DepositTypes.PLACER,settings,DepositRandomSequences.PLACER,70);
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }



    public record SourceStrength(double gold,double iron){}

    @Override
    public BlockState place(ColumnContext ctx,DepositCandidateSampler.Candidate c,
                            BlockState originalHost,BlockState state,int y){
        if(ctx.topY()-y<0||!ctx.biome().is(Tags.Biomes.IS_RIVER)) return null;
        boolean sediment=ctx.surfaceState().is(Blocks.SAND)||ctx.surfaceState().is(Blocks.RED_SAND)||ctx.surfaceState().is(Blocks.GRAVEL);
        if(!sediment) return null;
        SourceStrength source=ctx.sourceStrength(this);
        if(source.gold()<=0&&source.iron()<=0) return null;


        double dx=ctx.x()+.5-c.x(),dz=ctx.z()+.5-c.z();
        Shape config=settings().shape();
        if(ctx.topY()-y>config.y(c.shapeSeed())) return null;
        double r=config.x(c.shapeSeed()),rz=config.z(c.shapeSeed()^91);
        double score=1-sq(dx/r)-sq(dz/rz)
                +shapeNoise2(ctx.x(),ctx.z(),c.shapeSeed(),.015,.075,config.broadNoise(),config.detailNoise());
        if(score<-.10) return null;
        double placerGrade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.11,3);
        double p=GeologicalNoise.smoothstep(-.10,.35,score)*(.30+.55*placerGrade);
        if(GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.occupancySeed(),.18)>p) return null;
        if(source.gold()>=source.iron()&&source.gold()*placerGrade>.42)
            return ore(this,0,state);
        if(source.iron()*placerGrade>.38)
            return ore(this,1,state);
        return null;
    }

    public SourceStrength primarySourceStrength(ColumnContext ctx){
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
        // BIF lenses in a gneiss horizon provide a regional iron source to nearby rivers.
        List<Integer> gneissLayers=matchingLayers(ctx,CratonBlocks.GNEISS.getOrigin().getBaseBlock().defaultBlockState());
        if(!gneissLayers.isEmpty()){
            for(DepositCandidateSampler.Candidate c:ctx.bif().candidates()){
                BandedIronFormation bif=(BandedIronFormation)ctx.owners().get(c);
                int layer=gneissLayers.get(index(c.verticalSeed(),gneissLayers.size()));
                double gx=(ctx.geology().boundaryY(layer,c.x()+4,c.z())-ctx.geology().boundaryY(layer,c.x()-4,c.z()))/8.0;
                double gz=(ctx.geology().boundaryY(layer,c.x(),c.z()+4)-ctx.geology().boundaryY(layer,c.x(),c.z()-4))/8.0;
                double dip=Math.toDegrees(Math.atan(Math.hypot(gx,gz)));
                if(dip<bif.dipMin()||dip>bif.dipMax()) continue;
                double[] strike=ctx.geology().strike(layer,c.x(),c.z());
                double dx=ctx.x()+.5-c.x(),dz=ctx.z()+.5-c.z();
                double along=dx*strike[0]+dz*strike[1],across=-dx*strike[1]+dz*strike[0];
                double radial=Math.hypot(along/(bif.length()*.5+60),across/(bif.width()*.5+60));
                if(radial<1) iron=Math.max(iron,(1-radial)*.8);
            }
        }
        // Skarn candidates are intentionally not treated as placer sources unless a real
        // carbonate/intrusion contact can be reconstructed; candidate proximity alone is insufficient.
        return new SourceStrength(gold,iron);
    }

}
