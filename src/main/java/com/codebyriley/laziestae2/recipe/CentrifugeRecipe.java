package com.codebyriley.laziestae2.recipe;

import net.minecraft.item.ItemStack;

public class CentrifugeRecipe {

    private final ItemStackMatcher input;
    private final ItemStack output;

    public CentrifugeRecipe(ItemStackMatcher input, ItemStack output) {
        if (input == null) {
            throw new IllegalArgumentException("Centrifuge recipe input cannot be null");
        }

        if (output == null || output.stackSize <= 0) {
            throw new IllegalArgumentException("Centrifuge recipe output cannot be empty");
        }

        this.input = input;
        this.output = output.copy();
    }

    public boolean matches(ItemStack stack) {
        return input.matches(stack);
    }

    public ItemStackMatcher getInput() {
        return input;
    }

    public ItemStack getOutput() {
        return output.copy();
    }
}
