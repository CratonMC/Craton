package com.teamtea.craton.data.tag;


import com.teamtea.craton.api.block.ExtendedBlockFamily;
import com.teamtea.craton.common.registry.CratonBlocks;
import com.teamtea.craton.common.core.StoneCollection;
import com.teamtea.craton.common.registry.CratonTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;


public final class CBlockTagProvider extends BlockTagsProvider {
    public CBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId) {
        super(output, lookupProvider, modId);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        for (StoneCollection collection : CratonBlocks.STONE_COLLECTIONS) {
            // addStoneFamilyTags(collection.origin().get());
            // addStoneFamilyTags(collection.polished().get());
            for (BlockFamily blockFamily : collection.getAll()) {
                addStoneFamilyTags(blockFamily);
            }
            tag(BlockTags.STONE_ORE_REPLACEABLES).add(collection.getOrigin().getBaseBlock());
        }
    }

    private void addStoneFamilyTags(BlockFamily family) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(family.getBaseBlock())
                .add(family.get(BlockFamily.Variant.STAIRS))
                .add(family.get(BlockFamily.Variant.SLAB))
                .add(family.get(BlockFamily.Variant.WALL))
                .add(ExtendedBlockFamily.getVerticalSlab(family));

        tag(BlockTags.NEEDS_STONE_TOOL)
                .add(family.getBaseBlock())
                .add(family.get(BlockFamily.Variant.STAIRS))
                .add(family.get(BlockFamily.Variant.SLAB))
                .add(family.get(BlockFamily.Variant.WALL))
                .add(ExtendedBlockFamily.getVerticalSlab(family));

        tag(BlockTags.WALLS)
                .add(family.get(BlockFamily.Variant.WALL));

        tag(CratonTags.Blocks.VERTICAL_SLABS).add(ExtendedBlockFamily.getVerticalSlab(family));

    }
    protected record Appender(TagAppender<Block> app) implements TagAppender<Block> {
        @Override
        public Appender add(ResourceKey<Block> element) {
            app.add(element);
            return this;
        }

        @Override
        public Appender addOptional(ResourceKey<Block> element) {
            app.addOptional(element);
            return this;
        }

        @Override
        public Appender addTag(TagKey<Block> tag) {
            app.addTag(tag);
            return this;
        }

        @Override
        public Appender addOptionalTag(TagKey<Block> tag) {
            app.addOptionalTag(tag);
            return this;
        }

        @Override
        public Appender add(TagEntry entry) {
            app.add(entry);
            return this;
        }

        @Override
        public Appender replace(boolean value) {
            app.replace(value);
            return this;
        }

        @Override
        public Appender remove(ResourceKey<Block> element) {
            app.remove(element);
            return this;
        }

        @Override
        public Appender remove(TagKey<Block> tag) {
            app.remove(tag);
            return this;
        }

        public Appender add(Block... blocks) {
            for (Block block : blocks) {
                add(BuiltInRegistries.BLOCK.wrapAsHolder(block).getKey());
            }
            return this;
        }

        public Appender addAll(Iterable<Block> blocks) {
            for (Block block : blocks) {
                add(block);
            }
            return this;
        }
    }

    @Override
    protected Appender tag(TagKey<Block> tag) {
        return new Appender(super.tag(tag));
    }
}
