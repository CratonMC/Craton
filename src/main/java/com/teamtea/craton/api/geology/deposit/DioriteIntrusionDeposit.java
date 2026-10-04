package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockState;
import com.teamtea.craton.common.core.DepositFieldEngine.ColumnContext;
import com.teamtea.craton.common.core.IntrusiveDepositField.IntrusionResult;
import com.teamtea.craton.common.core.*;
import static com.teamtea.craton.common.core.DepositFieldSupport.*;


public final class DioriteIntrusionDeposit extends IntrusionDeposit {
    public static final MapCodec<DioriteIntrusionDeposit> CODEC =
            codecFor(3, false, DioriteIntrusionDeposit::new);

    public DioriteIntrusionDeposit(Settings settings) {
        super(DepositTypes.DIORITE,settings);
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }



    @Override
    public void sample(ColumnContext ctx,DepositCandidateSampler.Candidate c,int y,IntrusionResult best){
        Shape config=settings().shape();
        double angle=angle(c.rotationSeed());
        double wx=ctx.x()+.5+GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.shapeSeed(),.013,3)*7-c.x();
        double wz=ctx.z()+.5+GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.shapeSeed()^0x91,.013,3)*7-c.z();
        double rotDx=wx,rotDz=wz;
        double rotCos=Math.cos(angle),rotSin=Math.sin(angle);
        double rotAlong=rotDx*rotCos+rotDz*rotSin,rotAcross=-rotDx*rotSin+rotDz*rotCos;
        double rx=config.x(c.shapeSeed()),rz=config.z(c.shapeSeed()^17),ry=config.y(c.shapeSeed()^31);
        double cy=config.height(ctx.minY(),c.verticalSeed());
        double ny=(y+.5-cy)/ry,rad=Math.sqrt(sq(rotAlong/rx)+sq(rotAcross/rz));
        double score=1-Math.sqrt(rad*rad+ny*ny)
                +shapeNoise(ctx.x(),y,ctx.z(),c.shapeSeed()^0xD1,.010,.046,config.broadNoise(),config.detailNoise());
        double dist=score*Math.min(Math.min(rx,rz),ry);
        if(score>best.score()) best.set(score,dist,c,ny,rad);
    }

    @Override
    public BlockState placeOre(ColumnContext ctx,BlockState host,int y,IntrusionResult intrusion){
        DepositCandidateSampler.Candidate c=intrusion.candidate();
        double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.050,3);
        double mix=GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.oreSeed(),.105);
        double hydro=(1-Math.abs(intrusion.normalizedY()+.05))*.65+intrusion.score()*.55;
        if(hydro>.48&&occupies(hydro-.35,grade,c,ctx.x(),y,ctx.z(),.12,.58)){
            if(intrusion.radial()>.58&&mix>.43) return ore(this,2,host);
            if(grade>.66&&mix>.64) return ore(this,1,host);
            return ore(this,0,host);
        }
        return host;
    }


}
