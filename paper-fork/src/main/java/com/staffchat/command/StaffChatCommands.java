package com.staffchat.command;

import com.staffchat.config.StaffChatConfig;
import com.staffchat.discord.DiscordWebhookHandler;
import com.staffchat.permission.PermissionChecker;
import com.staffchat.player.PlayerStateManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command handlers for StaffChat
 */
public class StaffChatCommands implements CommandExecutor, TabCompleter {
    
    private final JavaPlugin plugin;
    
    public StaffChatCommands(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register staff chat commands
     */
    public static void register(JavaPlugin plugin) {
        StaffChatCommands commandHandler = new StaffChatCommands(plugin);
        
        plugin.getCommand("staffchat").setExecutor(commandHandler);
        plugin.getCommand("sc").setExecutor(commandHandler);
        plugin.getCommand("chat").setExecutor(commandHandler);
        
        plugin.getCommand("staffchat").setTabCompleter(commandHandler);
        plugin.getCommand("chat").setTabCompleter(commandHandler);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName().toLowerCase()) {
            case "staffchat":
            case "sc":
                return handleStaffChat(sender, args);
            case "chat":
                return handleChat(sender, args);
            default:
                return false;
        }
    }

    /**
     * Handle staff chat command execution
     */
    private boolean handleStaffChat(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /" + (sender instanceof Player ? "staffchat" : "sc") + " <message>", NamedTextColor.RED));
            return true;
        }

        String message = String.join(" ", args);

        if (!(sender instanceof Player)) {
            Component formattedMessage = Component.text("[Staff] ", NamedTextColor.DARK_AQUA)
                    .append(Component.text("[Console]", NamedTextColor.RED))
                    .append(Component.text(" " + message));

            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (PermissionChecker.hasPermission(player, StaffChatConfig.getPermissionNode())) {
                    player.sendMessage(formattedMessage);
                }
            }

            DiscordWebhookHandler.sendMessage("Console", message);
            return true;
        }

        Player player = (Player) sender;

        // Check permission
        if (!PermissionChecker.hasPermission(player, StaffChatConfig.getPermissionNode())) {
            player.sendMessage(Component.text("You do not have permission to use staff chat", NamedTextColor.RED));
            return true;
        }

        // Format and send the message
        Component formattedMessage = formatMessage(player.getName(), message);

        // Send message to all players with permission
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (PermissionChecker.hasPermission(p, StaffChatConfig.getPermissionNode())) {
                p.sendMessage(formattedMessage);
            }
        }

        // Send to Discord webhook if enabled
        DiscordWebhookHandler.sendMessage(player.getName(), message);

        return true;
    }

    /**
     * Handle chat command
     */
    private boolean handleChat(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("This command can only be executed by players", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /chat <normal|staff>", NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;
        String mode = args[0].toLowerCase();

        switch (mode) {
            case "normal":
                return handleChatMode(player, PlayerStateManager.PlayerChatMode.NORMAL);
            case "staff":
                return handleChatMode(player, PlayerStateManager.PlayerChatMode.STAFF);
            default:
                player.sendMessage(Component.text("Invalid chat mode. Use 'normal' or 'staff'", NamedTextColor.RED));
                return true;
        }
    }

    /**
     * Handle chat mode switching
     */
    private boolean handleChatMode(Player player, PlayerStateManager.PlayerChatMode mode) {
        // Set the player's chat mode
        PlayerStateManager.setChatMode(player, mode);

        if (mode == PlayerStateManager.PlayerChatMode.STAFF) {
            // Check if player has staff chat permission first
            if (!PermissionChecker.hasPermission(player, StaffChatConfig.getPermissionNode())) {
                PlayerStateManager.setChatMode(player, PlayerStateManager.PlayerChatMode.NORMAL);
                player.sendMessage(Component.text("You do not have permission to use staff chat", NamedTextColor.RED));
                return true;
            }
            player.sendMessage(Component.text("You are now speaking in Staff chat. ", NamedTextColor.DARK_AQUA, TextDecoration.BOLD)
                    .append(Component.text("To switch back to normal, do ", NamedTextColor.DARK_AQUA).decoration(TextDecoration.BOLD, false))
                    .append(Component.text("/chat normal", NamedTextColor.DARK_AQUA, TextDecoration.BOLD)));
        } else {
            player.sendMessage(Component.text("You are now speaking in Normal chat. ", NamedTextColor.AQUA, TextDecoration.BOLD)
                    .append(Component.text("To switch to staff chat, do ", NamedTextColor.AQUA).decoration(TextDecoration.BOLD, false))
                    .append(Component.text("/chat staff", NamedTextColor.AQUA, TextDecoration.BOLD)));
        }

        return true;
    }

    /**
     * Format staff chat message using legacy ampersand color codes from config
     */
    private Component formatMessage(String playerName, String message) {
        String prefix = StaffChatConfig.getMessagePrefix();
        return LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + playerName + ": " + message);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("chat") && args.length == 1) {
            return Arrays.asList("normal", "staff").stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return null;
    }
}
