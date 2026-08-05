package com.codebyriley.laziestae2.integration.wdmla;

import com.codebyriley.laziestae2.Constants;
import net.minecraft.util.ResourceLocation;

/**
 * Provider ids for the WDMla integration. WDMla derives config categories and
 * language keys from these, so the paths must stay lowercase snake_case.
 */
public final class WdmlaIdentifiers {

    public static final String NAMESPACE = Constants.MOD_ID;

    public static final ResourceLocation MACHINE = id("machine");
    public static final ResourceLocation FAST_CRAFTER = id("fast_crafter");
    public static final ResourceLocation LEVEL_MAINTAINER = id("level_maintainer");
    public static final ResourceLocation MASS_ASSEMBLER = id("mass_assembler");
    public static final ResourceLocation SIDE_CONFIG = id("side_config");

    private WdmlaIdentifiers() {
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(NAMESPACE, path);
    }
}
