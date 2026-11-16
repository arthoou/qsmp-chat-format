package com.arthou.qsmpchatformat;

import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.chat.ChatType;
import net.minecraft.Util;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Join, leave and chat messages in the QSMP-inspired format:
 * <pre>
 * [+] name
 * [-] name
 * Name » message
 * </pre>
 *
 * Vanilla broadcasts its own yellow "joined/left the game" lines with no event
 * to cancel them, so every player gets a small outbound filter on their
 * connection that drops exactly those translatable messages. Chat is cancelled
 * and re-broadcast as a system message in the new format, which also lands in
 * the server console.
 */
public final class ChatFormatHandler {
    private static final String FILTER_NAME = "qsmp_chat_format_filter";

    private ChatFormatHandler() {
    }

    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            installFilter(event.getPlayer());
        }
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        installFilter(player);
        player.server.getPlayerList().broadcastMessage(ChatMessages.join(player.getGameProfile().getName()), ChatType.SYSTEM, Util.NIL_UUID);
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        player.server.getPlayerList().broadcastMessage(ChatMessages.leave(player.getGameProfile().getName()), ChatType.SYSTEM, Util.NIL_UUID);
    }

    public static void onServerChat(ServerChatEvent event) {
        if (event.isCanceled()) {
            return;
        }
        event.setCanceled(true);
        ServerPlayer player = event.getPlayer();
        player.server.getPlayerList().broadcastMessage(ChatMessages.chat(player.getDisplayName(), event.getMessage()), ChatType.CHAT, player.getUUID());
    }

    private static void installFilter(ServerPlayer player) {
        Connection connection = player.connection.connection;
        if (connection.channel() == null || connection.channel().pipeline().get(FILTER_NAME) != null) {
            return;
        }

        connection.channel().pipeline().addBefore("packet_handler", FILTER_NAME, new VanillaPresenceFilter());
    }

    static final class VanillaPresenceFilter extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext context, Object message, ChannelPromise promise) throws Exception {
            if (message instanceof ClientboundChatPacket packet && isVanillaJoinOrLeave(packet.getMessage())) {
                promise.setSuccess();
                return;
            }

            super.write(context, message, promise);
        }

    }

    private static boolean isVanillaJoinOrLeave(Component message) {
        if (!(message instanceof TranslatableComponent contents)) {
            return false;
        }

        String key = contents.getKey();
        return key.equals("multiplayer.player.left")
            || key.equals("multiplayer.player.joined")
            || key.equals("multiplayer.player.joined.renamed");
    }

}
