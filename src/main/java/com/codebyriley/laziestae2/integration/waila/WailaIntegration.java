package com.codebyriley.laziestae2.integration.waila;

import com.codebyriley.laziestae2.Constants;
import com.codebyriley.laziestae2.tile.base.ISideConfigurable;
import com.codebyriley.laziestae2.tile.base.TileMachine;
import com.codebyriley.laziestae2.tile.machines.TileFastCrafter;
import com.codebyriley.laziestae2.tile.machines.TileLevelMaintainer;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerController;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerPart;
import cpw.mods.fml.common.event.FMLInterModComms;
import mcp.mobius.waila.api.IWailaRegistrar;

/**
 * Fallback for players running plain Waila rather than WDMla.
 * <p>
 * WDMla claims the "Waila" mod id and keeps the legacy API, so it would run this
 * registration too. {@code WdmlaPlugin} names {@link #REGISTRATION_METHOD} as the
 * method it overrides, which makes WDMla drop the IMC request and leaves only its
 * own richer providers registered.
 */
public final class WailaIntegration {

    /** Mod id claimed by Waila, and by WDMla for backwards compatibility. */
    public static final String WAILA_MOD_ID = "Waila";

    public static final String REGISTRATION_METHOD =
            "com.codebyriley.laziestae2.integration.waila.WailaIntegration.register";

    static final String CONFIG_PREFIX = Constants.MOD_ID + ".";

    private static final String CONFIG_CATEGORY = Constants.NAME;

    private WailaIntegration() { }

    /** Asks Waila to call {@link #register(IWailaRegistrar)} during its IMC phase. */
    public static void sendRegistrationRequest() {
        FMLInterModComms.sendMessage(WAILA_MOD_ID, "register", REGISTRATION_METHOD);
    }

    @SuppressWarnings("unused") // Invoked reflectively by Waila.
    public static void register(IWailaRegistrar registrar) {
        register(registrar, WailaTooltipProvider.MACHINE, "Machines", TileMachine.class);
        register(registrar, WailaTooltipProvider.FAST_CRAFTER, "Preemptive Assembly Unit", TileFastCrafter.class);
        register(registrar, WailaTooltipProvider.LEVEL_MAINTAINER, "ME Level Maintainer", TileLevelMaintainer.class);
        register(registrar, WailaTooltipProvider.MASS_ASSEMBLER, "Mass Assembly Chamber",
                TileMassAssemblerController.class, TileMassAssemblerPart.class);

        // Face IO is read from the client tile, so this one needs no NBT provider.
        registrar.addConfig(CONFIG_CATEGORY, WailaTooltipProvider.SIDE_CONFIG.getConfigKey(),
                "Face Configuration (sneak)", true);
        registrar.registerBodyProvider(WailaTooltipProvider.SIDE_CONFIG, ISideConfigurable.class);
    }

    private static void register(IWailaRegistrar registrar, WailaTooltipProvider provider, String label,
            Class<?>... targets) {
        registrar.addConfig(CONFIG_CATEGORY, provider.getConfigKey(), label, true);

        for (Class<?> target : targets) {
            registrar.registerBodyProvider(provider, target);
            registrar.registerNBTProvider(provider, target);
        }
    }
}
