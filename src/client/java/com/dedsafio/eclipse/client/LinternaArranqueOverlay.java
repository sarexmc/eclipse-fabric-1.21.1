package com.dedsafio.eclipse.client;

import com.dedsafio.eclipse.item.LinternaItem;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import java.util.Locale;

/** Cuenta regresiva de encendido: círculo oscuro con el tiempo (2.0 -> 0.0) y un contorno blanco que se va completando. */
public final class LinternaArranqueOverlay {
    private LinternaArranqueOverlay() {}

    public static void render(GuiGraphics g, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.options.hideGui) return;

        ItemStack stack = LinternaItem.isStarting(player.getMainHandItem()) ? player.getMainHandItem()
                : LinternaItem.isStarting(player.getOffhandItem()) ? player.getOffhandItem() : ItemStack.EMPTY;
        if (stack.isEmpty()) return;

        int w = g.guiWidth();
        int h = g.guiHeight();
        double total = LinternaItem.WARMUP_TICKS;
        double elapsed = mc.level.getGameTime() + partialTick - LinternaItem.startTime(stack);
        float frac = (float) Mth.clamp(elapsed / total, 0.0, 1.0);
        float remaining = (float) Math.max(0.0, (total - elapsed) / 20.0);

        float cx = w / 2f;
        float cy = h / 2f + h * 0.20f;
        float r = Math.max(12f, h * 0.045f);
        float thick = Math.max(1.5f, r * 0.12f);

        g.flush();
        Matrix4f m = g.pose().last().pose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // fondo oscuro
        BufferBuilder b = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        b.addVertex(m, cx, cy, 0f).setColor(0f, 0f, 0f, 0.6f);
        int seg = 64;
        for (int i = 0; i <= seg; i++) {
            double a = Math.PI * 2.0 * i / seg;
            b.addVertex(m, cx + (float) Math.cos(a) * r, cy + (float) Math.sin(a) * r, 0f).setColor(0f, 0f, 0f, 0.6f);
        }
        BufferUploader.drawWithShader(b.buildOrThrow());

        // contorno tenue completo
        ring(m, cx, cy, r - thick, r, 0.0, Math.PI * 2.0, 0.22f);

        // contorno blanco que da la vuelta (empieza arriba, sentido horario)
        if (frac > 0f) {
            double start = -Math.PI / 2.0;
            ring(m, cx, cy, r - thick, r, start, start + Math.PI * 2.0 * frac, 1.0f);
        }

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        String text = String.format(Locale.ROOT, "%.1f", remaining);
        g.drawCenteredString(mc.font, text, (int) cx, (int) (cy - mc.font.lineHeight / 2f), 0xFFFFFF);
    }

    private static void ring(Matrix4f m, float cx, float cy, float rin, float rout, double a0, double a1, float alpha) {
        int n = Math.max(2, (int) Math.ceil(64 * Math.abs(a1 - a0) / (Math.PI * 2.0)));
        BufferBuilder b = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < n; i++) {
            double s0 = a0 + (a1 - a0) * i / n;
            double s1 = a0 + (a1 - a0) * (i + 1) / n;
            b.addVertex(m, cx + (float) Math.cos(s0) * rin, cy + (float) Math.sin(s0) * rin, 0f).setColor(1f, 1f, 1f, alpha);
            b.addVertex(m, cx + (float) Math.cos(s0) * rout, cy + (float) Math.sin(s0) * rout, 0f).setColor(1f, 1f, 1f, alpha);
            b.addVertex(m, cx + (float) Math.cos(s1) * rout, cy + (float) Math.sin(s1) * rout, 0f).setColor(1f, 1f, 1f, alpha);
            b.addVertex(m, cx + (float) Math.cos(s1) * rin, cy + (float) Math.sin(s1) * rin, 0f).setColor(1f, 1f, 1f, alpha);
        }
        BufferUploader.drawWithShader(b.buildOrThrow());
    }
}
