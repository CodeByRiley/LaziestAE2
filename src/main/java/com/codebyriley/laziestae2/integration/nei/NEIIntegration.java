package com.codebyriley.laziestae2.integration.nei;

import codechicken.nei.api.API;

public final class NEIIntegration {

    private static boolean initialized;

    private NEIIntegration() {
    }

    public static void init() {
        if (initialized) {
            return;
        }

        API.registerNEIGuiHandler(new NEIGuiSpaceHandler());

        register(new AggregatorRecipeHandler());
        register(new CentrifugeRecipeHandler());
        register(new EtcherRecipeHandler());
        register(new EnergizerRecipeHandler());

        initialized = true;
    }

    private static void register(LazyMachineRecipeHandler handler) {
        API.registerRecipeHandler(handler);
        API.registerUsageHandler(handler);
    }
}
