package com.arthou.qsmpchatformat;

import com.arthou.qsmpchatformat.client.ChatFormatModListDescription;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(QsmpChatFormatMod.MOD_ID)
public final class QsmpChatFormatMod {
    public static final String MOD_ID = "qsmp_chat_format";

    public QsmpChatFormatMod() {
        MinecraftForge.EVENT_BUS.addListener(ChatFormatHandler::onDatapackSync);
        MinecraftForge.EVENT_BUS.addListener(ChatFormatHandler::onPlayerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(ChatFormatHandler::onPlayerLoggedOut);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, ChatFormatHandler::onServerChat);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ChatFormatModListDescription.register();
        }
    }
}
