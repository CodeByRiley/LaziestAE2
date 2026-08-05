package com.codebyriley.laziestae2.recipe;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class ExactItemStackMatcher implements ItemStackMatcher {

    private final ItemStack expected;

    public ExactItemStackMatcher(ItemStack expected) {
        if (expected == null)
            throw new IllegalArgumentException("Expected stack cannot be null");

        this.expected = expected.copy();
        this.expected.stackSize = 1;
    }

    @Override
    public boolean matches(ItemStack stack) {
        if (stack == null || stack.stackSize <= 0 || stack.getItem() != expected.getItem())
            return false;

        if (expected.getItemDamage() != OreDictionary.WILDCARD_VALUE && stack.getItemDamage() != expected.getItemDamage())
            return false;

        return !expected.hasTagCompound() || ItemStack.areItemStackTagsEqual(expected, stack);
    }

    @Override
    public ItemStack getDisplayStack() {
        return expected.copy();
    }

    @Override
    public java.util.List<ItemStack> getDisplayStacks() {
        return java.util.Collections.singletonList(expected.copy());
    }
}
