package com.unassigned.sshook;

import com.unassigned.sshook.packet.WebhookSyncPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SSHook implements ModInitializer {
	public static final String MOD_ID = "sshook";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Identifier WEBHOOK_SYNC_PACKET_ID = Identifier.of(SSHook.MOD_ID, "webhook_sync");

	@Override
	public void onInitialize() {

		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
			LOGGER.info("[SSHook] Server-side installation detected, loading applicable webhooks...");
			SSHookConfig.loadConfig();
			if(!SSHookConfig.webhooks.isEmpty()) LOGGER.info("[SSHook] Successfully loaded " + SSHookConfig.webhooks.size() + " webhooks!");
			else LOGGER.info("[SSHook] No webhooks were provided, connect clients will have to provide their own webhooks!");
		}

		PayloadTypeRegistry.playS2C().register(WebhookSyncPayload.ID, WebhookSyncPayload.CODEC);

		if(!SSHookConfig.webhooks.isEmpty()) {
			ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
				if (entity instanceof ServerPlayerEntity) {
					ServerPlayerEntity player = (ServerPlayerEntity) entity;

					for (Map.Entry<String, String> entry : SSHookConfig.webhooks.entrySet()) {
						ServerPlayNetworking.send(player, new WebhookSyncPayload(entry.getKey(), entry.getValue()));
					}

					ServerPlayNetworking.send(player, new WebhookSyncPayload("enable_bot_name_override", Boolean.toString(SSHookConfig.enableBotNameOverride)));
					ServerPlayNetworking.send(player, new WebhookSyncPayload("custom_server_name", SSHookConfig.customServerName));
				}
			});
		}

	}
}