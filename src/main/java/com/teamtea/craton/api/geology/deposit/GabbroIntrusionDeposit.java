package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockState;
import com.teamtea.craton.common.core.DepositFieldEngine.ColumnContext;
import com.teamtea.craton.common.core.IntrusiveDepositField.IntrusionResult;
import com.teamtea.craton.common.core.*;
import static com.teamtea.craton.common.core.DepositFieldSupport.*;


public final class GabbroIntrusionDeposit extends FieldDeposit {
    public static final MapCodec<GabbroIntrusionDeposit> CODEC =
            codecFor(2, false, GabbroIntrusionDeposit::new);

    public GabbroIntrusionDeposit(Settings settings) {
        super(DepositTypes.GABBRO,settings);
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }



    public static IntrusionResult sampleGabbro(ColumnContext ctx,int y){
        IntrusionResult best=ctx.gabbroResult();
        best.reset(2);
        if(ctx.gabbro().isEmpty()) return best;
        for(DepositCandidateSampler.Candidate c:ctx.gabbro()){
            FieldDeposit.Shape config=shape(ctx,c);
            double angle=angle(c.rotationSeed());
            double rotDx=ctx.x()+.5-c.x(),rotDz=ctx.z()+.5-c.z();
            double rotCos=Math.cos(angle),rotSin=Math.sin(angle);
            double rotAlong=rotDx*rotCos+rotDz*rotSin,rotAcross=-rotDx*rotSin+rotDz*rotCos;
            double rx=config.x(c.shapeSeed()),rz=config.z(c.shapeSeed()^17);
            double cy=config.height(ctx.minY(),c.verticalSeed());
            double radial=Math.sqrt(sq(rotAlong/rx)+sq(rotAcross/rz));
            double bowl=cy+18*radial*radial+GeologicalNoise.fbm2(ctx.x(),ctx.z(),c.shapeSeed(),.014,3)*5;
            double half=config.y(c.shapeSeed()^71);
            double vertical=Math.abs(y+.5-bowl)/half;
            double score=1-radial-vertical*.78
                    +shapeNoise(ctx.x(),y,ctx.z(),c.shapeSeed()^0x77,.009,.050,config.broadNoise(),config.detailNoise());
            double dist=score*Math.min(Math.min(rx,rz),half);
            double ny=(y+.5-bowl)/half;
            if(score>best.score()) best.set(score,dist,c,ny,radial);
        }
        return best;
    }

    public static BlockState placeOre(ColumnContext ctx,BlockState host,int y,IntrusionResult intrusion){
        DepositCandidateSampler.Candidate c=intrusion.candidate();
        double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.050,3);
        double mix=GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.oreSeed(),.105);
        double floor=(-intrusion.normalizedY()-.12)+GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.alterationSeed(),.033,3)*.20;
        if(intrusion.score()>.02&&floor>.18&&occupies(intrusion.score()+floor*.35,grade,c,ctx.x(),y,ctx.z(),.05,.70))
            return ore(ctx,c,mix>.78?1:0,host);
        return host;
    }


}
