package com.staffchat.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.staffchat.config.StaffChatConfig;
import com.staffchat.discord.DiscordWebhookHandler;
import com.staffchat.permission.PermissionChecker;
import com.staffchat.player.PlayerStateManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Command registration for StaffChat
 */
public class StaffChatCommands {

    /**
     * Register staff chat commands
     */
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // Register /staffchat command with subcommands
            dispatcher.register(
                    literal("staffchat")
                            .then(argument("message", StringArgumentType.greedyString())
                                    .executes(context -> handleStaffChat(context.getSource(), StringArgumentType.getString(context, "message"))))
                            .then(literal("reload")
                                    .executes(context -> handleReload(context.getSource())))
            );

            // Register /sc alias
            dispatcher.register(
                    literal("sc")
                            .then(argument("message", StringArgumentType.greedyString())
                                    .executes(context -> handleStaffChat(context.getSource(), StringArgumentType.getString(context, "message"))))
            );

            // Register /chat command with subcommands
            dispatcher.register(
                    literal("chat")
                            .then(literal("normal")
                                    .executes(context -> handleChatMode(context.getSource(), PlayerStateManager.PlayerChatMode.NORMAL)))
                            .then(literal("staff")
                                    .executes(context -> handleChatMode(context.getSource(), PlayerStateManager.PlayerChatMode.STAFF)))
            );
        });
    }

    /**
     * Handle staff chat command execution
     */
    private static int handleStaffChat(ServerCommandSource source, String message) {
        // Get the server
        var server = source.getServer();
        if (server == null) {
            source.sendError(Text.literal("Server not found"));
            return 0;
        }

        if (!source.isExecutedByPlayer()) {
            String formattedMessage = "§9[Staff] §r§c[Console]§r " + message;

            server.getPlayerManager().getPlayerList().forEach(p -> {
                if (PermissionChecker.hasPermission(p, StaffChatConfig.getPermissionNode())) {
                    p.sendMessage(Text.literal(formattedMessage), false);
                }
            });

            DiscordWebhookHandler.sendMessage("Console", message);
            return Command.SINGLE_SUCCESS;
        }

        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("Player not found"));
            return 0;
        }

        // Check permission
        if (!PermissionChecker.hasPermission(player, StaffChatConfig.getPermissionNode())) {
            source.sendError(Text.literal("§cYou do not have permission to use staff chat"));
            return 0;
        }

        // Format and send the message
        String formattedMessage = formatMessage(player.getName().getString(), message);

        // Send message to all players with permission
        server.getPlayerManager().getPlayerList().forEach(p -> {
            if (PermissionChecker.hasPermission(p, StaffChatConfig.getPermissionNode())) {
                p.sendMessage(Text.literal(formattedMessage), false);
            }
        });

        // Send to Discord webhook if enabled
        DiscordWebhookHandler.sendMessage(player.getName().getString(), message);

        return Command.SINGLE_SUCCESS;
    }

    /**
     * Handle reload command
     */
    private static int handleReload(ServerCommandSource source) {
        // Check if the source is from a player
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be executed by players"));
            return 0;
        }

        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("Player not found"));
            return 0;
        }

        // Check permission (require staff chat permission to reload config)
        if (!PermissionChecker.hasPermission(player, StaffChatConfig.getPermissionNode())) {
            source.sendError(Text.literal("§cYou do not have permission to reload the config"));
            return 0;
        }

        try {
            StaffChatConfig.loadConfig();
            player.sendMessage(Text.literal("§a[Staff] Config reloaded successfully!"), false);

            // Notify all staff about the reload
            var server = source.getServer();
            if (server != null) {
                server.getPlayerManager().getPlayerList().forEach(p -> {
                    if (PermissionChecker.hasPermission(p, StaffChatConfig.getPermissionNode())) {
                        p.sendMessage(Text.literal("§9[Staff] §rConfig was reloaded by " + player.getName().getString()), false);
                    }
                });
            }

            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendError(Text.literal("§cFailed to reload config: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * Handle chat mode switching
     */
    private static int handleChatMode(ServerCommandSource source, PlayerStateManager.PlayerChatMode mode) {
        // Check if the source is from a player
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command can only be executed by players"));
            return 0;
        }

        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("Player not found"));
            return 0;
        }

        // Set the player's chat mode
        PlayerStateManager.setChatMode(player, mode);

        if (mode == PlayerStateManager.PlayerChatMode.STAFF) {
            // Check if player has staff chat permission first
            if (!PermissionChecker.hasPermission(player, StaffChatConfig.getPermissionNode())) {
                PlayerStateManager.setChatMode(player, PlayerStateManager.PlayerChatMode.NORMAL);
                source.sendError(Text.literal("§cYou do not have permission to use staff chat"));
                return 0;
            }
            player.sendMessage(Text.literal("§9§lYou are now speaking in Staff chat. §r§9To switch back to normal, do §l/chat normal"), false);
        } else {
            player.sendMessage(Text.literal("§b§lYou are now speaking in Normal chat. §r§bTo switch to staff chat, do §l/chat staff"), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    /**
     * Format staff chat message
     */
    private static String formatMessage(String playerName, String message) {
        // Convert & color codes to § codes
        String prefix = StaffChatConfig.getMessagePrefix().replace("&", "§");
        return prefix + playerName + ": " + message;
    }
}
