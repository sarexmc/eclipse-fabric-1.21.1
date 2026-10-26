package com.dedsafio.eclipse.client;

import com.dedsafio.eclipse.DedsafioEclipse;
import com.dedsafio.eclipse.ModEntities;
import com.dedsafio.eclipse.ModItems;
import com.dedsafio.eclipse.item.LinternaItem;
import com.dedsafio.eclipse.network.EclipseSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

public class DedsafioEclipseClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // el manejador ya corre en el hilo del cliente
        ClientPlayNetworking.registerGlobalReceiver(EclipseSyncPayload.TYPE, (payload, context) ->
                ClientEclipse.apply(payload.active(), payload.progress(), payload.speed()));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientEclipse.tick();
            CinematicFx.tick();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ClientEclipse.reset();
            CinematicFx.reset();
        });

        EntityRendererRegistry.register(ModEntities.HEROBRINE, HerobrineRenderer::new);

        ItemProperties.register(ModItems.LINTERNA, ResourceLocation.fromNamespaceAndPath(DedsafioEclipse.MOD_ID, "on"),
                (stack, level, entity, seed) -> LinternaItem.isOn(stack) ? 1.0F : 0.0F);

        WorldRenderEvents.LAST.register(EclipseWorldRenderer::render);

        HudRenderCallback.EVENT.register((graphics, tickCounter) -> {
            float delta = tickCounter.getGameTimeDeltaPartialTick(false);
            LinternaArranqueOverlay.render(graphics, delta);
            CinematicHud.render(graphics, delta);
        });
    }
}
