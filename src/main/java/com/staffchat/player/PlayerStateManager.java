package com.staffchat.player;

import net.minecraft.server.level.ServerPlayer;

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
    public static PlayerChatMode getChatMode(ServerPlayer player) {
        return playerChatModes.getOrDefault(player.getUUID(), PlayerChatMode.NORMAL);
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
    public static void setChatMode(ServerPlayer player, PlayerChatMode mode) {
        playerChatModes.put(player.getUUID(), mode);
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
    public static void removePlayer(ServerPlayer player) {
        playerChatModes.remove(player.getUUID());
    }

    /**
     * Remove a player's chat mode by UUID
     */
    public static void removePlayer(UUID playerUUID) {
        playerChatModes.remove(playerUUID);
    }
}
