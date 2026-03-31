package com.staffchat.player;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player-specific state like chat mode preference
 */
public class PlayerStateManager {
    private static final Map<UUID, PlayerChatMode> playerChatModes = new HashMap<>();

    public enum PlayerChatMode {
        NORMAL("normal"),
        STAFF("staff");

        private final String displayName;

        PlayerChatMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Get a player's current chat mode
     */
    public static PlayerChatMode getChatMode(Player player) {
        return playerChatModes.getOrDefault(player.getUniqueId(), PlayerChatMode.NORMAL);
    }

    /**
     * Get a player's current chat mode by UUID
     */
    public static PlayerChatMode getChatMode(UUID playerUUID) {
        return playerChatModes.getOrDefault(playerUUID, PlayerChatMode.NORMAL);
    }

    /**
     * Set a player's chat mode
     */
    public static void setChatMode(Player player, PlayerChatMode mode) {
        playerChatModes.put(player.getUniqueId(), mode);
    }

    /**
     * Set a player's chat mode by UUID
     */
    public static void setChatMode(UUID playerUUID, PlayerChatMode mode) {
        playerChatModes.put(playerUUID, mode);
    }

    /**
     * Remove a player's chat mode (when they disconnect)
     */
    public static void removePlayer(Player player) {
        playerChatModes.remove(player.getUniqueId());
    }

    /**
     * Remove a player's chat mode by UUID
     */
    public static void removePlayer(UUID playerUUID) {
        playerChatModes.remove(playerUUID);
    }
}
