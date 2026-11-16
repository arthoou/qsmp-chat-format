package com.arthou.qsmpchatformat;

import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.launch.knot.Knot;

/** Applies the actual Fabric transformer without starting Minecraft or a world. */
public final class MixinLoadTest {
    public static void main(String[] args) throws Exception {
        System.setProperty("fabric.development", "true");
        ClassLoader gameLoader = new Knot(EnvType.SERVER).init(new String[] {"--nogui"});
        requireInjected(gameLoader, "net.minecraft.server.players.PlayerList", "qsmpChatFormat$join");
        requireInjected(gameLoader, "net.minecraft.server.players.PlayerList", "qsmpChatFormat$chat");
        requireInjected(gameLoader, "net.minecraft.server.network.ServerGamePacketListenerImpl", "qsmpChatFormat$leave");
        System.out.println("All three QSMP chat/presence mixins applied through Fabric Loader");
    }

    private static void requireInjected(ClassLoader loader, String target, String method) throws Exception {
        Class<?> transformed = Class.forName(target, false, loader);
        if (Arrays.stream(transformed.getDeclaredMethods()).noneMatch(m -> m.getName().contains(method))) {
            throw new AssertionError("Mixin was not applied: " + target + " / " + method);
        }
    }
}
