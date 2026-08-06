package com.codebyriley.laziestae2.init;

import com.codebyriley.laziestae2.block.AcceleratorTiles;
import com.codebyriley.laziestae2.block.BlockCompressedAccelerator;
import com.codebyriley.laziestae2.block.ItemBlockCompressedAccelerator;
import com.codebyriley.laziestae2.block.BlockMassAssembler;
import com.codebyriley.laziestae2.block.BlockMachine;
import com.codebyriley.laziestae2.block.ItemBlockMetadata;
import com.codebyriley.laziestae2.integration.ae2.Ae2Fork;
import cpw.mods.fml.common.registry.GameRegistry;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ModBlocks {

    public static BlockMachine machine;
    public static BlockMassAssembler bigAssembler;

    /** Keyed by co-processor count. Empty when AE2 already provides these. */
    public static final Map<Integer, BlockCompressedAccelerator> accelerators =
            new LinkedHashMap<Integer, BlockCompressedAccelerator>();

    private ModBlocks() { }

    public static void register() {
        machine = new BlockMachine();
        bigAssembler = new BlockMassAssembler();

        GameRegistry.registerBlock(machine, ItemBlockMetadata.class, "machine");
        GameRegistry.registerBlock(bigAssembler, ItemBlockMetadata.class, "big_assembler");

        registerAccelerators();
    }

    /**
     * Compressed crafting accelerators stand in for the ones GTNH's AE2 fork
     * adds, so they only exist on stock rv3. Registering both would leave two
     * parallel ladders doing the same job.
     */
    private static void registerAccelerators() {
        if (Ae2Fork.hasCompressedAccelerators())
            return;

        for (int tier : AcceleratorTiles.TIERS) {
            BlockCompressedAccelerator block = new BlockCompressedAccelerator(tier, AcceleratorTiles.get(tier));
            accelerators.put(tier, block);
            // AE2's own ItemBlock; the vanilla one leaves its blocks without an inventory icon.
            GameRegistry.registerBlock(
                    block, ItemBlockCompressedAccelerator.class, "accelerator_" + tier + "x");
        }
    }
}
