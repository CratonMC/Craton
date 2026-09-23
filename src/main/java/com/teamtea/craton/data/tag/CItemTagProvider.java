package com.teamtea.craton.data.tag;


import com.teamtea.craton.Craton;
import com.teamtea.craton.api.block.ExtendedBlockFamily;
import com.teamtea.craton.common.registry.CratonBlocks;
import com.teamtea.craton.common.core.StoneCollection;
import com.teamtea.craton.common.registry.CratonTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import org.jspecify.annotations.NonNull;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.concurrent.CompletableFuture;


public final class CItemTagProvider extends ItemTagsProvider {

    public CItemTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> providerCompletableFuture) {
        super(packOutput, providerCompletableFuture, Craton.MODID);
    }

    @Override
    protected void addTags(HolderLookup.@NonNull Provider pProvider) {
        for (StoneCollection collection : CratonBlocks.STONE_COLLECTIONS) {
            // addStoneFamilyTags(collection.origin().get());
            // addStoneFamilyTags(collection.polished().get());
            for (BlockFamily blockFamily : collection.getAll()) {
                addStoneFamilyTags(blockFamily);
            }
        }
    }

    private void addStoneFamilyTags(BlockFamily family) {
        tag(ItemTags.WALLS)
                .add(family.get(BlockFamily.Variant.WALL).asItem());

        tag(CratonTags.Items.VERTICAL_SLABS).add(ExtendedBlockFamily.getVerticalSlab(family).asItem()
        );
    }

    protected record Appender(TagAppender<Item> app) implements TagAppender<Item> {
        @Override
        public Appender add(ResourceKey<Item> element) {
            app.add(element);
            return this;
        }

        @Override
        public Appender addOptional(ResourceKey<Item> element) {
            app.addOptional(element);
            return this;
        }

        @Override
        public Appender addTag(TagKey<Item> tag) {
            app.addTag(tag);
            return this;
        }

        @Override
        public Appender addOptionalTag(TagKey<Item> tag) {
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
        public Appender remove(ResourceKey<Item> element) {
            app.remove(element);
            return this;
        }

        @Override
        public Appender remove(TagKey<Item> tag) {
            app.remove(tag);
            return this;
        }

        public Appender add(Item... items) {
            for (Item item : items) {
                add(BuiltInRegistries.ITEM.wrapAsHolder(item).getKey());
            }
            return this;
        }

        public Appender add(Holder<Item>... items) {
            for (Holder<Item> item : items) {
                add(item.getKey());
            }
            return this;
        }
    }

    @Override
    protected Appender tag(TagKey<Item> tag) {
        return new Appender(super.tag(tag));
    }
}
