package com.codebyriley.laziestae2.integration.tooltip;

import com.codebyriley.laziestae2.Constants;
import net.minecraft.util.StatCollector;

/** Shared formatting for in-world tooltip lines. */
public final class TooltipText {

    private static final String KEY_PREFIX = "tooltip." + Constants.MOD_ID + ".";

    private TooltipText() { }

    public static String translate(String suffix) {
        return StatCollector.translateToLocal(KEY_PREFIX + suffix);
    }

    public static String format(String suffix, Object... args) {
        return StatCollector.translateToLocalFormatted(KEY_PREFIX + suffix, args);
    }

    /** AE amounts are stored as doubles but only ever displayed as whole units. */
    public static String ae(double amount) {
        return String.format("%,d", Math.round(amount));
    }

    public static String count(long amount) {
        return String.format("%,d", amount);
    }

    public static String onOff(boolean value) {
        return translate(value ? "on" : "off");
    }

    /** Text stand-in for front ends that cannot draw a progress bar. */
    public static String percent(int current, int max) {
        return max <= 0 ? "0%" : (Math.min(100, current * 100 / max) + "%");
    }
}
