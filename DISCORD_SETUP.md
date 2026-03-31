# StaffChat Discord Integration Guide

## Overview

The StaffChat mod now supports bidirectional Discord integration:
- **Messages from Minecraft → Discord**: Staff chat messages are sent to Discord as simple text
- **Messages from Discord → Minecraft**: Any message in the Discord channel is broadcast to in-game staff chat

## Setup Instructions

### 1. Create a Discord Webhook

1. Go to your Discord server
2. Navigate to **Server Settings** → **Integrations** → **Webhooks**
3. Click **New Webhook**
4. Name it (e.g., "Staff Chat")
5. Choose the channel for staff messages
6. Click **Copy Webhook URL**
7. Save the URL for later

### 2. Configure the Mod

Edit `config/staffchat/config.json`:

```json
{
  "permissionNode": "staffchat.use",
  "enableDiscordWebhook": true,
  "discordWebhookUrl": "https://discord.com/api/webhooks/YOUR_WEBHOOK_ID/YOUR_TOKEN",
  "discordWebhookUsername": "Staff Chat",
  "discordWebhookAvatarUrl": "",
  "messagePrefix": "&9[Staff] &r",
  "discordListenerPort": 3000
}
```

**Configuration Options:**
- `enableDiscordWebhook`: Set to `true` to enable Discord integration
- `discordWebhookUrl`: Your webhook URL from step 1
- `discordWebhookUsername`: Display name for messages sent from the mod
- `discordWebhookAvatarUrl`: Optional avatar URL for the webhook
- `messagePrefix`: Prefix for in-game staff messages
- `discordListenerPort`: Port for receiving Discord messages (default: 3000)

### 3. Setup Discord Webhook for Receiving Messages

To receive messages from Discord in-game, you need to set up a **Discord bot webhook** that sends messages back to your server:

1. Create another webhook in the same Discord channel
2. Set the webhook URL to point to your server: `http://your-server-ip:3000`
3. This webhook should be triggered whenever someone posts in the staff channel

**Note:** Depending on your Discord setup, you may need to:
- Use a Discord bot with webhook capabilities
- Configure message forwarding manually
- Use a Discord bot library to handle message routing

### 4. Restart the Server

After configuration, restart your Minecraft server. The mod will:
- Listen for webhook messages on port 3000
- Automatically broadcast Discord messages to in-game staff
- Send all staff chat messages to Discord

## Message Format

### Minecraft → Discord
When a staff member uses `/sc message`:
```
PlayerName message content
```

### Discord → Minecraft  
When a message is sent to the webhook:
```
[Staff] [Discord] AuthorName: message content
```

Messages are displayed in the staff chat color (blue by default).

## Usage

### In-Game Commands
```
/staffchat This is a staff message
/sc Quick staff message
```

These commands:
1. Show the message to all players with `staffchat.use` permission
2. Automatically post to Discord webhook

### Discord Chat
Simply type in the staff Discord channel. The message will:
1. Be broadcast to all in-game staff
2. Show with `[Discord]` prefix to indicate source

## Security & Permissions

### Permission Node
All players must have the `staffchat.use` permission to:
- See staff chat messages (in-game and from Discord)
- Send staff chat messages

### Discord Webhook Security
- Keep your webhook URL private (don't share it)
- The listener only accepts messages from the configured webhook
- Bot messages from other apps are automatically ignored

### Firewall Configuration
The listener port (default 3000) should be:
- Exposed to Discord's servers if using hosted bot
- Protected behind your server firewall if using internal only
- Can be changed in the config if port 3000 is in use

## Troubleshooting

### Discord messages not appearing in-game

**Possible causes:**
1. Webhook listener not running - check server logs for initialization message
2. Port 3000 is blocked or already in use - change `discordListenerPort` in config
3. Discord webhook not pointing to correct server IP - verify webhook URL
4. Message payload format incorrect - ensure Discord webhook is sending proper JSON

**Solution:**
```
1. Check logs: Look for "Discord webhook listener started on port 3000"
2. Verify port: netstat -an | grep 3000
3. Test webhook: Use a tool like Postman to test the endpoint
4. Check permissions: Ensure webhook channel permissions are correct
```

### In-game messages not appearing on Discord

**Possible causes:**
1. Discord integration not enabled - check `enableDiscordWebhook: true`
2. Webhook URL incorrect - verify URL is correct
3. Webhook deleted - recreate webhook in Discord
4. Network error - check server logs for errors

**Solution:**
```
1. Verify config: enableDiscordWebhook should be true
2. Verify webhook: Check Discord server integrations
3. Test webhook: Send a message with /sc and check Discord
4. Check logs: Look for HTTP errors in server output
```

### "Discord webhook listener not listening"

**Solution:**
- Port 3000 is already in use by another application
- Change `discordListenerPort` to another available port
- Restart the server
- Update Discord webhook URL if using external IP

## Advanced Configuration

### Custom Port
If port 3000 is already in use, change it in the config:
```json
{
  "discordListenerPort": 8080
}
```

Then update your Discord webhook to point to `http://your-server-ip:8080`

### Custom Message Prefix
Customize the in-game prefix with color codes:
```json
{
  "messagePrefix": "&c[ADMIN] &r"
}
```

### Disable Webhook Sending
To disable sending to Discord (but still receive):
```json
{
  "enableDiscordWebhook": false,
  "discordListenerPort": 3000
}
```

The server will still listen for Discord messages on port 3000.

## Message Flow Diagram

```
Minecraft Server
    ↓
    ├→ /staffchat command
    │   ↓
    │   Check permission
    │   ↓
    │   Broadcast to authorized players (in-game)
    │   ↓
    │   Send to Discord webhook (async)
    │
    └← Discord webhook listener (port 3000)
        ↓
        Receive message from Discord
        ↓
        Broadcast to authorized players (in-game)
```

## Best Practices

1. **Use a dedicated Discord channel** for staff chat only
2. **Set strict permissions** on the webhook and channel
3. **Keep webhook URLs private** - treat like passwords
4. **Test locally first** before deploying to production
5. **Monitor logs** for any integration errors
6. **Backup config file** before making changes

## Webhook Message Format

If implementing your own webhook, send JSON in this format:

```json
{
  "author": {
    "username": "DiscordUserName",
    "bot": false
  },
  "content": "Message content here"
}
```

The listener will ignore:
- Messages without `author.username`
- Messages without `content`
- Messages where `author.bot` is true
