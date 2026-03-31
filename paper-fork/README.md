# StaffChat Paper Plugin

This is a Paper 1.21.1 fork of the original Fabric StaffChat mod. This version has been converted from Fabric to Paper/Bukkit APIs while maintaining all the original functionality.

## Features

- Staff chat with permission-based access control
- LuckPerms integration for permissions
- Discord webhook support for two-way communication
- Discord bot integration (optional)
- Chat mode switching (/chat normal/staff)
- Color code support in messages

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
- Set custom message prefixes
- Configure permission nodes

## Dependencies

- **LuckPerms** (required) - For permission management
- **Paper 1.21.1** (required) - Server software

## Building

To build this plugin from source:

```bash
cd paper-fork
./gradlew build
```

The built plugin JAR will be in `build/libs/`.

## Differences from Fabric Version

- Converted from Fabric ModInitializer to Bukkit JavaPlugin
- Uses Bukkit command system instead of Fabric Brigadier
- Uses Bukkit event system instead of Fabric event callbacks
- Removed Fabric-specific mixins (not needed for Paper)
- Updated all imports from Fabric to Bukkit APIs
- Uses ChatColor instead of section signs for colors
- Uses Bukkit Player instead of ServerPlayerEntity
- Uses Bukkit Server instead of MinecraftServer

## License

Same license as the original Fabric version.
