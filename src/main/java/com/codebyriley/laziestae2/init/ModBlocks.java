package com.codebyriley.laziestae2.init;

import com.codebyriley.laziestae2.block.BlockMassAssembler;
import com.codebyriley.laziestae2.block.BlockMachine;
import com.codebyriley.laziestae2.block.ItemBlockMetadata;
import cpw.mods.fml.common.registry.GameRegistry;

public final class ModBlocks {

    public static BlockMachine machine;
    public static BlockMassAssembler bigAssembler;

    private ModBlocks() { }

    public static void register() {
        machine = new BlockMachine();
        bigAssembler = new BlockMassAssembler();

        GameRegistry.registerBlock(machine, ItemBlockMetadata.class, "machine");
        GameRegistry.registerBlock(bigAssembler, ItemBlockMetadata.class, "big_assembler");
    }
}
