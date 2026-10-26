package com.dedsafio.eclipse.client;

import com.dedsafio.eclipse.eclipse.EclipseSky;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

/**
 * Momentos cinematográficos del eclipse (solo cliente):
 * barras de cine, título al empezar, latido que se acelera, trueno grave y temblor al llegar a la totalidad,
 * y un cierre suave cuando vuelve la luz.
 */
public final class CinematicFx {
    private CinematicFx() {}

    private static boolean wasActive, totalityHit, endHit;
    private static int barsTicks, pulse, heartbeatCd, tickCount;
    private static float bars, prevBars;
    private static float lastYaw, lastPitch;

    public static void reset() {
        wasActive = totalityHit = endHit = false;
        barsTicks = pulse = heartbeatCd = 0;
        bars = prevBars = 0f;
        lastYaw = lastPitch = 0f;
    }

    private static void play(SoundEvent sound, float pitch, float volume) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
    }

    public static void tick() {
        tickCount++;
        Minecraft mc = Minecraft.getInstance();
        boolean active = mc.level != null && ClientEclipse.isActiveHere();
        float p = ClientEclipse.progress;

        if (active && !wasActive) {
            totalityHit = false;
            endHit = false;
            barsTicks = 160;
            heartbeatCd = 40;
            mc.gui.setTimes(15, 80, 25);
            mc.gui.setSubtitle(Component.literal("Mantén la linterna encendida"));
            mc.gui.setTitle(Component.literal("ECLIPSE"));
            play(SoundEvents.WITHER_AMBIENT, 0.5f, 0.8f);
        }
        if (!active && wasActive) {
            totalityHit = false;
            endHit = false;
            barsTicks = 0;
            pulse = 0;
        }

        if (active) {
            if (!totalityHit && p >= 0.97f && ClientEclipse.speed >= 0f) {
                totalityHit = true;
                pulse = 80;
                barsTicks = 150;
                play(SoundEvents.LIGHTNING_BOLT_THUNDER, 0.5f, 2.0f);
                play(SoundEvents.BEACON_DEACTIVATE, 0.5f, 1.0f);
            }
            if (!endHit && ClientEclipse.speed < 0f) {
                endHit = true;
                barsTicks = 100;
                play(SoundEvents.BEACON_ACTIVATE, 0.6f, 1.0f);
            }
            if (p > 0.45f && --heartbeatCd <= 0) { // latido: se acelera y se hace más fuerte
                play(SoundEvents.WARDEN_HEARTBEAT, 0.9f + 0.4f * p, 0.5f + 0.9f * p);
                heartbeatCd = (int) Mth.lerp(p, 44f, 18f);
            }
        }
        wasActive = active;

        prevBars = bars;
        float target = barsTicks > 0 ? 1f : 0f;
        if (barsTicks > 0) barsTicks--;
        bars += (target - bars) * 0.12f;
        if (pulse > 0) pulse--;
        applyShake(mc);
    }

    /** Altura de las barras (0..1) ya suavizada. */
    public static float bars(float partial) {
        float b = Mth.lerp(partial, prevBars, bars);
        return b * b * (3f - 2f * b);
    }

    /**
     * Temblor de cámara sin mixins: cada tick se suma al giro del jugador la diferencia entre el temblor de ahora
     * y el del tick anterior, así no se acumula ningún desvío (el giro neto es el que tú haces con el ratón).
     */
    private static void applyShake(Minecraft mc) {
        float ty = 0f, tp = 0f;
        if (mc.player != null && mc.screen == null && ClientEclipse.isActiveHere()) {
            float p = ClientEclipse.progress;
            float amp = 0.18f * EclipseSky.smooth(0.85f, 1f, p);
            if (pulse > 0) {
                float k = pulse / 80f;
                amp += 1.3f * k * k;
            }
            if (amp > 0.001f) {
                float t = tickCount;
                ty = amp * (float) (Math.sin(t * 1.9) + 0.6 * Math.sin(t * 4.3));
                tp = amp * (float) (Math.cos(t * 2.3) + 0.5 * Math.sin(t * 5.1));
            }
        }
        if (mc.player != null) {
            mc.player.setYRot(mc.player.getYRot() + (ty - lastYaw));
            mc.player.setXRot(Mth.clamp(mc.player.getXRot() + (tp - lastPitch), -90f, 90f));
        }
        lastYaw = ty;
        lastPitch = tp;
    }
}
