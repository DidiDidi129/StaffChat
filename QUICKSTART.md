# StaffChat Mod - Quick Start Guide

## Installation Steps

### 1. Prerequisites
- Minecraft Paper server running Minecraft 26.1 or later
- LuckPerms plugin installed on your server

### 2. Install the Plugin
1. Download the `staffchat-1.0.0.jar` file
2. Place it in your server's `plugins` folder
3. Restart the server

### 3. Configuration
After the first server start, a configuration file will be created at:
```
config/staffchat/config.json
```

### 4. Basic Setup

#### Give Players Permission
Using LuckPerms, grant players the staff chat permission:

```
/lp user <playername> permission set staffchat.use true
```

Or add it to a group:

```
/lp group <groupname> permission set staffchat.use true
```

#### Test the Command
1. Log in as a player with the `staffchat.use` permission
2. Run: `/staffchat Hello, staff!`
3. Or use the alias: `/sc Hello, staff!`
4. All players with the permission should see the message

## Configuration Guide

### Basic Configuration (No Discord)

Keep the default config as-is:

```json
{
  "permissionNode": "staffchat.use",
  "enableDiscordWebhook": false,
  "discordWebhookUrl": "",
  "discordWebhookUsername": "Staff Chat",
  "discordWebhookAvatarUrl": "",
  "messagePrefix": "&9[StaffChat] &r"
}
```

### Advanced Configuration (With Discord)

1. **Create a Discord Webhook:**
   - Go to your Discord server
   - Server Settings → Integrations → Webhooks
   - Click "New Webhook"
   - Customize the name and avatar (optional)
   - Copy the webhook URL

2. **Update the Config:**

```json
{
  "permissionNode": "staffchat.use",
  "enableDiscordWebhook": true,
  "discordWebhookUrl": "https://discord.com/api/webhooks/1234567890/abcdefghijklmnop",
  "discordWebhookUsername": "Staff Chat",
  "discordWebhookAvatarUrl": "https://example.com/avatar.png",
  "messagePrefix": "&9[StaffChat] &r"
}
```

3. **Reload or Restart**
   - Restart the server or reload the config file
   - Test a staff chat message and it should appear in Discord

## Custom Permission Node

To use a custom permission node instead of `staffchat.use`:

1. Edit `config/staffchat/config.json`
2. Change `"permissionNode"` to your desired permission
3. Restart the server
4. Grant players the new permission via LuckPerms

Example:

```json
{
  "permissionNode": "staff.chat.access",
  ...
}
```

Then grant it:
```
/lp user <playername> permission set staff.chat.access true
```

## Color Codes

You can customize the message prefix using Minecraft color codes:

| Code | Color | Code | Color |
|------|-------|------|-------|
| `&0` | Black | `&8` | Dark Gray |
| `&1` | Dark Blue | `&9` | Blue |
| `&2` | Dark Green | `&a` | Green |
| `&3` | Dark Cyan | `&b` | Cyan |
| `&4` | Dark Red | `&c` | Red |
| `&5` | Dark Purple | `&d` | Light Purple |
| `&6` | Gold | `&e` | Yellow |
| `&7` | Gray | `&f` | White |

Formatting codes:
- `&l` - Bold
- `&m` - Strikethrough
- `&n` - Underline
- `&o` - Italic
- `&r` - Reset

Example custom prefix:
```json
{
  "messagePrefix": "&l&c[STAFF] &r&f"
}
```

## Troubleshooting

### Command doesn't work - "You do not have permission"

**Solution:**
1. Check you have the correct permission: `/lp user <yourname> permission info`
2. Ensure the permission matches the config file's `permissionNode`
3. Reload permissions: `/lp user <yourname> parent clear` and then re-apply

### Discord webhook not working

**Solution:**
1. Verify the webhook URL is correct
2. Check that `enableDiscordWebhook` is set to `true`
3. Look for error messages in server logs
4. Test the webhook URL in a tool like Postman
5. Ensure the webhook channel still exists and bot has permission to post

### "LuckPerms not found" error

**Solution:**
1. Install LuckPerms on your server if not already installed
2. Download from: https://luckperms.net/download
3. Place the JAR in your `plugins` folder
4. Restart the server

### Configuration not applying

**Solution:**
1. Stop the server completely
2. Edit `config/staffchat/config.json`
3. Start the server again
4. Check server logs to confirm the config loaded

## Advanced Usage

### Monitoring Staff Chat in Discord

Once you've set up the webhook, all staff chat messages will appear in the specified Discord channel as embeds with:
- The staff member's name (author)
- The message content
- A timestamp
- A blue color for easy identification

### Combining with Moderation

You can use StaffChat for:
- Quick staff communications
- Reporting rule violations discreetly
- Coordinating moderation actions
- Logging in Discord for records

## Need Help?

- Check the main README.md for more detailed documentation
- Review the config.example.json for reference
- Check server logs for error messages
- Verify LuckPerms is properly installed and configured
