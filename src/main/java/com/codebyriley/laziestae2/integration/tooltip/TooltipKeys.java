package com.codebyriley.laziestae2.integration.tooltip;

import com.codebyriley.laziestae2.Constants;

/**
 * Keys for the synchronisation tag shared by every in-world tooltip front end.
 * <p>
 * All providers looking at one block write into a single compound, and some mods
 * dump a whole tile entity into it, so each section is nested under its own key.
 */
public final class TooltipKeys {

    public static final String MACHINE = Constants.MOD_ID + ":machine";
    public static final String FAST_CRAFTER = Constants.MOD_ID + ":fast_crafter";
    public static final String LEVEL_MAINTAINER = Constants.MOD_ID + ":level_maintainer";
    public static final String MASS_ASSEMBLER = Constants.MOD_ID + ":mass_assembler";

    private TooltipKeys() { }
}
