package com.teamtea.craton.common.core;

import com.teamtea.craton.api.geology.GeologyLayer;
import com.teamtea.craton.api.geology.GeologyProfile;
import com.teamtea.craton.api.geology.deposit.BandedIronFormation;
import com.teamtea.craton.api.geology.deposit.Deposit;
import com.teamtea.craton.common.registry.CratonBlocks;
import com.teamtea.craton.common.registry.CratonContents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class WorldSetter {

    public static void rebuildCloumnExtension(BlockColumn column,BlockPos.MutableBlockPos pos,int x,int z,
                                              int startingHeight,ChunkAccess chunk,Holder<Biome> biome,
                                              PositionalRandomFactory random){
        boolean debugmode=false;
        Optional<Holder<GeologyProfile>> optional=CratonContents.getGeologyProfile(biome);
        if(optional.isEmpty()) return;
        List<Holder<GeologyLayer>> layers=optional.get().value().layers();
        if(layers.isEmpty()) return;

        LevelHeightAccessor height=chunk.getHeightAccessorForGeneration();
        int minY=height.getMinY();
        startingHeight=chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG,x,z);
        BlockState surfaceState=column.getBlock(startingHeight);
        int topY=startingHeight-getSurfaceCut(surfaceState,random,pos.setY(startingHeight));
       if(debugmode)
        for (int i = topY; i <= startingHeight; i++) {
            column.setBlock(i,Blocks.AIR.defaultBlockState());
        }
        if(topY<=minY) return;

        GeologyFieldSampler geology=new GeologyFieldSampler(layers,minY,random);
        List<BifColumnHit> bifHits=prepareBandedIronFormations(CratonContents.getDeposits(),geology,x,z,random);
        DepositFieldEngine.ColumnContext depositContext=DepositFieldEngine.prepare(random,geology,x,z,minY,topY,surfaceState,biome);

        for(int y=minY;y<=topY;y++){
            BlockState current=chunk.getBlockState(pos.setY(y));
            if(!shouldReplace(current)) {
                if(debugmode)
                column.setBlock(y,Blocks.AIR.defaultBlockState());
                continue;
            }
            BlockState host=geology.sample(x,y,z);
            BlockState state=applyBandedIronFormations(bifHits,host,x,y,z);
            state=DepositFieldEngine.apply(depositContext,host,state,y);
            if(debugmode)
                if(state==host||state==current){
                column.setBlock(y,Blocks.AIR.defaultBlockState());
                continue;
            }
            column.setBlock(y,state);
        }
    }

    private static int getSurfaceCut(BlockState state,PositionalRandomFactory random,BlockPos.MutableBlockPos pos){
        if(state.is(BlockTags.DIRT)) return random.at(pos).nextInt(1,2);
        if(state.is(BlockTags.SAND)) return random.at(pos).nextInt(3,5);
        if(state.is(Blocks.GRASS_BLOCK)) return random.at(pos).nextInt(2,3);
        return random.at(pos).nextInt(0,1);
    }

    private static boolean shouldReplace(BlockState state){
        return state.is(BlockTags.BASE_STONE_OVERWORLD)||state.is(BlockTags.DIRT)
                ||state.is(Blocks.GRASS_BLOCK)||state.is(BlockTags.SAND)||state.is(Blocks.GRAVEL);
    }

    private record BifColumnHit(BandedIronFormation deposit,double centerY,double horizontalScore,
                                double satelliteScore,long gradeSeed,long occupancySeed){}

    private static List<BifColumnHit> prepareBandedIronFormations(List<Holder<Deposit>> deposits,GeologyFieldSampler geology,
                                                                  int x,int z,PositionalRandomFactory random){
        List<Integer> gneissLayers=new ArrayList<>();
        for(int i=0;i<geology.layers().size();i++)
            if(geology.layers().get(i).value().blockState().is(CratonBlocks.GNEISS.getOrigin().getBaseBlock())) gneissLayers.add(i);
        if(gneissLayers.isEmpty()) return List.of();

        List<BifColumnHit> hits=new ArrayList<>();
        for(Holder<Deposit> holder:deposits){
            if(!(holder.value() instanceof BandedIronFormation bif)) continue;
            String id=holder.unwrapKey().map(key->key.identifier().toString()).orElseGet(()->bif.getType().toString());
            long depositSalt=DepositCandidateSampler.salt(id);
            int cellSize=Math.max(64,(int)Math.ceil(Math.max(bif.length(),bif.width())));
            double reach=Math.hypot(bif.length()*.5,bif.width()*.5)*Math.max(1.25,Math.sqrt(1.24+bif.broadNoise()+bif.detailNoise()))+36;
            List<DepositCandidateSampler.Candidate> candidates=DepositCandidateSampler.query(
                    random,DepositRandomSequences.STRATIFORM,depositSalt,x,z,cellSize,reach,2);

            for(DepositCandidateSampler.Candidate candidate:candidates){
                // Formation frequency is independent from the lateral dimensions of each lens.
                if(rand01(candidate.shapeSeed()^depositSalt)>=bif.frequency()) continue;
                // A BIF instance binds to exactly one eligible gneiss horizon, not every gneiss band in the profile.
                int targetLayer=gneissLayers.get((int)Math.floor(rand01(candidate.verticalSeed())*gneissLayers.size())%gneissLayers.size());
                double gx=(geology.boundaryY(targetLayer,candidate.x()+4,candidate.z())
                        -geology.boundaryY(targetLayer,candidate.x()-4,candidate.z()))/8.0;
                double gz=(geology.boundaryY(targetLayer,candidate.x(),candidate.z()+4)
                        -geology.boundaryY(targetLayer,candidate.x(),candidate.z()-4))/8.0;
                double hostDip=Math.toDegrees(Math.atan(Math.hypot(gx,gz)));
                if(hostDip<bif.dipMin()||hostDip>bif.dipMax()) continue;
                double horizontalScore=horizontalScore(bif,geology,targetLayer,x,z,candidate);
                double satelliteScore=satelliteScore(bif,geology,targetLayer,x,z,candidate);
                if(horizontalScore<-.24&&satelliteScore<-.20) continue;
                double halfThickness=bif.thickness()*.5;
                double verticalOffset=GeologicalNoise.fbm2(x,z,candidate.shapeSeed(),.035,2)*Math.min(halfThickness*.22,1.5);
                double topBoundary=geology.boundaryY(targetLayer,x,z);
                double bottomBoundary=targetLayer+1<geology.layers().size()
                        ?geology.boundaryY(targetLayer+1,x,z):geology.minY();
                double available=Math.max(1,topBoundary-bottomBoundary);
                double inset=Math.min(available*.5,Math.max(2.0,bif.thickness()*.5+1.0));
                double centerY=topBoundary-inset+verticalOffset;
                hits.add(new BifColumnHit(bif,centerY,horizontalScore,satelliteScore,
                        candidate.gradeSeed(),candidate.occupancySeed()));
            }
        }
        return hits;
    }

    private static BlockState applyBandedIronFormations(List<BifColumnHit> hits,BlockState host,int x,int y,int z){
        if(!host.is(CratonBlocks.GNEISS.getOrigin().getBaseBlock())) return host;
        for(BifColumnHit hit:hits){
            BandedIronFormation bif=hit.deposit();
            double halfThickness=Math.max(.75,bif.thickness()*.5);
            double verticalScore=1-Math.abs(y+.5-hit.centerY())/halfThickness;
            double body=Math.min(hit.horizontalScore(),verticalScore);
            double satellite=Math.min(hit.satelliteScore(),1-Math.abs(y+.5-hit.centerY())/Math.min(2.2,halfThickness));
            if(body<-.20&&satellite<-.16) continue;

            double envelope=GeologicalNoise.smoothstep(-.18,.48,body);
            double phase=(y+.5-hit.centerY())/Math.max(2,bif.bandScale());
            double banding=GeologicalNoise.clamp(.5+.35*Math.sin(phase*Math.PI*2)
                    +.15*GeologicalNoise.fbm(x,y,z,hit.gradeSeed(),.075,2),0,1);
            double enrichment=.5+.5*GeologicalNoise.fbm2(x,z,hit.gradeSeed()^0xB1F0L,
                    1/Math.max(16,bif.enrichmentScale()),3);
            double probability=(.08+.74*envelope)*(.35+.35*banding+.30*enrichment);
            double occupancy=GeologicalNoise.occupancy(x,y,z,hit.occupancySeed(),.16);
            if(body>-.20&&occupancy<probability) return bif.ore().value().getOreState(host);
            double satelliteChance=GeologicalNoise.smoothstep(-.16,.42,satellite)*(.24+.35*banding);
            if(satellite>-.16&&occupancy<satelliteChance) return bif.ore().value().getOreState(host);
        }
        return host;
    }

    private static double horizontalScore(BandedIronFormation bif,GeologyFieldSampler geology,int layerIndex,
                                          int x,int z,DepositCandidateSampler.Candidate candidate){
        int anchorX=candidate.x(),anchorZ=candidate.z();
        double[] strike=geology.strike(layerIndex,anchorX,anchorZ);
        double dx=x+.5-anchorX,dz=z+.5-anchorZ,along=dx*strike[0]+dz*strike[1];
        double across=-dx*strike[1]+dz*strike[0];
        double a=Math.max(1,bif.length()*.5),b=Math.max(1,bif.width()*.5);
        double normalized=sq(along/a)+sq(across/b);
        double warp=GeologicalNoise.fbm2(x,z,candidate.shapeSeed(),.006,3)*bif.broadNoise()
                +GeologicalNoise.fbm2(x,z,candidate.shapeSeed()^0x5DEECE66DL,.052,2)*bif.detailNoise();
        return 1-normalized+warp;
    }

    private static double satelliteScore(BandedIronFormation bif,GeologyFieldSampler geology,int layerIndex,
                                         int x,int z,DepositCandidateSampler.Candidate candidate){
        double[] strike=geology.strike(layerIndex,candidate.x(),candidate.z());
        double dx=x+.5-candidate.x(),dz=z+.5-candidate.z();
        double along=dx*strike[0]+dz*strike[1];
        double across=-dx*strike[1]+dz*strike[0];
        double a=Math.max(1,bif.length()*.5),b=Math.max(1,bif.width()*.5);
        if(Math.abs(along)>a*1.25+25||Math.abs(across)>b*1.25+25) return -9;
        double best=-9;
        for(int i=0;i<Math.min(24,bif.satelliteCount());i++){
            long seed=DepositCandidateSampler.mix(candidate.shapeSeed()^(0x9E3779B97F4A7C15L*(i+1)));
            double theta=rand01(seed)*Math.PI*2;
            double radial=1.04+rand01(seed^0xA17L)*.20;
            double ca=Math.cos(theta),sa=Math.sin(theta);
            double podAlong=a*radial*ca,podAcross=b*radial*sa;
            double localAlong=along-podAlong,localAcross=across-podAcross;
            if(Math.abs(localAlong)>25||Math.abs(localAcross)>19) continue;
            double radiusAlong=8+rand01(seed^0xB31L)*13;
            double radiusAcross=5+rand01(seed^0xC49L)*9;
            double broad=GeologicalNoise.fbm2(x,z,seed,.026,3)*.23;
            double detail=GeologicalNoise.fbm2(x,z,seed^0xD52L,.085,2)*.09;
            best=Math.max(best,1-sq(localAlong/radiusAlong)-sq(localAcross/radiusAcross)+broad+detail);
        }
        return best;
    }

    private static double rand01(long seed){
        long z=DepositCandidateSampler.mix(seed);
        return ((z>>>11)&((1L<<53)-1))*0x1.0p-53;
    }

    private static double sq(double x){return x*x;}
}
