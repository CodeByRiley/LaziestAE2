package com.codebyriley.laziestae2.integration.nei;

import com.codebyriley.laziestae2.block.BlockMachine;
import com.codebyriley.laziestae2.recipe.AggregatorRecipe;
import com.codebyriley.laziestae2.recipe.ItemStackMatcher;
import com.codebyriley.laziestae2.recipe.ProcessingRecipeRegistry;
import net.minecraft.item.ItemStack;

public class AggregatorRecipeHandler extends LazyMachineRecipeHandler {

    // JEI category background is 130x34; slots at (4+20i,8) and output (104,8),
    // shifted by the centering offset (18,15) plus the 1px slot border.
    private static final int[][] INPUT_POS = { { 23, 24 }, { 43, 24 }, { 63, 24 } };
    private static final int OUT_X = 123;
    private static final int OUT_Y = 24;

    @Override
    protected String getTitleKey() {
        return "container.laziestae2.aggregator";
    }

    @Override
    public String getGuiTexture() {
        return "laziestae2:textures/jei/aggregator.png";
    }

    @Override
    protected int getBackgroundWidth() {
        return 130;
    }

    @Override
    protected int getBackgroundHeight() {
        return 34;
    }

    @Override
    protected int getMachineMetadata() {
        return BlockMachine.AGGREGATOR;
    }

    @Override
    public String getOverlayIdentifier() {
        return "laziestae2.aggregator";
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (getOverlayIdentifier().equals(outputId)) {
            for (AggregatorRecipe recipe : ProcessingRecipeRegistry.getAggregatorRecipes()) {
                arecipes.add(createCached(recipe));
            }
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        boolean isMachine = isMachineStack(result);
        for (AggregatorRecipe recipe : ProcessingRecipeRegistry.getAggregatorRecipes()) {
            if (isMachine || isSameOutput(recipe.getOutput(), result)) {
                arecipes.add(createCached(recipe));
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        boolean isMachine = isMachineStack(ingredient);
        for (AggregatorRecipe recipe : ProcessingRecipeRegistry.getAggregatorRecipes()) {
            CachedMachineRecipe cached = createCached(recipe);
            if (isMachine || cached.usesIngredient(ingredient)) {
                arecipes.add(cached);
            }
        }
    }

    private CachedMachineRecipe createCached(AggregatorRecipe recipe) {
        CachedMachineRecipe cached = new CachedMachineRecipe(recipe.getOutput(), OUT_X, OUT_Y);
        ItemStackMatcher[] inputs = recipe.getInputs();
        for (int i = 0; i < inputs.length && i < INPUT_POS.length; i++) {
            cached.addIngredient(inputs[i], INPUT_POS[i][0], INPUT_POS[i][1]);
        }
        return cached;
    }
}
