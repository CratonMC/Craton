package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.registry.CratonBlocks;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;
import com.teamtea.craton.common.core.DepositFieldEngine.ColumnContext;
import com.teamtea.craton.common.core.StratiformDepositField.DepositSample;
import com.teamtea.craton.common.core.*;
import static com.teamtea.craton.common.core.DepositFieldSupport.*;


public final class SandstoneUraniumDeposit extends FieldDeposit {
    public static final MapCodec<SandstoneUraniumDeposit> CODEC =
            codecFor(1, false, SandstoneUraniumDeposit::new);

    public SandstoneUraniumDeposit(Settings settings) {
        super(DepositTypes.URANIUM,settings);
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }

    public static BlockState place(ColumnContext ctx,BlockState host,int y){
        DepositSample sample=sandstoneRollFront(ctx,ctx.sandstoneUranium(),y);
        if(sample!=null&&occupies(sample.score(),sample.grade(),sample.candidate(),ctx.x(),y,ctx.z(),.12,.62))
            return ore(ctx,sample.candidate(),0,host);
        return host;
    }



    public static DepositSample sandstoneRollFront(ColumnContext ctx,List<DepositCandidateSampler.Candidate> candidates,int y){
        List<Integer> sandstoneLayers=matchingLayers(ctx,Blocks.SANDSTONE.defaultBlockState());
        if(sandstoneLayers.isEmpty()) return null;
        DepositSample best=ctx.stratiformSample();
        best.reset();
        for(DepositCandidateSampler.Candidate c:candidates){
            int layer=sandstoneLayers.get(index(c.verticalSeed(),sandstoneLayers.size()));
            double angle=angle(c.rotationSeed());
            double rotDx=ctx.x()+.5-c.x(),rotDz=ctx.z()+.5-c.z();
            double rotCos=Math.cos(angle),rotSin=Math.sin(angle);
            double rotAlong=rotDx*rotCos+rotDz*rotSin,rotAcross=-rotDx*rotSin+rotDz*rotCos;
            FieldDeposit.Shape config=shape(ctx,c);
            double rx=config.x(c.shapeSeed()),rz=config.z(c.shapeSeed()^91);
            double sandstoneTop=ctx.geology().boundaryY(layer,ctx.x(),ctx.z());
            double thickness=ctx.geology().thickness(layer);
            double cy=sandstoneTop-thickness*.52;
            double dy=(y+.5-cy)/Math.max(5,thickness*.42);
            double front=rotAcross-rz*.32*(1-dy*dy);
            double band=config.y(c.gradeSeed());
            double body=Math.min(1-Math.abs(rotAlong)/rx,1-Math.abs(front)/band)
                    +shapeNoise(ctx.x(),y,ctx.z(),c.shapeSeed()^0x55,.010,.055,config.broadNoise(),config.detailNoise());
            double satellite=satellitePods(c,ctx.x(),y,ctx.z(),c.x(),cy,c.z(),rx,thickness*.38,rz,angle,0x55,config);
            double score=Math.max(body,satellite*.65-.04);
            double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.07,3);
            if(best.candidate()==null||score>best.score()) best.set(score,grade,c);
        }
        return best.candidate()==null?null:best;
    }

}
