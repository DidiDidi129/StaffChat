# StaffChat Mod - Code Structure and Architecture

## Project Overview

The StaffChat mod is a Fabric 26.2 mod that provides staff-only chat functionality with permission management via LuckPerms and optional Discord webhook integration.

## Directory Structure

```
staffchat-template-26.2/
├── src/
│   ├── main/
│   │   ├── java/com/staffchat/
│   │   │   ├── Staffchat.java                 # Main mod entry point
│   │   │   ├── config/
│   │   │   │   └── StaffChatConfig.java       # Configuration management
│   │   │   ├── permission/
│   │   │   │   └── PermissionChecker.java     # LuckPerms integration
│   │   │   ├── command/
│   │   │   │   └── StaffChatCommands.java     # Command registration and handling
│   │   │   └── discord/
│   │   │       └── DiscordWebhookHandler.java # Discord webhook integration
│   │   └── resources/
│   │       ├── fabric.mod.json                # Mod manifest
│   │       ├── staffchat.mixins.json          # Mixin configuration
│   │       └── assets/
│   └── client/
│       └── java/com/staffchat/
│           └── StaffchatClient.java           # Client-side initialization
├── gradle/
├── build.gradle                                # Build configuration
├── gradle.properties                           # Version and dependency info
├── settings.gradle
└── README.md                                   # Full documentation
```

## Core Components

### 1. Staffchat.java (Main Entry Point)
**Location:** `src/main/java/com/staffchat/Staffchat.java`

**Responsibilities:**
- Implements `ModInitializer` for Fabric
- Initializes configuration loading
- Sets up LuckPerms integration
- Registers commands
- Handles server lifecycle events

**Key Methods:**
- `onInitialize()` - Called when mod loads
- `ServerLifecycleEvents.SERVER_STARTED` - Initializes LuckPerms after server starts

```java
@Override
public void onInitialize() {
    StaffChatConfig.loadConfig();
    ServerLifecycleEvents.SERVER_STARTED.register(server -> {
        var luckPerms = LuckPermsProvider.get();
        PermissionChecker.init(luckPerms);
    });
    StaffChatCommands.register();
}
```

### 2. StaffChatConfig.java (Configuration Management)
**Location:** `src/main/java/com/staffchat/config/StaffChatConfig.java`

**Responsibilities:**
- Load and save JSON configuration files
- Manage configuration properties
- Provide getter/setter methods for all settings
- Auto-create default config on first run

**Key Features:**
- Uses Gson for JSON serialization
- Creates `config/staffchat/config.json` automatically
- Stores configuration in a nested `Config` class
- Supports hot-reloading of settings

**Configuration Fields:**
```java
public static class Config {
    public String permissionNode = "staffchat.use";
    public boolean enableDiscordWebhook = false;
    public String discordWebhookUrl = "";
    public String discordWebhookUsername = "Staff Chat";
    public String discordWebhookAvatarUrl = "";
    public String messagePrefix = "&9[StaffChat] &r";
}
```

### 3. PermissionChecker.java (LuckPerms Integration)
**Location:** `src/main/java/com/staffchat/permission/PermissionChecker.java`

**Responsibilities:**
- Initialize LuckPerms API
- Check player permissions
- Handle permission check errors gracefully
- Provide fallback behavior if LuckPerms is unavailable

**Key Methods:**
- `init(LuckPerms api)` - Initialize with LuckPerms instance
- `hasPermission(ServerPlayerEntity player, String permissionNode)` - Check if player has permission
- `isAvailable()` - Check if LuckPerms is initialized

**Permission Checking Logic:**
```java
User user = luckPerms.getUserManager().loadUser(playerUUID).join();
return user.getCachedData().getPermissionData()
    .checkPermission(permissionNode)
    .asBoolean();
```

### 4. StaffChatCommands.java (Command Registration)
**Location:** `src/main/java/com/staffchat/command/StaffChatCommands.java`

**Responsibilities:**
- Register `/staffchat` command
- Register `/sc` alias command
- Handle command execution
- Validate permissions
- Format and distribute messages
- Integrate with Discord webhooks

**Command Registration:**
- Uses Fabric's `CommandRegistrationCallback`
- Implements Brigadier command trees
- Supports greedy string arguments for multi-word messages

**Execution Flow:**
1. Check if executor is a player
2. Verify player has required permission
3. Format message with color codes
4. Send to all authorized players on server
5. Send to Discord webhook if enabled

**Message Format:**
```
[StaffChat] PlayerName: message content
```

### 5. DiscordWebhookHandler.java (Discord Integration)
**Location:** `src/main/java/com/staffchat/discord/DiscordWebhookHandler.java`

**Responsibilities:**
- Send staff chat messages to Discord via webhook
- Format messages as Discord embeds
- Handle HTTP requests asynchronously
- Provide error logging

