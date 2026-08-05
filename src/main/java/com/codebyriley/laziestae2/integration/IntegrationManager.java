package com.codebyriley.laziestae2.integration;

import com.codebyriley.laziestae2.LaziestAE2;
import com.codebyriley.laziestae2.integration.waila.WailaIntegration;
import cpw.mods.fml.common.Loader;

public final class IntegrationManager {

    private static final String MINETWEAKER_MOD_ID = "MineTweaker3";
    private static final String WDMLA_MOD_ID = "wdmla";

    private static boolean neiLoaded;
    private static boolean mineTweakerLoaded;
    private static boolean wdmlaLoaded;
    private static boolean wailaLoaded;

    private IntegrationManager() {
    }

    public static void initCommon() {
        neiLoaded = Loader.isModLoaded("NotEnoughItems");
        mineTweakerLoaded = Loader.isModLoaded(MINETWEAKER_MOD_ID);
        // WDMla finds our plugin class by annotation scan, so there is nothing to
        // register here; the flag exists for logging and for other integrations.
        wdmlaLoaded = Loader.isModLoaded(WDMLA_MOD_ID);
        // WDMla claims the "Waila" mod id too, and drops this request in favour of
        // its own providers, so the legacy path is only reached on plain Waila.
        wailaLoaded = Loader.isModLoaded(WailaIntegration.WAILA_MOD_ID);

        if (wailaLoaded) {
            try {
                WailaIntegration.sendRegistrationRequest();
                LaziestAE2.logger.info("Requested {} tooltip registration",
                        wdmlaLoaded ? "WDMla" : "Waila");
            } catch (Throwable t) {
                wailaLoaded = false;
                LaziestAE2.logger.warn("Failed to request Waila tooltip registration", t);
            }
        }

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

    public static boolean isWdmlaLoaded() {
        return wdmlaLoaded;
    }

    public static boolean isWailaLoaded() {
        return wailaLoaded;
    }
}
