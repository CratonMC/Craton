package com.teamtea.craton.common.survey;

import com.teamtea.craton.common.worldgen.geology.GeologyClimateSampler;
import com.teamtea.craton.common.worldgen.geology.GeologyFieldSampler;

import com.teamtea.craton.Craton;
import com.teamtea.craton.common.loot.OreAvailability;
import com.teamtea.craton.common.registry.CratonContents;
import com.teamtea.craton.common.registry.CratonItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/** Uses vanilla map ids, colors, storage, synchronization, markers and item-frame rendering. */
@EventBusSubscriber
public class SurveyMaps {
    public static final byte SCALE=3;
    private static final int MAX_JOBS=4,MAX_UNITS_PER_TICK=64;
    private static final long TICK_BUDGET_NANOS=2_000_000;
    private static final Map<MinecraftServer,State> STATES=new WeakHashMap<>();
    private record Key(ResourceKey<Level> dimension,int x,int z){}
    private static class State {
        CratonContents.GenerationData generation;
        Object availabilityVersion;
        final ArrayDeque<Job> jobs=new ArrayDeque<>();
        final Map<Key,ItemStack> completed=new LinkedHashMap<>(16,.75f,true){
            @Override protected boolean removeEldestEntry(Map.Entry<Key,ItemStack> eldest){return size()>64;}
        };
    }
    private SurveyMaps(){}

    public static boolean start(ServerPlayer player,InteractionHand hand){
        ItemStack input=player.getItemInHand(hand);
        if(!input.is(CratonItems.SURVEY_CHART.value()))return false;
        ServerLevel level=player.level();
        if(!level.dimension().equals(Level.OVERWORLD)
                ||!(level.getChunkSource().getGenerator() instanceof NoiseBasedChunkGenerator)){
            player.sendOverlayMessage(Component.translatable("survey.craton.unsupported"));return false;
        }
        var generation=CratonContents.getGenerationData();
        if(generation.regions().isEmpty()){
            player.sendOverlayMessage(Component.translatable("survey.craton.no_data"));return false;
        }
        State state=STATES.computeIfAbsent(level.getServer(),ignored -> new State());
        refresh(state,generation);
        for(Job job:state.jobs)if(job.player==player){
            player.sendOverlayMessage(Component.translatable("survey.craton.busy"));return false;
        }
        var bounds=MapItemSavedData.createFresh(player.getX(),player.getZ(),SCALE,true,true,level.dimension());
        Key key=new Key(level.dimension(),bounds.centerX,bounds.centerZ);
        ItemStack cached=state.completed.get(key);
        if(cached!=null&&MapItem.getSavedData(cached,level)!=null){
            deliver(player,hand,input,cached.copy());return true;
        }
        if(state.jobs.size()>=MAX_JOBS){
            player.sendOverlayMessage(Component.translatable("survey.craton.queue_full"));return false;
        }
        state.jobs.addLast(new Job(player,hand,input,level,generation,key));
        player.sendOverlayMessage(Component.translatable("survey.craton.started"));return true;
    }
    private static void deliver(ServerPlayer player,InteractionHand hand,ItemStack input,ItemStack map){
        player.setItemInHand(hand,ItemUtils.createFilledResult(input,player,map));
        player.sendOverlayMessage(Component.translatable("survey.craton.finished"));
    }
    private static void refresh(State state,CratonContents.GenerationData generation){
        Object availabilityVersion=OreAvailability.version();
        if(state.generation==generation&&state.availabilityVersion==availabilityVersion)return;
        for(Job job:state.jobs)job.player.sendOverlayMessage(Component.translatable("survey.craton.cancelled"));
        state.jobs.clear();state.completed.clear();state.generation=generation;state.availabilityVersion=availabilityVersion;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post event){
        State state=STATES.get(event.getServer());if(state==null)return;
        refresh(state,CratonContents.getGenerationData());
        long deadline=System.nanoTime()+TICK_BUDGET_NANOS;
        for(int unit=0;unit<MAX_UNITS_PER_TICK&&!state.jobs.isEmpty()&&System.nanoTime()<deadline;unit++){
            Job job=state.jobs.removeFirst();
            if(!job.valid()){
                if(!job.player.hasDisconnected())job.player.sendOverlayMessage(Component.translatable("survey.craton.cancelled"));
                continue;
            }
            try{
                if(job.step()){
                    ItemStack map=job.finish();state.completed.put(job.key,map.copy());
                    deliver(job.player,job.hand,job.input,map);
                    Craton.LOGGER.debug("Survey {}: cells={}, candidates={}, footprints={}, cpuMs={}, maxUnitMs={}",
                            job.key,job.survey.visitedCells(),job.survey.visitedCandidates(),job.canvas.markCount(),
                            job.cpuNanos/1e6,job.maxUnitNanos/1e6);
                }else state.jobs.addLast(job);
            }catch(RuntimeException exception){
                job.player.sendOverlayMessage(Component.translatable("survey.craton.failed"));
                Craton.LOGGER.error("Could not prepare mineral survey map",exception);
            }
        }
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event){STATES.remove(event.getServer());}

