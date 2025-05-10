package com.unassigned.sshook.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import static com.unassigned.sshook.SSHook.WEBHOOK_SYNC_PACKET_ID;

public record WebhookSyncPayload(String webhook_id, String webhook_url) implements CustomPayload {

    public static final Id<WebhookSyncPayload> ID = new Id<>(WEBHOOK_SYNC_PACKET_ID);
    public static final PacketCodec<RegistryByteBuf, WebhookSyncPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, WebhookSyncPayload::webhook_id,
            PacketCodecs.STRING, WebhookSyncPayload::webhook_url,
            WebhookSyncPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
