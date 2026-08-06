package com.codebyriley.laziestae2.recipe;

import net.minecraft.item.ItemStack;

/** What the four processing machines' recipes have in common. */
public interface ProcessingRecipe {

    ItemStack getOutput();
}
