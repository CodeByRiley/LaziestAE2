package com.codebyriley.laziestae2.tile.accelerator;

import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.tile.crafting.TileCraftingTile;
import com.codebyriley.laziestae2.integration.ae2.CraftingCpuAccelerators;

/**
 * Crafting accelerator worth more than one co-processor. Registered only when
 * AE2 is stock rv3; GTNH's fork ships its own compressed accelerators.
 *
 * AE2's cluster calculator accepts any {@link TileCraftingTile}, so these join a
 * real crafting CPU. It reads what a tile *is* out of block metadata, which also
 * carries AE2's own powered and formed bits; since each tier here is its own
 * block, that reading is meaningless for us and this class answers for itself
 * instead.
 */
public abstract class TileCompressedAccelerator extends TileCraftingTile {

    private final int coProcessors;

    protected TileCompressedAccelerator(int coProcessors) {
        this.coProcessors = coProcessors;
    }

    /** Metadata says otherwise — bits 0-1 are AE2's type field, which we do not use. */
    @Override
    public boolean isAccelerator() {
        return true;
    }

    /**
     * AE2 calls this with the cluster being assembled just before counting this
     * tile as a single accelerator, and with null when the cluster is destroyed.
     */
    @Override
    public void updateStatus(CraftingCPUCluster cluster) {
        super.updateStatus(cluster);
        CraftingCpuAccelerators.addCoProcessors(cluster, coProcessors - 1);
    }
}
