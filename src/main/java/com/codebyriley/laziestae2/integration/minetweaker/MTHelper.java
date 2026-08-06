package com.codebyriley.laziestae2.integration.minetweaker;

import com.codebyriley.laziestae2.recipe.ItemStackMatcher;
import java.util.ArrayList;
import java.util.List;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/** Conversion between MineTweaker's item types and ours. */
public final class MTHelper {

    private MTHelper() { }

    public static ItemStack toStack(IItemStack stack) {
        return stack == null ? null : (ItemStack)stack.getInternal();
    }

    /** Flattens an ingredient (item, ore entry, or a union of them) into its stacks. */
    public static List<ItemStack> toStacks(IIngredient ingredient) {
        List<ItemStack> stacks = new ArrayList<ItemStack>();

        if (ingredient == null)
            return stacks;

        for (IItemStack item : ingredient.getItems()) {
            ItemStack stack = toStack(item);
            if (stack != null)
                stacks.add(stack);
        }

        return stacks;
    }

    /**
     * Queues one removal action per stack an ingredient expands to. Every machine
     * removes recipes the same way; only the noun and the registry call differ.
     */
    public static void removeAll(IIngredient output, String machineNoun, final Remover remover) {
        for (final ItemStack stack : toStacks(output)) {
            MineTweakerAPI.apply(new MTAction("Removing " + machineNoun + " recipes for "
                    + stack.getDisplayName()) {
                @Override
                public void apply() {
                    remover.remove(stack);
                }
            });
        }
    }

    /** The one registry call that differs between machines. */
    public interface Remover {

        void remove(ItemStack output);
    }

    public static ItemStackMatcher toMatcher(IIngredient ingredient) {
        List<ItemStack> stacks = toStacks(ingredient);
        return stacks.isEmpty() ? null : new IngredientMatcher(stacks);
    }

    /** Matches any of the stacks an ingredient expands to. */
    private static class IngredientMatcher implements ItemStackMatcher {

        private final List<ItemStack> options;

        IngredientMatcher(List<ItemStack> options) {
            this.options = options;
        }

        @Override
        public boolean matches(ItemStack stack) {
            if (stack == null || stack.stackSize <= 0)
                return false;

            for (ItemStack option : options) {
                if (option.getItem() != stack.getItem())
                    continue;

                if (option.getItemDamage() == OreDictionary.WILDCARD_VALUE
                        || option.getItemDamage() == stack.getItemDamage()) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public ItemStack getDisplayStack() {
            return options.isEmpty() ? null : options.get(0).copy();
        }

        @Override
        public List<ItemStack> getDisplayStacks() {
            List<ItemStack> copies = new ArrayList<ItemStack>(options.size());
            for (ItemStack option : options) {
                copies.add(option.copy());
            }
            return copies;
        }
    }
}
