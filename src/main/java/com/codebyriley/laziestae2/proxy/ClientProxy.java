package com.codebyriley.laziestae2.proxy;

import com.codebyriley.laziestae2.client.gui.ClientGuiHandler;
import com.codebyriley.laziestae2.client.ClientIntegrationManager;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.network.IGuiHandler;

public class ClientProxy extends CommonProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        ClientIntegrationManager.init();
    }

    @Override
    protected IGuiHandler createGuiHandler() {
        return new ClientGuiHandler();
    }
}
