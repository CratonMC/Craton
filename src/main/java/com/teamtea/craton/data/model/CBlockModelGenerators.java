package com.teamtea.craton.data.model;

import com.teamtea.craton.Craton;
import com.teamtea.craton.common.registry.CratonBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;


public class CBlockModelGenerators {
    private final BlockModelGenerators models;


    public static final TextureSlot TS_1 = TextureSlot.create("1");

    public CBlockModelGenerators(BlockModelGenerators models) {
        this.models = models;
    }

    public void run() {
        stone(CratonBlocks.PEGMATITE.get());
        stone(CratonBlocks.GNEISS.get());
        stone(CratonBlocks.RHYOLITE.get());
        stone(CratonBlocks.MARBLE.get());
        stone(CratonBlocks.LIMESTONE.get());
        stone(CratonBlocks.GABBRO.get());
    }

    private void stone(Block block) {
        simpleBlockItem(
                block,
                resource(BuiltInRegistries.BLOCK.getKey(block).getPath()),
                true
        );
    }

    private void simpleBlockItem(Block block, Identifier textureid, boolean item) {
        simpleBlockItem(block, textureid, ModelTemplates.CUBE_ALL, item);
    }

    private void simpleBlockItem(Block block, Identifier textureid, ModelTemplate modelTemplate, boolean item) {
        models.createTrivialBlock(block,
                (b) -> TexturedModel.createDefault((_) ->
                        TextureMapping.cube(new Material(textureid))
                                .put(TextureSlot.PARTICLE, new Material(textureid)), modelTemplate).get(b));
        if (item && block.asItem() != Items.AIR)
            models.registerSimpleItemModel(block, ModelLocationUtils.getModelLocation(block));
    }


    public void addSimple(Block block) {
        models.createNonTemplateModelBlock(block);
    }

    public void addSimple(Block block, Block parent) {
        models.createNonTemplateModelBlock(block, parent);
    }


    public Identifier id(String id) {
        return Craton.parse(id);
    }

    public Identifier resource(String path) {
        return Craton.rl("block/" + path);
    }
}
