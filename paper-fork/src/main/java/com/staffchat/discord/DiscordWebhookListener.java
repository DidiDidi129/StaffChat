package com.staffchat.discord;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.staffchat.StaffChat;
import com.staffchat.config.StaffChatConfig;
import com.staffchat.permission.PermissionChecker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Listens for Discord webhook messages and broadcasts them to staff chat
 */
public class DiscordWebhookListener implements Runnable {
    private static final int DEFAULT_PORT = 3000;
    private Server server;
    private volatile boolean running = false;
    private ServerSocket serverSocket;

    public DiscordWebhookListener(Server server) {
        this.server = server;
    }

    /**
     * Start listening for webhook messages
     */
    public void start() {
        if (running) {
            return;
        }

        try {
            serverSocket = new ServerSocket(DEFAULT_PORT);
            running = true;
            new Thread(this, "DiscordWebhookListener").start();
            StaffChat.LOGGER.info("Discord webhook listener started on port " + DEFAULT_PORT);
        } catch (Exception e) {
            StaffChat.LOGGER.error("Failed to start Discord webhook listener", e);
        }
    }

    /**
     * Stop listening for webhook messages
     */
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (Exception e) {
            StaffChat.LOGGER.error("Error closing webhook listener", e);
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                new Thread(new WebhookHandler(clientSocket, server)).start();
            } catch (Exception e) {
                if (running) {
                    StaffChat.LOGGER.error("Error accepting webhook connection", e);
                }
            }
        }
    }

    /**
     * Handles individual webhook requests
     */
    private static class WebhookHandler implements Runnable {
        private final Socket socket;
        private final Server server;

        public WebhookHandler(Socket socket, Server server) {
            this.socket = socket;
            this.server = server;
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
                );

                StringBuilder requestBody = new StringBuilder();
                String line;
                boolean bodyStarted = false;

                while ((line = reader.readLine()) != null) {
                    if (bodyStarted) {
                        requestBody.append(line);
                    } else if (line.isEmpty()) {
                        bodyStarted = true;
                    }
                }

                // Send HTTP response
                String response = "HTTP/1.1 200 OK\r\nContent-Length: 0\r\n\r\n";
                socket.getOutputStream().write(response.getBytes(StandardCharsets.UTF_8));
                socket.close();

                // Process the Discord message
                if (requestBody.length() > 0) {
                    processDiscordMessage(requestBody.toString());
                }

            } catch (Exception e) {
                StaffChat.LOGGER.debug("Error handling webhook request", e);
            }
        }

        /**
         * Process a Discord webhook message
         */
        private void processDiscordMessage(String json) {
            try {
                JsonElement element = JsonParser.parseString(json);
                if (!element.isJsonObject()) {
                    return;
                }

                JsonObject payload = element.getAsJsonObject();

                // Ignore bot messages and our own messages
                JsonObject author = payload.getAsJsonObject("author");
                if (author != null && author.has("bot") && author.get("bot").getAsBoolean()) {
                    return;
                }

                // Get author name
                String authorName = "Discord";
                if (author != null && author.has("username")) {
                    authorName = author.get("username").getAsString();
                }

                // Get message content
                String content = "";
                if (payload.has("content")) {
                    content = payload.get("content").getAsString();
                }

                if (content.isEmpty()) {
                    return;
                }

                // Broadcast to all staff chat players
                String finalAuthorName = authorName;
                String finalContent = content;
                server.getScheduler().runTask(StaffChat.getInstance(), () -> broadcastToStaffChat(finalAuthorName, finalContent));

            } catch (Exception e) {
                StaffChat.LOGGER.debug("Error processing Discord message", e);
            }
        }

        /**
         * Broadcast Discord message to all players with staff chat permission
         */
        private void broadcastToStaffChat(String authorName, String message) {
            try {
                Component formattedMessage = formatDiscordMessage(authorName, message);

                server.getOnlinePlayers().forEach(player -> {
                    if (PermissionChecker.hasPermission(player, StaffChatConfig.getPermissionNode())) {
                        player.sendMessage(formattedMessage);
                    }
                });

                StaffChat.LOGGER.info("[Discord] " + authorName + ": " + message);

            } catch (Exception e) {
                StaffChat.LOGGER.error("Error broadcasting Discord message to staff chat", e);
            }
        }

        /**
         * Format Discord message for display in-game
         */
        private Component formatDiscordMessage(String authorName, String message) {
            String prefix = StaffChatConfig.getMessagePrefix();
            return LegacyComponentSerializer.legacyAmpersand()
                    .deserialize(prefix + "[Discord] " + authorName + ": " + message);
        }
    }
}
