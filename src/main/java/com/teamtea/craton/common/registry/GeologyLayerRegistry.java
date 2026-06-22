package com.teamtea.craton.common.registry;

import com.teamtea.craton.Craton;
import com.teamtea.craton.api.geology.GeologyLayer;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class GeologyLayerRegistry {
    public static final ResourceKey<GeologyLayer> DEEPSLATE = createKey("deepslate");
    public static final ResourceKey<GeologyLayer> STONE = createKey("stone");
    public static final ResourceKey<GeologyLayer> GRANITE = createKey("granite");
    public static final ResourceKey<GeologyLayer> DIORITE = createKey("diorite");
    public static final ResourceKey<GeologyLayer> ANDESITE = createKey("andesite");
    public static final ResourceKey<GeologyLayer> TUFF = createKey("tuff");
    public static final ResourceKey<GeologyLayer> BASALT = createKey("basalt");
    public static final ResourceKey<GeologyLayer> GRAVEL = createKey("gravel");

    public static final ResourceKey<GeologyLayer> GABBRO = createKey("gabbro");
    public static final ResourceKey<GeologyLayer> GNEISS = createKey("gneiss");
    public static final ResourceKey<GeologyLayer> LIMESTONE = createKey("limestone");
    public static final ResourceKey<GeologyLayer> MARBLE = createKey("marble");
    public static final ResourceKey<GeologyLayer> RHYOLITE = createKey("rhyolite");
    public static final ResourceKey<GeologyLayer> PEGMATITE = createKey("pegmatite");

    private static ResourceKey<GeologyLayer> createKey(String name) {
        return ResourceKey.create(CratonRegistries.GEOLOGY_LAYER, Craton.rl(name));
    }

    public static void bootstrap(BootstrapContext<GeologyLayer> context) {
        context.register(DEEPSLATE, layer(Blocks.DEEPSLATE, 48, 8.0, 0.006, 0.009, 77));
        context.register(STONE, layer(Blocks.STONE, 28.0, 8.0, 0.006, 0.009, 11));
        context.register(GRANITE, layer(Blocks.GRANITE, 24.0, 14.0, 0.008, 0.011, 22));
        context.register(DIORITE, layer(Blocks.DIORITE, 22.0, 12.0, 0.009, 0.010, 33));
        context.register(ANDESITE, layer(Blocks.ANDESITE, 26.0, 10.0, 0.007, 0.012, 44));
        context.register(TUFF, layer(Blocks.TUFF, 20.0, 16.0, 0.011, 0.008, 55));
        context.register(BASALT, layer(Blocks.BASALT, 30.0, 9.0, 0.006, 0.007, 66));
        context.register(GRAVEL, layer(Blocks.GRAVEL, 5, 4, 0.006, 0.007, 18));

        context.register(GABBRO, layer(CratonBlocks.GABBRO.getOrigin().getBaseBlock(), 30.0, 8.0, 0.006, 0.010, 101));
        context.register(GNEISS, layer(CratonBlocks.GNEISS.getOrigin().getBaseBlock(), 24.0, 18.0, 0.008, 0.012, 202));
        context.register(LIMESTONE, layer(CratonBlocks.LIMESTONE.getOrigin().getBaseBlock(), 26.0, 10.0, 0.010, 0.006, 303));
        context.register(MARBLE, layer(CratonBlocks.MARBLE.getOrigin().getBaseBlock(), 22.0, 14.0, 0.009, 0.011, 404));
        context.register(RHYOLITE, layer(CratonBlocks.RHYOLITE.getOrigin().getBaseBlock(), 28.0, 12.0, 0.012, 0.007, 505));
        context.register(PEGMATITE, layer(CratonBlocks.PEGMATITE.getOrigin().getBaseBlock(), 12.0, 6.0, 0.004, 0.005, 606));

    }

    private static GeologyLayer layer(
            Block block,
            double thickness,
            double amplitude,
            double freqX,
            double freqZ,
            int seed
    ) {
        return new GeologyLayer(
                block.defaultBlockState(),
                thickness,
                amplitude,
                freqX,
                freqZ,
                seed
        );
    }
}