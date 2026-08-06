package com.codebyriley.laziestae2.integration.minetweaker;

import com.codebyriley.laziestae2.recipe.CentrifugeRecipe;
import com.codebyriley.laziestae2.recipe.ItemStackMatcher;
import com.codebyriley.laziestae2.recipe.ProcessingRecipeRegistry;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.laziestae2.Centrifuge")
public class MTCentrifuge {

    @ZenMethod
    public static void addRecipe(IItemStack output, IIngredient input) {
        final ItemStack result = MTHelper.toStack(output);
        final ItemStackMatcher matcher = MTHelper.toMatcher(input);

        if (result == null || matcher == null) {
            MineTweakerAPI.logError("Centrifuge: recipe needs an output and an input");
            return;
        }

        MineTweakerAPI.apply(new MTAction("Adding centrifuge recipe for " + result.getDisplayName()) {
            @Override
            public void apply() {
                ProcessingRecipeRegistry.addCentrifugeRecipe(new CentrifugeRecipe(matcher, result));
            }
        });
    }

    @ZenMethod
    public static void removeRecipe(IIngredient output) {
        MTHelper.removeAll(output, "centrifuge", new MTHelper.Remover() {
            @Override
            public void remove(ItemStack stack) {
                ProcessingRecipeRegistry.removeCentrifugeRecipes(stack);
            }
        });
    }
}
