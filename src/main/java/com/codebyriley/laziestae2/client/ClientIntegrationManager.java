package com.codebyriley.laziestae2.client;

import com.codebyriley.laziestae2.config.LaziestConfig;
import com.codebyriley.laziestae2.integration.IntegrationManager;
import com.codebyriley.laziestae2.integration.nei.NEIIntegration;

public final class ClientIntegrationManager {

    private ClientIntegrationManager() { }

    public static void init() {
        if (LaziestConfig.enableNeiIntegration && IntegrationManager.isNeiLoaded())
            NEIIntegration.init();
    }
}
