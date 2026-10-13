package com.teamtea.craton.api.geology.deposit;

import com.teamtea.craton.common.worldgen.deposit.DepositCandidateSampler;
import com.teamtea.craton.common.worldgen.noise.GeologicalNoise;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.worldgen.deposit.DepositRandomSequences;

import com.teamtea.craton.common.registry.CratonBlocks;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;
import com.teamtea.craton.common.worldgen.deposit.DepositGenerationContext;
import com.teamtea.craton.common.worldgen.deposit.StratiformDepositField.DepositSample;

import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.*;

public class SandstoneUraniumDeposit extends FieldDeposit {
    public static final MapCodec<SandstoneUraniumDeposit> CODEC =
            codecFor(1, false, SandstoneUraniumDeposit::new);

    public SandstoneUraniumDeposit(Settings settings) {
        super(DepositTypes.URANIUM,settings,DepositRandomSequences.STRATIFORM);
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }

    public BlockState placeSample(DepositGenerationContext ctx,BlockState host,int y,DepositSample sample){
        if(occupies(sample.score(),sample.grade(),sample.candidate(),ctx.x(),y,ctx.z(),.12,.62))
            return abundantOre(this,ctx,sample.candidate(),y,0,host);
        return host;
    }

    public void sampleRollFront(DepositGenerationContext ctx,DepositCandidateSampler.Candidate c,int y,
                                List<Integer> sandstoneLayers,DepositSample best){
            Column column=(Column)ctx.columnGeometry(c);
            if(column==null){
                int layer=sandstoneLayers.get(index(c.verticalSeed(),sandstoneLayers.size()));
                double angle=angle(c.rotationSeed()),cos=Math.cos(angle),sin=Math.sin(angle);
                double dx=ctx.x()+.5-c.x(),dz=ctx.z()+.5-c.z();
                Shape config=settings().shape();
                double top=ctx.geology().boundaryY(layer,ctx.x(),ctx.z());
                double thickness=ctx.geology().layerThickness(layer,ctx.x(),ctx.z());
                column=new Column(dx*cos+dz*sin,-dx*sin+dz*cos,config.x(c.shapeSeed()),config.z(c.shapeSeed()^91),
                        top,thickness,config.y(c.gradeSeed()));
                ctx.columnGeometry(c,column);
            }
            double rotAlong=column.along(),rotAcross=column.across(),rx=column.rx(),rz=column.rz();
            double sandstoneTop=column.top(),thickness=column.thickness();
            if(thickness<=1E-9||y+.5>sandstoneTop||y+.5<sandstoneTop-thickness)return;
            double cy=sandstoneTop-thickness*.52;
            Shape config=settings().shape();
            double dy=(y+.5-cy)/Math.max(5,thickness*.42);
            double front=rotAcross-rz*.32*(1-dy*dy);
            double band=column.band();
            double upper=Math.min(1-Math.abs(rotAlong)/rx,1-Math.abs(front)/band)+config.broadNoise()+config.detailNoise();
            boolean possiblePods=config.satelliteCount()>0&&config.satelliteScale()>0
                    &&Math.abs(rotAlong)<=rx*1.27+24*config.satelliteScale()
                    &&Math.abs(rotAcross)<=rz*1.27+20*config.satelliteScale()
                    &&Math.abs(y+.5-cy)<=thickness*.38*.72+7*config.satelliteScale();
            // Below -.18 the ore envelope is exactly zero; the bound includes all noise amplitudes.
            if(upper<=-.18&&!possiblePods)return;
            double body=Math.min(1-Math.abs(rotAlong)/rx,1-Math.abs(front)/band)
                    +shapeNoise(ctx.x(),y,ctx.z(),c.shapeSeed()^0x55,.010,.055,config.broadNoise(),config.detailNoise());
            double satellite=satellitePods(c,ctx.x(),y,ctx.z(),c.x(),cy,c.z(),rx,thickness*.38,rz,angle(c.rotationSeed()),0x55,config);
            double score=Math.max(body,satellite*.65-.04);
            double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.07,3);
            if(best.candidate()==null||score>best.score()) best.set(score,grade,c);
    }

    private record Column(double along,double across,double rx,double rz,double top,double thickness,double band){}
}
