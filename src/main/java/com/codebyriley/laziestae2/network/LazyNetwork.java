package com.codebyriley.laziestae2.network;

import com.codebyriley.laziestae2.Constants;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public final class LazyNetwork {

    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(Constants.MOD_ID);

    private LazyNetwork() { }

    public static void init() {
        CHANNEL.registerMessage(MessageLevelMaintainerRequest.Handler.class, MessageLevelMaintainerRequest.class,
                0, Side.SERVER);
        CHANNEL.registerMessage(MessageMachineConfig.Handler.class, MessageMachineConfig.class,
                1, Side.SERVER);
    }
}
