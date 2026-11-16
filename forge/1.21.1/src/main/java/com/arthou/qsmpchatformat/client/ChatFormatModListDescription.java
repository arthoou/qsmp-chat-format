package com.arthou.qsmpchatformat.client;

import com.arthou.qsmpchatformat.QsmpChatFormatMod;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.ModListScreen;
import net.minecraftforge.client.gui.widget.ModListWidget;
import net.minecraftforge.client.gui.widget.ScrollPanel;
import net.minecraftforge.common.MinecraftForge;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/** Renders the description and author link as styled text in the Mods screen. */
public final class ChatFormatModListDescription {
    private static final String AUTHOR_URL = "https://discord.com/users/990003604725321808";

    /** The raw HTML description opens with the author link. */
    private static final String DESCRIPTION_START = "Changes ";
    private static final int LINK_COLOR = 0x5BC8FF;
    /** Same inset Forge's InfoPanel wraps its text to. */
    private static final int PANEL_TEXT_INSET = 12;

    private static Field selectedField;
    private static Field modInfoField;
    private static Field linesField;
    private static Field panelWidthField;
    private static boolean disabled;
    private static List<FormattedCharSequence> injected;

    private ChatFormatModListDescription() {
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.addListener(ChatFormatModListDescription::onRenderPre);
    }

    @SuppressWarnings("unchecked")
    private static void onRenderPre(ScreenEvent.Render.Pre event) {
        if (disabled || !(event.getScreen() instanceof ModListScreen screen)) {
            return;
        }

        try {
            resolveFields();
            if (!(selectedField.get(screen) instanceof ModListWidget.ModEntry entry)
                || !QsmpChatFormatMod.MOD_ID.equals(entry.getInfo().getModId())) {
                return;
            }

            Object panel = modInfoField.get(screen);
            if (panel == null) {
                return;
            }

            // Forge rebuilds the list whenever the selection changes or the
            // window resizes; only rewrite a list we have not produced ourselves.
            List<FormattedCharSequence> lines = (List<FormattedCharSequence>) linesField.get(panel);
            if (lines == injected || lines.isEmpty()) {
                return;
            }

            int start = findDescriptionStart(lines);
            if (start < 0) {
                return;
            }

            Font font = Minecraft.getInstance().font;
            int wrapWidth = panelWidthField.getInt(panel) - PANEL_TEXT_INSET;
            List<FormattedCharSequence> rewritten = new ArrayList<>(lines.subList(0, start));
            for (Component line : descriptionLines()) {
                if (wrapWidth > 0) {
                    rewritten.addAll(font.split(line, wrapWidth));
                } else {
                    rewritten.add(line.getVisualOrderText());
                }
            }

            injected = rewritten;
            linesField.set(panel, rewritten);
        } catch (ReflectiveOperationException | RuntimeException e) {
            disabled = true;
            LogUtils.getLogger().warn("QSMP Chat Format could not restyle its Mods screen description; leaving it as is", e);
        }
    }

    private static List<Component> descriptionLines() {
        return List.of(
            Component.literal("Changes the chat format to resemble QSMP."),
            Component.literal("Made by ").append(link("Arthou", AUTHOR_URL))
        );
    }

    private static MutableComponent link(String text, String url) {
        return Component.literal(text).withStyle(Style.EMPTY
            .withColor(TextColor.fromRgb(LINK_COLOR))
            .withUnderlined(true)
            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(url))));
    }

    /** The HTML description is the last block in the panel. */
    private static int findDescriptionStart(List<FormattedCharSequence> lines) {
        for (int i = 0; i < lines.size(); i++) {
            FormattedCharSequence line = lines.get(i);
            if (line != null && plainText(line).startsWith(DESCRIPTION_START)) {
                return i;
            }
        }

        return -1;
    }

    private static String plainText(FormattedCharSequence line) {
        StringBuilder text = new StringBuilder();
        line.accept((index, style, codePoint) -> {
            text.appendCodePoint(codePoint);
            return true;
        });
        return text.toString();
    }

    private static void resolveFields() throws ReflectiveOperationException {
        if (selectedField != null) {
            return;
        }

        Field selected = ModListScreen.class.getDeclaredField("selected");
        Field modInfo = ModListScreen.class.getDeclaredField("modInfo");
        Field lines = modInfo.getType().getDeclaredField("lines");
        Field width = ScrollPanel.class.getDeclaredField("width");
        selected.setAccessible(true);
        modInfo.setAccessible(true);
        lines.setAccessible(true);
        width.setAccessible(true);

        modInfoField = modInfo;
        linesField = lines;
        panelWidthField = width;
        selectedField = selected;
    }
}
