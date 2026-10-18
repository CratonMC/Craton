package com.teamtea.craton.common.block;

import com.teamtea.craton.common.registry.CratonDataMaps;
import com.teamtea.craton.common.loot.OreAvailability;
import net.minecraft.world.level.block.Block;

/** The host is immutable block identity; mineral drops are reloadable block data. */
public class OreBlock extends Block {
    private final Block host;
    private record CachedDrops(Object version, CratonDataMaps.OreDrops source, CratonDataMaps.OreDrops resolved) {}
    private volatile CachedDrops cachedDrops;

    public OreBlock(Block host, Properties properties){
        super(properties);
        this.host=host;
    }

    public Block host(){return host;}

    /** Reloadable product definition attached to this registered block. */
    public CratonDataMaps.OreDrops oreDrops() {
        return builtInRegistryHolder().getData(CratonDataMaps.ORE_DROPS);
    }
    /** Resolve tag conditions once per block and reload, rather than per generated voxel. */
    public CratonDataMaps.OreDrops availableDrops() {
        var data = oreDrops();
        Object version = OreAvailability.version();
        CachedDrops cached = cachedDrops;
        if (cached != null && cached.version() == version && cached.source() == data) return cached.resolved();
        var resolved = OreAvailability.resolve(data);
        cachedDrops = new CachedDrops(version, data, resolved);
        return resolved;
    }
}

