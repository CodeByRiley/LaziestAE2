package com.codebyriley.laziestae2.recipe;

import net.minecraft.item.ItemStack;

public class AggregatorRecipe implements ProcessingRecipe {

    public static final int INPUT_COUNT = 3;

    private final ItemStackMatcher[] inputs;
    private final ItemStack output;

    public AggregatorRecipe(ItemStackMatcher inputA, ItemStackMatcher inputB, ItemStackMatcher inputC, ItemStack output) {
        if (inputA == null || inputB == null || inputC == null)
            throw new IllegalArgumentException("Aggregator recipe inputs cannot be null");

        if (output == null || output.stackSize <= 0)
            throw new IllegalArgumentException("Aggregator recipe output cannot be empty");

        this.inputs = new ItemStackMatcher[] { inputA, inputB, inputC };
        this.output = output.copy();
    }

    public boolean matches(ItemStack inputA, ItemStack inputB, ItemStack inputC) {
        return getMatchingSlots(inputA, inputB, inputC) != null;
    }

    public int[] getMatchingSlots(ItemStack inputA, ItemStack inputB, ItemStack inputC) {
        ItemStack[] stacks = { inputA, inputB, inputC };
        int[] matchedSlots = new int[INPUT_COUNT];
        boolean[] usedSlots = new boolean[INPUT_COUNT];

        return matchInput(0, stacks, usedSlots, matchedSlots) ? matchedSlots : null;
    }

    public ItemStackMatcher[] getInputs() {
        return inputs.clone();
    }

    public ItemStack getOutput() {
        return output.copy();
    }

    private boolean matchInput(int inputIndex, ItemStack[] stacks, boolean[] usedSlots, int[] matchedSlots) {
        if (inputIndex >= inputs.length)
            return true;

        for (int slot = 0; slot < stacks.length; slot++) {
            if (!usedSlots[slot] && inputs[inputIndex].matches(stacks[slot])) {
                usedSlots[slot] = true;
                matchedSlots[inputIndex] = slot;

                if (matchInput(inputIndex + 1, stacks, usedSlots, matchedSlots))
                    return true;

                usedSlots[slot] = false;
            }
        }

        return false;
    }
}
