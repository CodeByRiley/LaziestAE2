package com.codebyriley.laziestae2.recipe;

import net.minecraft.item.ItemStack;

public interface ItemStackMatcher {

    boolean matches(ItemStack stack);

    ItemStack getDisplayStack();

    /** All stacks this matcher accepts, for cycling recipe displays. */
    java.util.List<ItemStack> getDisplayStacks();
}
