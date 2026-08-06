package com.codebyriley.laziestae2.integration.ae2;

import appeng.me.cluster.implementations.CraftingCPUCluster;
import com.codebyriley.laziestae2.LaziestAE2;
import java.lang.reflect.Field;

/**
 * Reflective top-up of a crafting CPU's co-processor count.
 *
 * AE2 counts accelerators with a literal increment — {@code addTile} does
 * {@code accelerator++} rather than asking the tile how much it is worth — so a
 * compressed accelerator cannot report its own tier through any supported path.
 * Instead each tile adds its surplus directly, immediately before AE2 adds the
 * one it already counts.
 *
 * The cluster resets the field in its constructor and a new cluster is built on
 * every structure change, so the surplus re-applies per rebuild rather than
 * accumulating.
 *
 * Fails soft: if AE2's internals ever change shape, compressed accelerators
 * quietly degrade to one co-processor each rather than breaking the CPU.
 */
public final class CraftingCpuAccelerators {

    private static Field acceleratorField;
    private static boolean available;

    static {
        try {
            acceleratorField = CraftingCPUCluster.class.getDeclaredField("accelerator");
            acceleratorField.setAccessible(true);
            available = true;
        } catch (Throwable t) {
            available = false;
            LaziestAE2.logger.warn("AE2 co-processor count unreachable; "
                    + "compressed accelerators will each count as one", t);
        }
    }

    private CraftingCpuAccelerators() { }

    public static boolean isAvailable() {
        return available;
    }

    /**
     * Adds {@code surplus} co-processors to a cluster being assembled. Called
     * from a tile's {@code updateStatus}, which AE2 invokes with the new cluster
     * just before counting that tile, and with null when the cluster is torn
     * down.
     */
    public static void addCoProcessors(CraftingCPUCluster cluster, int surplus) {
        if (!available || cluster == null || surplus <= 0)
            return;

        try {
            acceleratorField.setInt(cluster, acceleratorField.getInt(cluster) + surplus);
        } catch (Throwable t) {
            available = false;
            LaziestAE2.logger.warn("Disabling compressed accelerator scaling "
                    + "after an AE2 internals failure", t);
        }
    }
}
