package com.arthou.qsmpchatformat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class ChatMessagesTest {
    public static void main(String[] args) {
        Component name = new net.minecraft.network.chat.TextComponent("arthouxyz");
        Component chat = ChatMessages.chat(name, "Hello, QSMP! %s /tpa");
        require(chat.getString().equals("arthouxyz » Hello, QSMP! %s /tpa"), "chat example and raw text");
        require(chat.getStyle().getColor().getValue() == ChatFormatting.YELLOW.getColor(), "yellow name");
        require(chat.getSiblings().get(0).getStyle().getColor().getValue() == ChatFormatting.DARK_GRAY.getColor(), "dark gray separator");
        require(chat.getSiblings().get(1).getStyle().getColor().getValue() == ChatFormatting.WHITE.getColor(), "white content");
        require(name.getStyle().getColor() == null && name.getSiblings().isEmpty(), "source display name is unchanged");
        Component joined = ChatMessages.presence(new net.minecraft.network.chat.TranslatableComponent("multiplayer.player.joined", name), "arthouxyz", true);
        Component renamed = ChatMessages.presence(new net.minecraft.network.chat.TranslatableComponent("multiplayer.player.joined.renamed", name, "old"), "arthouxyz", true);
        Component left = ChatMessages.presence(new net.minecraft.network.chat.TranslatableComponent("multiplayer.player.left", name), "arthouxyz", false);
        require(joined.getString().equals("[+] arthouxyz"), "join");
        require(renamed.getString().equals("[+] arthouxyz"), "renamed join");
        require(left.getString().equals("[-] arthouxyz"), "leave");
        require(joined.getSiblings().get(1).getStyle().getColor().getValue() == ChatFormatting.GREEN.getColor(), "green plus");
        require(left.getSiblings().get(1).getStyle().getColor().getValue() == ChatFormatting.RED.getColor(), "red minus");
        Component unrelated = new net.minecraft.network.chat.TranslatableComponent("chat.type.announcement", name, "hello");
        Component literal = new net.minecraft.network.chat.TextComponent("other message");
        require(ChatMessages.presence(unrelated, "arthouxyz", true) == unrelated, "unrelated translations are preserved");
        require(ChatMessages.presence(literal, "arthouxyz", false) == literal, "unrelated literals are preserved");
        Component leaveTranslation = new net.minecraft.network.chat.TranslatableComponent("multiplayer.player.left", name);
        require(ChatMessages.presence(leaveTranslation, "arthouxyz", true) == leaveTranslation, "join hook does not replace leave");
        System.out.println("Focused chat/presence assertions passed");
    }

    private static void require(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
    }
}
