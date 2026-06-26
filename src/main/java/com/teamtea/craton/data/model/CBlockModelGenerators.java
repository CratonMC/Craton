package com.teamtea.craton.data.model;

import com.mojang.math.Quadrant;
import com.teamtea.craton.Craton;
import com.teamtea.craton.api.geology.block.ExtendedBlockFamily;
import com.teamtea.craton.common.block.VerticalSlabBlock;
import com.teamtea.craton.common.registry.CratonBlocks;
import com.teamtea.craton.common.core.StoneCollection;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.BlockFamily;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.Optional;


public class CBlockModelGenerators {
    private final BlockModelGenerators models;


    public static final TextureSlot TS_1 = TextureSlot.create("1");
    public static final ModelTemplate VERTICAL_SLAB = new ModelTemplate(
            Optional.of(Craton.rl("block/template_vertical_slab_all")),
            Optional.empty(),
            TextureSlot.ALL
    );

    public CBlockModelGenerators(BlockModelGenerators models) {
        this.models = models;
    }

    public void run() {
        for (StoneCollection collection : CratonBlocks.STONE_COLLECTIONS) {
            addStoneFamily(collection.origin().get());
            addStoneFamily(collection.polished().get());
            addStoneFamily(collection.brick().get());
            addStoneFamily(collection.mossyBrick().get());
        }
    }

    private void addStoneFamily(BlockFamily family) {
        Block block = family.getBaseBlock();
        models.family(block)
                .generateFor(family);
        models.registerSimpleItemModel(block, ModelLocationUtils.getModelLocation(block));
        // simpleBlockItem(
        //         block,
        //         resource(BuiltInRegistries.BLOCK.getKey(block).getPath()),
        //         true
        // );
        if (family instanceof ExtendedBlockFamily et
                && et.getVerticalSlab() instanceof VerticalSlabBlock verticalSlabBlock)
            addVerticalSlab(verticalSlabBlock, family.getBaseBlock());
    }

    private void addVerticalSlab(VerticalSlabBlock verticalSlab, Block baseBlock) {
        Identifier model = VERTICAL_SLAB.create(
                verticalSlab,
                TextureMapping.cube(baseBlock),
                models.modelOutput
        );

        Identifier fullModel = ModelLocationUtils.getModelLocation(baseBlock);

        models.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(verticalSlab)
                        .with(PropertyDispatch.initial(VerticalSlabBlock.TYPE)
                                .generate(type -> switch (type) {
                                    case NORTH -> new MultiVariant(WeightedList.of(
                                            new Variant(model)
                                    ));
                                    case SOUTH -> new MultiVariant(WeightedList.of(
                                            new Variant(model).withYRot(Quadrant.R180)
                                    ));
                                    case WEST -> new MultiVariant(WeightedList.of(
                                            new Variant(model).withYRot(Quadrant.R270)
                                    ));
                                    case EAST -> new MultiVariant(WeightedList.of(
                                            new Variant(model).withYRot(Quadrant.R90)
                                    ));
                                    case DOUBLE -> new MultiVariant(WeightedList.of(
                                            new Variant(fullModel)
                                    ));
                                }))
        );

        models.registerSimpleItemModel(verticalSlab, model);
    }

    private String name(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).getPath();
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
