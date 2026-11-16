package com.arthou.qsmpchatformat.mixin;

import com.arthou.qsmpchatformat.ChatMessages;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import java.util.function.Function;
import net.minecraft.network.chat.TranslatableComponent;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerMixin {
    @Shadow public ServerPlayer player;

    @ModifyArg(method = "onDisconnect", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"), index = 0)
    private Component qsmpChatFormat$leave(Component original) {
        return ChatMessages.presence(original, player.getGameProfile().getName(), false);
    }

    // Change only the normal-chat broadcast after validation and command dispatch.
    // Wrap the original per-player factory so text filtering and null suppression survive.
    @ModifyArgs(method = "handleChat(Lnet/minecraft/server/network/TextFilter$FilteredText;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Ljava/util/function/Function;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"))
    private void qsmpChatFormat$chat(Args args) {
        Component original = args.get(0);
        Function<ServerPlayer, Component> originalFactory = args.get(1);
        args.set(0, qsmpChatFormat$format(original));
        args.set(1, (Function<ServerPlayer, Component>) recipient -> qsmpChatFormat$format(originalFactory.apply(recipient)));
    }

    private Component qsmpChatFormat$format(Component original) {
        if (original instanceof TranslatableComponent translated && translated.getKey().equals("chat.type.text")) {
            Object[] args = translated.getArgs();
            if (args.length == 2) {
                String text = args[1] instanceof Component component ? component.getString() : String.valueOf(args[1]);
                return ChatMessages.chat(player.getDisplayName(), text);
            }
        }
        return original;
    }
}
