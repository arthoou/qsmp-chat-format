package com.arthou.qsmpchatformat.mixin;

import com.arthou.qsmpchatformat.ChatMessages;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    // Replace the one existing broadcast, including renamed-player joins.
    @ModifyArgs(method = "placeNewPlayer", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"))
    private void qsmpChatFormat$join(Args args, Connection connection, ServerPlayer player) {
        args.set(0, ChatMessages.presence(args.get(0), player.getGameProfile().getName(), true));
    }
}
