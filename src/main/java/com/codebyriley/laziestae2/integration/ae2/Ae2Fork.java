package com.codebyriley.laziestae2.integration.ae2;

import appeng.api.AEApi;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IItemDefinition;
import com.codebyriley.laziestae2.LaziestAE2;
import com.google.common.base.Optional;
import java.lang.reflect.Method;
import net.minecraft.item.ItemStack;

/**
 * Feature probe for GTNH's AE2 Unofficial fork. Both it and stock rv3 use the
 * mod id "appliedenergistics2", and we compile against stock, so fork-only API
 * is reached by reflection and its absence is a normal outcome rather than an
 * error.
 *
 * The fork adds compressed crafting accelerators — 4x, 16x, 64x, 256x, 1024x
 * and 4096x — alongside the plain one that stock rv3 stops at.
 */
public final class Ae2Fork {

    /** Cheapest fork-only accelerator; its presence stands in for the whole ladder. */
    private static final String MARKER_METHOD = "craftingAccelerator4x";

    private static boolean compressedAccelerators;

    static {
        try {
            IBlocks.class.getMethod(MARKER_METHOD);
            compressedAccelerators = true;
            LaziestAE2.logger.info("AE2 Unofficial detected; using its compressed crafting accelerators");
        } catch (NoSuchMethodException e) {
            // Stock rv3. Expected, not a failure.
            compressedAccelerators = false;
        } catch (Throwable t) {
            compressedAccelerators = false;
            LaziestAE2.logger.warn("Could not probe AE2 for compressed accelerators", t);
        }
    }

    private Ae2Fork() { }

    /** True when AE2 offers crafting accelerators above the plain 1x. */
    public static boolean hasCompressedAccelerators() {
        return compressedAccelerators;
    }

    /**
     * Crafting accelerator of the given multiplier, or null when this AE2 has no
     * such tier. A multiplier of 1 is the plain accelerator that stock rv3 has.
     */
    public static ItemStack getCraftingAccelerator(int multiplier) {
        IBlocks blocks = AEApi.instance().definitions().blocks();

        if (multiplier <= 1)
            return toStack(blocks.craftingAccelerator());

        if (!compressedAccelerators)
            return null;

        try {
            Method method = IBlocks.class.getMethod("craftingAccelerator" + multiplier + "x");
            return toStack((IItemDefinition)method.invoke(blocks));
        } catch (NoSuchMethodException e) {
            // A tier this fork does not carry.
            return null;
        } catch (Throwable t) {
            LaziestAE2.logger.warn("Failed to read AE2 crafting accelerator {}x", multiplier, t);
            return null;
        }
    }

    private static ItemStack toStack(IItemDefinition definition) {
        if (definition == null)
            return null;

        Optional<ItemStack> stack = definition.maybeStack(1);
        return stack.isPresent() ? stack.get() : null;
    }
}
