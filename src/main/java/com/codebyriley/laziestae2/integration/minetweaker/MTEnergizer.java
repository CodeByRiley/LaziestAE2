package com.codebyriley.laziestae2.integration.minetweaker;

import com.codebyriley.laziestae2.recipe.EnergizerRecipe;
import com.codebyriley.laziestae2.recipe.ItemStackMatcher;
import com.codebyriley.laziestae2.recipe.ProcessingRecipeRegistry;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.laziestae2.Energizer")
public class MTEnergizer {

    @ZenMethod
    public static void addRecipe(IItemStack output, IIngredient input, int energy) {
        final ItemStack result = MTHelper.toStack(output);
        final ItemStackMatcher matcher = MTHelper.toMatcher(input);

        if (result == null || matcher == null) {
            MineTweakerAPI.logError("Energizer: recipe needs an output and an input");
            return;
        }

        if (energy <= 0) {
            MineTweakerAPI.logError("Energizer: energy cost must be positive");
            return;
        }

        MineTweakerAPI.apply(new MTAction("Adding energizer recipe for " + result.getDisplayName()) {
            @Override
            public void apply() {
                ProcessingRecipeRegistry.addEnergizerRecipe(new EnergizerRecipe(matcher, energy, result));
            }
        });
    }

    @ZenMethod
    public static void removeRecipe(IIngredient output) {
        MTHelper.removeAll(output, "energizer", new MTHelper.Remover() {
            @Override
            public void remove(ItemStack stack) {
                ProcessingRecipeRegistry.removeEnergizerRecipes(stack);
            }
        });
    }
}