    private static class Job {
        final ServerPlayer player;
        final InteractionHand hand;
        final ItemStack input;
        final ServerLevel level;
        final CratonContents.GenerationData generation;
        final Key key;
        SurveyMapCanvas canvas;
        DepositSurvey survey;
        long cpuNanos,maxUnitNanos;
        Job(ServerPlayer player,InteractionHand hand,ItemStack input,ServerLevel level,
            CratonContents.GenerationData generation,Key key){
            this.player=player;this.hand=hand;this.input=input;this.level=level;this.generation=generation;this.key=key;
        }
        boolean valid(){
            return !player.hasDisconnected()&&player.isAlive()&&player.level()==level
                    &&player.getItemInHand(hand)==input&&input.is(CratonItems.SURVEY_CHART.value())
                    &&generation==CratonContents.getGenerationData();
        }
        boolean step(){
            long start=System.nanoTime();
            try{
                if(survey==null){initialize();return false;}
                return survey.step();
            }finally{
                long elapsed=System.nanoTime()-start;cpuNanos+=elapsed;maxUnitNanos=Math.max(maxUnitNanos,elapsed);
            }
        }
        void initialize(){
            var generator=(NoiseBasedChunkGenerator)level.getChunkSource().getGenerator();
            var settings=generator.generatorSettings().value();
            var randomState=level.getChunkSource().randomState();
            // Reconstruct the exact root passed by RandomState to MaterialSystem, including legacy worlds.
            var random=settings.getRandomSource().newInstance(randomState.seed()).forkPositional();
            var sampler=randomState.createClimateSampler(SamplerContext.EMPTY_UNCACHED);
            var climate=new GeologyClimateSampler(sampler,settings.seaLevel());
            var biomes=generator.getBiomeSource().createResolver(sampler);
            var geology=new GeologyFieldSampler(generation.transition(),level.getMinY(),random,climate);
            canvas=new SurveyMapCanvas(key.x(),key.z(),SCALE,MapColor.SAND.getPackedId(MapColor.Brightness.LOW),palette());
            survey=new DepositSurvey(generation.depositPlan(),geology,random,level.getMinY(),canvas,new DepositSurvey.Environment(){
                public boolean river(int x,int z){return biomes.getNoiseBiome(Math.floorDiv(x,4),Math.floorDiv(settings.seaLevel(),4),Math.floorDiv(z,4)).is(Tags.Biomes.IS_RIVER);}
                public boolean ocean(int x,int z){return biomes.getNoiseBiome(Math.floorDiv(x,4),Math.floorDiv(settings.seaLevel(),4),Math.floorDiv(z,4)).is(Tags.Biomes.IS_OCEAN);}
                public double surfaceY(int x,int z){return climate.regionalSurfaceY(x,z);}
            });
        }
        ItemStack finish(){
            // No map id or item is consumed until the complete image is ready.
            ItemStack map=MapItem.create(level,key.x(),key.z(),SCALE,true,true);
            var data=MapItem.getSavedData(map,level);
            if(data==null||data.centerX!=key.x()||data.centerZ!=key.z())throw new IllegalStateException("Survey map bounds mismatch");
            data.colors=canvas.colors();
            var locked=data.locked();locked.setDirty();
            level.setMapData(map.get(DataComponents.MAP_ID),locked);
            map.set(DataComponents.CUSTOM_NAME,Component.translatable("item.craton.mineral_survey_map"));
            List<Component> lore=new ArrayList<>();
            lore.add(Component.translatable("survey.craton.approximate").withStyle(ChatFormatting.GRAY));
            lore.add(Component.translatable("survey.craton.shading").withStyle(ChatFormatting.GRAY));
            lore.add(Component.translatable("survey.craton.coverage",1024,1024).withStyle(ChatFormatting.GRAY));
            lore.add(Component.translatable("survey.craton.legend.metals"));
            lore.add(Component.translatable("survey.craton.legend.other"));
            lore.add(Component.translatable("survey.craton.legend.contacts"));
            map.set(DataComponents.LORE,new ItemLore(lore));return map;
        }
    }
    private static byte[][] palette(){
        MapColor[] colors={MapColor.COLOR_LIGHT_GRAY,MapColor.COLOR_BLACK,MapColor.COLOR_ORANGE,
                MapColor.COLOR_PURPLE,MapColor.COLOR_LIGHT_GREEN,MapColor.COLOR_LIGHT_BLUE,MapColor.GOLD,
                MapColor.COLOR_CYAN,MapColor.EMERALD,MapColor.COLOR_BROWN,MapColor.COLOR_RED,
                MapColor.COLOR_PINK,MapColor.COLOR_MAGENTA};
        byte[][] out=new byte[colors.length][3];
        for(int i=0;i<colors.length;i++){
            out[i][0]=colors[i].getPackedId(MapColor.Brightness.LOW);
            out[i][1]=colors[i].getPackedId(MapColor.Brightness.NORMAL);
            out[i][2]=colors[i].getPackedId(MapColor.Brightness.HIGH);
        }
        return out;
    }
}
