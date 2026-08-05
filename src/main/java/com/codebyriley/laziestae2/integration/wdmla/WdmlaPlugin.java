package com.codebyriley.laziestae2.integration.wdmla;

import com.codebyriley.laziestae2.Constants;
import com.codebyriley.laziestae2.block.BlockMachine;
import com.codebyriley.laziestae2.block.BlockMassAssembler;
import com.codebyriley.laziestae2.integration.waila.WailaIntegration;
import com.codebyriley.laziestae2.tile.base.TileMachine;
import com.codebyriley.laziestae2.tile.machines.TileFastCrafter;
import com.codebyriley.laziestae2.tile.machines.TileLevelMaintainer;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerController;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerPart;
import com.gtnewhorizons.wdmla.api.IWDMlaClientRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaCommonRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaPlugin;
import com.gtnewhorizons.wdmla.api.WDMlaPlugin;

/**
 * Entry point for WDMla, the GTNewHorizons successor to Waila.
 * <p>
 * WDMla discovers this class by scanning for the annotation, so nothing here is
 * touched unless WDMla is installed; no {@code Loader.isModLoaded} guard is needed.
 * Data providers are registered against tile classes and component providers
 * against block classes, as the API requires.
 */
@SuppressWarnings("unused")
@WDMlaPlugin(
        uid = Constants.MOD_ID,
        dependencies = { Constants.MOD_ID },
        // WDMla also answers to the "Waila" mod id, so it would otherwise run our
        // legacy registration as well and show every line twice.
        overridingRegistrationMethodName = WailaIntegration.REGISTRATION_METHOD)
public class WdmlaPlugin implements IWDMlaPlugin {

    @Override
    public void register(IWDMlaCommonRegistration registration) {
        registration.registerBlockDataProvider(MachineProvider.INSTANCE, TileMachine.class);
        registration.registerBlockDataProvider(FastCrafterProvider.INSTANCE, TileFastCrafter.class);
        registration.registerBlockDataProvider(LevelMaintainerProvider.INSTANCE, TileLevelMaintainer.class);
        registration.registerBlockDataProvider(MassAssemblerProvider.INSTANCE, TileMassAssemblerController.class);
        registration.registerBlockDataProvider(MassAssemblerProvider.INSTANCE, TileMassAssemblerPart.class);
    }

    @Override
    public void registerClient(IWDMlaClientRegistration registration) {
        registration.registerBlockComponent(MachineProvider.INSTANCE, BlockMachine.class);
        registration.registerBlockComponent(FastCrafterProvider.INSTANCE, BlockMachine.class);
        registration.registerBlockComponent(LevelMaintainerProvider.INSTANCE, BlockMachine.class);
        registration.registerBlockComponent(SideConfigProvider.INSTANCE, BlockMachine.class);
        registration.registerBlockComponent(MassAssemblerProvider.INSTANCE, BlockMassAssembler.class);
    }
}
