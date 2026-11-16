package com.arthou.qsmpchatformat;

import java.lang.reflect.Method;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.network.chat.ChatType;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

/** Focused contract checks against Minecraft's real text components. */
public final class ChatFormatVerification {
    private static int checks;

    public static void main(String[] args) throws Exception {
        check(ChatMessages.join("arthouxyz").getString().equals("[+] arthouxyz"), "join text");
        check(ChatMessages.leave("arthouxyz").getString().equals("[-] arthouxyz"), "leave text");
        Component displayName = new TextComponent("arthouxyz");
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
            check((boolean) filter.invoke(null, new TranslatableComponent(key, "arthouxyz")), "presence key " + key);
        }
        for (Component message : List.of(new TextComponent("multiplayer.player.joined"), new TranslatableComponent("chat.type.announcement", "Server", "hello"), new TranslatableComponent("commands.message.display.incoming", "arthouxyz", "hi"))) {
            check(!(boolean) filter.invoke(null, message), "unrelated messages preserved");
        }
        EmbeddedChannel channel = new EmbeddedChannel(new ChatFormatHandler.VanillaPresenceFilter());
        for (String key : List.of("multiplayer.player.joined", "multiplayer.player.joined.renamed", "multiplayer.player.left")) {
            check(!channel.writeOutbound(new ClientboundChatPacket(new TranslatableComponent(key, "arthouxyz"), ChatType.SYSTEM, Util.NIL_UUID)), "vanilla presence packet suppressed");
        }
        Object literal = new ClientboundChatPacket(new TextComponent("Keep this server message"), ChatType.SYSTEM, Util.NIL_UUID);
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
