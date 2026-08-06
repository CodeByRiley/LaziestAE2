package com.codebyriley.laziestae2.recipe;

import net.minecraft.item.ItemStack;

/**
 * Circuit etching recipe. Inputs are positional, mirroring the inscriber-like
 * slot layout: top, bottom, middle.
 */
public class EtcherRecipe implements ProcessingRecipe {

    public static final int SLOT_TOP = 0;
    public static final int SLOT_BOTTOM = 1;
    public static final int SLOT_MIDDLE = 2;
    public static final int INPUT_COUNT = 3;

    private final ItemStackMatcher[] inputs;
    private final ItemStack output;

    public EtcherRecipe(ItemStackMatcher top, ItemStackMatcher bottom, ItemStackMatcher middle, ItemStack output) {
        if (top == null || bottom == null || middle == null)
            throw new IllegalArgumentException("Etcher recipe inputs cannot be null");

        if (output == null || output.stackSize <= 0)
            throw new IllegalArgumentException("Etcher recipe output cannot be empty");

        this.inputs = new ItemStackMatcher[] { top, bottom, middle };
        this.output = output.copy();
    }

    public boolean matches(ItemStack top, ItemStack bottom, ItemStack middle) {
        return inputs[SLOT_TOP].matches(top)
                && inputs[SLOT_BOTTOM].matches(bottom)
                && inputs[SLOT_MIDDLE].matches(middle);
    }

    public boolean matchesSlot(int slot, ItemStack stack) {
        return slot >= 0 && slot < INPUT_COUNT && inputs[slot].matches(stack);
    }

    public ItemStackMatcher[] getInputs() {
        return inputs.clone();
    }

    public ItemStack getOutput() {
        return output.copy();
    }
}
