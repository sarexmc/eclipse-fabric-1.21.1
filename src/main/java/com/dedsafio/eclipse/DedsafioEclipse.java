package com.dedsafio.eclipse;

import com.dedsafio.eclipse.command.EclipseCommand;
import com.dedsafio.eclipse.eclipse.EclipseServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import com.dedsafio.eclipse.network.EclipseSyncPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class DedsafioEclipse implements ModInitializer {
    public static final String MOD_ID = "dedsafio_eclipse";

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(EclipseSyncPayload.TYPE, EclipseSyncPayload.CODEC);

        ModItems.register();
        ModEntities.register();
        ModCreativeTabs.register();

        ServerTickEvents.END_SERVER_TICK.register(EclipseServer::tick);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> EclipseServer.onJoin(handler.player));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                EclipseCommand.register(dispatcher));
    }
}
