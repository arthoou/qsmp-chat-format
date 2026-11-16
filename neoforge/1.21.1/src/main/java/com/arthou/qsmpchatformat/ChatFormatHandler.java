package com.arthou.qsmpchatformat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.minecraft.ChatFormatting;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Server-side formatting using vanilla packets, without a client requirement. */
public final class ChatFormatHandler {
    private static final String FILTER_NAME = "qsmp_chat_format_presence_filter";

    private ChatFormatHandler() {
    }

    public static void onDatapackSync(OnDatapackSyncEvent event) {
        // PlayerList fires this before the first vanilla join announcement.
        if (event.getPlayer() != null) {
            installFilter(event.getPlayer());
        }
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            installFilter(player);
            player.level().getServer().getPlayerList().broadcastSystemMessage(joinMessage(player.getGameProfile().getName()), false);
        }
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.level().getServer().getPlayerList().broadcastSystemMessage(quitMessage(player.getGameProfile().getName()), false);
        }
    }

    public static void onServerChat(ServerChatEvent event) {
        if (event.isCanceled()) {
            return;
        }
        event.setCanceled(true);
        ServerPlayer player = event.getPlayer();
        Component formatted = chatMessage(player.getDisplayName(), event.getRawText());
        player.level().getServer().getPlayerList().broadcastSystemMessage(formatted,
            recipient -> recipient.getChatVisibility() == ChatVisiblity.FULL ? formatted : null, false);
    }

    private static void installFilter(ServerPlayer player) {
        Connection connection = player.connection.getConnection();
        if (connection.channel() != null && connection.channel().pipeline().get(FILTER_NAME) == null) {
            connection.channel().pipeline().addBefore("packet_handler", FILTER_NAME, new VanillaPresenceFilter());
        }
    }

    static final class VanillaPresenceFilter extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext context, Object message, ChannelPromise promise) throws Exception {
            if (message instanceof ClientboundSystemChatPacket packet && isVanillaJoinOrLeave(packet.content())
                || message instanceof ClientboundDisguisedChatPacket disguised && isVanillaJoinOrLeave(disguised.message())) {
                promise.setSuccess();
                return;
            }
            super.write(context, message, promise);
        }
    }

    static boolean isVanillaJoinOrLeave(Component message) {
        if (!(message.getContents() instanceof TranslatableContents contents)) {
            return false;
        }
        String key = contents.getKey();
        return key.equals("multiplayer.player.left")
            || key.equals("multiplayer.player.joined")
            || key.equals("multiplayer.player.joined.renamed");
    }

    static Component joinMessage(String username) {
        return Component.empty()
            .append(Component.literal("[").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("+").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("] ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(username).withStyle(ChatFormatting.WHITE));
    }

    static Component quitMessage(String username) {
        return Component.empty()
            .append(Component.literal("[").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("-").withStyle(ChatFormatting.RED))
            .append(Component.literal("] ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(username).withStyle(ChatFormatting.WHITE));
    }

    static Component chatMessage(Component displayName, String rawText) {
        MutableComponent message = displayName.copy().withStyle(ChatFormatting.YELLOW);
        message.append(Component.literal(" » ").withStyle(ChatFormatting.DARK_GRAY));
        message.append(Component.literal(rawText).withStyle(ChatFormatting.WHITE));
        return message;
    }
}
