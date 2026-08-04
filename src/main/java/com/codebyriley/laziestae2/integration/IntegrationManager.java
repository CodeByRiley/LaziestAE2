package com.codebyriley.laziestae2.integration;

import com.codebyriley.laziestae2.LaziestAE2;
import cpw.mods.fml.common.Loader;

public final class IntegrationManager {

    private static final String MINETWEAKER_MOD_ID = "MineTweaker3";

    private static boolean neiLoaded;
    private static boolean mineTweakerLoaded;

    private IntegrationManager() {
    }

    public static void initCommon() {
        neiLoaded = Loader.isModLoaded("NotEnoughItems");
        mineTweakerLoaded = Loader.isModLoaded(MINETWEAKER_MOD_ID);

        if (mineTweakerLoaded) {
            try {
                com.codebyriley.laziestae2.integration.minetweaker.MineTweakerIntegration.init();
                LaziestAE2.logger.info("Registered MineTweaker recipe support");
            } catch (Throwable t) {
                // A MineTweaker API change should not stop the mod from loading.
                mineTweakerLoaded = false;
                LaziestAE2.logger.warn("Failed to register MineTweaker support", t);
            }
        }
    }

    public static boolean isNeiLoaded() {
        return neiLoaded;
    }

    public static boolean isMineTweakerLoaded() {
        return mineTweakerLoaded;
    }
}
