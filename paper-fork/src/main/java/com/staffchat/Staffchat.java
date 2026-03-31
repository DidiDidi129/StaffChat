package com.staffchat;

import com.staffchat.command.StaffChatCommands;
import com.staffchat.config.StaffChatConfig;
import com.staffchat.discord.DiscordBotClient;
import com.staffchat.discord.DiscordWebhookHandler;
import com.staffchat.discord.DiscordWebhookListener;
import com.staffchat.event.ChatEventListener;
import com.staffchat.permission.PermissionChecker;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StaffChat extends JavaPlugin {
	public static final String MOD_ID = "staffchat";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static DiscordWebhookListener discordListener;
	private static DiscordBotClient discordBot;
	private static StaffChat instance;

	public static StaffChat getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {
		// This code runs as soon as the server starts and enables the plugin.
		instance = this;

		LOGGER.info("Initializing StaffChat plugin...");

		// Load configuration
		StaffChatConfig.loadConfig();

		// Register chat event listener for mode-based chat routing
		getServer().getPluginManager().registerEvents(new ChatEventListener(), this);

		// Initialize LuckPerms integration
		try {
			LuckPerms luckPerms = LuckPermsProvider.get();
			PermissionChecker.init(luckPerms);
			LOGGER.info("LuckPerms integration initialized successfully");
		} catch (IllegalStateException e) {
			LOGGER.warn("LuckPerms not found on the server. StaffChat will not work without LuckPerms!");
			getServer().getPluginManager().disablePlugin(this);
			return;
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
			discordListener = new DiscordWebhookListener(getServer());
			discordListener.start();
			LOGGER.info("Discord webhook listener initialized");
		}

		// Register commands
		StaffChatCommands.register(this);

		LOGGER.info("StaffChat plugin initialized successfully!");
	}

	@Override
	public void onDisable() {
		// This code runs when the server stops and disables the plugin.
		instance = null;
		if (discordBot != null) {
			discordBot.stop();
			discordBot = null;
		}
		if (discordListener != null) {
			discordListener.stop();
			discordListener = null;
		}
		LOGGER.info("Discord services stopped");
	}
}