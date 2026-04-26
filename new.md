# StaffChat Mod — Spec Sheet

## Overview

StaffChat is a **Fabric mod** for Minecraft servers. Its purpose is to provide a **private staff-only chat channel** that is separate from normal player chat, with optional two-way Discord integration via a Discord bot.

---

## Core Functionality

### 1. Staff Chat Channel

- Staff messages are **only visible to players who have the staff chat permission**.
- Normal players never see staff chat messages.
- Messages are formatted with a configurable prefix (e.g. `[Staff]`) followed by the player's name and message.
- The console can also send staff chat messages (shown as `[Console]`).

### 2. Commands

| Command | Description |
|---|---|
| `/staffchat <message>` | Send a message to staff chat |
| `/sc <message>` | Shorthand alias for `/staffchat` |
| `/staffchat reload` | Reload the config file (staff permission required) |
| `/chat staff` | Toggle your chat mode to Staff — all normal typed messages go to staff chat instead |
| `/chat normal` | Toggle your chat mode back to normal chat |

### 3. Chat Mode Toggling

- Players can switch their chat mode between **normal** and **staff**.
- When in **staff mode**, anything a player types in normal chat is automatically redirected to the staff chat channel instead of going to all players.
- When in **normal mode**, chat works as usual.
- The mode is stored per-player in memory (resets on disconnect).

### 4. Permissions

- Uses **LuckPerms** for permission checks.
- The permission node is configurable (default: `staffchat.use`).
- A player must have this permission to:
  - Send staff chat messages
  - See staff chat messages
  - Switch to staff chat mode
  - Reload the config
- If LuckPerms is not installed, the mod will log a warning and refuse all staff chat actions.

### 5. Discord Integration (Bot)

- An optional **Discord bot** provides **two-way** staff chat bridging between Minecraft and a Discord channel.
- **Minecraft → Discord**: When a staff message is sent in-game, the bot forwards it to a designated Discord text channel.
- **Discord → Minecraft**: When a non-bot user sends a message in the designated Discord channel, it appears in-game for all staff players formatted as `[Staff] [Discord] username: message`.
- **Reply support**: If a Discord user replies to another message, the in-game message shows who they were replying to.
- The bot requires a bot token and a Discord channel ID to function.

---

## Configuration

The mod creates a JSON config file automatically on first run. It supports:

| Option | Description |
|---|---|
| `permissionNode` | The LuckPerms permission node for staff chat access (default: `staffchat.use`) |
| `messagePrefix` | The prefix shown before staff messages, supports `&` color codes (e.g. `&9[Staff] &r`) |
| `enableDiscordBot` | Whether to enable Discord bot integration |
| `discordBotToken` | The Discord bot token |
| `discordStaffChannelId` | The Discord channel ID for the staff chat bridge |
| `discordMessageFormat` | Format for Minecraft→Discord messages, supports `{user}` and `{message}` placeholders |

---

## Message Formatting

- Supports Minecraft `&` color codes in the prefix (e.g. `&9` = blue, `&r` = reset, `&l` = bold).
- Formatted messages are sent as system messages (not normal chat), so they bypass chat signing.

---

## Lifecycle Behavior

- Configuration loads on mod initialization.
- LuckPerms and Discord services initialize when the server starts.
- Discord bot disconnects cleanly when the server stops.
- Player chat mode state is held in memory and is lost when a player disconnects (no persistence needed).

---

## Dependencies

| Dependency | Purpose |
|---|---|
| Fabric API | Core Fabric mod utilities and event hooks |
| LuckPerms | Permission management |
| JDA (Java Discord API) | Discord bot integration |
| Gson | JSON config reading/writing |

---

## Suggested Implementation Approach

1. **Start with the Fabric mod scaffold** — use the standard Fabric example mod as a starting point.
2. **Add a config system** — load/save a JSON file with Gson on mod initialization.
3. **Implement permission checking** — integrate LuckPerms API, called on every staff chat action.
4. **Register commands** — use Fabric's `CommandRegistrationCallback` to register `/staffchat`, `/sc`, and `/chat`.
5. **Add chat mode state** — keep a `Map<UUID, ChatMode>` in memory to track each player's current mode.
6. **Hook into chat events** — use `ServerMessageEvents.ALLOW_CHAT_MESSAGE` to intercept normal chat and redirect staff-mode players to staff chat; return `false` to block the original message.
7. **Add Discord bot** — use JDA to connect a bot, listen for messages in the configured channel, and relay them in-game. On the outgoing side, call the bot's send method whenever a staff message is sent in-game.
8. **Handle server lifecycle** — start Discord services in `SERVER_STARTED`, stop them in `SERVER_STOPPED`.
