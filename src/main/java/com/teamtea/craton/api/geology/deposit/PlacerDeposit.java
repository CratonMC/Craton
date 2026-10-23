package com.teamtea.craton.api.geology.deposit;

import com.teamtea.craton.common.worldgen.deposit.DepositCandidateSampler;
import com.teamtea.craton.common.worldgen.deposit.PreparedPegmatite;
import com.teamtea.craton.common.worldgen.noise.GeologicalNoise;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.MapCodec;
import com.teamtea.craton.common.worldgen.deposit.DepositRandomSequences;

import com.teamtea.craton.common.registry.CratonBlocks;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import java.util.List;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import com.teamtea.craton.common.worldgen.deposit.DepositGenerationContext;

import static com.teamtea.craton.common.worldgen.deposit.DepositFieldSupport.*;

import com.teamtea.craton.api.geology.GeologyProfile;
import com.teamtea.craton.common.registry.CratonContents;
import com.teamtea.craton.common.worldgen.deposit.DepositFieldEngine;
import com.teamtea.craton.common.worldgen.deposit.DepositGenerationContext;
import com.teamtea.craton.common.worldgen.geology.GeologyClimateSampler;
import com.teamtea.craton.common.worldgen.geology.GeologyFieldSampler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.common.Tags;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;
public class PlacerDeposit extends OrdinaryDeposit {
    public static final MapCodec<PlacerDeposit> CODEC = RecordCodecBuilder.<PlacerDeposit>mapCodec(i -> i.group(
            Settings.CODEC.fieldOf("settings").forGetter(PlacerDeposit::settings),
            Codec.intRange(1, 1000000).optionalFieldOf("rarity", 2).forGetter(PlacerDeposit::rarity),
            RegistryCodecs.holderSet(Registries.BLOCK).optionalFieldOf("replaceable",
                    HolderSet.direct(Blocks.SAND.builtInRegistryHolder(), Blocks.RED_SAND.builtInRegistryHolder(), Blocks.GRAVEL.builtInRegistryHolder())).forGetter(PlacerDeposit::replaceableBlocks),
            Codec.intRange(8, 512).optionalFieldOf("river_search_radius", 96).forGetter(PlacerDeposit::riverSearchRadius),
            Codec.intRange(1, 512).optionalFieldOf("epithermal_source_distance", 120).forGetter(PlacerDeposit::epithermalSourceDistance),
            Codec.intRange(1, 512).optionalFieldOf("vein_source_distance", 135).forGetter(PlacerDeposit::veinSourceDistance),
            Codec.intRange(1, 512).optionalFieldOf("diorite_source_distance", 145).forGetter(PlacerDeposit::dioriteSourceDistance),
            Codec.intRange(1, 512).optionalFieldOf("vms_source_distance", 105).forGetter(PlacerDeposit::vmsSourceDistance)
    ).apply(i, PlacerDeposit::new)).flatXmap(
            d -> FieldDeposit.validate(d.settings(), 2, false).map(v -> d),
            d -> com.mojang.serialization.DataResult.success(d));

    private final int rarity;
    private final HolderSet<Block> replaceableBlocks;
    private final int riverSearchRadius, epithermalSourceDistance, veinSourceDistance, dioriteSourceDistance, vmsSourceDistance;

