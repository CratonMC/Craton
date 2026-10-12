package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockState;
import com.teamtea.craton.common.core.DepositFieldEngine.ColumnContext;
import com.teamtea.craton.common.core.IntrusiveDepositField.IntrusionResult;
import com.teamtea.craton.common.core.*;
import static com.teamtea.craton.common.core.DepositFieldSupport.*;


public final class GraniteIntrusionDeposit extends IntrusionDeposit {
    public static final MapCodec<GraniteIntrusionDeposit> CODEC =
            codecFor(2, false, GraniteIntrusionDeposit::new);

    public GraniteIntrusionDeposit(Settings settings) {
        super(DepositTypes.GRANITE,settings);
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }



    @Override
    public Prepared prepare(DepositCandidateSampler.Candidate c,int minY){
        return prepare(c,minY,0xA7,0xC3);
    }

    @Override
    public void prepareColumn(ColumnContext ctx,Prepared prepared,double scoreCutoff){
        prepared.prepareEllipsoidColumn(ctx.x(),ctx.z(),scoreCutoff,8,true);
    }

    @Override
    public void sample(ColumnContext ctx,Prepared prepared,int y,IntrusionResult best){
        if(prepared.outside(y)) return;
        Shape config=settings().shape();
        DepositCandidateSampler.Candidate c=prepared.candidate;
        double rx=prepared.rx,rz=prepared.rz,ry=prepared.ry,cy=prepared.cy;
        double ny=(y+.5-cy)/ry;
        double radialLower=prepared.radialLower;
        double upper=Math.max(1-Math.sqrt(radialLower*radialLower+ny*ny),
                1-Math.sqrt(sq(radialLower/.46)+sq((y+.5-(cy+ry*.62))/(ry*.42))))
                +prepared.noiseAmplitude;
        if(prepared.cannotImprove(upper,best)) return;
        double wx=ctx.x()+.5+GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.shapeSeed(),.009,3)*8-c.x();
        double wz=ctx.z()+.5+GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.shapeSeed()^0x51,.009,3)*8-c.z();
        double rotDx=wx,rotDz=wz;
        double rotCos=prepared.rotCos,rotSin=prepared.rotSin;
        double rotAlong=rotDx*rotCos+rotDz*rotSin,rotAcross=-rotDx*rotSin+rotDz*rotCos;
        double rad=Math.sqrt(sq(rotAlong/rx)+sq(rotAcross/rz));
        double chamber=1-Math.sqrt(rad*rad+ny*ny);
        double cupolaCy=cy+ry*.62, cupola=1-Math.sqrt(sq(rotAlong/(rx*.46))+sq(rotAcross/(rz*.46))+sq((y+.5-cupolaCy)/(ry*.42)));
        double score=Math.max(chamber,cupola)
                +shapeNoise(ctx.x(),y,ctx.z(),c.shapeSeed()^0xB5297A4DL,.008,.041,config.broadNoise(),config.detailNoise());
        double dist=score*Math.min(Math.min(rx,rz),ry);
        if(score>best.score()) best.set(score,dist,c,ny,rad);
    }

    @Override
    public BlockState placeOre(ColumnContext ctx,BlockState host,int y,IntrusionResult intrusion){
        DepositCandidateSampler.Candidate c=intrusion.candidate();
        double cupola=intrusion.normalizedY()*.55+intrusion.score()*.55;
        if(cupola<=.32) return host;
        double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.050,3);
        if(occupies(cupola-.25,grade,c,ctx.x(),y,ctx.z(),.10,.52)){
            double mix=GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.oreSeed(),.105);
            return ore(this,mix>.64-.18*intrusion.radial()+.08*intrusion.normalizedY()?1:0,host);
        }
        return host;
    }


}
