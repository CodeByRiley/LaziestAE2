package com.codebyriley.laziestae2.recipe;

import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class OreDictionaryItemStackMatcher implements ItemStackMatcher {

    private final String oreName;

    public OreDictionaryItemStackMatcher(String oreName) {
        if (oreName == null || oreName.length() == 0)
            throw new IllegalArgumentException("Ore dictionary name cannot be empty");

        this.oreName = oreName;
    }

    @Override
    public boolean matches(ItemStack stack) {
        if (stack == null || stack.stackSize <= 0)
            return false;

        int[] oreIds = OreDictionary.getOreIDs(stack);
        for (int oreId : oreIds) {
            if (oreName.equals(OreDictionary.getOreName(oreId)))
                return true;
        }

        return false;
    }

    @Override
    public ItemStack getDisplayStack() {
        List ores = OreDictionary.getOres(oreName);
        return ores.isEmpty() ? null : ((ItemStack)ores.get(0)).copy();
    }

    @Override
    public java.util.List<ItemStack> getDisplayStacks() {
        List ores = OreDictionary.getOres(oreName);
        java.util.List<ItemStack> stacks = new java.util.ArrayList<ItemStack>(ores.size());
        for (Object ore : ores) {
            stacks.add(((ItemStack)ore).copy());
        }
        return stacks;
    }
}
