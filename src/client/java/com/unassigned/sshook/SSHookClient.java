package com.unassigned.sshook;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.unassigned.sshook.common.DiscordWebhook;
import com.unassigned.sshook.packet.WebhookSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class SSHookClient implements ClientModInitializer {

	// webhooks gathered from the server once the player joined.
	public static Map<String, String> SERVER_WEBHOOKS = new HashMap<>();
	public static boolean SERVER_BOT_NAME_OVERRIDE = false;
	public static String SERVER_CUSTOM_NAME = "";

	// used in command to get screenshot file
	public static File LAST_SCREENSHOT_FILE;

	@Override
	public void onInitializeClient() {
		SSHookClientConfig.initClientConfig();

		ClientPlayNetworking.registerGlobalReceiver(WebhookSyncPayload.ID, ((payload, context) -> {
			context.client().execute(() -> {
				if(Objects.equals(payload.webhook_id(), "enable_bot_name_override")) SERVER_BOT_NAME_OVERRIDE = Boolean.parseBoolean(payload.webhook_url());
				else if(Objects.equals(payload.webhook_id(), "custom_server_name")) SERVER_CUSTOM_NAME = payload.webhook_url();
				else
				{
					SSHook.LOGGER.info("[SSHook] Received webhook (" + payload.webhook_id() + ") from server!");
					SERVER_WEBHOOKS.put(payload.webhook_id(), payload.webhook_url());
				}
			});
		}));

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(
					ClientCommandManager.literal("upload_screenshot")
							.then(ClientCommandManager.argument("webhookName", StringArgumentType.word())
									.executes(ctx -> {
										String name = StringArgumentType.getString(ctx, "webhookName");
										ClientPlayerEntity player = MinecraftClient.getInstance().player;

										Map<String, String> webhooks = SSHookClientConfig.INSTANCE.useServerOverride
												? SSHookClient.SERVER_WEBHOOKS
												: SSHookClientConfig.INSTANCE.clientWebhooks;

										if (!webhooks.containsKey(name)) {
											player.sendMessage(Text.literal("Webhook \"" + name + "\" not found.").formatted(Formatting.RED), false);
											return 0;
										}

										// file2 must be stored somewhere persistently for the command to work after the hook
										File lastScreenshotFile = SSHookClient.LAST_SCREENSHOT_FILE;
										if (lastScreenshotFile == null || !lastScreenshotFile.exists()) {
											player.sendMessage(Text.literal("No screenshot available to upload.").formatted(Formatting.RED), false);
											return 0;
										}

										handleDiscordWebhook(MinecraftClient.getInstance(), player, lastScreenshotFile, name, webhooks.get(name));
										return 1;
									})
							)
			);
		});

	}

	public static void handleDiscordWebhook(MinecraftClient instance, ClientPlayerEntity player, File file, String webhookName, String webhookUrl) {
		if (player == null || webhookUrl == null) return;

		String playerName = player.getName().getString();
		String playerUUID = player.getUuidAsString();
		Vec3d pos = player.getPos();
		String coordinates = String.format("%.2f, %.2f, %.2f", pos.x, pos.y, pos.z);
		float yaw = player.getYaw();
		String direction = getFacingDirection(yaw);
		String biome = player.getWorld()
				.getBiome(player.getBlockPos())
				.getKey()
				.map(key -> key.getValue().getPath())
				.orElse("unknown")
				.replace("_", " ")
				.toLowerCase(Locale.ROOT);
		String dimension = player.getWorld().getRegistryKey().getValue().getPath().replace("_", " ");
		String serverAddress = (instance.getCurrentServerEntry() == null) ? "N/A" : instance.getCurrentServerEntry().address;
		if(!SERVER_CUSTOM_NAME.isEmpty()) serverAddress = SERVER_CUSTOM_NAME;

		String payloadJson = DiscordWebhook.createPayload(
				playerName,
				playerUUID,
				serverAddress,
				dimension,
				coordinates,
				direction,
				biome,
				SSHookClientConfig.INSTANCE.useServerOverride ? SSHookClient.SERVER_BOT_NAME_OVERRIDE : SSHookClientConfig.INSTANCE.enableBotNameOverride
		);

		new Thread(() -> {
			try {
				DiscordWebhook.sendWebhookWithFile(webhookUrl, file.getAbsolutePath(), payloadJson);
				MinecraftClient.getInstance().execute(() -> {
					player.sendMessage(Text.literal("Screenshot uploaded to webhook: " + webhookName).formatted(Formatting.GREEN), false);
				});
			} catch (IOException e) {
				SSHook.LOGGER.error("Error sending webhook", e);
				MinecraftClient.getInstance().execute(() -> {
					player.sendMessage(Text.literal("Failed to upload screenshot.").formatted(Formatting.RED), false);
				});
			}
		}).start();
	}

	private static String getFacingDirection(float yaw) {
		// Normalize yaw to 0-360 degrees
		float normalizedYaw = yaw % 360;
		if (normalizedYaw < 0) {
			normalizedYaw += 360;
		}
		// Determine direction based on yaw
		if (normalizedYaw >= 337.5 || normalizedYaw < 22.5) {
			return "South";
		} else if (normalizedYaw >= 22.5 && normalizedYaw < 67.5) {
			return "South-West";
		} else if (normalizedYaw >= 67.5 && normalizedYaw < 112.5) {
			return "West";
		} else if (normalizedYaw >= 112.5 && normalizedYaw < 157.5) {
			return "North-West";
		} else if (normalizedYaw >= 157.5 && normalizedYaw < 202.5) {
			return "North";
		} else if (normalizedYaw >= 202.5 && normalizedYaw < 247.5) {
			return "North-East";
		} else if (normalizedYaw >= 247.5 && normalizedYaw < 292.5) {
			return "East";
		} else {
			return "South-East";
		}
	}

}