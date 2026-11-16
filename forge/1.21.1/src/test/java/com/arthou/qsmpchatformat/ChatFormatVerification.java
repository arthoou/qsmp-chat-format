package com.arthou.qsmpchatformat;

import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

/** Exercises formatting and the real outbound packet filter without launching Minecraft. */
public final class ChatFormatVerification {
    public static void main(String[] args) throws Exception {
        verifyPackMetadata();
        var chat = ChatFormatHandler.chatMessage(Component.literal("arthouxyz"), "Hello, everyone!");
        require(chat.getString().equals("arthouxyz » Hello, everyone!"), "chat format");
        require(chat.getStyle().getColor().getValue() == ChatFormatting.YELLOW.getColor(), "yellow name");
        require(chat.getSiblings().get(0).getStyle().getColor().getValue() == ChatFormatting.DARK_GRAY.getColor(), "dark gray separator");
        require(chat.getSiblings().get(1).getStyle().getColor().getValue() == ChatFormatting.WHITE.getColor(), "white message");
        require(ChatFormatHandler.joinMessage("arthouxyz").getString().equals("[+] arthouxyz"), "join format");
        require(ChatFormatHandler.quitMessage("arthouxyz").getString().equals("[-] arthouxyz"), "leave format");
        require(ChatFormatHandler.joinMessage("arthouxyz").getSiblings().get(1).getStyle().getColor().getValue() == ChatFormatting.GREEN.getColor(), "green join marker");
        require(ChatFormatHandler.quitMessage("arthouxyz").getSiblings().get(1).getStyle().getColor().getValue() == ChatFormatting.RED.getColor(), "red leave marker");

        EmbeddedChannel channel = new EmbeddedChannel(new ChatFormatHandler.VanillaPresenceFilter());
        for (String key : new String[] {"multiplayer.player.joined", "multiplayer.player.joined.renamed", "multiplayer.player.left"}) {
            require(!channel.writeOutbound(new ClientboundSystemChatPacket(Component.translatable(key, "arthouxyz"), false)), "suppress " + key);
        }
        var literal = new ClientboundSystemChatPacket(Component.literal("arthouxyz joined the game"), false);
        require(channel.writeOutbound(literal) && channel.readOutbound() == literal, "preserve literal system message");
        var advancement = new ClientboundSystemChatPacket(Component.translatable("chat.type.advancement.task", "arthouxyz", "Stone Age"), false);
        require(channel.writeOutbound(advancement) && channel.readOutbound() == advancement, "preserve other translation");
        var customJoin = new ClientboundSystemChatPacket(ChatFormatHandler.joinMessage("arthouxyz"), false);
        require(channel.writeOutbound(customJoin) && channel.readOutbound() == customJoin, "preserve formatted join exactly once");
        Object unrelated = new Object();
        require(channel.writeOutbound(unrelated) && channel.readOutbound() == unrelated, "preserve other packets");
        channel.finishAndReleaseAll();
        System.out.println("QSMP Chat Format verification passed: pack JSON and format, text, colors, join/leave suppression, unrelated messages, and formatted message passthrough.");
    }

    private static void verifyPackMetadata() throws Exception {
        try (var stream = ChatFormatVerification.class.getResourceAsStream("/pack.mcmeta")) {
            require(stream != null, "pack.mcmeta is packaged");
            var root = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(
                stream, java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
            var metadata = root.getAsJsonObject("pack");
            require(metadata.get("pack_format").getAsInt() == 34, "Minecraft 1.21.1 resource format");
            require(!metadata.get("description").getAsString().isBlank(), "resource pack description");
        }
    }

    private static void require(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }
}
