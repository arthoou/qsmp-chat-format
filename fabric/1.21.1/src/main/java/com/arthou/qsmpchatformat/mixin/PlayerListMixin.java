package com.arthou.qsmpchatformat.mixin;

import com.arthou.qsmpchatformat.ChatMessages;
import java.util.function.Function;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Shadow public abstract void broadcastSystemMessage(Component message,
        Function<ServerPlayer, Component> messageFactory, boolean overlay);

    // Modify the existing broadcast so joins are announced exactly once.
    @ModifyArgs(method = "placeNewPlayer", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
    private void qsmpChatFormat$join(Args args, Connection connection, ServerPlayer player, CommonListenerCookie cookie) {
        args.set(0, ChatMessages.presence(args.get(0), player.getGameProfile().getName(), true));
    }

    // Fabric's allow/observe callbacks run at HEAD, before this original call.
    // Command messages have a separate overload and retain their original path.
    @Inject(method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V"),
        cancellable = true)
    private void qsmpChatFormat$chat(PlayerChatMessage message, ServerPlayer sender, ChatType.Bound bound, CallbackInfo ci) {
        if (bound.chatType().is(ChatType.CHAT)) {
            Component formatted = ChatMessages.chat(sender.getDisplayName(), message.signedContent());
            boolean[] fullyFiltered = { false };
            broadcastSystemMessage(formatted, recipient -> {
                boolean filter = sender.shouldFilterMessageTo(recipient);
                if (filter && message.isFullyFiltered()) fullyFiltered[0] = true;
                if (recipient.getChatVisibility() != ChatVisiblity.FULL) return null;
                if (!filter) return formatted;
                String filtered = message.filterMask().apply(message.signedContent());
                if (filtered == null) {
                    fullyFiltered[0] = true;
                    return null;
                }
                return ChatMessages.chat(sender.getDisplayName(), filtered);
            }, false);
            if (fullyFiltered[0]) sender.sendSystemMessage(PlayerList.CHAT_FILTERED_FULL);
            ci.cancel();
        }
    }
}
