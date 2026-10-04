package com.teamtea.craton.api.geology.deposit;

import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.core.DepositRandomSequences;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import java.util.List;
import com.teamtea.craton.common.core.DepositFieldEngine.ColumnContext;
import com.teamtea.craton.common.core.*;
import static com.teamtea.craton.common.core.DepositFieldSupport.*;


public final class WeatheringDeposit extends OrdinaryDeposit {
    public static final MapCodec<WeatheringDeposit> CODEC =
            codecFor(1, false, WeatheringDeposit::new);

    public WeatheringDeposit(Settings settings) {
        super(DepositTypes.WEATHERING,settings,DepositRandomSequences.WEATHERING,40);
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }



    @Override
    public BlockState place(ColumnContext ctx,DepositCandidateSampler.Candidate c,
                            BlockState originalHost,BlockState state,int y){
        if(!originalHost.is(Blocks.BASALT)||ctx.biome().is(Tags.Biomes.IS_OCEAN)) return null;
        int depth=ctx.topY()-y;
        if(depth<1) return null;

        Shape config=settings().shape();
        if(depth>config.y(c.shapeSeed())) return null;
        double dx=ctx.x()+.5-c.x(),dz=ctx.z()+.5-c.z();
        double r=config.x(c.shapeSeed()),rz=config.z(c.shapeSeed()^91);
        double footprint=1-sq(dx/r)-sq(dz/rz)
                +shapeNoise2(ctx.x(),ctx.z(),c.shapeSeed(),.010,.052,config.broadNoise(),config.detailNoise());
        double satellite=satellitePods(c,ctx.x(),y,ctx.z(),c.x(),ctx.topY()-config.y(c.shapeSeed())*.5,c.z(),r,config.y(c.shapeSeed())*.38,rz,0,0xBA6817L,config);
        footprint=Math.max(footprint,satellite*.65-.04);
        if(footprint<-.10) return null;
        double edgeP=GeologicalNoise.smoothstep(-.10,.38,footprint);
        if(GeologicalNoise.occupancy(ctx.x(),y,ctx.z(),c.occupancySeed(),.15)>edgeP) return null;

        List<BlockState> alteration=field(ctx,c).settings().alterationRocks();
        double thickness=config.y(c.shapeSeed());
        if(depth<=thickness*3/13) return alteration.size()>0?alteration.get(0):settings().rock();
        if(depth<=thickness*9/13){
            double grade=.5+.5*GeologicalNoise.fbm(ctx.x(),y,ctx.z(),c.gradeSeed(),.075,3);
            if(grade>.42) return ore(this,0,state);
            return alteration.size()>1?alteration.get(1):settings().rock();
        }
        return alteration.size()>2?alteration.get(2):settings().rock();
    }

}
