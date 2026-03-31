# StaffChat Paper Plugin

This is a Paper 26.1+ plugin that provides staff-only chat functionality. It is designed to work with Minecraft 26.1 and future versions by using the stable Paper Adventure API rather than deprecated interfaces.

## Features

- Staff chat with permission-based access control
- LuckPerms integration for permissions
- Discord webhook support for two-way communication
- Discord bot integration (optional)
- Chat mode switching (/chat normal/staff)
- Color code support in messages (uses Adventure component API)

## Commands

- `/staffchat <message>` or `/sc <message>` - Send a message to staff chat
- `/chat <normal|staff>` - Switch between normal and staff chat modes

## Permissions

- `staffchat.use` - Allows using staff chat commands
- `staffchat.admin` - Allows administrative staff chat functions

## Installation

1. Place the compiled JAR file in your server's `plugins` folder
2. Install LuckPerms as a dependency
3. Restart your server
4. Configure the plugin settings in the generated config file

## Configuration

The plugin will generate a configuration file where you can:
- Enable/disable Discord webhook functionality
- Configure Discord bot settings
- Set custom message prefixes (using `&` color codes)
- Configure permission nodes

## Dependencies

- **LuckPerms** (required) - For permission management
- **Paper 26.1+** (required) - Server software

## Building

To build this plugin from source:

```bash
cd paper-fork
./gradlew build
```

The built plugin JAR will be in `build/libs/`.

## Version Compatibility

This plugin targets Paper 26.1 as its minimum API version. By using stable Paper APIs (Adventure components, `AsyncChatEvent`) instead of deprecated interfaces, it should continue working in future Paper and Minecraft versions without code changes.

## License

Same license as the original Fabric version.
