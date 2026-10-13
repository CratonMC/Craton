package com.teamtea.craton.api.geology.deposit;

import com.teamtea.craton.common.worldgen.deposit.DepositCandidateSampler;
import com.teamtea.craton.common.worldgen.deposit.DepositGenerationContext;
import com.teamtea.craton.common.worldgen.deposit.DepositRandomSequences;
import com.teamtea.craton.common.worldgen.noise.GeologicalNoise;

import com.mojang.serialization.MapCodec;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.*;

/** Shallow wetland organic deposits evaluated against original surface soils only. */
public class PeatDeposit extends OrdinaryDeposit {
    public static final MapCodec<PeatDeposit> CODEC=codecFor(1,false,PeatDeposit::new);
    public PeatDeposit(Settings settings){super(DepositTypes.PEAT,settings,DepositRandomSequences.PEAT,80,
            Math.hypot(settings.shape().radiusXMax(),settings.shape().radiusZMax())*Math.sqrt(1+settings.shape().broadNoise())+1);}
    @Override public MapCodec<? extends Deposit> codec(){return CODEC;}
    public static boolean soil(BlockState state){return state.is(Blocks.MUD)||state.is(Blocks.DIRT)||state.is(Blocks.GRASS_BLOCK);}
    @Override public boolean canPlace(DepositGenerationContext ctx,BlockState host,BlockState state,int y){
        return ctx.biome().is(Tags.Biomes.IS_SWAMP)&&!ctx.biome().is(Tags.Biomes.IS_OCEAN)
                &&soil(state)&&ctx.surfaceY()-y>=0;
    }
    private record Column(double footprint,double thickness){}
    private static final Column OUTSIDE=new Column(Double.NEGATIVE_INFINITY,0);
    @Override public BlockState place(DepositGenerationContext ctx,DepositCandidateSampler.Candidate c,
                                      BlockState host,BlockState state,int y){
        if(!canPlace(ctx,host,state,y))return null;
        Shape shape=settings().shape();double depth=ctx.surfaceY()-y;
        if(depth>shape.radiusYMax())return null;
        Column column=(Column)ctx.columnGeometry(c);
        if(column==null){
            double dx=ctx.x()+.5-c.x(),dz=ctx.z()+.5-c.z();
            double d2=sq(dx/shape.x(c.shapeSeed()))+sq(dz/shape.z(c.shapeSeed()^91));
            if(d2>1+shape.broadNoise()){ctx.columnGeometry(c,OUTSIDE);return null;}
            double footprint=1-d2
                    +GeologicalNoise.fbm2(ctx.x(),ctx.z(),c.shapeSeed(),.035,1)*shape.broadNoise();
            column=new Column(footprint,shape.y(c.shapeSeed()));ctx.columnGeometry(c,column);
        }
        if(column.footprint()<=0||depth>column.thickness()*Math.sqrt(Math.min(1,column.footprint())))return null;
        return abundantOre(this,ctx,c,y,0,state);
    }
}
