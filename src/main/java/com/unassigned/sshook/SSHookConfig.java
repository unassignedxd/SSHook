package com.unassigned.sshook;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class SSHookConfig {
    public static Map<String, String> webhooks = new HashMap<>();
    public static boolean enableBotNameOverride = true;
    public static String customServerName = "";

    public static void loadConfig() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("sshook-server.json");

        if (!Files.exists(configPath)) {
            try {
                Map<String, Object> defaultConfig = new HashMap<>();
                defaultConfig.put("customServerName", "play.example.com");
                defaultConfig.put("webhooks", Map.of("ExampleWebhook", "https://your.webhook/here"));
                defaultConfig.put("enableBotNameOverride", true);

                String json = new GsonBuilder().setPrettyPrinting().create().toJson(defaultConfig);
                Files.writeString(configPath, json);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (Reader reader = Files.newBufferedReader(configPath)) {
            JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();

            JsonObject webhooksJson = obj.getAsJsonObject("webhooks");
            for (Map.Entry<String, JsonElement> entry : webhooksJson.entrySet()) {
                webhooks.put(entry.getKey(), entry.getValue().getAsString());
            }

            if (obj.has("enableBotNameOverride")) {
                enableBotNameOverride = obj.get("enableBotNameOverride").getAsBoolean();
            }

            if(obj.has("customServerName")) customServerName = obj.get("customServerName").getAsString();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
