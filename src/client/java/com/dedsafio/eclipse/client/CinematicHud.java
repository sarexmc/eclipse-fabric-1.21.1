package com.dedsafio.eclipse.client;

import net.minecraft.client.gui.GuiGraphics;

/** Barras de cine arriba y abajo. */
public final class CinematicHud {
    private CinematicHud() {}

    public static void render(GuiGraphics g, float partialTick) {
        float b = CinematicFx.bars(partialTick);
        if (b <= 0.002f) return;
        int w = g.guiWidth();
        int h = g.guiHeight();
        int barH = (int) (h * 0.12f * b);
        if (barH <= 0) return;
        g.fill(0, 0, w, barH, 0xFF000000);
        g.fill(0, h - barH, w, h, 0xFF000000);
    }
}
