package com.codebyriley.laziestae2.init;

import com.codebyriley.laziestae2.Constants;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerController;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerCraftingCoprocessor;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerFrame;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerIoPort;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerPatternProvider;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerVent;
import com.codebyriley.laziestae2.tile.machines.TileAggregator;
import com.codebyriley.laziestae2.tile.machines.TileCentrifuge;
import com.codebyriley.laziestae2.tile.machines.TileEnergizer;
import com.codebyriley.laziestae2.tile.machines.TileEtcher;
import com.codebyriley.laziestae2.tile.machines.TileFastCrafter;
import com.codebyriley.laziestae2.tile.machines.TileLevelMaintainer;
import cpw.mods.fml.common.registry.GameRegistry;

public final class ModTiles {

    private ModTiles() {
    }

    public static void register() {
        GameRegistry.registerTileEntity(TileAggregator.class, Constants.MOD_ID + ".aggregator");
        GameRegistry.registerTileEntity(TileCentrifuge.class, Constants.MOD_ID + ".centrifuge");
        GameRegistry.registerTileEntity(TileEtcher.class, Constants.MOD_ID + ".etcher");
        GameRegistry.registerTileEntity(TileFastCrafter.class, Constants.MOD_ID + ".fast_crafter");
        GameRegistry.registerTileEntity(TileLevelMaintainer.class, Constants.MOD_ID + ".level_maintainer");
        GameRegistry.registerTileEntity(TileEnergizer.class, Constants.MOD_ID + ".energizer");

        GameRegistry.registerTileEntity(TileMassAssemblerFrame.class, Constants.MOD_ID + ".mass_assembler_frame");
        GameRegistry.registerTileEntity(TileMassAssemblerVent.class, Constants.MOD_ID + ".mass_assembler_vent");
        GameRegistry.registerTileEntity(TileMassAssemblerController.class, Constants.MOD_ID + ".mass_assembler_controller");
        GameRegistry.registerTileEntity(TileMassAssemblerPatternProvider.class, Constants.MOD_ID + ".mass_assembler_pattern_provider");
        GameRegistry.registerTileEntity(TileMassAssemblerCraftingCoprocessor.class, Constants.MOD_ID + ".mass_assembler_crafting_coprocessor");
        GameRegistry.registerTileEntity(TileMassAssemblerIoPort.class, Constants.MOD_ID + ".mass_assembler_io_port");
    }
}
