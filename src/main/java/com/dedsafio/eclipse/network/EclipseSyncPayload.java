package com.dedsafio.eclipse.network;

import com.dedsafio.eclipse.DedsafioEclipse;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Servidor -> cliente: estado del eclipse (activo, progreso 0..1, velocidad por tick). */
public record EclipseSyncPayload(boolean active, float progress, float speed) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<EclipseSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(DedsafioEclipse.MOD_ID, "sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EclipseSyncPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, EclipseSyncPayload::active,
            ByteBufCodecs.FLOAT, EclipseSyncPayload::progress,
            ByteBufCodecs.FLOAT, EclipseSyncPayload::speed,
            EclipseSyncPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
