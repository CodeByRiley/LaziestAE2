package com.codebyriley.laziestae2.tile.massassembler;

import com.codebyriley.laziestae2.block.BlockMassAssembler;

public enum MassAssemblerPartType {
    FRAME(BlockMassAssembler.FRAME),
    VENT(BlockMassAssembler.VENT),
    CONTROLLER(BlockMassAssembler.CONTROLLER),
    PATTERN_PROVIDER(BlockMassAssembler.PATTERN_PROVIDER),
    CRAFTING_COPROCESSOR(BlockMassAssembler.CRAFTING_COPROCESSOR, 1),
    CRAFTING_COPROCESSOR_4(BlockMassAssembler.CRAFTING_COPROCESSOR_4, 4),
    CRAFTING_COPROCESSOR_16(BlockMassAssembler.CRAFTING_COPROCESSOR_16, 16),
    CRAFTING_COPROCESSOR_64(BlockMassAssembler.CRAFTING_COPROCESSOR_64, 64),
    CRAFTING_COPROCESSOR_256(BlockMassAssembler.CRAFTING_COPROCESSOR_256, 256),
    IO_PORT(BlockMassAssembler.IO_PORT);

    private final int metadata;

    /** Coprocessor units this part contributes; 0 for non-coprocessor parts. */
    private final int weight;

    MassAssemblerPartType(int metadata) {
        this(metadata, 0);
    }

    MassAssemblerPartType(int metadata, int weight) {
        this.metadata = metadata;
        this.weight = weight;
    }

    public int getMetadata() {
        return metadata;
    }

    public int getCoprocessorUnits() {
        return weight;
    }

    public static MassAssemblerPartType fromMetadata(int metadata) {
        for (MassAssemblerPartType type : values()) {
            if (type.metadata == metadata)
                return type;
        }

        return null;
    }
}
