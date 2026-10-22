package com.teamtea.craton.client.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.IFocusGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.Locale;

/** Main result/probability and bonus result/probability stay on screen. */
public final class OreDropCategory extends AbstractRecipeCategory<OreDropDisplay> {
    public OreDropCategory(IGuiHelper gui) {
        super(CratonJeiPlugin.ORE_DROPS, Component.translatable("jei.craton.ore_drops"),
            gui.createDrawableIngredient(mezz.jei.api.constants.VanillaTypes.ITEM_STACK,
                new ItemStack(Items.IRON_PICKAXE)), 252, 138);
    }
    @Override public void setRecipe(IRecipeLayoutBuilder builder, OreDropDisplay d, IFocusGroup focuses) {
        builder.addInputSlot(15, 35).setStandardSlotBackground().add(d.ore());
        builder.addOutputSlot(214, 35).setOutputSlotBackground().add(d.result());
    }
    @Override public void draw(OreDropDisplay d, IRecipeSlotsView slots, GuiGraphicsExtractor g, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        g.fill(4, 3, 248, 29, 0xFFD6D3C8);
        g.text(font, Component.translatable(d.bonus() ? "jei.craton.ui.bonus" : "jei.craton.ui.primary"),
            9, 6, 0xFF303030, false);
        g.text(font, Component.literal(shorten(d.ore().getHoverName().getString(), 34)),
            9, 18, 0xFF555555, false);
        g.fill(5, 33, 247, 66, 0xFFE2E2E2);
        g.text(font, Component.literal("→"), 126, 40, 0xFF303030, false);
        g.text(font, Component.literal(shorten(d.result().getHoverName().getString(), 34)),
            9, 71, 0xFF303030, false);
        g.text(font, Component.translatable("jei.craton.ui.base_chance",
            String.format(Locale.ROOT, "%.1f%%", d.baseChance() * 100.0)),
            9, 85, 0xFF303030, false);
        g.text(font, Component.translatable("jei.craton.ui.drop_count", d.minCount(), d.maxCount()),
            145, 85, 0xFF303030, false);
        g.fill(9, 101, 242, 115, 0xFF555555);
        g.fill(10, 102, 241, 114, 0xFFDDDDDD);
        int width = (int) Math.round(231 * Math.max(0.0, Math.min(1.0, d.baseChance())));
        if (width > 0) g.fill(10, 102, 10 + width, 114, d.bonus() ? 0xFF9F8CC8 : 0xFF6BA484);
        g.text(font, Component.translatable("jei.craton.ui.fortune_effect"),
            9, 124, 0xFF555555, false);
    }
    private static String shorten(String s,int n) { return s.length()>n ? s.substring(0,n-1)+"…" : s; }
}
