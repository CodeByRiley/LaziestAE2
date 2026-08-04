package com.codebyriley.laziestae2.client.gui;

import com.codebyriley.laziestae2.config.LaziestConfig;
import com.codebyriley.laziestae2.integration.IntegrationManager;
import com.codebyriley.laziestae2.integration.nei.NEIRecipeLauncher;

/**
 * Guarded entry point for NEI lookups triggered from our GUIs. Keeps NEI class
 * references out of the GUI classes so they still load without NEI installed.
 */
public final class NEIGuiBridge {

    private NEIGuiBridge() {
    }

    public static boolean isAvailable() {
        return LaziestConfig.enableNeiIntegration && IntegrationManager.isNeiLoaded();
    }

    public static boolean openRecipes(String identifier) {
        if (identifier == null || !isAvailable()) {
            return false;
        }

        return NEIRecipeLauncher.openRecipes(identifier);
    }

    public static boolean openUsage(String identifier) {
        if (identifier == null || !isAvailable()) {
            return false;
        }

        return NEIRecipeLauncher.openUsage(identifier);
    }
}