    public PlacerDeposit(Settings settings) {
        this(settings, 2, HolderSet.direct(Blocks.SAND.builtInRegistryHolder(), Blocks.RED_SAND.builtInRegistryHolder(), Blocks.GRAVEL.builtInRegistryHolder()), 96, 120, 135, 145, 105);
    }
    public PlacerDeposit(Settings settings, int rarity, HolderSet<Block> replaceableBlocks) {
        this(settings, rarity, replaceableBlocks, 96, 120, 135, 145, 105);
    }
    public PlacerDeposit(Settings settings, int rarity, HolderSet<Block> replaceableBlocks, int riverSearchRadius,
                         int epithermalSourceDistance, int veinSourceDistance, int dioriteSourceDistance, int vmsSourceDistance) {
        super(DepositTypes.PLACER,settings,DepositRandomSequences.PLACER,70);
        this.rarity = rarity;
        this.replaceableBlocks = replaceableBlocks;
        this.riverSearchRadius = riverSearchRadius;
        this.epithermalSourceDistance = epithermalSourceDistance;
        this.veinSourceDistance = veinSourceDistance;
        this.dioriteSourceDistance = dioriteSourceDistance;
        this.vmsSourceDistance = vmsSourceDistance;
    }
    public int riverSearchRadius() { return riverSearchRadius; }
    public int epithermalSourceDistance() { return epithermalSourceDistance; }
    public int veinSourceDistance() { return veinSourceDistance; }
    public int dioriteSourceDistance() { return dioriteSourceDistance; }
    public int vmsSourceDistance() { return vmsSourceDistance; }
    public int rarity() { return rarity; }
    public HolderSet<Block> replaceableBlocks() { return replaceableBlocks; }
    private boolean replaceable(BlockState state) {
        return replaceableBlocks.contains(state.getBlock().builtInRegistryHolder());
    }

    @Override public MapCodec<? extends Deposit> codec() { return CODEC; }

    private static final TagKey<Item> GOLD_SOURCE_TAG=
            TagKey.create(Registries.ITEM,
                    Identifier.parse("c:ingots/gold"));
    public record SourceStrength(double gold,double iron){}
    public static final int RIVER_SEARCH_RADIUS=96;
    public record Anchor(int x,int z,boolean found){}

    /** Project a retained world-aligned candidate onto a nearby river without world/chunk reads. */
    public static Anchor riverAnchor(DepositCandidateSampler.Candidate candidate,java.util.function.LongPredicate river){
        return riverAnchor(candidate, river, RIVER_SEARCH_RADIUS);
    }
    public static Anchor riverAnchor(DepositCandidateSampler.Candidate candidate,java.util.function.LongPredicate river,int searchRadius){
        int x=candidate.x(),z=candidate.z();
        if(river==null||river.test(((long)x<<32)^(z&0xffffffffL)))return new Anchor(x,z,true);
        int best=Integer.MAX_VALUE,bx=x,bz=z;
        for(int radius=8;radius<=searchRadius;radius+=8){
            for(int dz=-radius;dz<=radius;dz+=8)for(int dx=-radius;dx<=radius;dx+=8){
                if(Math.abs(dx)!=radius&&Math.abs(dz)!=radius)continue;
                int distance=dx*dx+dz*dz;
                if(distance>=best||distance>searchRadius*searchRadius)continue;
                int px=x+dx,pz=z+dz;
                if(river.test(((long)px<<32)^(pz&0xffffffffL))){best=distance;bx=px;bz=pz;}
            }
            if(best<Integer.MAX_VALUE&&(radius+8)*(radius+8)>best)break;
        }
        return new Anchor(bx,bz,best<Integer.MAX_VALUE);
    }


    @Override
    public BlockState place(DepositGenerationContext ctx,DepositCandidateSampler.Candidate c,
                            BlockState originalHost,BlockState state,int y){
        if(ctx.surfaceY()-y<0||!ctx.biome().is(Tags.Biomes.IS_RIVER)) return null;
        boolean sediment=replaceable(originalHost);
        if(!sediment) return null;
        if(ctx.surfaceY()-y>settings().shape().y(c.shapeSeed()))return null;
        SourceStrength source=ctx.sourceStrength(this);
        double strength=Math.min(1,Math.max(source.gold(),source.iron()));
        if(strength<=0)return null;

        // Sources define eligibility; richness changes concentration without another empty lens mask.
        Abundance abundance=settings().abundance();
        double scale=1;
        if(abundance.enabled()){
            int cell=Math.max(24,abundance.cellX());
            double choice=GeologicalNoise.hashUnit(Math.floorDiv(ctx.x(),cell),0,Math.floorDiv(ctx.z(),cell),c.gradeSeed());
            scale=choice<abundance.poorChance()?abundance.poorScale():
                    choice>1-abundance.richChance()?abundance.richScale():abundance.normalScale();
        }
        double grade=.5+.5*GeologicalNoise.fbm2(ctx.x(),ctx.z(),c.gradeSeed(),.018,2);
        double density=(.025+.275*Math.sqrt(strength))*scale*(.65+.35*grade);
        if(GeologicalNoise.hashUnit(ctx.x(),y,ctx.z(),c.occupancySeed())>=density)return null;
        // Select the mineral independently of occupancy so overlapping sources can mix.
        double goldShare=source.gold()/(source.gold()+source.iron());
        double mineral=GeologicalNoise.hashUnit(ctx.x(),y,ctx.z(),c.occupancySeed()^0x504C414345525459L);
        return ore(this,mineral<goldShare?0:1,state);
    }

