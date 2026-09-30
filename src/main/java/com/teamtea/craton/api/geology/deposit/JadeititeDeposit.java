package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.registry.CratonBlocks;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.teamtea.craton.common.core.DepositFieldEngine.ColumnContext;
import com.teamtea.craton.common.core.*;
import static com.teamtea.craton.common.core.DepositFieldSupport.*;


public final class JadeititeDeposit extends FieldDeposit {
    public static final MapCodec<JadeititeDeposit> CODEC =
            codecFor(1, false, JadeititeDeposit::new);

    public JadeititeDeposit(Settings settings) {
        super(DepositTypes.JADEITITE,settings);
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }



    public static BlockState applyJadeitite(ColumnContext ctx,BlockState originalHost,BlockState state,int y){
        // Current rock registry has no eclogite/schist yet; gneiss/deepslate are conservative HP-background proxies.
        if(!originalHost.is(CratonBlocks.GNEISS.getOrigin().getBaseBlock())&&!originalHost.is(Blocks.DEEPSLATE)) return state;
        for(DepositCandidateSampler.Candidate c:ctx.jadeitite()){
            double angle=angle(c.rotationSeed());
            double rotDx=ctx.x()+.5-c.x(),rotDz=ctx.z()+.5-c.z();
            double rotCos=Math.cos(angle),rotSin=Math.sin(angle);
            double rotAlong=rotDx*rotCos+rotDz*rotSin,rotAcross=-rotDx*rotSin+rotDz*rotCos;
            FieldDeposit.Shape config=shape(ctx,c);
            double cy=config.height(ctx.minY(),c.verticalSeed());
            double rx=config.x(c.shapeSeed()),rz=config.z(c.shapeSeed()),ry=config.y(c.shapeSeed());
            double body=1-sq(rotAlong/rx)-sq(rotAcross/rz)-sq((y+.5-cy)/ry)
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

}
