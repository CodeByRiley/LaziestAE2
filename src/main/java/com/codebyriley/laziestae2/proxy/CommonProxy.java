package com.codebyriley.laziestae2.proxy;

import com.codebyriley.laziestae2.LaziestAE2;
import com.codebyriley.laziestae2.gui.GuiHandler;
import com.codebyriley.laziestae2.integration.IntegrationManager;
import com.codebyriley.laziestae2.network.LazyNetwork;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
    }

    public void init(FMLInitializationEvent event) {
        LazyNetwork.init();
        NetworkRegistry.INSTANCE.registerGuiHandler(LaziestAE2.instance, createGuiHandler());
        IntegrationManager.initCommon();
    }

    public void postInit(FMLPostInitializationEvent event) {
    }

    protected IGuiHandler createGuiHandler() {
        return new GuiHandler();
    }
}