    public SourceStrength primarySourceStrength(DepositGenerationContext ctx){
        double gold=0,iron=0;
        if(oreAvailable(0)){
            for(DepositCandidateSampler.Candidate c:ctx.epithermal()){
                if(!field(ctx,c).providesMetal(GOLD_SOURCE_TAG))continue;
                double cy=shape(ctx,c).height(ctx.minY(),c.verticalSeed());
                BlockState h=ctx.geology().sampleRaw(c.x(),(int)Math.round(cy),c.z());
                if(field(ctx,c) instanceof EpithermalDeposit epithermal && epithermal.matchesHost(h)) gold=Math.max(gold,proximity(ctx.x(),ctx.z(),c,epithermalSourceDistance));
            }
            for(DepositCandidateSampler.Candidate c:ctx.veins()){
                if(!field(ctx,c).providesMetal(GOLD_SOURCE_TAG))continue;
                PreparedPegmatite geometry=ctx.pegmatite(c,shape(ctx,c));
                double cy=geometry.centerY();
                BlockState h=ctx.geology().sampleRaw(c.x(),(int)Math.round(cy),c.z());
                if(geometry.attached()||h.is(Blocks.GRANITE)||isPegmatite(h)) gold=Math.max(gold,proximity(ctx.x(),ctx.z(),c,veinSourceDistance));
            }
            for(DepositCandidateSampler.Candidate c:ctx.diorite()){
                var parent=(DioriteIntrusionDeposit)field(ctx,c);
                if(parent.providesMetal(GOLD_SOURCE_TAG)&&parent.isFertile(c))
                    gold=Math.max(gold,proximity(ctx.x(),ctx.z(),c,dioriteSourceDistance)*.75);
            }
        }
        if(oreAvailable(1)){
            for(DepositCandidateSampler.Candidate c:ctx.vms()){
                if(!field(ctx,c).mineralizationAvailable())continue;
                VmsDeposit vms=(VmsDeposit)field(ctx,c);
                int layer=vms.selectedHorizon(ctx.geology(),c);
                if(layer<0) continue;
                double province=.5+.5*GeologicalNoise.fbm2(c.x(),c.z(),DepositRandomSequences.VMS.salt()^0x564D5350L,.0019,4);
                if(province<.46) continue;
                double cy=ctx.geology().boundaryY(layer,c.x(),c.z())-2.0;
                BlockState h=ctx.geology().sampleRaw(c.x(),(int)Math.round(cy),c.z());
                if(vms.matchesLens(h)) iron=Math.max(iron,proximity(ctx.x(),ctx.z(),c,vmsSourceDistance)*.55);
            }
            // BIF lenses in a gneiss horizon provide a regional iron source to nearby rivers.
            {
                for(DepositCandidateSampler.Candidate c:ctx.bif().candidates()){
                    BandedIronFormation bif=(BandedIronFormation)ctx.owners().get(c);
                    List<Integer> gneissLayers=bif.matchingHostLayers(ctx.geology(),c.x(),c.z());
                    if(gneissLayers.isEmpty()) continue;
                    int layer=gneissLayers.get(index(c.verticalSeed(),gneissLayers.size()));
                    double gx=(ctx.geology().boundaryY(layer,c.x()+4,c.z())-ctx.geology().boundaryY(layer,c.x()-4,c.z()))/8.0;
                    double gz=(ctx.geology().boundaryY(layer,c.x(),c.z()+4)-ctx.geology().boundaryY(layer,c.x(),c.z()-4))/8.0;
                    double dip=Math.toDegrees(Math.atan(Math.hypot(gx,gz)));
                    if(dip<bif.dipMin()||dip>bif.dipMax()) continue;
                    double[] strike=ctx.geology().strike(layer,c.x(),c.z());
                    double dx=ctx.x()+.5-c.x(),dz=ctx.z()+.5-c.z();
                    double along=dx*strike[0]+dz*strike[1],across=-dx*strike[1]+dz*strike[0];
                    double radial=Math.hypot(along/(bif.length()*.5+60),across/(bif.width()*.5+60));
                    if(radial<1) iron=Math.max(iron,(1-radial)*.8);
                }
            }
        }
        // Skarn candidates are intentionally not treated as placer sources unless a real
        // carbonate/intrusion contact can be reconstructed; candidate proximity alone is insufficient.
        return new SourceStrength(gold,iron);
    }

