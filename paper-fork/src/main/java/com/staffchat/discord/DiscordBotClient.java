package com.staffchat.discord;

import com.staffchat.Staffchat;
import com.staffchat.permission.PermissionChecker;
import com.staffchat.config.StaffChatConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

/**
 * Discord bot client for two-way staff chat integration
 */
public class DiscordBotClient {
    private JDA jda;
    private TextChannel staffChannel;
    private boolean running = false;

    /**
     * Start the Discord bot
     */
    public void start() {
        if (running) {
            Staffchat.LOGGER.warn("Discord bot is already running");
            return;
        }

        String token = StaffChatConfig.getDiscordBotToken();
        String channelId = StaffChatConfig.getDiscordStaffChannelId();

        if (token.isEmpty() || channelId.isEmpty()) {
            Staffchat.LOGGER.warn("Discord bot token or channel ID not configured");
            return;
        }

        try {
            jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(new StaffChatListener())
                    .build();

            // Wait for JDA to be ready
            jda.awaitReady();

            staffChannel = jda.getTextChannelById(channelId);
            if (staffChannel == null) {
                Staffchat.LOGGER.error("Could not find Discord channel with ID: " + channelId);
                jda.shutdown();
                return;
            }

            running = true;
            Staffchat.LOGGER.info("Discord bot started successfully");
        } catch (IllegalArgumentException e) {
            Staffchat.LOGGER.error("Failed to login to Discord: " + e.getMessage());
        } catch (InterruptedException e) {
            Staffchat.LOGGER.error("Discord bot startup interrupted");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            Staffchat.LOGGER.error("Unexpected error starting Discord bot: " + e.getMessage(), e);
        }
    }

    /**
     * Stop the Discord bot
     */
    public void stop() {
        if (!running) {
            return;
        }

        if (jda != null) {
            jda.shutdown();
        }

        running = false;
        Staffchat.LOGGER.info("Discord bot stopped");
    }

    /**
     * Send a message to the Discord staff chat channel
     */
    public void sendMessage(String username, String message) {
        if (!running || staffChannel == null) {
            return;
        }

        // Include the original username in a hidden format for reply detection
        String formatted = StaffChatConfig.getDiscordMessageFormat()
                .replace("{user}", username)
                .replace("{message}", message) + "\n||[OriginalUser:" + username + "]||";

        staffChannel.sendMessage(formatted).queue();
    }

    /**
     * Listener for Discord messages
     */
    private class StaffChatListener extends ListenerAdapter {
        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            // Ignore messages from bots
            if (event.getAuthor().isBot()) {
                return;
            }

            // Only process messages from the configured staff channel
            if (!event.getChannel().getId().equals(StaffChatConfig.getDiscordStaffChannelId())) {
                return;
            }

            Message message = event.getMessage();
            String content = message.getContentDisplay();

            // Ignore empty messages
            if (content.trim().isEmpty()) {
                return;
            }

            // Get the user's display name (nickname if available, otherwise username)
            User user = event.getAuthor();
            String displayName = user.getName();
            if (event.getMember() != null && event.getMember().getNickname() != null) {
                displayName = event.getMember().getNickname();
            }

            // Check if this is a reply to another message
            String replyInfo = "";
            Message referencedMessage = message.getMessageReference() != null ? message.getMessageReference().getMessage() : null;
            if (referencedMessage != null) {
                // Try to extract original game username from the referenced message
                String referencedContent = referencedMessage.getContentDisplay();
                String repliedDisplayName = null;
                
                // Look for hidden original user tag
                if (referencedContent.contains("[OriginalUser:")) {
                    int start = referencedContent.indexOf("[OriginalUser:") + 14;
                    int end = referencedContent.indexOf("]", start);
                    if (end > start) {
                        repliedDisplayName = referencedContent.substring(start, end);
                    }
                }
                
                // Fall back to Discord user if no original user found
                if (repliedDisplayName == null) {
                    User repliedUser = referencedMessage.getAuthor();
                    repliedDisplayName = repliedUser.getName();
                    if (referencedMessage.getMember() != null && referencedMessage.getMember().getNickname() != null) {
                        repliedDisplayName = referencedMessage.getMember().getNickname();
                    }
                }
                
                replyInfo = "§7|\n§7------> replying to §b" + repliedDisplayName + "§7\n";
            }

            // Send to in-game staff chat
            MinecraftServer server = Staffchat.getServer();
            if (server != null) {
                String formattedMessage = "§9[Staff] §r§b[Discord]§r " + displayName + "§r: " + replyInfo + content;
                server.getPlayerManager().getPlayerList().forEach(p -> {
                    if (PermissionChecker.hasPermission(p, StaffChatConfig.getPermissionNode())) {
                        p.sendMessage(Text.literal(formattedMessage), false);
                    }
                });
            } else {
                Staffchat.LOGGER.warn("Server reference not available for Discord message relay");
            }
        }
    }
}