**Key Features:**
- Asynchronous sending (runs in separate thread)
- Non-blocking server operations
- Formatted embeds with:
  - Title: "Staff Chat Message"
  - Description: Message content
  - Author: Player name
  - Timestamp: Message time
  - Color: Blue (5814783)

**Discord Payload Structure:**
```json
{
  "username": "Staff Chat",
  "avatar_url": "optional_url",
  "embeds": [{
    "title": "Staff Chat Message",
    "description": "message content",
    "author": { "name": "PlayerName" },
    "timestamp": "2026-01-26T18:07:30.000Z",
    "color": 5814783
  }]
}
```

## Data Flow

### Message Sending Flow

```
Player runs /staffchat message
    ↓
StaffChatCommands.handleStaffChat()
    ↓
PermissionChecker.hasPermission() → Check LuckPerms
    ↓
If permitted:
    ├→ Format message with prefix
    ├→ Send to all players with permission
    └→ DiscordWebhookHandler.sendMessage() → Async webhook request
    ↓
If not permitted:
    └→ Send error message to player
```

### Initialization Flow

```
Server starts
    ↓
Staffchat.onInitialize()
    ├→ StaffChatConfig.loadConfig() → Load from config/staffchat/config.json
    ├→ StaffChatCommands.register() → Register command handlers
    └→ ServerLifecycleEvents.SERVER_STARTED
        └→ PermissionChecker.init(LuckPerms) → Initialize permissions
```

## Dependencies

### Build Dependencies
- **Minecraft 26.2** - Game library
- **Fabric API 0.146.0+26.2** - Fabric utilities
- **Yarn Mappings 26.2+build.4** - Deobfuscation mappings
- **LuckPerms API 5.4** - Permission management
- **Gson 2.10.1** - JSON handling

### Runtime Dependencies
- **LuckPerms Mod** - Required for permission checks
- **Fabric Loader 0.18.7+** - Mod loading
- **Java 21** - Runtime environment

## Build System

**Build Tool:** Gradle with Fabric Loom

**Key Tasks:**
- `./gradlew build` - Full build (compile, jar, remap)
- `./gradlew clean` - Clean build artifacts
- `./gradlew remapJar` - Remap JAR to Yarn mappings

**Output:**
- `build/libs/staffchat-1.0.0.jar` - Compiled mod JAR
- `build/libs/staffchat-1.0.0-sources.jar` - Source code JAR

## Configuration Loading

The mod uses a configuration file-based system:

1. **First Launch:**
   - Creates `config/staffchat/` directory
   - Creates default `config.json` with preset values

2. **Subsequent Launches:**
   - Loads existing configuration
   - Applies settings from JSON file

3. **Configuration Changes:**
   - Edit `config/staffchat/config.json`
   - Restart server for changes to take effect

## Error Handling

### Permission Check Failures
- Catches exceptions during LuckPerms queries
- Denies permission as fallback
- Logs detailed error messages

### Discord Webhook Failures
- Asynchronous errors don't crash server
- Logs HTTP status codes
- Gracefully continues on network errors

### Configuration Failures
- Defaults to built-in defaults if file read fails
- Creates default config on corruption
- Logs all file I/O errors

## Extensibility

The code is designed for easy extension:

1. **Add New Commands:**
   - Extend `StaffChatCommands.register()`
   - Add new Brigadier command trees
   - Implement new handlers

2. **Custom Integrations:**
   - Follow `DiscordWebhookHandler` pattern
   - Create new handler classes in `com.staffchat.*`
   - Integrate in command execution flow

3. **Configuration Options:**
   - Add new fields to `StaffChatConfig.Config` class
   - Automatic JSON serialization/deserialization
   - Add getters/setters in `StaffChatConfig` class

## Compilation Notes

### Yarn Mappings
The project uses Yarn mappings for Fabric, which provides human-readable method and field names compared to obfuscated Minecraft code.

### Remapping
Gradle automatically handles remapping:
- Source code uses Yarn names (readable)
- Built JAR remaps to Yarn intermediary format
- Server deobfuscates using mapping files

### Java Version
- **Compilation:** Java 21
- **Target:** Java 21
- **Required:** Java 21+ runtime

## Testing

While unit tests aren't included, you can test locally:

1. Run a Fabric server with the mod JAR
2. Test commands: `/staffchat test`
3. Verify permission checks work with LuckPerms
4. Enable Discord webhook and test message delivery
5. Check `config/staffchat/config.json` auto-creation

## Security Considerations

1. **Permission Checks:** Always verified via LuckPerms before allowing messages
2. **Config Files:** Stored in server config directory (access controlled by file permissions)
3. **Discord Webhooks:** Uses HTTPS for secure transmission
4. **Input Validation:** Messages are limited to chat message length
5. **Async Operations:** Prevents server thread blocking on network requests

## Performance Impact

- **Minimal overhead:** Permission checks cached by LuckPerms
- **Asynchronous webhooks:** Non-blocking Discord operations
- **Efficient filtering:** Only sends to authorized players
- **Config caching:** Configuration loaded once at startup
