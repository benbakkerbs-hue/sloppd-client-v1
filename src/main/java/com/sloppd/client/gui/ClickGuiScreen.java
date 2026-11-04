package com.sloppd.client.gui;

import com.sloppd.client.module.ClientModule;
import com.sloppd.client.module.ModuleCategory;
import com.sloppd.client.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {
    private static final int W = 132, HEADER = 23, ROW = 20;
    private static final int ACCENT = 0xFFAD75FF, MUTED = 0xFF9292A5;
    private final List<Panel> panels = new ArrayList<>();
    private Panel dragging;
    private double offsetX, offsetY;

    public ClickGuiScreen() {
        super(Text.literal("sloppd client"));
        int[][] xy = {{18,42},{158,42},{298,42},{18,170},{158,170},{298,170}};
        ModuleCategory[] categories = ModuleCategory.values();
        for (int i = 0; i < categories.length; i++) panels.add(new Panel(categories[i], xy[i][0], xy[i][1]));
    }

    @Override public void render(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, width, height, 0xB8101018);
        ctx.drawTextWithShadow(textRenderer, Text.literal("sloppd client"), 15, 14, ACCENT);
        ctx.drawTextWithShadow(textRenderer, Text.literal("v1.2.0  •  Fabric 1.21.11"), 112, 14, MUTED);
        for (Panel p : panels) {
            List<ClientModule> mods = ModuleManager.byCategory(p.category);
            int h = HEADER + (p.collapsed ? 0 : mods.size() * ROW);
            ctx.fill(p.x, p.y, p.x + W, p.y + h, 0xFF191922);
            ctx.fill(p.x, p.y, p.x + W, p.y + HEADER, 0xFF252532);
            ctx.fill(p.x, p.y, p.x + 3, p.y + HEADER, ACCENT);
            ctx.drawTextWithShadow(textRenderer, Text.literal(p.category.displayName), p.x + 9, p.y + 8, -1);
            ctx.drawTextWithShadow(textRenderer, Text.literal(p.collapsed ? "+" : "−"), p.x + W - 14, p.y + 8, ACCENT);
            if (p.collapsed) continue;
            for (int i = 0; i < mods.size(); i++) {
                ClientModule m = mods.get(i);
                int y = p.y + HEADER + i * ROW;
                if (mx >= p.x && mx <= p.x + W && my >= y && my < y + ROW)
                    ctx.fill(p.x + 2, y, p.x + W - 2, y + ROW, 0xFF292936);
                ctx.drawTextWithShadow(textRenderer, Text.literal(m.getName()), p.x + 8, y + 6,
                        m.isEnabled() ? ACCENT : MUTED);
                ctx.fill(p.x + W - 17, y + 6, p.x + W - 8, y + 15,
                        m.isEnabled() ? ACCENT : 0xFF454550);
            }
        }
        ctx.drawTextWithShadow(textRenderer, Text.literal("RIGHT SHIFT  |  ESC  |  DRAG PANELS"),
                15, height - 18, MUTED);
        super.render(ctx, mx, my, delta);
    }

    @Override public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        if (click.button() == 0) {
            for (int i = panels.size() - 1; i >= 0; i--) {
                Panel p = panels.get(i);
                if (mx >= p.x && mx <= p.x + W && my >= p.y && my <= p.y + HEADER) {
                    panels.remove(p); panels.add(p);
                    if (mx >= p.x + W - 21) p.collapsed = !p.collapsed;
                    else { dragging = p; offsetX = mx - p.x; offsetY = my - p.y; }
                    return true;
                }
                if (!p.collapsed) {
                    List<ClientModule> mods = ModuleManager.byCategory(p.category);
                    for (int j = 0; j < mods.size(); j++) {
                        int y = p.y + HEADER + j * ROW;
                        if (mx >= p.x && mx <= p.x + W && my >= y && my < y + ROW) {
                            mods.get(j).toggle(MinecraftClient.getInstance());
                            return true;
                        }
                    }
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override public boolean mouseDragged(Click click, double dx, double dy) {
        double mx = click.x(), my = click.y();
        if (dragging != null && click.button() == 0) {
            dragging.x = (int)(mx - offsetX); dragging.y = (int)(my - offsetY); return true;
        }
        return super.mouseDragged(click, dx, dy);
    }
    @Override public boolean mouseReleased(Click click) {
        if (click.button() == 0) { dragging = null; return true; }
        return super.mouseReleased(click);
    }
    @Override public boolean shouldPause() { return false; }
    @Override public void close() { if (client != null) client.setScreen(null); }

    private static final class Panel {
        final ModuleCategory category; int x, y; boolean collapsed;
        Panel(ModuleCategory category, int x, int y) { this.category = category; this.x = x; this.y = y; }
    }
}