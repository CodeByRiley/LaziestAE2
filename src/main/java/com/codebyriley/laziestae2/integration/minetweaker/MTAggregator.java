package com.codebyriley.laziestae2.integration.minetweaker;

import com.codebyriley.laziestae2.recipe.AggregatorRecipe;
import com.codebyriley.laziestae2.recipe.ItemStackMatcher;
import com.codebyriley.laziestae2.recipe.ProcessingRecipeRegistry;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.laziestae2.Aggregator")
public class MTAggregator {

    @ZenMethod
    public static void addRecipe(IItemStack output, IIngredient inputA, IIngredient inputB, IIngredient inputC) {
        final ItemStack result = MTHelper.toStack(output);
        final ItemStackMatcher a = MTHelper.toMatcher(inputA);
        final ItemStackMatcher b = MTHelper.toMatcher(inputB);
        final ItemStackMatcher c = MTHelper.toMatcher(inputC);

        if (result == null || a == null || b == null || c == null) {
            MineTweakerAPI.logError("Aggregator: recipe needs an output and three inputs");
            return;
        }

        MineTweakerAPI.apply(new MTAction("Adding aggregator recipe for " + result.getDisplayName()) {
            @Override
            public void apply() {
                ProcessingRecipeRegistry.addAggregatorRecipe(new AggregatorRecipe(a, b, c, result));
            }
        });
    }

    @ZenMethod
    public static void removeRecipe(IIngredient output) {
        MTHelper.removeAll(output, "aggregator", new MTHelper.Remover() {
            @Override
            public void remove(ItemStack stack) {
                ProcessingRecipeRegistry.removeAggregatorRecipes(stack);
            }
        });
    }
}
