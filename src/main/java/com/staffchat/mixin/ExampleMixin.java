package com.staffchat.mixin;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;

// This mixin is kept for compatibility but no longer handles chat messages
// Chat is now handled exclusively by ChatEventListener using Fabric events
@Mixin(ServerPlayNetworkHandler.class)
public class ExampleMixin {
	// Empty - chat handling moved to ChatEventListener to prevent conflicts
}