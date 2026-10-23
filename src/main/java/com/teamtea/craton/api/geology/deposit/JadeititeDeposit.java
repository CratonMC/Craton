package com.teamtea.craton.api.geology.deposit;

import com.teamtea.craton.common.worldgen.deposit.DepositCandidateSampler;
import com.teamtea.craton.common.worldgen.deposit.PreparedPods;
import com.teamtea.craton.common.worldgen.noise.GeologicalNoise;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.worldgen.deposit.DepositRandomSequences;

import com.teamtea.craton.common.registry.CratonBlocks;
import net.minecraft.world.level.block.state.BlockState;
import com.teamtea.craton.common.worldgen.deposit.DepositGenerationContext;

import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.*;

public class JadeititeDeposit extends OrdinaryDeposit {
    public static final MapCodec<JadeititeDeposit> CODEC =
            codecFor(1, false, JadeititeDeposit::new);

    public JadeititeDeposit(Settings settings) {
        super(DepositTypes.JADEITITE,settings,DepositRandomSequences.SPECIAL,60);
    }

    /** Shared host-rock rule for generation, placer provenance, and survey. */
    public boolean matchesHost(BlockState state) {
        return hostMatches(state, settings().hostTags());
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }

    @Override
    public boolean canPlace(DepositGenerationContext ctx,BlockState originalHost,BlockState state,int y){
        return state.equals(originalHost)&&(matchesHost(originalHost));
    }

    @Override
    public BlockState place(DepositGenerationContext ctx,DepositCandidateSampler.Candidate c,
                            BlockState originalHost,BlockState state,int y){
        if(!state.equals(originalHost)) return null;
        if(!matchesHost(originalHost)) return null;

        Column column=(Column)ctx.columnGeometry(c);
        if(column==null){column=prepareColumn(ctx,c);ctx.columnGeometry(c,column);}
        Shape config=settings().shape();
        double cy=column.cy(),rx=column.rx(),rz=column.rz(),ry=column.ry();
        double rotAlong=column.along(),rotAcross=column.across();
        double maxBody=1-sq(rotAlong/rx)-sq(rotAcross/rz)-sq((y+.5-cy)/ry)
                +config.broadNoise()+config.detailNoise();
        if(maxBody<-.12-1E-9&&!column.pods().possible(ctx.x(),y,ctx.z())) return null;
        double body=1-sq(rotAlong/rx)-sq(rotAcross/rz)-sq((y+.5-cy)/ry)
                +shapeNoise(ctx.x(),y,ctx.z(),c.shapeSeed(),.013,.065,config.broadNoise(),config.detailNoise());
        double pods=column.pods().score(ctx.x(),y,ctx.z());
        double score=Math.max(body,pods*.65-.04);
        if(score<-.12) return null;
        double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.085,3);
        if(occupies(score,grade,c,ctx.x(),y,ctx.z(),.08,.74))
            return abundantOre(this,ctx,c,y,0,settings().rock());
        if(GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.occupancySeed(),.16)
                <GeologicalNoise.smoothstep(-.12,.25,score)) return settings().rock();
        return null;
    }
    private record Column(double cy,double rx,double rz,double ry,double along,double across,PreparedPods pods){}
    private Column prepareColumn(DepositGenerationContext ctx,DepositCandidateSampler.Candidate c){
            double angle=angle(c.rotationSeed());
            double rotDx=ctx.x()+.5-c.x(),rotDz=ctx.z()+.5-c.z();
            double rotCos=Math.cos(angle),rotSin=Math.sin(angle);
            double rotAlong=rotDx*rotCos+rotDz*rotSin,rotAcross=-rotDx*rotSin+rotDz*rotCos;
        Shape config=settings().shape();
        double cy=config.height(ctx.minY(),c.verticalSeed());
        double rx=config.x(c.shapeSeed()),rz=config.z(c.shapeSeed()),ry=config.y(c.shapeSeed());
        return new Column(cy,rx,rz,ry,rotAlong,rotAcross,
                ctx.ordinaryPods(c,cy,rx,ry,rz,angle,0x6ADEL,config));
    }

}
