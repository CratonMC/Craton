package com.teamtea.craton.common.registry;

import com.teamtea.craton.Craton;
import com.teamtea.craton.api.geology.GeologyLayer;
import com.teamtea.craton.api.geology.GeologyProfile;
import com.teamtea.craton.api.geology.GeologyRegion;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

import java.util.List;
import java.util.stream.Stream;

/** Climate geology regions; layer order is top-to-bottom, independent of biome tags. */
public class GeologyProfileRegistry {
    public static final ResourceKey<GeologyProfile> OCEAN = createKey("ocean");
    public static final ResourceKey<GeologyProfile> COASTAL = createKey("coastal");
    public static final ResourceKey<GeologyProfile> PLAINS = createKey("plains");
    public static final ResourceKey<GeologyProfile> HILLS = createKey("hills");
    public static final ResourceKey<GeologyProfile> MOUNTAINS = createKey("mountains");
    public static final ResourceKey<GeologyProfile> COLD_PLAINS = createKey("cold_plains");
    public static final ResourceKey<GeologyProfile> ARID_PLAINS = createKey("arid_plains");
    public static final ResourceKey<GeologyProfile> ARID_HILLS = createKey("arid_hills");
    public static final ResourceKey<GeologyProfile> WARM_HUMID = createKey("warm_humid");
    public static final ResourceKey<GeologyProfile> WARM_SEMIARID = createKey("warm_semiarid");

    private GeologyProfileRegistry() {}

    private static ResourceKey<GeologyProfile> createKey(String name) {
        return ResourceKey.create(CratonRegistries.GEOLOGY_PROFILE, Craton.rl(name));
    }

    public static void bootstrap(BootstrapContext<GeologyProfile> context) {
        var layers = context.lookup(CratonRegistries.GEOLOGY_LAYER);
        var regions = context.lookup(CratonRegistries.GEOLOGY_REGION);
        register(context, layers, regions, OCEAN, GeologyRegionRegistry.OCEAN,
                GeologyLayerRegistry.BASALT, GeologyLayerRegistry.TUFF, GeologyLayerRegistry.GABBRO, GeologyLayerRegistry.ECLOGITE);
        register(context, layers, regions, COASTAL, GeologyRegionRegistry.COASTAL,
                GeologyLayerRegistry.LIMESTONE, GeologyLayerRegistry.SHALE, GeologyLayerRegistry.SANDSTONE, GeologyLayerRegistry.SCHIST, GeologyLayerRegistry.GNEISS, GeologyLayerRegistry.ECLOGITE);
        register(context, layers, regions, PLAINS, GeologyRegionRegistry.PLAINS,
                GeologyLayerRegistry.LIMESTONE, GeologyLayerRegistry.SHALE, GeologyLayerRegistry.SANDSTONE, GeologyLayerRegistry.SCHIST, GeologyLayerRegistry.GNEISS, GeologyLayerRegistry.ECLOGITE);
        register(context, layers, regions, HILLS, GeologyRegionRegistry.HILLS,
                GeologyLayerRegistry.ANDESITE, GeologyLayerRegistry.TUFF, GeologyLayerRegistry.SCHIST, GeologyLayerRegistry.GNEISS, GeologyLayerRegistry.GABBRO, GeologyLayerRegistry.ECLOGITE);
        register(context, layers, regions, MOUNTAINS, GeologyRegionRegistry.MOUNTAINS,
                GeologyLayerRegistry.ANDESITE, GeologyLayerRegistry.TUFF, GeologyLayerRegistry.LIMESTONE,
                GeologyLayerRegistry.SHALE, GeologyLayerRegistry.MARBLE, GeologyLayerRegistry.SCHIST,
                GeologyLayerRegistry.GNEISS, GeologyLayerRegistry.ECLOGITE);
        register(context, layers, regions, COLD_PLAINS, GeologyRegionRegistry.COLD_PLAINS,
                GeologyLayerRegistry.SHALE, GeologyLayerRegistry.SANDSTONE, GeologyLayerRegistry.SCHIST, GeologyLayerRegistry.GNEISS, GeologyLayerRegistry.ECLOGITE);
        register(context, layers, regions, ARID_PLAINS, GeologyRegionRegistry.ARID_PLAINS,
                GeologyLayerRegistry.SANDSTONE, GeologyLayerRegistry.ARKOSE_SANDSTONE, GeologyLayerRegistry.SHALE, GeologyLayerRegistry.GNEISS, GeologyLayerRegistry.ECLOGITE);
        register(context, layers, regions, ARID_HILLS, GeologyRegionRegistry.ARID_HILLS,
                GeologyLayerRegistry.ARKOSE_SANDSTONE, GeologyLayerRegistry.SHALE, GeologyLayerRegistry.SANDSTONE, GeologyLayerRegistry.SCHIST, GeologyLayerRegistry.GNEISS, GeologyLayerRegistry.ECLOGITE);
        register(context, layers, regions, WARM_HUMID, GeologyRegionRegistry.WARM_HUMID,
                GeologyLayerRegistry.LIMESTONE, GeologyLayerRegistry.SHALE, GeologyLayerRegistry.TUFF, GeologyLayerRegistry.SCHIST, GeologyLayerRegistry.GNEISS, GeologyLayerRegistry.ECLOGITE);
        register(context, layers, regions, WARM_SEMIARID, GeologyRegionRegistry.WARM_SEMIARID,
                GeologyLayerRegistry.BASALT, GeologyLayerRegistry.TUFF, GeologyLayerRegistry.RHYOLITE, GeologyLayerRegistry.ANDESITE, GeologyLayerRegistry.GABBRO, GeologyLayerRegistry.ECLOGITE);
    }

    @SafeVarargs
    private static void register(BootstrapContext<GeologyProfile> context,
                                 HolderGetter<GeologyLayer> layers,
                                 HolderGetter<GeologyRegion> regions,
                                 ResourceKey<GeologyProfile> key, ResourceKey<GeologyRegion> region,
                                 ResourceKey<GeologyLayer>... keys) {
        List<Holder<GeologyLayer>> entries = Stream.of(keys)
                .<Holder<GeologyLayer>>map(layers::getOrThrow)
                .toList();

        context.register(key, new GeologyProfile(regions.getOrThrow(region), entries));
    }
}
