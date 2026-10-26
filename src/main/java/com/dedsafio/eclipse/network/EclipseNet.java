package com.dedsafio.eclipse.network;

import com.dedsafio.eclipse.eclipse.EclipseData;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class EclipseNet {
    private EclipseNet() {}

    public static void sendToAll(MinecraftServer server, EclipseData d) {
        EclipseSyncPayload payload = new EclipseSyncPayload(d.active, d.progress, d.speed);
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(p, payload);
        }
    }

    public static void sendTo(ServerPlayer player, EclipseData d) {
        ServerPlayNetworking.send(player, new EclipseSyncPayload(d.active, d.progress, d.speed));
    }
}
