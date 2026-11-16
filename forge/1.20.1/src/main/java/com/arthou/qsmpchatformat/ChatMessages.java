package com.arthou.qsmpchatformat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/** Shared visible format; no player state or networking is needed to format text. */
public final class ChatMessages {
    private ChatMessages() {}

    public static Component join(String username) {
        return presence(username, "+", ChatFormatting.GREEN);
    }

    public static Component leave(String username) {
        return presence(username, "-", ChatFormatting.RED);
    }

    private static Component presence(String username, String symbol, ChatFormatting color) {
        return Component.empty()
            .append(Component.literal("[").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(symbol).withStyle(color))
            .append(Component.literal("] ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(username).withStyle(ChatFormatting.WHITE));
    }

    public static Component chat(Component displayName, String rawText) {
        MutableComponent message = displayName.copy().withStyle(ChatFormatting.YELLOW);
        message.append(Component.literal(" » ").withStyle(ChatFormatting.DARK_GRAY));
        message.append(Component.literal(rawText).withStyle(ChatFormatting.WHITE));
        return message;
    }
}
