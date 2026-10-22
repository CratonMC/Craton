package com.teamtea.craton.client.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.IFocusGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Stratigraphy rendered from baked block-model side faces, in top-to-bottom order. */
public final class GeologyCategory extends AbstractRecipeCategory<GeologyDisplay> {
    private static final int X = 7, WIDTH = 151, TOP = 39, BOTTOM = 214;

    public GeologyCategory(IGuiHelper gui) {
        super(CratonJeiPlugin.GEOLOGY, Component.translatable("jei.craton.geology"),
            gui.createDrawableIngredient(mezz.jei.api.constants.VanillaTypes.ITEM_STACK,
                new ItemStack(Items.STONE)), 252, 228);
    }

    @Override public void setRecipe(IRecipeLayoutBuilder builder, GeologyDisplay d, IFocusGroup focuses) {
        // Keep model-face cross-section unobstructed; slots on the right support JEI navigation.
        int count = d.strata().size();
        int[] bounds = bounds(d, count);
        // Slot backgrounds are 18px high. Do not overlap them on dense profiles.
        // All layers remain rendered in the cross-section, regardless of slot capacity.
        if (count <= (BOTTOM - TOP) / 18) {
            for (int n = 0; n < count; n++) {
                int mid = TOP + (bounds[n] + bounds[n + 1]) / 2;
                builder.addInputSlot(166, Math.max(TOP, Math.min(BOTTOM - 18, mid - 9)))
                    .setStandardSlotBackground().add(d.strata().get(n));
            }
        }
    }

    @Override public void draw(GeologyDisplay d, IRecipeSlotsView slots, GuiGraphicsExtractor g, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        g.fill(4, 3, 248, 34, 0xFFDAD6CD);
        g.text(font, trim(d.id(), 30), 9, 7, 0xFF252525, false);

        g.text(font, Component.translatable(d.hostLookup() ? "jei.craton.ui.host_rocks" : "jei.craton.ui.geology_region"),
                9, 20, 0xFF505050, false);
        int count = d.strata().size();
        if (count == 0) {
            g.text(font, "No rock data", 9, 45, 0xFF333333, false);
            return;
        }
        int[] bounds = bounds(d, count);
        g.fill(X - 2, TOP - 2, X + WIDTH + 2, BOTTOM + 2, 0xFF30363A);
        g.enableScissor(X, TOP, X + WIDTH, BOTTOM);
        for (int n = 0; n < count; n++) {
            int y0 = TOP + bounds[n], y1 = TOP + bounds[n + 1];
            ItemStack rock = d.strata().get(n);
            TextureAtlasSprite face = faceSprite(rock);
            g.fill(X, y0, X + WIDTH, y1, 0xFF696D70);
            if (face != null) {
                // Tile the actual BAKED block-model side sprite horizontally and vertically.
                for (int y = y0; y < y1; y += 16) {
                    for (int x = X; x < X + WIDTH; x += 16) {
                        g.blitSprite(RenderPipelines.GUI_TEXTURED, face, x, y, 16, 16);
                    }
                }
            }
            g.fill(X, y1 - 1, X + WIDTH, y1, 0xAA202428);
        }
        g.disableScissor();
        // Dense profiles keep every layer visible, with compact labels for tight bands.
        g.enableScissor(187, TOP, 249, BOTTOM);
        for (int n = 0; n < count; n++) {
            int mid = TOP + (bounds[n] + bounds[n + 1]) / 2;
            int bandHeight = bounds[n + 1] - bounds[n];
            if (bandHeight < 9) continue; // A thinner band remains visible in the cross-section.
            ItemStack rock = d.strata().get(n);
            int labelY = Math.max(TOP, Math.min(BOTTOM - 9, mid - 4));
            g.text(font, trim(rock.getHoverName().getString(), 10), 188,
                labelY, 0xFF252525, false);
            if (bandHeight >= 19 && !d.hostLookup() && n < d.thickness().size()) {
                g.text(font, String.format(Locale.ROOT, "%.0fm", d.thickness().get(n)),
                    188, Math.min(BOTTOM - 9, labelY + 9), 0xFF555555, false);
            }
        }
        g.disableScissor();
    }

    /** Extract the NORTH-facing quad's sprite from the registered baked block model. */
    private static TextureAtlasSprite faceSprite(ItemStack item) {
        if (!(item.getItem() instanceof net.minecraft.world.item.BlockItem blockItem)) return null;
        var mc = Minecraft.getInstance();
        var state = blockItem.getBlock().defaultBlockState();
        var model = mc.getModelManager().getBlockStateModelSet().get(state);
        if (model == null) return null;
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(0L), parts);
        for (BlockStateModelPart part : parts) {
            for (var quad : part.getQuads(Direction.NORTH)) return quad.materialInfo().sprite();
            for (var quad : part.getQuads(null)) {
                if (quad.direction() == Direction.NORTH) return quad.materialInfo().sprite();
            }
        }
        return model.particleMaterial().sprite();
    }

    private static int[] bounds(GeologyDisplay d, int count) {
        int[] result = new int[count + 1];
        if (count == 0) return result;
        double total = 0;
        double[] weights = new double[count];
        for (int n = 0; n < count; n++) {
            double weight = d.hostLookup() || n >= d.thickness().size() ? 1 : d.thickness().get(n);
            weights[n] = Double.isFinite(weight) ? Math.max(0.01, weight) : 1;
            total += weights[n];
        }
        int height = BOTTOM - TOP;
        // Minimum adapts to the number of layers, avoiding negative free space.
        int minimum = count <= 9 ? 18 : Math.min(8, height / count);
        int flexible = Math.max(0, height - minimum * count);
        double cumulative = 0;
        for (int n = 1; n < count; n++) {
            cumulative += weights[n - 1] / total;
            result[n] = minimum * n + (int)Math.round(flexible * cumulative);
        }
        result[count] = height;
        return result;
    }
    private static String trim(String value, int limit) {
        return value.length() > limit ? value.substring(0, limit - 1) + "…" : value;
    }
}
