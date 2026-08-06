package com.codebyriley.laziestae2.block;

import com.codebyriley.laziestae2.tile.accelerator.TileCompressedAccelerator1024;
import com.codebyriley.laziestae2.tile.accelerator.TileCompressedAccelerator16;
import com.codebyriley.laziestae2.tile.accelerator.TileCompressedAccelerator256;
import com.codebyriley.laziestae2.tile.accelerator.TileCompressedAccelerator4;
import com.codebyriley.laziestae2.tile.accelerator.TileCompressedAccelerator4096;
import com.codebyriley.laziestae2.tile.accelerator.TileCompressedAccelerator64;
import net.minecraft.tileentity.TileEntity;

/**
 * Maps a co-processor count to its tile. Each tier needs its own class because
 * tile entities are restored by registered class, not by block state.
 */
public final class AcceleratorTiles {

    /** Multipliers, matching the ladder GTNH's AE2 fork uses. */
    public static final int[] TIERS = { 4, 16, 64, 256, 1024, 4096 };

    private AcceleratorTiles() { }

    public static Class<? extends TileEntity> get(int coProcessors) {
        switch (coProcessors) {
            case 4:
                return TileCompressedAccelerator4.class;
            case 16:
                return TileCompressedAccelerator16.class;
            case 64:
                return TileCompressedAccelerator64.class;
            case 256:
                return TileCompressedAccelerator256.class;
            case 1024:
                return TileCompressedAccelerator1024.class;
            case 4096:
                return TileCompressedAccelerator4096.class;
            default:
                return null;
        }
    }
}
