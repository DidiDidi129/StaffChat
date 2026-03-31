package com.staffchat.event;

import com.staffchat.StaffChat;
import com.staffchat.config.StaffChatConfig;
import com.staffchat.discord.DiscordWebhookHandler;
import com.staffchat.permission.PermissionChecker;
import com.staffchat.player.PlayerStateManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Handles chat message interception and routing based on player's chat mode
 */
public class ChatEventListener implements Listener {

    /**
     * Handle async player chat events
     */
    @EventHandler
    public void onAsyncChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        
        // Check if the player is in staff chat mode
        if (PlayerStateManager.getChatMode(player) == PlayerStateManager.PlayerChatMode.STAFF) {
            // Check if player has staff chat permission
            if (!PermissionChecker.hasPermission(player, StaffChatConfig.getPermissionNode())) {
                // Send error and cancel the message
                player.sendMessage(Component.text("You do not have permission to use staff chat", NamedTextColor.RED));
                event.setCancelled(true);
                return;
            }

            // Get the message content as plain text
            String messageContent = PlainTextComponentSerializer.plainText().serialize(event.message());

            // Format the message using legacy ampersand color codes from config
            String prefix = StaffChatConfig.getMessagePrefix();
            Component formattedMessage = LegacyComponentSerializer.legacyAmpersand()
                    .deserialize(prefix + player.getName() + ": " + messageContent);

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






