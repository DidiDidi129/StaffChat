package com.staffchat;

import com.staffchat.command.StaffChatCommands;
import com.staffchat.config.StaffChatConfig;
import com.staffchat.discord.DiscordBotClient;
import com.staffchat.discord.DiscordWebhookHandler;
import com.staffchat.discord.DiscordWebhookListener;
import com.staffchat.event.ChatEventListener;
import com.staffchat.permission.PermissionChecker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.luckperms.api.LuckPermsProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Staffchat implements ModInitializer {
	public static final String MOD_ID = "staffchat";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static DiscordWebhookListener discordListener;
	private static DiscordBotClient discordBot;
	private static MinecraftServer server;

	public static MinecraftServer getServer() {
		return server;
	}

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Initializing StaffChat mod...");

		// Load configuration
		StaffChatConfig.loadConfig();

		// Register chat event listener for mode-based chat routing
		ChatEventListener.register();

		// Initialize LuckPerms integration when server starts
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			Staffchat.server = server;
			// Set server reference for chat event listener
			ChatEventListener.setServer(server);

			try {
				var luckPerms = LuckPermsProvider.get();
				PermissionChecker.init(luckPerms);
				LOGGER.info("LuckPerms integration initialized successfully");
			} catch (IllegalStateException e) {
				LOGGER.warn("LuckPerms not found on the server. StaffChat will not work without LuckPerms!");
			}

			// Initialize Discord bot if enabled
			if (StaffChatConfig.isDiscordBotEnabled()) {
				discordBot = new DiscordBotClient();
				DiscordWebhookHandler.setBotClient(discordBot);
				discordBot.start();
				LOGGER.info("Discord bot initialized");
			} else {
				LOGGER.info("Discord bot not enabled or not configured; skipping bot initialization");
			}

			// Start Discord webhook listener if enabled
			if (StaffChatConfig.isDiscordWebhookEnabled()) {
				discordListener = new DiscordWebhookListener(server);
				discordListener.start();
				LOGGER.info("Discord webhook listener initialized");
			}
		});

		// Stop Discord services when server stops
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			Staffchat.server = null;
			if (discordBot != null) {
				discordBot.stop();
				discordBot = null;
			}
			if (discordListener != null) {
				discordListener.stop();
				discordListener = null;
				LOGGER.info("Discord services stopped");
			}
		});

		// Register commands
		StaffChatCommands.register();

		LOGGER.info("StaffChat mod initialized successfully!");
	}
}