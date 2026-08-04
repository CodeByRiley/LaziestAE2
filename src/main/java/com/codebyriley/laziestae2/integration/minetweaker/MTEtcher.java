package com.codebyriley.laziestae2.integration.minetweaker;

import com.codebyriley.laziestae2.recipe.EtcherRecipe;
import com.codebyriley.laziestae2.recipe.ItemStackMatcher;
import com.codebyriley.laziestae2.recipe.OreDictionaryItemStackMatcher;
import com.codebyriley.laziestae2.recipe.ProcessingRecipeRegistry;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.laziestae2.Etcher")
public class MTEtcher {

    /** Full form: the etcher's top, bottom and middle inputs are positional. */
    @ZenMethod
    public static void addRecipe(IItemStack output, IIngredient top, IIngredient bottom, IIngredient middle) {
        final ItemStack result = MTHelper.toStack(output);
        final ItemStackMatcher topMatcher = MTHelper.toMatcher(top);
        final ItemStackMatcher bottomMatcher = MTHelper.toMatcher(bottom);
        final ItemStackMatcher middleMatcher = MTHelper.toMatcher(middle);

        if (result == null || topMatcher == null || bottomMatcher == null || middleMatcher == null) {
            MineTweakerAPI.logError("Etcher: recipe needs an output and three inputs");
            return;
        }

        MineTweakerAPI.apply(new MTAction("Adding etcher recipe for " + result.getDisplayName()) {
            @Override
            public void apply() {
                ProcessingRecipeRegistry.addEtcherRecipe(
                        new EtcherRecipe(topMatcher, bottomMatcher, middleMatcher, result));
            }
        });
    }

    /** Short form using the default redstone and silicon press inputs. */
    @ZenMethod
    public static void addRecipe(IItemStack output, IIngredient middle) {
        final ItemStack result = MTHelper.toStack(output);
        final ItemStackMatcher middleMatcher = MTHelper.toMatcher(middle);

        if (result == null || middleMatcher == null) {
            MineTweakerAPI.logError("Etcher: recipe needs an output and an input");
            return;
        }

        MineTweakerAPI.apply(new MTAction("Adding etcher recipe for " + result.getDisplayName()) {
            @Override
            public void apply() {
                ProcessingRecipeRegistry.addEtcherRecipe(new EtcherRecipe(
                        new OreDictionaryItemStackMatcher("dustRedstone"),
                        new OreDictionaryItemStackMatcher("itemSilicon"),
                        middleMatcher,
                        result));
            }
        });
    }

    @ZenMethod
    public static void removeRecipe(IIngredient output) {
        for (final ItemStack stack : MTHelper.toStacks(output)) {
            MineTweakerAPI.apply(new MTAction("Removing etcher recipes for " + stack.getDisplayName()) {
                @Override
                public void apply() {
                    ProcessingRecipeRegistry.removeEtcherRecipes(stack);
                }
            });
        }
    }
}
