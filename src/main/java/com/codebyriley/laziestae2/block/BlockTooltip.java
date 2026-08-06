package com.codebyriley.laziestae2.block;

import java.util.List;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

/**
 * Appends "&lt;unlocalized name&gt;.tooltip" if present, followed by any numbered
 * continuation lines ("...tooltip.2", "...tooltip.3", and so on).
 */
public final class BlockTooltip {

    private static final int MAX_LINES = 8;

    private BlockTooltip() { }

    public static void append(List<String> tooltip, String unlocalizedName) {
        String base = unlocalizedName + ".tooltip";
        String first = translateOrNull(base);
        if (first == null)
            return;

        tooltip.add(EnumChatFormatting.GRAY + first);

        for (int line = 2; line <= MAX_LINES; line++) {
            String extra = translateOrNull(base + "." + line);
            if (extra == null)
                break;

            tooltip.add(EnumChatFormatting.GRAY + extra);
        }
    }

    private static String translateOrNull(String key) {
        String translated = StatCollector.translateToLocal(key);
        return translated == null || translated.equals(key) ? null : translated;
    }
}