    public boolean generateFeature(WorldGenLevel level,ChunkGenerator generator,RandomSource random,BlockPos origin){
        if (random.nextInt(rarity) != 0) return false;
        var generation=CratonContents.getGenerationData();
        if(generation.depositPlan().maximumPlacerDepth()<=0||!(generator instanceof NoiseBasedChunkGenerator noise))return false;
        int minX=Math.floorDiv(origin.getX(),16)*16,minZ=Math.floorDiv(origin.getZ(),16)*16;
        var chunk=level.getChunk(origin);
        // Avoid climate and candidate contexts in chunks without usable riverbed sediment.
        if(!hasSediment(chunk,minX,minZ,generation.depositPlan().maximumPlacerDepth(),level::getBiome, this))return false;
        var settings=noise.generatorSettings().value();
        var randomState=level.getLevel().getChunkSource().randomState();
        // Match the surface generator and survey, rather than the decoration RNG supplied to this feature.
        var root=settings.getRandomSource().newInstance(randomState.seed()).forkPositional();
        var sampler=randomState.createClimateSampler(SamplerContext.EMPTY_UNCACHED);
        var climate=new GeologyClimateSampler(sampler,settings.seaLevel());
        return placeChunk(chunk,minX,minZ,generation,root,climate,level::getBiome, this)>0;
    }


    public static String inspect(WorldGenLevel level,ChunkGenerator generator,BlockPos origin){
        var generation=CratonContents.getGenerationData();
        if(generation.depositPlan().maximumPlacerDepth()<=0)return "Craton placer: rule=source_density_v36 status=disabled_or_missing_definition";
        if(!(generator instanceof NoiseBasedChunkGenerator noise))return "Craton placer: rule=source_density_v36 status=unsupported_generator";
        var settings=noise.generatorSettings().value();
        var randomState=level.getLevel().getChunkSource().randomState();
        var root=settings.getRandomSource().newInstance(randomState.seed()).forkPositional();
        var sampler=randomState.createClimateSampler(SamplerContext.EMPTY_UNCACHED);
        var climate=new GeologyClimateSampler(sampler,settings.seaLevel());
        return inspectChunk(level.getChunk(origin),Math.floorDiv(origin.getX(),16)*16,Math.floorDiv(origin.getZ(),16)*16,
                generation,root,climate,level::getBiome);
    }


