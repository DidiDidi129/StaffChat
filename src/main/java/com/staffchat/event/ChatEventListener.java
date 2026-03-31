package com.staffchat.event;

import com.staffchat.Staffchat;
import com.staffchat.config.StaffChatConfig;
import com.staffchat.discord.DiscordWebhookHandler;
import com.staffchat.permission.PermissionChecker;
import com.staffchat.player.PlayerStateManager;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

/**
 * Handles chat message interception and routing based on player's chat mode
 */
public class ChatEventListener {
    private static MinecraftServer cachedServer;

    /**
     * Set the server reference (called during initialization)
     */
    public static void setServer(MinecraftServer server) {
        cachedServer = server;
    }

    /**
     * Register chat event listeners
     */
    public static void register() {
        // Listen to chat messages before they're broadcast and optionally block them
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            // Check if the player is in staff chat mode
            if (PlayerStateManager.getChatMode(sender) == PlayerStateManager.PlayerChatMode.STAFF) {
                // Check if player has staff chat permission
                if (!PermissionChecker.hasPermission(sender, StaffChatConfig.getPermissionNode())) {
                    // Send error and cancel the message
                    sender.sendMessage(Text.literal("§cYou do not have permission to use staff chat"), false);
                    // Return false to block the original message from being broadcast to normal chat
                    return false;
                }

                // Get the message content from the Text object
                String messageContent = message.getContent().getString();

                // Format the message
                String prefix = StaffChatConfig.getMessagePrefix().replace("&", "§");
                String formattedMessage = prefix + sender.getName().getString() + ": " + messageContent;

                // Use cached server reference
                MinecraftServer server = cachedServer;
                if (server != null) {
                    // Send to all players with permission
                    server.getPlayerManager().getPlayerList().forEach(p -> {
                        if (PermissionChecker.hasPermission(p, StaffChatConfig.getPermissionNode())) {
                            p.sendMessage(Text.literal(formattedMessage), false);
                        }
                    });

                    // Send to Discord webhook if enabled
                    DiscordWebhookHandler.sendMessage(sender.getName().getString(), messageContent);
                } else {
                    Staffchat.LOGGER.warn("Server reference not initialized for chat event listener");
                }

                // Return false to block the original message (don't let it go to normal chat)
                return false;
            }

            // Return true to let normal messages through
            return true;
        });
    }
}






