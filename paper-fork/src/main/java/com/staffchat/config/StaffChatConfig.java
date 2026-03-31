package com.staffchat.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.staffchat.StaffChat;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration manager for StaffChat mod
 */
public class StaffChatConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = Paths.get("config", "staffchat");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.json");

    public static class Config {
        public String permissionNode = "staffchat.use";
        public boolean enableDiscordWebhook = false;
        public String discordWebhookUrl = "";
        public String discordWebhookUsername = "Staff Chat";
        public String discordWebhookAvatarUrl = "";
        public String messagePrefix = "&9[Staff] &r";
        public int discordListenerPort = 3000;
        public String discordMessageFormat = "{user} >> {message}";
        // Discord bot settings
        public boolean enableDiscordBot = false;
        public String discordBotToken = "";
        public String discordStaffChannelId = "";

        public Config() {}
    }

    private static Config config;

    /**
     * Load or create the configuration
     */
    public static void loadConfig() {
        try {
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }

            if (!Files.exists(CONFIG_FILE)) {
                createDefaultConfig();
            } else {
                loadExistingConfig();
            }

            StaffChat.LOGGER.info("StaffChat configuration loaded successfully");
        } catch (IOException e) {
            StaffChat.LOGGER.error("Failed to load StaffChat configuration", e);
            config = new Config();
        }
    }

    /**
     * Create default configuration file
     */
    private static void createDefaultConfig() throws IOException {
        config = new Config();
        Files.createDirectories(CONFIG_DIR);

        try (FileWriter writer = new FileWriter(CONFIG_FILE.toFile())) {
            GSON.toJson(config, writer);
        }

        StaffChat.LOGGER.info("Created default StaffChat configuration at " + CONFIG_FILE.toAbsolutePath());
    }

    /**
     * Load existing configuration file
     */
    private static void loadExistingConfig() throws IOException {
        try (FileReader reader = new FileReader(CONFIG_FILE.toFile())) {
            config = GSON.fromJson(reader, Config.class);
            if (config == null) {
                config = new Config();
            }
        }
    }

    /**
     * Save the current configuration
     */
    public static void saveConfig() {
        try {
            Files.createDirectories(CONFIG_DIR);
            try (FileWriter writer = new FileWriter(CONFIG_FILE.toFile())) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            StaffChat.LOGGER.error("Failed to save StaffChat configuration", e);
        }
    }

    /**
     * Get the permission node required to use staff chat
     */
    public static String getPermissionNode() {
        return config != null ? config.permissionNode : "staffchat.use";
    }

    /**
     * Check if Discord webhook is enabled
     */
    public static boolean isDiscordWebhookEnabled() {
        return config != null && config.enableDiscordWebhook && !config.discordWebhookUrl.isEmpty();
    }

    /**
     * Get the Discord webhook URL
     */
    public static String getDiscordWebhookUrl() {
        return config != null ? config.discordWebhookUrl : "";
    }

    /**
     * Get the Discord webhook username
     */
    public static String getDiscordWebhookUsername() {
        return config != null ? config.discordWebhookUsername : "Staff Chat";
    }

    /**
     * Get the Discord webhook avatar URL
     */
    public static String getDiscordWebhookAvatarUrl() {
        return config != null ? config.discordWebhookAvatarUrl : "";
    }

    /**
     * Get the Discord listener port
     */
    public static int getDiscordListenerPort() {
        return config != null ? config.discordListenerPort : 3000;
    }

    /**
     * Get the Discord message format
     */
    public static String getDiscordMessageFormat() {
        return config != null ? config.discordMessageFormat : "{user} >> {message}";
    }

    /**
     * Get the message prefix
     */
    public static String getMessagePrefix() {
        return config != null ? config.messagePrefix : "&9[StaffChat] &r";
    }

    /**
     * Update permission node
     */
    public static void setPermissionNode(String permissionNode) {
        if (config != null) {
            config.permissionNode = permissionNode;
            saveConfig();
        }
    }

    /**
     * Update Discord webhook settings
     */
    public static void setDiscordWebhook(boolean enabled, String url, String username, String avatarUrl) {
        if (config != null) {
            config.enableDiscordWebhook = enabled;
            config.discordWebhookUrl = url;
            config.discordWebhookUsername = username;
            config.discordWebhookAvatarUrl = avatarUrl;
            saveConfig();
        }
    }

    /**
     * Check if Discord bot is enabled
     */
    public static boolean isDiscordBotEnabled() {
        return config != null && config.enableDiscordBot && !config.discordBotToken.isEmpty() && !config.discordStaffChannelId.isEmpty();
    }

    /**
     * Get the Discord bot token
     */
    public static String getDiscordBotToken() {
        return config != null ? config.discordBotToken : "";
    }

    /**
     * Get the Discord staff channel ID
     */
    public static String getDiscordStaffChannelId() {
        return config != null ? config.discordStaffChannelId : "";
    }
}
