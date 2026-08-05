package com.codebyriley.laziestae2.recipe;

import net.minecraft.item.ItemStack;

public class EnergizerRecipe {

    private final ItemStackMatcher input;
    private final int energyRequired;
    private final ItemStack output;

    public EnergizerRecipe(ItemStackMatcher input, int energyRequired, ItemStack output) {
        if (input == null)
            throw new IllegalArgumentException("Energizer recipe input cannot be null");

        if (energyRequired <= 0)
            throw new IllegalArgumentException("Energizer recipe energy must be positive");

        if (output == null || output.stackSize <= 0)
            throw new IllegalArgumentException("Energizer recipe output cannot be empty");

        this.input = input;
        this.energyRequired = energyRequired;
        this.output = output.copy();
    }

    public boolean matches(ItemStack stack) {
        return input.matches(stack);
    }

    public ItemStackMatcher getInput() {
        return input;
    }

    public int getEnergyRequired() {
        return energyRequired;
    }

    public ItemStack getOutput() {
        return output.copy();
    }
}
