package com.codebyriley.laziestae2.integration.nei;

import codechicken.lib.gui.GuiDraw;
import com.codebyriley.laziestae2.block.BlockMachine;
import com.codebyriley.laziestae2.recipe.EnergizerRecipe;
import com.codebyriley.laziestae2.recipe.ProcessingRecipeRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

public class EnergizerRecipeHandler extends LazyMachineRecipeHandler {

    // JEI category background is 90x47; input (4,8), output (64,8), energy label
    // at (3,36), shifted by the centering offset (38,9) plus the 1px slot border.
    private static final int IN_X = 43;
    private static final int IN_Y = 18;
    private static final int OUT_X = 103;
    private static final int OUT_Y = 18;
    private static final int ENERGY_TEXT_X = 41;
    private static final int ENERGY_TEXT_Y = 45;

    @Override
    protected String getTitleKey() {
        return "container.laziestae2.energizer";
    }

    @Override
    public String getGuiTexture() {
        return "laziestae2:textures/jei/energizer.png";
    }

    @Override
    protected int getBackgroundWidth() {
        return 90;
    }

    @Override
    protected int getBackgroundHeight() {
        return 47;
    }

    @Override
    protected int getMachineMetadata() {
        return BlockMachine.ENERGIZER;
    }

    @Override
    public String getOverlayIdentifier() {
        return "laziestae2.energizer";
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (getOverlayIdentifier().equals(outputId)) {
            for (EnergizerRecipe recipe : ProcessingRecipeRegistry.getEnergizerRecipes()) {
                arecipes.add(createCached(recipe));
            }
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        boolean isMachine = isMachineStack(result);
        for (EnergizerRecipe recipe : ProcessingRecipeRegistry.getEnergizerRecipes()) {
            if (isMachine || isSameOutput(recipe.getOutput(), result)) {
                arecipes.add(createCached(recipe));
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        boolean isMachine = isMachineStack(ingredient);
        for (EnergizerRecipe recipe : ProcessingRecipeRegistry.getEnergizerRecipes()) {
            CachedMachineRecipe cached = createCached(recipe);
            if (isMachine || cached.usesIngredient(ingredient)) {
                arecipes.add(cached);
            }
        }
    }

    @Override
    public void drawExtras(int recipe) {
        EnergizerRecipe source = findRecipeForIndex(recipe);
        if (source != null) {
            String label = StatCollector.translateToLocalFormatted(
                    "gui.laziestae2.energizer.energy", source.getEnergyRequired());
            GuiDraw.drawString(label, ENERGY_TEXT_X, ENERGY_TEXT_Y, 0xFFFFFF, true);
        }
    }

    private EnergizerRecipe findRecipeForIndex(int index) {
        // Recipes are appended in registry order for all load paths with a single
        // recipe type, so match by output against the cached entry.
        if (index < 0 || index >= arecipes.size()) {
            return null;
        }

        ItemStack output = ((CachedMachineRecipe)arecipes.get(index)).getResult().item;
        for (EnergizerRecipe recipe : ProcessingRecipeRegistry.getEnergizerRecipes()) {
            if (isSameOutput(recipe.getOutput(), output)) {
                return recipe;
            }
        }
        return null;
    }

    private CachedMachineRecipe createCached(EnergizerRecipe recipe) {
        CachedMachineRecipe cached = new CachedMachineRecipe(recipe.getOutput(), OUT_X, OUT_Y);
        cached.addIngredient(recipe.getInput(), IN_X, IN_Y);
        return cached;
    }
}
