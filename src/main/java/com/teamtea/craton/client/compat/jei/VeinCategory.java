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

/** Bounded schematic using real deposit shape parameters; never predicts world coordinates. */
public final class VeinCategory extends AbstractRecipeCategory<VeinDisplay> {
    private static final int LEFT = 9, TOP = 44, RIGHT = 243, BOTTOM = 206;
    public VeinCategory(IGuiHelper gui) {
        super(CratonJeiPlugin.VEINS, Component.translatable("jei.craton.veins"),
            gui.createDrawableIngredient(mezz.jei.api.constants.VanillaTypes.ITEM_STACK,
                new ItemStack(Items.IRON_ORE)), 252, 286);
    }
    @Override public void setRecipe(IRecipeLayoutBuilder builder, VeinDisplay d, IFocusGroup focuses) {
        for (int n = 0; n < Math.min(5, d.ores().size()); n++)
            builder.addOutputSlot(9 + n * 47, 225).setStandardSlotBackground().add(d.ores().get(n));
    }
    @Override public void draw(VeinDisplay d, IRecipeSlotsView slots, GuiGraphicsExtractor g, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        g.fill(4, 3, 248, 39, 0xFFD8D4CB);
        g.text(font, shorten(d.id(), 36), 8, 7, 0xFF252525, false);
        g.text(font, "矿床纵剖面 · 等比例形态示意", 8, 20, 0xFF555555, false);
        g.text(font, "灰：母岩   棕：矿化带   金：矿体", 8, 31, 0xFF555555, false);
        g.fill(LEFT - 2, TOP - 2, RIGHT + 2, BOTTOM + 2, 0xFF353B3F);
        g.enableScissor(LEFT, TOP, RIGHT, BOTTOM);
        // A continuous host-rock field rather than arbitrary square-grid ore noise.
        g.fill(LEFT, TOP, RIGHT, BOTTOM, 0xFF666E73);
        for (int y = TOP + 12; y < BOTTOM; y += 14)
            g.fill(LEFT, y, RIGHT, y + 1, 0xFF798186);
        // All coordinates are normalized schematic coordinates, not predicted world blocks.
        // The silhouettes follow the major terms of each corresponding generator's density field.
        double hx = Math.max(1.0, Math.max(d.radiusX(), d.radiusZ()));
        double hy = Math.max(0.5, d.radiusY());
        // Some deposit fields (VMS feeder systems, veins and dipping bands) extend
        // beyond their nominal ellipsoid radius. Reserve this space at the same world scale.
        double extentX = hx * (d.geometry().equals("band") ? 1.40 : 1.18);
        double extentY = hy * (d.geometry().equals("pipe") ? 1.25 : 1.18);
        if (d.geometry().equals("vms")) extentY = Math.max(extentY, 55.0);
        if (d.geometry().equals("band")) {
            extentY = Math.max(extentY, Math.tan(Math.toRadians(Math.min(65, d.dip()))) * hx * 0.6 + 2.0 * hy);
        }
        // Single pixels-per-block scale for both axes: no vertical stretching or flattening.
        // Keep 12% breathing room, and preserve world-space aspect ratio.
        double scale = Math.min((RIGHT - LEFT - 18) / (2.0 * extentX),
                                (BOTTOM - TOP - 18) / (2.0 * extentY));
        int seed = d.id().hashCode();
        for (int py = TOP + 2; py < BOTTOM - 2; py += 2) {
            for (int px = LEFT + 2; px < RIGHT - 2; px += 2) {
                // World-space coordinates, then normalize independently by actual worldgen radii.
                double worldX = (px + 1 - (LEFT + RIGHT) * 0.5) / scale;
                double worldY = (py + 1 - (TOP + BOTTOM) * 0.5) / scale;
                double x = worldX / hx;
                double y = worldY / hy; // Positive is downward.
                double yDip = worldY / Math.max(1.0, extentY);
                double n = .035 * Math.sin(15 * x + seed * .001) * Math.cos(11 * y + 2.1);
                double layer = y + .10 * Math.sin(5*x + .7);
                boolean ore = false, alteration = false, feeder = false;
                switch (d.geometry()) {
                    case "band" -> { // BIF is a dipping planar band, not an oval.
                        double slope = Math.tan(Math.toRadians(Math.min(65, d.dip()))) * .48;
                        double distance = (worldY + Math.tan(Math.toRadians(Math.min(65, d.dip()))) * worldX * .50)
                                / Math.max(1.0, hy);
                        ore = Math.abs(distance) < .65 + n && Math.abs(x) < 1.00;
                        alteration = Math.abs(distance) < 1.05 && Math.abs(x) < 1.00;
                    }
                    case "stratiform" -> { // One mineralized sedimentary horizon.
                        double distance = layer - .13;
                        ore = Math.abs(distance) < .11 + n && Math.abs(x) < 1.0;
                        alteration = Math.abs(distance) < .20 && Math.abs(x) < 1.06;
                    }
                    case "rollfront" -> { // C-shaped front limited to sandstone host bed.
                        double curve = .34 * (1 - Math.min(1, Math.pow(y/.66, 2)));
                        double distance = x - curve;
                        ore = Math.abs(distance) < .095 + n && Math.abs(y) < .68;
                        alteration = Math.abs(distance) < .17 && Math.abs(y) < .74;
                    }
                    case "vms" -> { // Submarine lens plus downward tapering feeder stockwork.
                        double lens = Math.pow(x/.75, 2) + Math.pow((y+.28)/.30, 2);
                        double drift = .07 * Math.sin(7 * y + .2);
                        double feederY = worldY / Math.max(30, 55.0);
                        feeder = worldY > -2 && worldY < 55 && Math.abs(x - drift) < .10 + .16 * (1-feederY);
                        ore = lens < 1 + n || feeder;
                        alteration = lens < 1.20 || (worldY > -2 && worldY < 55 && Math.abs(x-drift) < .21+.19*(1-worldY/55.0));
                    }
                    case "pipe" -> { // Kimberlite pipe widens upward: radius ~ height^.78.
                        double t = Math.max(0, Math.min(1, (1.15-y)/2.30));
                        double width = .12 + .66 * Math.pow(t,.78);
                        ore = Math.abs(x - .045*Math.sin(y*5)) < width + n;
                        alteration = Math.abs(x - .045*Math.sin(y*5)) < width + .10;
                    }
                    case "intrusion" -> { // Pluton dome, broad upper contact and deeper stalk.
                        double width = .30 + .63 * Math.sqrt(Math.max(0, 1-Math.pow((y-.28)/1.35,2)));
                        ore = y > -.83 && y < 1.1 && Math.abs(x)<width+n;
                        alteration = y > -.97 && y < 1.17 && Math.abs(x)<width+.11;
                    }
                    case "contact" -> { // Skarn occupies outer rim of an intrusion, not its core.
                        double body = Math.pow(x/.70,2) + Math.pow((y-.38)/.88,2);
                        ore = body > .93 && body < 1.22 + n && x > -.67;
                        alteration = body > .80 && body < 1.40 && x > -.78;
                    }
                    case "pegmatite", "epithermal" -> { // Irregular, branching fracture network.
                        double axis = x + .40*y - .12*Math.sin(6*y+1);
                        double branch = x - .34 - .10*y;
                        ore = (Math.abs(axis) < .082+n && Math.abs(y)<1.10)
                                || (y < -.06 && Math.abs(branch) < .04 && Math.abs(x)<.8);
                        alteration = Math.abs(axis)<.16 || (y<-.06 && Math.abs(branch)<.10);
                    }
                    case "redstone" -> { // Layer-confined ribbon, gently bent along strike.
                        double axis = x - .10*Math.sin(5*y);
                        ore = Math.abs(y)<.28 && Math.abs(axis)<.95;
                        alteration = Math.abs(y)<.37 && Math.abs(axis)<1.05;
                    }
                    case "weathering" -> { // Layered weathering profile below a surface.
                        double surface = -.48+.07*Math.sin(x*5);
                        alteration = y > surface && y < surface+.57 && Math.abs(x)<1.0;
                        ore = y > surface+.12 && y < surface+.40 && Math.abs(x)<.97;
                    }
                    case "peat" -> { // Shallow surface-bound deposit.
                        double surface = -.44+.08*Math.sin(x*4);
                        ore = y > surface && y < surface+.27 && Math.abs(x)<1;
                    }
                    case "placer" -> { // Narrow channel-bottom accumulation, not a buried lens.
                        double bed = -.10+.25*Math.sin(x*2.7)+.08*Math.sin(x*8);
                        ore = y > bed && y < bed+.12 && Math.abs(x)<1.05;
                    }
                    case "pods" -> { // Jadeitite ellipsoid with disconnected satellites.
                        double main = Math.pow(x/.67,2)+Math.pow(y/.72,2);
                        double pod1 = Math.pow((x+.76)/.16,2)+Math.pow((y-.43)/.14,2);
                        double pod2 = Math.pow((x-.74)/.19,2)+Math.pow((y+.30)/.13,2);
                        ore = main < 1+n || pod1<1 || pod2<1;
                        alteration = main<1.2 || pod1<1.2 || pod2<1.2;
                    }
                    default -> { // Ordinary ellipsoid with a moderately irregular boundary.
                        double body = x*x + y*y;
                        ore = body<1+n;
                        alteration = body<1.18;
                    }
                }
                if (alteration || ore) {
                    int color = ore ? ((seed ^ (px*734287) ^ (py*912931)) & 15) < 3
                            ? 0xFFDAA86C : 0xFFAD6740 : 0xFF867D67;
                    g.fill(px, py, px+2, py+2, color);
                }
            }
        }
        g.disableScissor();
        // Put size outside of the clipping viewport; white on the dark frame.
        g.fill(LEFT, 189, RIGHT, 204, 0xD52D3538);
        g.text(font, String.format(Locale.ROOT, "水平半径 %.0f · 垂直参考半径 %.0f 格", hx, hy), 12, 192, 0xFFFFFFFF, true);
        g.text(font, Component.translatable("jei.craton.ui.vein_ores"), 8, 212, 0xFF353535, false);
        for (int n = 0; n < Math.min(5, d.ores().size()); n++)
            g.text(font, shorten(d.ores().get(n).getHoverName().getString(), 7), 7 + n * 47, 245, 0xFF353535, false);
        if (d.ores().size() > 5) g.text(font, "+" + (d.ores().size() - 5), 227, 212, 0xFF353535, false);
        g.fill(4, 260, 248, 283, 0xFFD6D3C8);
        String height = Double.isFinite(d.heightMin()) && Double.isFinite(d.heightMax())
                ? String.format(Locale.ROOT, "高度 %.0f–%.0f", d.heightMin(), d.heightMax()) : "高度依地层而定";
        g.text(font, height, 8, 267, 0xFF303030, false);
        g.text(font, String.format(Locale.ROOT, "频率参数 %.1f%%", d.frequency() * 100.0),
                142, 267, 0xFF303030, false);
    }
    private static String shorten(String s, int max) { return s.length() > max ? s.substring(0, max-1) + "…" : s; }
}
