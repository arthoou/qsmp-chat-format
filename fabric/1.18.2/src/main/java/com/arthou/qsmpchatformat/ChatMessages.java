package com.arthou.qsmpchatformat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

/** Formats only ordinary chat and the game's existing presence announcements. */
public final class ChatMessages {
    private ChatMessages() {}

    public static Component presence(Component original, String username, boolean joined) {
        if (!(original instanceof TranslatableComponent contents)) {
            return original;
        }
        String key = contents.getKey();
        boolean expected = joined
            ? key.equals("multiplayer.player.joined") || key.equals("multiplayer.player.joined.renamed")
            : key.equals("multiplayer.player.left");
        return expected ? presence(username, joined) : original;
    }

    public static Component presence(String username, boolean joined) {
        return new net.minecraft.network.chat.TextComponent("")
            .append(new net.minecraft.network.chat.TextComponent("[").withStyle(ChatFormatting.GRAY))
            .append(new net.minecraft.network.chat.TextComponent(joined ? "+" : "-").withStyle(joined ? ChatFormatting.GREEN : ChatFormatting.RED))
            .append(new net.minecraft.network.chat.TextComponent("] ").withStyle(ChatFormatting.GRAY))
            .append(new net.minecraft.network.chat.TextComponent(username).withStyle(ChatFormatting.WHITE));
    }

    public static Component chat(Component displayName, String rawText) {
        return displayName.copy().withStyle(ChatFormatting.YELLOW)
            .append(new net.minecraft.network.chat.TextComponent(" » ").withStyle(ChatFormatting.DARK_GRAY))
            .append(new net.minecraft.network.chat.TextComponent(rawText).withStyle(ChatFormatting.WHITE));
    }
}
