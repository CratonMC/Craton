package com.teamtea.craton.common.block;

import com.teamtea.craton.common.registry.CratonDataMaps;
import com.teamtea.craton.common.loot.OreAvailability;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags;

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

    /**
     * Coal behaves like vanilla coal ore: mining releases 0-2 XP, but Silk Touch
     * does not. The vanilla block-breaking pipeline supplies dropExperience=false
     * when experience should be suppressed (including Silk Touch).
     * Item drops remain handled by the reloadable ore loot table.
     */
    @Override
    protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos,
                                   ItemStack tool, boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, tool, dropExperience);
        if (!dropExperience) return;
        if (state.is(Tags.Blocks.ORES_COAL)) {
            popExperience(level, pos, level.getRandom().nextInt(3));
        }
    }

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

