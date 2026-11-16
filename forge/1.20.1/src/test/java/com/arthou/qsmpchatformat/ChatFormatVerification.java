package com.arthou.qsmpchatformat;

import java.lang.reflect.Method;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.chat.Component;

/** Focused contract checks against Minecraft's real text components. */
public final class ChatFormatVerification {
    private static int checks;

    public static void main(String[] args) throws Exception {
        check(ChatMessages.join("arthouxyz").getString().equals("[+] arthouxyz"), "join text");
        check(ChatMessages.leave("arthouxyz").getString().equals("[-] arthouxyz"), "leave text");
        Component displayName = Component.literal("arthouxyz");
        Component chat = ChatMessages.chat(displayName, "Hello, everyone! §a <test>");
        check(chat.getString().equals("arthouxyz » Hello, everyone! §a <test>"), "raw chat text");
        check(displayName.getString().equals("arthouxyz"), "display name not mutated");
        List<Integer> colors = new ArrayList<>();
        chat.visit((style, text) -> {
            if (!text.isEmpty()) colors.add(style.getColor().getValue());
            return java.util.Optional.empty();
        }, net.minecraft.network.chat.Style.EMPTY);
        check(colors.equals(List.of(ChatFormatting.YELLOW.getColor(), ChatFormatting.DARK_GRAY.getColor(), ChatFormatting.WHITE.getColor())), "chat colors");
        check(ChatMessages.join("arthouxyz").getSiblings().get(1).getStyle().getColor().getValue() == ChatFormatting.GREEN.getColor(), "join plus green");
        check(ChatMessages.leave("arthouxyz").getSiblings().get(1).getStyle().getColor().getValue() == ChatFormatting.RED.getColor(), "leave minus red");
        Method filter = ChatFormatHandler.class.getDeclaredMethod("isVanillaJoinOrLeave", Component.class);
        filter.setAccessible(true);
        for (String key : List.of("multiplayer.player.joined", "multiplayer.player.joined.renamed", "multiplayer.player.left")) {
            check((boolean) filter.invoke(null, Component.translatable(key, "arthouxyz")), "presence key " + key);
        }
        for (Component message : List.of(Component.literal("multiplayer.player.joined"), Component.translatable("chat.type.announcement", "Server", "hello"), Component.translatable("commands.message.display.incoming", "arthouxyz", "hi"))) {
            check(!(boolean) filter.invoke(null, message), "unrelated messages preserved");
        }
        EmbeddedChannel channel = new EmbeddedChannel(new ChatFormatHandler.VanillaPresenceFilter());
        for (String key : List.of("multiplayer.player.joined", "multiplayer.player.joined.renamed", "multiplayer.player.left")) {
            check(!channel.writeOutbound(new ClientboundSystemChatPacket(Component.translatable(key, "arthouxyz"), false)), "vanilla presence packet suppressed");
        }
        Object literal = new ClientboundSystemChatPacket(Component.literal("Keep this server message"), false);
        check(channel.writeOutbound(literal) && channel.readOutbound() == literal, "other system packet preserved");
        Object unrelated = new Object();
        check(channel.writeOutbound(unrelated) && channel.readOutbound() == unrelated, "other packet preserved");
        channel.finishAndReleaseAll();
        System.out.println("QSMP Chat Format verification passed: " + checks + " checks.");
    }

    private static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
        checks++;
    }
}
