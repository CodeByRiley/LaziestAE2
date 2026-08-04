package com.codebyriley.laziestae2.tile.massassembler;

import com.codebyriley.laziestae2.block.BlockMassAssembler;

public enum MassAssemblerPartType {
    FRAME(BlockMassAssembler.FRAME),
    VENT(BlockMassAssembler.VENT),
    CONTROLLER(BlockMassAssembler.CONTROLLER),
    PATTERN_PROVIDER(BlockMassAssembler.PATTERN_PROVIDER),
    CRAFTING_COPROCESSOR(BlockMassAssembler.CRAFTING_COPROCESSOR),
    IO_PORT(BlockMassAssembler.IO_PORT);

    private final int metadata;

    MassAssemblerPartType(int metadata) {
        this.metadata = metadata;
    }

    public int getMetadata() {
        return metadata;
    }

    public static MassAssemblerPartType fromMetadata(int metadata) {
        for (MassAssemblerPartType type : values()) {
            if (type.metadata == metadata) {
                return type;
            }
        }

        return null;
    }
}
