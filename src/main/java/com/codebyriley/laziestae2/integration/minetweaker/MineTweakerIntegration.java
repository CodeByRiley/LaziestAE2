package com.codebyriley.laziestae2.integration.minetweaker;

import minetweaker.MineTweakerAPI;

/**
 * Only referenced once MineTweaker is confirmed loaded, so the MineTweaker
 * classes are never resolved when the mod is absent.
 */
public final class MineTweakerIntegration {

    private MineTweakerIntegration() {
    }

    public static void init() {
        MineTweakerAPI.registerClass(MTAggregator.class);
        MineTweakerAPI.registerClass(MTCentrifuge.class);
        MineTweakerAPI.registerClass(MTEtcher.class);
        MineTweakerAPI.registerClass(MTEnergizer.class);
    }
}
