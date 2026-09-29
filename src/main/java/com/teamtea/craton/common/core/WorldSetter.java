package com.teamtea.craton.common.core;

import com.teamtea.craton.api.geology.GeologyLayer;
import com.teamtea.craton.api.geology.GeologyProfile;
import com.teamtea.craton.api.geology.deposit.BandedIronFormation;
import com.teamtea.craton.api.geology.deposit.Deposit;
import com.teamtea.craton.common.registry.CratonContents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
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
    private static final int DEBUG_NONE=0, DEBUG_DEPOSIT=1, DEBUG_SECTION=2;
    private static final int DEBUG_MODE=DEBUG_DEPOSIT, DEBUG_CHUNK_RADIUS=8, DEBUG_SECTION_WIDTH=2;
    private static final int CX=256, CZ=256, DEBUG_CHUNK_X=Math.floorDiv(CX,16), DEBUG_CHUNK_Z=Math.floorDiv(CZ,16);
    private static final double CY=8, RX=105, RY=105, RZ=90;

    public static void rebuildCloumnExtension(BlockColumn column, BlockPos.MutableBlockPos pos, int x, int z,
                                              int startingHeight, ChunkAccess chunk, Holder<Biome> biome,
                                              PositionalRandomFactory random) {
        Optional<Holder<GeologyProfile>> optional=CratonContents.getGeologyProfile(biome);
        if(optional.isEmpty()) return;
        List<Holder<GeologyLayer>> layers=optional.get().value().layers();
        if(layers.isEmpty()) return;

        LevelHeightAccessor height=chunk.getHeightAccessorForGeneration();
        int minY=height.getMinY(), chunkX=Math.floorDiv(x,16), chunkZ=Math.floorDiv(z,16);
        boolean debug=false;

        startingHeight=chunk.getHeight(debug?Heightmap.Types.WORLD_SURFACE_WG:Heightmap.Types.OCEAN_FLOOR_WG,x,z);
        int topY=debug?startingHeight:startingHeight-getSurfaceCut(column.getBlock(startingHeight),random,pos.setY(startingHeight));
        if(topY<=minY) return;

        if(debug){
            for(int y=minY;y<=topY;y++){
                BlockState state=applyHydrothermalDeposit(Blocks.AIR.defaultBlockState(),x,y,z);
                column.setBlock(y,state);
            }
            return;
        }

        List<Holder<Deposit>> deposits=CratonContents.getDeposits();
        List<BifColumnHit> bifHits=prepareBandedIronFormations(deposits,layers,minY,x,z,random);
        for(int y=minY;y<=topY;y++){
            BlockState current=chunk.getBlockState(pos.setY(y));
            if(!shouldReplace(current)) continue;
            BlockState state=getGeologyState(layers,y,minY,x,z);
            state=applyHydrothermalDeposit(state,x,y,z);
            state=applyBandedIronFormations(bifHits,state,y);
            column.setBlock(y,state);
        }
    }

    /*
     * Intrusion system in normalized deposit space.
     * Deep chamber -> cupola -> vertical stock -> several directional apophyses.
     * Contact mineralization is derived from the boundary of that same body.
     */
    private static BlockState applyHydrothermalDeposit(BlockState host,int x,int y,int z){
        double X=(x+.5-CX)/RX, Y=(y+.5-CY)/RY, Z=(z+.5-CZ)/RZ;
        X+=fbm(x,y,z,.010,3,31)*.055;
        Z+=fbm(x,y,z,.010,3,79)*.055;
        Y+=fbm(x,y,z,.008,2,137)*.025;

        double chamber=1-sq(X/.72)-sq(Z/.68)-sq((Y+.72)/.32);
        double cupola=1-sq(X/.42)-sq(Z/.38)-sq((Y+.34)/.25);

        double axisX=X+fbm(x,y,z,.006,2,211)*.045, axisZ=Z+fbm(x,y,z,.006,2,337)*.040;
        double t=smooth(-.58,.82,Y), stockR=lerp(t,.20,.075);
        double stock=1-Math.hypot(axisX,axisZ)/stockR;
        stock=Math.min(stock,smooth(-.72,-.50,Y));
        stock=Math.min(stock,1-smooth(.78,.94,Y));

        // Directional lobes: no radial annulus, so they cannot form a 360-degree disk.
        double lobes=Math.max(
                lobe(X,Y,Z,.10,-.20,.82,.13,.18,.075),
                lobe(X,Y,Z,2.72,-.16,.70,.10,.17,.070));
        lobes=Math.max(lobes,lobe(X,Y,Z,1.48,-.08,.58,.18,.15,.065));
        lobes=Math.max(lobes,lobe(X,Y,Z,4.35,-.12,.64,.14,.16,.070));

        double intrusion=Math.max(Math.max(chamber,cupola),Math.max(stock,lobes));
        intrusion+=fbm(x-311,y+79,z+173,.034,2,601)*.045;

        // Host-side contact shell. Mineralization follows the intrusion instead of defining its own geometry.
        double contact=band(intrusion,-.16,.01);
        double cupolaBias=.55+.45*bell(Y+.05,.52);
        double fluid=.64+.36*fbm(x+911,y-317,z-613,.050,3,733);
        double primary=contact*cupolaBias*fluid;

        // Supergene/oxidized part: only the shallow continuation of existing mineralization.
        double oxidation=primary*smooth(.18,.58,Y)*(1-smooth(.86,1.05,Math.hypot(X,Z)));

        if(intrusion>0) return Blocks.GRANITE.defaultBlockState();
        if(oxidation>.43) return Blocks.GOLD_BLOCK.defaultBlockState();
        if(primary>.43) return Blocks.COPPER_BLOCK.weathering().unaffected().defaultBlockState();
        return host;
    }

    /*
     * Curved directional apophysis in normalized space.
     * a = azimuth, y0 = root height, len = horizontal reach,
     * rise = end rise, width/thick = horizontal/vertical radius.
     */
    private static double lobe(double X,double Y,double Z,double a,double y0,double len,double rise,double width,double thick){
        double ca=Math.cos(a), sa=Math.sin(a), along=X*ca+Z*sa, across=-X*sa+Z*ca;
        double q=Math.clamp(along/len,0,1), centerY=y0+rise*q+.055*Math.sin(q*Math.PI);
        double root=Math.max(0,-along)/.12, end=Math.max(0,along-len)/.12;
        double taper=.72+.28*Math.sin(q*Math.PI);
        double d=Math.sqrt(sq(across/(width*taper))+sq((Y-centerY)/(thick*taper))+sq(root)+sq(end));
        return 1-d;
    }

    private static double fbm(double x,double y,double z,double f,int octaves,int seed){
        double v=0,a=.55,sum=0;
        for(int i=0;i<octaves;i++,f*=2.03,a*=.5){v+=noise(x,y,z,f,seed+i*101)*a;sum+=a;}
        return v/sum;
    }

    private static double noise(double x,double y,double z,double f,int seed){
        x=x*f+seed*1.371;y=y*f-seed*.917;z=z*f+seed*.613;
        return (Math.sin(x+Math.sin(z*.73))+Math.sin(z*1.31+Math.cos(y*.79))+Math.cos(y*1.17+Math.sin(x*.67)))/3;
    }

    private static double band(double v,double outer,double inner){
        return smooth(outer,inner,v)*(1-smooth(inner,inner+.08,v));
    }

    private static double bell(double x,double w){x/=w;return Math.exp(-x*x*2);}
    private static double smooth(double a,double b,double x){double t=Math.clamp((x-a)/(b-a),0,1);return t*t*(3-2*t);}
    private static double lerp(double t,double a,double b){return a+(b-a)*t;}
    private static double sq(double x){return x*x;}

    private static int getSurfaceCut(BlockState state,PositionalRandomFactory random,BlockPos.MutableBlockPos pos){
        if(state.is(BlockTags.DIRT)) return random.at(pos).nextInt(1,2);
        if(state.is(BlockTags.SAND)) return random.at(pos).nextInt(3,5);
        if(state.is(Blocks.GRASS_BLOCK)) return random.at(pos).nextInt(2,3);
        return random.at(pos).nextInt(0,1);
    }

    private static boolean shouldReplace(BlockState state){
        return state.is(BlockTags.BASE_STONE_OVERWORLD)||state.is(BlockTags.DIRT)
                ||state.is(Blocks.GRASS_BLOCK)||state.is(BlockTags.SAND);
    }

    private static BlockState getGeologyState(List<Holder<GeologyLayer>> layers,int y,int minY,int x,int z){
        if(layers.isEmpty()) throw new IllegalArgumentException("Geology profile has no layers");
        double depth=y-minY+getRegionalWarp(x,z)-getDepositDome(x,z), boundary=0;
        for(int i=layers.size()-1;i>=0;i--){
            GeologyLayer layer=layers.get(i).value();
            boundary+=Math.max(1,layer.thickness());
            if(depth<Math.max(1,boundary+getLayerBoundaryOffset(layer,x,z))) return layer.blockState();
        }
        return layers.getFirst().value().blockState();
    }

    private static double getDepositDome(int x,int z){
        double X=(x-CX)/RX,Z=(z-CZ)/RZ;
        return 30*Math.exp(-(X*X+Z*Z)*2.1);
    }

    private record BifColumnHit(BandedIronFormation deposit,double boundaryY){}

    private static List<BifColumnHit> prepareBandedIronFormations(List<Holder<Deposit>> deposits,List<Holder<GeologyLayer>> layers,
                                                                  int minY,int x,int z,PositionalRandomFactory random){
        List<BifColumnHit> hits=new ArrayList<>();
        for(Holder<Deposit> holder:deposits){
            if(!(holder.value() instanceof BandedIronFormation bif)) continue;
            String id=holder.unwrapKey().map(key->key.identifier().toString()).orElseGet(()->bif.getType().toString());
            long depositSalt=DepositCandidateSampler.salt(id);
            int cellSize=Math.max(64,(int)Math.ceil(Math.max(bif.length(),bif.width())));
            double reach=Math.hypot(bif.length()*.5,bif.width()*.5);
            List<DepositCandidateSampler.Candidate> candidates=DepositCandidateSampler.query(random,depositSalt,x,z,cellSize,reach,2);
            for(int layerIndex=0;layerIndex<layers.size();layerIndex++){
                for(DepositCandidateSampler.Candidate candidate:candidates){
                    if(!isInsideHorizontalFootprint(bif,layers,layerIndex,minY,x,z,candidate)) continue;
                    double halfThickness=bif.thickness()*.5;
                    double verticalOffset=shapeNoise(x,0,z,candidate.shapeSeed(),.035)*Math.min(halfThickness*.22,1.5);
                    double boundaryY=getLayerBoundaryY(layers,layerIndex,minY,x,z)+verticalOffset;
                    hits.add(new BifColumnHit(bif,boundaryY));
                }
            }
        }
        return hits;
    }

    private static BlockState applyBandedIronFormations(List<BifColumnHit> hits,BlockState host,int y){
        for(BifColumnHit hit:hits){
            BandedIronFormation bif=hit.deposit();
            double halfThickness=bif.thickness()*.5;
            if(Math.abs(y+.5-hit.boundaryY())<=halfThickness) return bif.ore().value().getOreState(host);
        }
        return host;
    }

    private static boolean isInsideHorizontalFootprint(BandedIronFormation bif,List<Holder<GeologyLayer>> layers,
                                                       int layerIndex,int minY,int x,int z,DepositCandidateSampler.Candidate candidate){
        int anchorX=candidate.x(),anchorZ=candidate.z();
        double[] strike=getLayerStrike(layers,layerIndex,minY,anchorX,anchorZ);
        double dx=x+.5-anchorX,dz=z+.5-anchorZ,along=dx*strike[0]+dz*strike[1];
        double across=-dx*strike[1]+dz*strike[0];
        double a=Math.max(1,bif.length()*.5),b=Math.max(1,bif.width()*.5);
        double normalized=sq(along/a)+sq(across/b);
        double edgeNoise=shapeNoise(x,0,z,candidate.shapeSeed(),.018)*.18
                +shapeNoise(x,0,z,candidate.shapeSeed()^0x5DEECE66DL,.047)*.06;
        return normalized<=1+edgeNoise;
    }

    private static double shapeNoise(double x,double y,double z,long seed,double frequency){
        int s=(int)(seed^(seed>>>32));
        return noise(x,y,z,frequency,s);
    }

    private static double getLayerBoundaryY(List<Holder<GeologyLayer>> layers,int layerIndex,int minY,int x,int z){
        double boundary=0;
        for(int i=layers.size()-1;i>=layerIndex;i--) boundary+=Math.max(1,layers.get(i).value().thickness());
        GeologyLayer layer=layers.get(layerIndex).value();
        return minY+boundary+getLayerBoundaryOffset(layer,x,z)-getRegionalWarp(x,z);
    }

    private static double[] getLayerStrike(List<Holder<GeologyLayer>> layers,int layerIndex,int minY,int x,int z){
        double gx=getLayerBoundaryY(layers,layerIndex,minY,x+4,z)-getLayerBoundaryY(layers,layerIndex,minY,x-4,z);
        double gz=getLayerBoundaryY(layers,layerIndex,minY,x,z+4)-getLayerBoundaryY(layers,layerIndex,minY,x,z-4);
        double sx=-gz,sz=gx,length=Math.sqrt(sx*sx+sz*sz);
        return length<1E-6?new double[]{1,0}:new double[]{sx/length,sz/length};
    }

    private static double getRegionalWarp(int x,int z){
        return Math.sin(x*.003+z*.001)*18+Math.cos(x*.002-z*.004)*14;
    }

    private static double getLayerBoundaryOffset(GeologyLayer layer,int x,int z){
        int seed=layer.seed();
        double a=layer.amplitude(),fx=layer.freqX(),fz=layer.freqZ();
        double sx=x+seed*37.17,sz=z-seed*19.31;
        return Math.sin(sx*fx+sz*fz)*a+Math.cos(sx*fz*1.7-sz*fx*1.3)*a*.45+Math.sin((sx+sz)*.035)*a*.18;
    }
}
