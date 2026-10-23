package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.worldgen.deposit.DepositCandidateSampler;
import com.teamtea.craton.common.worldgen.deposit.DepositGenerationContext;
import com.teamtea.craton.common.worldgen.deposit.DepositRandomSequences;
import com.teamtea.craton.common.worldgen.noise.GeologicalNoise;
import com.teamtea.craton.common.registry.CratonBlocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.*;

/** Curved, discontinuous hydrothermal veins confined to an eclogite horizon. */
public class DeepHydrothermalRedstoneDeposit extends OrdinaryDeposit {
    public static final MapCodec<DeepHydrothermalRedstoneDeposit> CODEC =
            codecFor(1,false,DeepHydrothermalRedstoneDeposit::new);

    public DeepHydrothermalRedstoneDeposit(Settings settings) {
        super(DepositTypes.DEEP_REDSTONE,settings,DepositRandomSequences.VEIN,35);
    }

    /** Shared host-rock rule for generation, placer provenance, and survey. */
    public boolean matchesHost(BlockState state) {
        return hostMatches(state, settings().hostTags());
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }

    @Override
    public boolean canPlace(DepositGenerationContext ctx,BlockState originalHost,BlockState state,int y){
        return state==originalHost&&matchesHost(originalHost);
    }

    @Override
    public BlockState place(DepositGenerationContext ctx,DepositCandidateSampler.Candidate candidate,
                            BlockState originalHost,BlockState state,int y) {
        if(state!=originalHost || !matchesHost(originalHost)) return null;
        Column column=(Column)ctx.columnGeometry(candidate);
        if(column==null){column=prepareColumn(ctx,candidate,originalHost);ctx.columnGeometry(candidate,column);}
        if(!column.valid()) return null;
        double center=column.center(),halfHeight=column.halfHeight(),along=column.along(),across=column.across();
        double length=column.length(),width=column.width(),bend=column.bend();
        Shape shape=settings().shape();
        if(Math.abs(y+.5-center)>halfHeight*1.25+2) return null;
        if(!ctx.enriched(this,candidate,y))return null;
        double score=Math.min(1-Math.abs(along)/length,1-Math.abs(across-bend)/width);
        score=Math.min(score,1-Math.abs(y+.5-center)/Math.max(1,halfHeight));
        score+=shapeNoise(ctx.x(),y,ctx.z(),candidate.shapeSeed(),.012,.065,
                shape.broadNoise(),shape.detailNoise());
        if(score<-.18) return null;
        double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),candidate.gradeSeed(),.08,3);
        return occupies(score,grade,candidate,ctx.x(),y,ctx.z(),.10,.70)
                ?ore(this,0,originalHost):null;
    }    private record Column(boolean valid,double center,double halfHeight,double along,double across,double length,double width,double bend){}
    private static final Column INVALID_COLUMN=new Column(false,0,0,0,0,0,0,0);
    private Column prepareColumn(DepositGenerationContext ctx,DepositCandidateSampler.Candidate candidate,BlockState originalHost){
        List<Integer> layers=matchingLayers(ctx,originalHost,candidate.x(),candidate.z());
        if(layers.isEmpty()) return INVALID_COLUMN;
        int layer=layers.get(index(candidate.verticalSeed(),layers.size()));
        double top=ctx.geology().boundaryY(layer,ctx.x(),ctx.z());
        double bottom=layer+1<ctx.geology().layers().size()
                ?ctx.geology().boundaryY(layer+1,ctx.x(),ctx.z()):ctx.minY();
        double thickness=top-bottom;
        if(thickness<=1E-9) return INVALID_COLUMN;
        double center=top-thickness*(.35+.30*rand01(candidate.verticalSeed()^0x4A8L));
        Shape shape=settings().shape();
        double halfHeight=Math.min(shape.y(candidate.shapeSeed()),thickness*.38);

        double theta=angle(candidate.rotationSeed());
        double dx=ctx.x()+.5-candidate.x(),dz=ctx.z()+.5-candidate.z();
        double along=dx*Math.cos(theta)+dz*Math.sin(theta);
        double across=-dx*Math.sin(theta)+dz*Math.cos(theta);
        double length=shape.x(candidate.shapeSeed()),width=shape.z(candidate.shapeSeed()^0xB41L);
        if(Math.abs(along)>length*1.25+12||Math.abs(across)>width*1.25+12) return INVALID_COLUMN;
        double bend=GeologicalNoise.fbm(along,center,0,candidate.shapeSeed(),.025,3)*width*.8;
        return new Column(true,center,halfHeight,along,across,length,width,bend);
    }

}
