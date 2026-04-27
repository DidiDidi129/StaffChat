package com.staffchat.discord;

import com.google.gson.JsonObject;
import com.staffchat.Staffchat;
import com.staffchat.config.StaffChatConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Handler for sending messages to Discord via webhooks or bot
 */
public class DiscordWebhookHandler {
    private static DiscordBotClient botClient;

    /**
     * Set the Discord bot client instance
     */
    public static void setBotClient(DiscordBotClient client) {
        botClient = client;
    }

    /**
     * Send a staff chat message to Discord
     */
    public static void sendMessage(String playerName, String message) {
        // Prefer bot if enabled
        if (StaffChatConfig.isDiscordBotEnabled() && botClient != null) {
            botClient.sendMessage(playerName, message);
            Staffchat.LOGGER.debug("Sent message via Discord bot from: " + playerName);
            return;
        }

        // Fall back to webhook if bot is not enabled
        if (!StaffChatConfig.isDiscordWebhookEnabled()) {
            Staffchat.LOGGER.debug("Discord bot and webhook both disabled; message not sent");
            return;
        }

        Staffchat.LOGGER.debug("Sending message via Discord webhook from: " + playerName);
        // Send asynchronously to avoid blocking the server thread
        new Thread(() -> sendMessageAsync(playerName, message)).start();
    }

    /**
     * Send message to Discord asynchronously
     */
    private static void sendMessageAsync(String playerName, String message) {
        try {
            String webhookUrl = StaffChatConfig.getDiscordWebhookUrl();

            if (webhookUrl == null || webhookUrl.isEmpty()) {
                Staffchat.LOGGER.warn("Discord webhook URL is not configured");
                return;
            }

            URL url = URI.create(webhookUrl).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            // Format the message using the configured format
            String formattedContent = formatDiscordMessage(playerName, message);

            // Create simple text message instead of embed
            JsonObject payload = new JsonObject();
            payload.addProperty("content", formattedContent);

            String jsonPayload = payload.toString();
            Staffchat.LOGGER.debug("Sending to Discord webhook: " + jsonPayload);

            // Send the webhook
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
                os.flush();
            }

            // Check response
            int responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();

            if (responseCode >= 200 && responseCode < 300) {
                Staffchat.LOGGER.info("Successfully sent message to Discord webhook (HTTP " + responseCode + ")");
            } else {
                Staffchat.LOGGER.warn("Discord webhook returned status code: " + responseCode + " " + responseMessage);
            }

            connection.disconnect();

        } catch (IOException e) {
            Staffchat.LOGGER.error("Failed to send message to Discord webhook: " + e.getMessage(), e);
        } catch (Exception e) {
            Staffchat.LOGGER.error("Unexpected error sending to Discord webhook", e);
        }
    }

    /**
     * Format message according to Discord message format config
     */
    private static String formatDiscordMessage(String playerName, String message) {
        String format = StaffChatConfig.getDiscordMessageFormat();
        return format.replace("{user}", playerName).replace("{message}", message);
    }
}
