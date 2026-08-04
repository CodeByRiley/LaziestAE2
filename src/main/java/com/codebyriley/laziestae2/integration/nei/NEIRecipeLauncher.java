package com.codebyriley.laziestae2.integration.nei;

import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiUsageRecipe;

/**
 * Opens NEI recipe screens from our own GUIs. Only referenced behind an
 * {@code IntegrationManager.isNeiLoaded()} check, so the class (and its NEI
 * imports) never load when NEI is absent.
 */
public final class NEIRecipeLauncher {

    private NEIRecipeLauncher() {
    }

    public static boolean openRecipes(String identifier) {
        return GuiCraftingRecipe.openRecipeGui(identifier);
    }

    public static boolean openUsage(String identifier) {
        return GuiUsageRecipe.openRecipeGui(identifier);
    }
}
