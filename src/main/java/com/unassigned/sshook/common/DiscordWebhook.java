package com.unassigned.sshook.common;

import com.google.gson.Gson;
import com.unassigned.sshook.SSHook;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class DiscordWebhook {
    private static final Pattern URL_PATTERN = Pattern.compile("^https?://.*$");

    public static String createPayload(String playerName, String playerUUID, String server, String world, String coordinates, String direction, String biome, boolean enableNameOverride) {
        Gson gson = new Gson();

        // Create embed fields
        List<Map<String, Object>> fields = new ArrayList<>();
        fields.add(createField("Username", validateString(playerName), true));
        fields.add(createField("Server", validateString(server), true));
        fields.add(createField("World", validateString(world), true));
        fields.add(createField("Coordinates", validateString(coordinates), true));
        fields.add(createField("Direction", validateString(direction), true));
        fields.add(createField("Biome", validateString(biome), true));

        // Create embed
        Map<String, Object> embed = new HashMap<>();
        embed.put("color", 3447003);
        embed.put("fields", fields);

        // Create payload
        Map<String, Object> payload = new HashMap<>();
        if(enableNameOverride) payload.put("username", playerName);
        if(enableNameOverride) payload.put("avatar_url", "https://mc-heads.net/avatar/" + playerUUID + ".png");

        List<Map<String, Object>> embeds = new ArrayList<>();
        embeds.add(embed);
        payload.put("embeds", embeds);

        return gson.toJson(payload);
    }

    public static void sendWebhookWithFile(String webhookUrl, String filePath, String embedJson) throws IOException {
        File file = new File(filePath);
        String boundary = Long.toHexString(System.currentTimeMillis()); // Random boundary
        String CRLF = "\r\n";

        URL url = new URL(webhookUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (
                OutputStream output = connection.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true);
        ) {
            // JSON payload part
            writer.append("--" + boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"payload_json\"").append(CRLF);
            writer.append("Content-Type: application/json; charset=UTF-8").append(CRLF);
            writer.append(CRLF).append(embedJson).append(CRLF).flush();

            // File part
            writer.append("--" + boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"").append(CRLF);
            writer.append("Content-Type: image/jpeg").append(CRLF); // Adjust MIME type as needed
            writer.append(CRLF).flush();
            Files.copy(file.toPath(), output);
            output.flush();
            writer.append(CRLF).flush();

            // End boundary
            writer.append("--" + boundary + "--").append(CRLF).flush();
        }

        int responseCode = connection.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            SSHook.LOGGER.info("Webhook sent successfully with file.");
        } else {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                SSHook.LOGGER.error("Failed to send webhook. Response code: " + responseCode);
                SSHook.LOGGER.error("Response: " + response.toString());
            }
        }
    }

    private static Map<String, Object> createField(String name, String value, boolean inline) {
        Map<String, Object> field = new HashMap<>();
        field.put("name", name);
        field.put("value", value);
        field.put("inline", inline);
        return field;
    }

    private static String validateString(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "N/A";
        }
        // Truncate to Discord's limits (1024 for value, 256 for name)
        return input.length() > 1024 ? input.substring(0, 1024) : input;
    }

    private static boolean isValidUrl(String url) {
        return url != null && !url.trim().isEmpty() && URL_PATTERN.matcher(url).matches();
    }
}