    public static boolean hasSediment(ChunkAccess chunk,int minX,int minZ,double depth,Function<BlockPos,Holder<Biome>> biomes, PlacerDeposit definition){
        if(depth<=0)return false;
        int minY=chunk.getHeightAccessorForGeneration().getMinY(),maxY=chunk.getHeightAccessorForGeneration().getMaxY();
        var pos=new BlockPos.MutableBlockPos(minX,minY,minZ);
        for(int z=minZ;z<minZ+16;z++)for(int x=minX;x<minX+16;x++){
            int top=chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG,x,z);
            if(top<minY||top>maxY||!biomes.apply(pos.set(x,top,z)).is(Tags.Biomes.IS_RIVER))continue;
            int bottom=(int)Math.max(minY+1,Math.ceil(top-depth));
            for(int y=top;y>=bottom;y--){
                var state=chunk.getBlockState(pos.setY(y));
                if(definition.replaceable(state))return true;
            }
        }
        return false;
    }

    /** Shared core for feature execution and isolated chunk-boundary checks. */
    public static int placeChunk(ChunkAccess chunk,int minX,int minZ,CratonContents.GenerationData generation,
                                 PositionalRandomFactory root,GeologyClimateSampler climate,
                                 Function<BlockPos,Holder<Biome>> biomes, PlacerDeposit definition){
        double depth=generation.depositPlan().maximumPlacerDepth();
        if(depth<=0)return 0;
        int minY=chunk.getHeightAccessorForGeneration().getMinY(),maxY=chunk.getHeightAccessorForGeneration().getMaxY();
        DepositGenerationContext deposits=null;
        GeologyFieldSampler continuous=null;
        Map<Holder<GeologyProfile>,GeologyFieldSampler> profiles=null;
        BlockPos.MutableBlockPos pos=new BlockPos.MutableBlockPos(minX,minY,minZ);
        int changed=0;
        for(int z=minZ;z<minZ+16;z++)for(int x=minX;x<minX+16;x++){
            int surface=chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG,x,z);
            if(surface<minY||surface>maxY)continue;
            pos.set(x,surface,z);
            var biome=biomes.apply(pos);
            if(!biome.is(Tags.Biomes.IS_RIVER))continue;
            var surfaceState=chunk.getBlockState(pos);
            boolean sediment=false;
            int bottom=(int)Math.max(minY+1,Math.ceil(surface-depth));
            for(int y=surface;y>=bottom;y--){
                var state=chunk.getBlockState(pos.setY(y));
                if(definition.replaceable(state)){sediment=true;break;}
            }
            if(!sediment)continue;
            var profile=generation.getGeologyProfile(climate.sample(x,z));
            if(profile.isEmpty()||profile.get().value().layers().isEmpty())continue;
            GeologyFieldSampler geology;
            if(generation.transition()!=null&&!generation.transition().layers().isEmpty()){
                if(continuous==null)continuous=new GeologyFieldSampler(generation.transition(),minY,root,climate);
                geology=continuous;
            }else{
                if(profiles==null)profiles=new IdentityHashMap<>();
                geology=profiles.get(profile.get());
                if(geology==null){
                    geology=new GeologyFieldSampler(profile.get().value().layers(),minY,root,climate);
                    profiles.put(profile.get(),geology);
                }
            }
            if(deposits==null){
                deposits=new DepositGenerationContext(root,generation.depositPlan());
            }
            DepositFieldEngine.prepare(deposits,geology,x,z,minY,surface,surface,surfaceState,biome);
            if(deposits.placer().isEmpty())continue;
            for(int y=surface;y>=bottom;y--){
                var old=chunk.getBlockState(pos.setY(y));
                if(!definition.replaceable(old))continue;
                var state=DepositFieldEngine.applyPlacer(deposits,old,y);
                if(!state.equals(old)){
                    chunk.setBlockState(pos,state,Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS);
                    changed++;
                }
            }
        }
        return changed;
    }

    /** Diagnose the natural-placement funnel; no block writes or candidate rerolls. */
    public static String inspectChunk(ChunkAccess chunk,int minX,int minZ,CratonContents.GenerationData generation,
                                      PositionalRandomFactory root,GeologyClimateSampler climate,
                                      Function<BlockPos,Holder<Biome>> biomes){
        double depth=generation.depositPlan().maximumPlacerDepth();
        if(depth<=0)return "Craton placer: rule=source_density_v36 status=disabled_or_missing_definition";
        var definition=generation.depositPlan().definitions().stream().map(d->d.deposit())
                .filter(d->d instanceof PlacerDeposit)
                .map(d->(PlacerDeposit)d).findFirst().orElseThrow();
        int minY=chunk.getHeightAccessorForGeneration().getMinY(),maxY=chunk.getHeightAccessorForGeneration().getMaxY();
        int river=0,sediment=0,profiles=0,candidates=0,sourced=0,gold=0,iron=0,other=0,existingGold=0,existingIron=0;
        double maxGold=0,maxIron=0;
        DepositGenerationContext deposits=null;
        GeologyFieldSampler continuous=null;
        Map<Holder<GeologyProfile>,GeologyFieldSampler> fallback=new IdentityHashMap<>();
        var pos=new BlockPos.MutableBlockPos(minX,minY,minZ);
        for(int z=minZ;z<minZ+16;z++)for(int x=minX;x<minX+16;x++){
            int surface=chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG,x,z);
            if(surface<minY||surface>maxY)continue;
            var biome=biomes.apply(pos.set(x,surface,z));
            if(!biome.is(Tags.Biomes.IS_RIVER))continue;
            river++;
            var surfaceState=chunk.getBlockState(pos);
            int bottom=(int)Math.max(minY+1,Math.ceil(surface-depth));
            boolean eligible=false;
            for(int y=surface;y>=bottom;y--){
                var state=chunk.getBlockState(pos.setY(y));
                if(definition.replaceable(state))eligible=true;
                String id=state.toString();
                if(id.contains("auriferous"))existingGold++;
                else if(id.contains("ferriferous"))existingIron++;
            }
            if(!eligible)continue;
            sediment++;
            var profile=generation.getGeologyProfile(climate.sample(x,z));
            if(profile.isEmpty()||profile.get().value().layers().isEmpty())continue;
            profiles++;
            GeologyFieldSampler geology;
            if(generation.transition()!=null&&!generation.transition().layers().isEmpty()){
                if(continuous==null)continuous=new GeologyFieldSampler(generation.transition(),minY,root,climate);
                geology=continuous;
            }else{
                geology=fallback.get(profile.get());
                if(geology==null){geology=new GeologyFieldSampler(profile.get().value().layers(),minY,root,climate);fallback.put(profile.get(),geology);}
            }
            if(deposits==null){
                deposits=new DepositGenerationContext(root,generation.depositPlan());
            }
            DepositFieldEngine.prepare(deposits,geology,x,z,minY,surface,surface,surfaceState,biome);
            if(deposits.placer().isEmpty())continue;
            candidates++;
            var source=deposits.sourceStrength(definition);
            maxGold=Math.max(maxGold,source.gold());maxIron=Math.max(maxIron,source.iron());
            if(Math.max(source.gold(),source.iron())>0)sourced++;
            for(int y=surface;y>=bottom;y--){
                var old=chunk.getBlockState(pos.setY(y));
                if(!definition.replaceable(old))continue;
                var result=DepositFieldEngine.applyPlacer(deposits,old,y);
                if(result.equals(old))continue;
                String id=result.toString();
                if(id.contains("auriferous"))gold++;
                else if(id.contains("ferriferous"))iron++;
                else other++;
            }
        }
        String status=gold+iron+other==0&&existingGold+existingIron>0?"already_mineralized":river==0?"no_river":sediment==0?"no_sediment":profiles==0?"no_geology_profile":
                candidates==0?"no_candidates":sourced==0?"no_source":gold+iron+other==0?"no_mineralization":"ready";
        return String.format(java.util.Locale.ROOT,"Craton placer: rule=source_density_v36 chunk=%d,%d status=%s river=%d sediment=%d geology=%d candidates=%d source=%d maxGold=%.3f maxIron=%.3f predictedGold=%d predictedIron=%d predictedOther=%d existingGold=%d existingIron=%d",
                Math.floorDiv(minX,16),Math.floorDiv(minZ,16),status,river,sediment,profiles,candidates,sourced,maxGold,maxIron,gold,iron,other,existingGold,existingIron);
    }
}
