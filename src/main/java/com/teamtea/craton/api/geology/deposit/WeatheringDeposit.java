package com.teamtea.craton.api.geology.deposit;

import com.teamtea.craton.common.worldgen.deposit.DepositCandidateSampler;
import com.teamtea.craton.common.worldgen.deposit.PreparedPods;
import com.teamtea.craton.common.worldgen.noise.GeologicalNoise;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.worldgen.deposit.DepositRandomSequences;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import java.util.List;
import com.teamtea.craton.common.worldgen.deposit.DepositGenerationContext;

import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.*;

public class WeatheringDeposit extends OrdinaryDeposit {
    public static final MapCodec<WeatheringDeposit> CODEC =
            codecFor(1, false, WeatheringDeposit::new);

    public WeatheringDeposit(Settings settings) {
        super(DepositTypes.WEATHERING,settings,DepositRandomSequences.WEATHERING,40);
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }

    @Override
    public boolean canPlace(DepositGenerationContext ctx,BlockState originalHost,BlockState state,int y){
        return state.equals(originalHost)&&originalHost.is(Blocks.BASALT)&&!ctx.biome().is(Tags.Biomes.IS_OCEAN)&&ctx.topY()-y>=1;
    }

    @Override
    public BlockState place(DepositGenerationContext ctx,DepositCandidateSampler.Candidate c,
                            BlockState originalHost,BlockState state,int y){
        if(!state.equals(originalHost)) return null;
        if(!originalHost.is(Blocks.BASALT)||ctx.biome().is(Tags.Biomes.IS_OCEAN)) return null;
        int depth=ctx.topY()-y;
        if(depth<1) return null;

        Column column=(Column)ctx.columnGeometry(c);
        if(column==null){column=prepareColumn(ctx,c);ctx.columnGeometry(c,column);}
        if(depth>column.thickness()) return null;
        double footprint=column.footprint();
        double satellite=column.pods().score(ctx.x(),y,ctx.z());
        footprint=Math.max(footprint,satellite*.65-.04);
        if(footprint<-.10) return null;
        double edgeP=GeologicalNoise.smoothstep(-.10,.38,footprint);
        if(GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.occupancySeed(),.15)>edgeP) return null;

        List<BlockState> alteration=settings().alterationRocks();
        double thickness=column.thickness();
        if(depth<=thickness*3/13) return alteration.size()>0?alteration.get(0):settings().rock();
        if(depth<=thickness*9/13){
            double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.075,3);
            if(grade>.42) return abundantOre(this,ctx,c,y,0,state,alteration.size()>1?alteration.get(1):settings().rock());
            return alteration.size()>1?alteration.get(1):settings().rock();
        }
        return alteration.size()>2?alteration.get(2):settings().rock();
    }
    private static class Column {
        private double thickness,footprint;
        private PreparedPods pods;
        double thickness(){return thickness;}
        double footprint(){return footprint;}
        PreparedPods pods(){return pods;}
    }
    private Column prepareColumn(DepositGenerationContext ctx,DepositCandidateSampler.Candidate c){
        Shape config=settings().shape();
        double thickness=config.y(c.shapeSeed());
        double dx=ctx.x()+.5-c.x(),dz=ctx.z()+.5-c.z();
        double r=config.x(c.shapeSeed()),rz=config.z(c.shapeSeed()^91);
        double footprint=1-sq(dx/r)-sq(dz/rz)
                +shapeNoise2(ctx.x(),ctx.z(),c.shapeSeed(),.010,.052,config.broadNoise(),config.detailNoise());
        Object cached=ctx.reusableGeometry(c);
        Column column=cached instanceof Column previous?previous:new Column();
        column.thickness=thickness;column.footprint=footprint;
        column.pods=ctx.ordinaryPods(c,ctx.topY()-thickness*.5,r,thickness*.38,rz,0,0xBA6817L,config);
        if(column!=cached)ctx.reusableGeometry(c,column);
        return column;
    }

}
