# StaffChat Mod - Fabric 1.21.11

A Fabric mod for Minecraft 1.21.11 that adds a staff chat feature with LuckPerms integration and Discord webhook support.

## Features

- **Commands**: `/staffchat <message>` and `/sc <message>` for sending staff chat messages
- **Permission Control**: Uses LuckPerms for permission management (configurable permission node)
- **Discord Integration**: Optional Discord webhook support to relay messages to a Discord channel
- **Configurable**: All settings can be customized via a JSON config file
- **Efficient**: Asynchronous Discord webhook sending to avoid server lag

## Requirements

- **Minecraft 1.21.11**
- **Fabric Loader** (0.18.4+)
- **Fabric API** (0.141.2+)
- **LuckPerms** (required for permission checks)

## Installation

1. Place the compiled JAR file in your `mods` folder
2. Ensure you have LuckPerms installed on your server
3. Start the server - a default config file will be created at `config/staffchat/config.json`

## Configuration

The mod creates a configuration file at `config/staffchat/config.json` with the following options:

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

### Configuration Options

- **permissionNode**: The LuckPerms permission node required to use staff chat (default: `staffchat.use`)
- **enableDiscordWebhook**: Enable/disable Discord webhook integration
- **discordWebhookUrl**: Your Discord webhook URL
- **discordWebhookUsername**: Username displayed in Discord for the webhook
- **discordWebhookAvatarUrl**: Optional avatar URL for the webhook in Discord
- **messagePrefix**: Message prefix with color codes (use `&` for color, converted to `§`)

## Usage

### As a Player

1. Ensure you have the `staffchat.use` permission (or the configured permission node)
2. Use the command: `/staffchat <message>` or `/sc <message>`
3. Only players with the permission can see the message

### Permission Setup (LuckPerms)

Grant the permission to a player or group:

```
/lp user <username> permission set staffchat.use true
/lp group <groupname> permission set staffchat.use true
```

Or use the configured custom permission node:

```
/lp user <username> permission set <your.custom.permission> true
```

## Discord Webhook Setup

To enable Discord integration:

1. Create a webhook in your Discord server:
   - Go to Server Settings → Integrations → Webhooks
   - Click "New Webhook"
   - Copy the webhook URL

2. Edit `config/staffchat/config.json`:
   - Set `enableDiscordWebhook` to `true`
   - Paste your webhook URL in `discordWebhookUrl`
   - Optionally configure the username and avatar

3. Restart the server or reload the config

## Color Codes

The mod supports Minecraft color codes using the `&` character:

- `&0` - Black
- `&1` - Dark Blue
- `&2` - Dark Green
- `&3` - Dark Cyan
- `&4` - Dark Red
- `&5` - Dark Purple
- `&6` - Gold
- `&7` - Gray
- `&8` - Dark Gray
- `&9` - Blue
- `&a` - Green
- `&b` - Cyan
- `&c` - Red
- `&d` - Light Purple
- `&e` - Yellow
- `&f` - White
- `&l` - Bold
- `&m` - Strikethrough
- `&n` - Underline
- `&o` - Italic
- `&r` - Reset

## Troubleshooting

### "You do not have permission to use staff chat"

- Ensure you have the correct permission node set in LuckPerms
- Check the permission node in `config/staffchat/config.json`
- Verify that the permission is assigned to your user or group

### Discord messages not sending

- Ensure `enableDiscordWebhook` is set to `true`
- Verify the webhook URL is correct
- Check the server logs for error messages
- Ensure the webhook is still valid (Discord webhooks can expire)

### "LuckPerms not found on the server"

- Install LuckPerms on your server
- Restart the server after installing LuckPerms

## Compilation

To compile the mod:

```bash
./gradlew build
```

The compiled JAR will be in `build/libs/`

## Building from Source

1. Clone or download the mod source
2. Run `./gradlew build`
3. Find the compiled JAR in `build/libs/staffchat-<version>.jar`

## Dependencies

- **Fabric API**: Provides core Fabric utilities
- **LuckPerms API**: For permission management
- **Gson**: For JSON configuration and Discord webhook payloads

## License

This project is licensed under CC0-1.0 (Public Domain)

## Support

For issues, feature requests, or questions, please refer to the project repository or contact the mod author.
