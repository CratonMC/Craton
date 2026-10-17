package com.teamtea.craton.api.geology.deposit;

import com.teamtea.craton.common.worldgen.deposit.DepositCandidateSampler;
import com.teamtea.craton.common.worldgen.deposit.PreparedPegmatite;
import com.teamtea.craton.common.worldgen.noise.GeologicalNoise;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.worldgen.deposit.DepositRandomSequences;

import com.teamtea.craton.common.registry.CratonBlocks;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import java.util.List;
import com.teamtea.craton.common.worldgen.deposit.DepositGenerationContext;

import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.*;

public class PlacerDeposit extends OrdinaryDeposit {
    public static final MapCodec<PlacerDeposit> CODEC =
            codecFor(2, false, PlacerDeposit::new);

    public PlacerDeposit(Settings settings) {
        super(DepositTypes.PLACER,settings,DepositRandomSequences.PLACER,70);
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }

    public record SourceStrength(double gold,double iron){}
    public static final int RIVER_SEARCH_RADIUS=96;
    public record Anchor(int x,int z,boolean found){}

    /** Project a retained world-aligned candidate onto a nearby river without world/chunk reads. */
    public static Anchor riverAnchor(DepositCandidateSampler.Candidate candidate,java.util.function.LongPredicate river){
        int x=candidate.x(),z=candidate.z();
        if(river==null||river.test(((long)x<<32)^(z&0xffffffffL)))return new Anchor(x,z,true);
        int best=Integer.MAX_VALUE,bx=x,bz=z;
        for(int radius=8;radius<=RIVER_SEARCH_RADIUS;radius+=8){
            for(int dz=-radius;dz<=radius;dz+=8)for(int dx=-radius;dx<=radius;dx+=8){
                if(Math.abs(dx)!=radius&&Math.abs(dz)!=radius)continue;
                int distance=dx*dx+dz*dz;
                if(distance>=best||distance>RIVER_SEARCH_RADIUS*RIVER_SEARCH_RADIUS)continue;
                int px=x+dx,pz=z+dz;
                if(river.test(((long)px<<32)^(pz&0xffffffffL))){best=distance;bx=px;bz=pz;}
            }
            if(best<Integer.MAX_VALUE&&(radius+8)*(radius+8)>best)break;
        }
        return new Anchor(bx,bz,best<Integer.MAX_VALUE);
    }


    @Override
    public BlockState place(DepositGenerationContext ctx,DepositCandidateSampler.Candidate c,
                            BlockState originalHost,BlockState state,int y){
        if(ctx.surfaceY()-y<0||!ctx.biome().is(Tags.Biomes.IS_RIVER)) return null;
        boolean sediment=originalHost.is(Blocks.SAND)||originalHost.is(Blocks.RED_SAND)||originalHost.is(Blocks.GRAVEL);
        if(!sediment) return null;
        if(ctx.surfaceY()-y>settings().shape().y(c.shapeSeed()))return null;
        SourceStrength source=ctx.sourceStrength(this);
        double strength=Math.min(1,Math.max(source.gold(),source.iron()));
        if(strength<=0)return null;

        // Sources define eligibility; richness changes concentration without another empty lens mask.
        Abundance abundance=settings().abundance();
        double scale=1;
        if(abundance.enabled()){
            int cell=Math.max(24,abundance.cellX());
            double choice=GeologicalNoise.hashUnit(Math.floorDiv(ctx.x(),cell),0,Math.floorDiv(ctx.z(),cell),c.gradeSeed());
            scale=choice<abundance.poorChance()?abundance.poorScale():
                    choice>1-abundance.richChance()?abundance.richScale():abundance.normalScale();
        }
        double grade=.5+.5*GeologicalNoise.fbm2(ctx.x(),ctx.z(),c.gradeSeed(),.018,2);
        double density=(.025+.275*Math.sqrt(strength))*scale*(.65+.35*grade)*placement().frequency();
        if(GeologicalNoise.hashUnit(ctx.x(),y,ctx.z(),c.occupancySeed())>=density)return null;
        return ore(this,source.gold()>=source.iron()?0:1,state);
    }

    public SourceStrength primarySourceStrength(DepositGenerationContext ctx){
        double gold=0,iron=0;
        for(DepositCandidateSampler.Candidate c:ctx.epithermal()){
            double cy=shape(ctx,c).height(ctx.minY(),c.verticalSeed());
            BlockState h=ctx.geology().sampleRaw(c.x(),(int)Math.round(cy),c.z());
            if(h.is(Blocks.ANDESITE)||isRhyolite(h)) gold=Math.max(gold,proximity(ctx.x(),ctx.z(),c,120));
        }
        for(DepositCandidateSampler.Candidate c:ctx.veins()){
            PreparedPegmatite geometry=ctx.pegmatite(c,shape(ctx,c));
            double cy=geometry.centerY();
            BlockState h=ctx.geology().sampleRaw(c.x(),(int)Math.round(cy),c.z());
            if(geometry.attached()||h.is(Blocks.GRANITE)||isPegmatite(h)) gold=Math.max(gold,proximity(ctx.x(),ctx.z(),c,135));
        }
        for(DepositCandidateSampler.Candidate c:ctx.diorite()) gold=Math.max(gold,proximity(ctx.x(),ctx.z(),c,145)*.75);
        for(DepositCandidateSampler.Candidate c:ctx.vms()){
            List<Integer> tuffLayers=matchingLayers(ctx,Blocks.TUFF.defaultBlockState(),c.x(),c.z());
            if(tuffLayers.isEmpty()) continue;
            double province=.5+.5*GeologicalNoise.fbm2(c.x(),c.z(),DepositRandomSequences.VMS.salt()^0x564D5350L,.0019,4);
            if(province<.46) continue;
            int layer=tuffLayers.get(index(c.verticalSeed(),tuffLayers.size()));
            if(!VmsDeposit.isVolcanicHorizon(ctx.geology(),layer,c.x(),c.z())) continue;
            double cy=ctx.geology().boundaryY(layer,c.x(),c.z())-2.0;
            BlockState h=ctx.geology().sampleRaw(c.x(),(int)Math.round(cy),c.z());
            if(h.is(Blocks.TUFF)) iron=Math.max(iron,proximity(ctx.x(),ctx.z(),c,105)*.55);
        }
        // BIF lenses in a gneiss horizon provide a regional iron source to nearby rivers.
        {
            for(DepositCandidateSampler.Candidate c:ctx.bif().candidates()){
                List<Integer> gneissLayers=matchingLayers(ctx,
                        CratonBlocks.GNEISS.getOrigin().getBaseBlock().defaultBlockState(),c.x(),c.z());
                if(gneissLayers.isEmpty()) continue;
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
