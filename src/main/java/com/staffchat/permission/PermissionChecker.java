package com.staffchat.permission;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Permission checker using LuckPerms API
 */
public class PermissionChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger("staffchat");
    private static LuckPerms luckPerms;

    /**
     * Initialize the permission checker with LuckPerms
     */
    public static void init(LuckPerms api) {
        luckPerms = api;
        LOGGER.info("StaffChat permission checker initialized with LuckPerms");
    }

    /**
     * Check if a player has a specific permission
     */
    public static boolean hasPermission(ServerPlayerEntity player, String permissionNode) {
        if (luckPerms == null) {
            LOGGER.warn("LuckPerms not initialized, denying permission");
            return false;
        }

        try {
            UUID playerUUID = player.getUuid();
            User user = luckPerms.getUserManager().loadUser(playerUUID).join();

            if (user == null) {
                LOGGER.warn("Could not load user data for " + player.getName().getString());
                return false;
            }

            return user.getCachedData().getPermissionData()
                    .checkPermission(permissionNode)
                    .asBoolean();

        } catch (Exception e) {
            LOGGER.error("Error checking permission for player " + player.getName().getString(), e);
            return false;
        }
    }

    /**
     * Get the LuckPerms instance
     */
    public static LuckPerms getLuckPerms() {
        return luckPerms;
    }

    /**
     * Check if LuckPerms is available
     */
    public static boolean isAvailable() {
        return luckPerms != null;
    }
}
