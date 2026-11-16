package com.arthou.qsmpchatformat;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(QsmpChatFormatMod.MOD_ID)
public final class QsmpChatFormatMod {
    public static final String MOD_ID = "qsmp_chat_format";

    public QsmpChatFormatMod() {
        NeoForge.EVENT_BUS.addListener(ChatFormatHandler::onDatapackSync);
        NeoForge.EVENT_BUS.addListener(ChatFormatHandler::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(ChatFormatHandler::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, ChatFormatHandler::onServerChat);
    }
}
