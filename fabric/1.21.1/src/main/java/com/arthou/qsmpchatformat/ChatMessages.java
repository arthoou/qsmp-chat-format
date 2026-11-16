package com.arthou.qsmpchatformat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

/** Formats only ordinary chat and the game's existing presence announcements. */
public final class ChatMessages {
    private ChatMessages() {}

    public static Component presence(Component original, String username, boolean joined) {
        if (!(original.getContents() instanceof TranslatableContents contents)) {
            return original;
        }
        String key = contents.getKey();
        boolean expected = joined
            ? key.equals("multiplayer.player.joined") || key.equals("multiplayer.player.joined.renamed")
            : key.equals("multiplayer.player.left");
        return expected ? presence(username, joined) : original;
    }

    public static Component presence(String username, boolean joined) {
        return Component.empty()
            .append(Component.literal("[").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(joined ? "+" : "-").withStyle(joined ? ChatFormatting.GREEN : ChatFormatting.RED))
            .append(Component.literal("] ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(username).withStyle(ChatFormatting.WHITE));
    }

    public static Component chat(Component displayName, String rawText) {
        return displayName.copy().withStyle(ChatFormatting.YELLOW)
            .append(Component.literal(" » ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal(rawText).withStyle(ChatFormatting.WHITE));
    }
}
