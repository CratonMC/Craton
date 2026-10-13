package com.teamtea.craton.common.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import java.util.concurrent.CompletableFuture;
import java.util.Map;
import java.util.WeakHashMap;

/** Operator-only excavation utility for exposing the sides of geological sections. */
@EventBusSubscriber
public class GeologyDebugCommands {
    private static final Map<MinecraftServer,ExcavationJob> JOBS=new WeakHashMap<>();
    private GeologyDebugCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event){
        event.getDispatcher().register(Commands.literal("craton")
                .then(Commands.literal("excavate")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(context -> start(context.getSource(),1))
                        .then(Commands.literal("fast").executes(context -> start(context.getSource(),1,true)))
                        .then(Commands.literal("1").executes(context -> start(context.getSource(),1))
                                .then(Commands.literal("fast").executes(context -> start(context.getSource(),1,true))))
                        .then(Commands.literal("3").executes(context -> start(context.getSource(),3))
                                .then(Commands.literal("fast").executes(context -> start(context.getSource(),3,true))))
                        .then(Commands.literal("5").executes(context -> start(context.getSource(),5))
                                .then(Commands.literal("fast").executes(context -> start(context.getSource(),5,true))))
                        .then(Commands.literal("9").executes(context -> start(context.getSource(),9))
                                .then(Commands.literal("fast").executes(context -> start(context.getSource(),9,true))))
                        .then(Commands.literal("status").executes(context -> status(context.getSource())))
                        .then(Commands.literal("cancel").executes(context -> cancel(context.getSource())))));
    }

    private static int start(CommandSourceStack source,int size){
        return start(source,size,false);
    }

    private static int start(CommandSourceStack source,int size,boolean fast){
        MinecraftServer server=source.getServer();
        if(JOBS.containsKey(server)){
            source.sendFailure(Component.translatable("command.craton.excavate.busy"));
            return 0;
        }
        BlockPos center=BlockPos.containing(source.getPosition());
        ExcavationJob job=new ExcavationJob(source,Math.floorDiv(center.getX(),16),Math.floorDiv(center.getZ(),16),size,fast);
        JOBS.put(server,job);
        source.sendSuccess(() -> Component.translatable("command.craton.excavate.start",size,size,
                job.minX,job.maxX,job.minZ,job.maxZ),true);
        if(fast) source.sendSuccess(() -> Component.translatable("command.craton.excavate.fast"),false);
        return 1;
    }

    private static int status(CommandSourceStack source){
        ExcavationJob job=JOBS.get(source.getServer());
        if(job==null){
            source.sendFailure(Component.translatable("command.craton.excavate.idle"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.craton.excavate.status",
                job.finishedColumns,job.totalColumns,job.removed),false);
        return 1;
    }

    private static int cancel(CommandSourceStack source){
        ExcavationJob job=JOBS.remove(source.getServer());
        if(job==null){
            source.sendFailure(Component.translatable("command.craton.excavate.idle"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.craton.excavate.cancel",job.removed),true);
        return 1;
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event){
        ExcavationJob job=JOBS.get(event.getServer());
        if(job==null) return;
        try{
            if(job.step(job.operationBudget,job.timeBudget)){
                JOBS.remove(event.getServer());
                job.source.sendSuccess(() -> Component.translatable("command.craton.excavate.done",job.removed),true);
            }
        }catch(RuntimeException exception){
            JOBS.remove(event.getServer());
            job.source.sendFailure(Component.translatable("command.craton.excavate.failed",exception.getMessage()));
        }
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event){JOBS.remove(event.getServer());}

    /** Each column stops at the first bedrock block; neither it nor anything below it is touched. */
    static class ExcavationJob {
        final CommandSourceStack source;
        final ServerLevel level;
        final int minX,minZ,maxX,maxZ,minY,maxY,totalColumns;
        final int operationBudget;
        final long timeBudget;
        private final BlockPos.MutableBlockPos position=new BlockPos.MutableBlockPos();
        private int x,z,y,finishedColumns;
        private boolean columnStarted;
        private long removed;
        private CompletableFuture<?> pendingChunk;
        private int pendingX,pendingZ;

        private boolean readyChunk(){
            int cx=Math.floorDiv(x,16),cz=Math.floorDiv(z,16);
            if(level.getChunkSource().getChunkNow(cx,cz)!=null){pendingChunk=null;return true;}
            if(pendingChunk==null||pendingX!=cx||pendingZ!=cz){
                pendingX=cx;pendingZ=cz;
                pendingChunk=level.getChunkSource().getChunkFuture(cx,cz,ChunkStatus.FULL,true);
                return false;
            }
            if(!pendingChunk.isDone()) return false;
            // Never join an incomplete future on the server thread.
            pendingChunk.join();
            if(level.getChunkSource().getChunkNow(cx,cz)==null)
                throw new IllegalStateException("Unable to load chunk "+cx+", "+cz);
            pendingChunk=null;
            return true;
        }

        ExcavationJob(CommandSourceStack source,int chunkX,int chunkZ,int size){
            this(source,chunkX,chunkZ,size,false);
        }

        ExcavationJob(CommandSourceStack source,int chunkX,int chunkZ,int size,boolean fast){
            if(size!=1&&size!=3&&size!=5&&size!=9)
                throw new IllegalArgumentException("Excavation size must be 1, 3, 5 or 9 chunks");
            this.source=source;this.level=source.getLevel();
            int radius=size/2;
            minX=(chunkX-radius)*16;minZ=(chunkZ-radius)*16;
            maxX=minX+size*16-1;maxZ=minZ+size*16-1;
            minY=level.getMinY();maxY=level.getMaxY();
            totalColumns=size*size*256;
            operationBudget=fast?32768:8192;
            timeBudget=fast?16_000_000L:8_000_000L;
            x=minX;z=minZ;y=maxY;
        }

        boolean step(int budget,long nanos){
            if(finishedColumns==totalColumns) return true;
            long deadline=System.nanoTime()+nanos;
            if(!readyChunk()) return false;
            for(int visited=0;visited<budget&&finishedColumns<totalColumns;visited++){
                if((visited&15)==0&&System.nanoTime()>=deadline) break;
                // A new chunk request yields this tick instead of synchronously generating terrain.
                if(!columnStarted){
                    if(!readyChunk()) return false;
                    // WORLD_SURFACE includes water, leaves and player buildings.
                    // Read only after the asynchronous chunk request has completed.
                    y=Math.min(maxY,level.getHeight(Heightmap.Types.WORLD_SURFACE,x,z)-1);
                    columnStarted=true;
                    if(y<minY){nextColumn();continue;}
                }
                position.set(x,y,z);
                BlockState state=level.getBlockState(position);
                if(state.is(Blocks.BEDROCK)){
                    nextColumn();
                    continue;
                }
                if(!state.isAir()&&level.setBlock(position,Blocks.AIR.defaultBlockState(),
                        Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS)) removed++;
                if(--y<minY) nextColumn();
            }
            return finishedColumns==totalColumns;
        }

        private void nextColumn(){
            finishedColumns++;
            y=maxY;
            columnStarted=false;
            // Finish all 256 columns in this chunk before visiting the next one.
            if(Math.floorMod(x-minX,16)<15){x++;return;}
            x-=15;
            if(Math.floorMod(z-minZ,16)<15){z++;return;}
            z-=15;
            x+=16;
            if(x>maxX){x=minX;z+=16;}
        }
    }
}
