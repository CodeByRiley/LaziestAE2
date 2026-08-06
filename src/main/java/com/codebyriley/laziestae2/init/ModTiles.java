package com.codebyriley.laziestae2.init;

import com.codebyriley.laziestae2.Constants;
import com.codebyriley.laziestae2.integration.ae2.Ae2Fork;
import com.codebyriley.laziestae2.tile.accelerator.TileCompressedAccelerator1024;
import com.codebyriley.laziestae2.tile.accelerator.TileCompressedAccelerator16;
import com.codebyriley.laziestae2.tile.accelerator.TileCompressedAccelerator256;
import com.codebyriley.laziestae2.tile.accelerator.TileCompressedAccelerator4;
import com.codebyriley.laziestae2.tile.accelerator.TileCompressedAccelerator4096;
import com.codebyriley.laziestae2.tile.accelerator.TileCompressedAccelerator64;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerController;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerCraftingCoprocessor;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerCraftingCoprocessor16;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerCraftingCoprocessor256;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerCraftingCoprocessor4;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerCraftingCoprocessor64;
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

    private ModTiles() { }

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
        GameRegistry.registerTileEntity(TileMassAssemblerCraftingCoprocessor4.class, Constants.MOD_ID + ".mass_assembler_crafting_coprocessor_4");
        GameRegistry.registerTileEntity(TileMassAssemblerCraftingCoprocessor16.class, Constants.MOD_ID + ".mass_assembler_crafting_coprocessor_16");
        GameRegistry.registerTileEntity(TileMassAssemblerCraftingCoprocessor64.class, Constants.MOD_ID + ".mass_assembler_crafting_coprocessor_64");
        GameRegistry.registerTileEntity(TileMassAssemblerCraftingCoprocessor256.class, Constants.MOD_ID + ".mass_assembler_crafting_coprocessor_256");
        GameRegistry.registerTileEntity(TileMassAssemblerIoPort.class, Constants.MOD_ID + ".mass_assembler_io_port");

        registerAccelerators();
    }

    /** Only registered alongside the blocks, which stock rv3 alone gets. */
    private static void registerAccelerators() {
        if (Ae2Fork.hasCompressedAccelerators())
            return;

        GameRegistry.registerTileEntity(TileCompressedAccelerator4.class, Constants.MOD_ID + ".accelerator_4x");
        GameRegistry.registerTileEntity(TileCompressedAccelerator16.class, Constants.MOD_ID + ".accelerator_16x");
        GameRegistry.registerTileEntity(TileCompressedAccelerator64.class, Constants.MOD_ID + ".accelerator_64x");
        GameRegistry.registerTileEntity(TileCompressedAccelerator256.class, Constants.MOD_ID + ".accelerator_256x");
        GameRegistry.registerTileEntity(TileCompressedAccelerator1024.class, Constants.MOD_ID + ".accelerator_1024x");
        GameRegistry.registerTileEntity(TileCompressedAccelerator4096.class, Constants.MOD_ID + ".accelerator_4096x");
    }
}
