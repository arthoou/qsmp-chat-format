package com.arthou.qsmpchatformat.mixin;

import com.arthou.qsmpchatformat.ChatMessages;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import java.util.function.Function;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Shadow public abstract MinecraftServer getServer();
    @Shadow public abstract void broadcastSystemMessage(Component message,
        Function<ServerPlayer, Component> messageFactory, boolean overlay);

    @ModifyArgs(method = "placeNewPlayer", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
    private void qsmpChatFormat$join(Args args, Connection connection, ServerPlayer player) {
        args.set(0, ChatMessages.presence(args.get(0), player.getGameProfile().getName(), true));
    }

    // This call is reached after Fabric API's cancellable HEAD hooks. Commands use
    // a different overload and other chat types retain their existing decoration.
    @Inject(method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V"), cancellable = true)
    private void qsmpChatFormat$chat(PlayerChatMessage message, ServerPlayer player,
                                    ChatType.Bound bound, CallbackInfo ci) {
        ChatType normalChat = getServer().registryAccess().registryOrThrow(Registries.CHAT_TYPE).get(ChatType.CHAT);
        if (bound.chatType() != normalChat) return;

        Component formatted = ChatMessages.chat(player.getDisplayName(), message.signedContent());
        boolean[] fullyFiltered = { false };
        broadcastSystemMessage(formatted, recipient -> {
            boolean filter = player.shouldFilterMessageTo(recipient);
            if (filter && message.isFullyFiltered()) fullyFiltered[0] = true;
            if (recipient.getChatVisibility() != ChatVisiblity.FULL) return null;
            if (!filter) return formatted;
            String filtered = message.filterMask().apply(message.signedContent());
            return filtered == null ? null : ChatMessages.chat(player.getDisplayName(), filtered);
        }, false);
        if (fullyFiltered[0]) player.sendSystemMessage(PlayerList.CHAT_FILTERED_FULL);
        ci.cancel();
    }
}
