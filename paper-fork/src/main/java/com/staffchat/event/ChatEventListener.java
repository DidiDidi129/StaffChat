package com.staffchat.event;

import com.staffchat.StaffChat;
import com.staffchat.config.StaffChatConfig;
import com.staffchat.discord.DiscordWebhookHandler;
import com.staffchat.permission.PermissionChecker;
import com.staffchat.player.PlayerStateManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Handles chat message interception and routing based on player's chat mode
 */
public class ChatEventListener implements Listener {

    /**
     * Handle async player chat events
     */
    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        // Check if the player is in staff chat mode
        if (PlayerStateManager.getChatMode(player) == PlayerStateManager.PlayerChatMode.STAFF) {
            // Check if player has staff chat permission
            if (!PermissionChecker.hasPermission(player, StaffChatConfig.getPermissionNode())) {
                // Send error and cancel the message
                player.sendMessage(ChatColor.RED + "You do not have permission to use staff chat");
                event.setCancelled(true);
                return;
            }

            // Get the message content
            String messageContent = event.getMessage();

            // Format the message
            String prefix = ChatColor.translateAlternateColorCodes('&', StaffChatConfig.getMessagePrefix());
            String formattedMessage = prefix + player.getName() + ": " + messageContent;

            // Send to all players with permission
            for (Player p : StaffChat.getInstance().getServer().getOnlinePlayers()) {
                if (PermissionChecker.hasPermission(p, StaffChatConfig.getPermissionNode())) {
                    p.sendMessage(formattedMessage);
                }
            }

            // Send to Discord webhook if enabled
            DiscordWebhookHandler.sendMessage(player.getName(), messageContent);

            // Cancel the original event so it doesn't go to normal chat
            event.setCancelled(true);
        }
        // If not in staff chat mode, let the message go through normally
    }
}






