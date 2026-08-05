package com.codebyriley.laziestae2.integration.nei;

import com.codebyriley.laziestae2.block.BlockMachine;
import com.codebyriley.laziestae2.recipe.EtcherRecipe;
import com.codebyriley.laziestae2.recipe.ItemStackMatcher;
import com.codebyriley.laziestae2.recipe.ProcessingRecipeRegistry;
import net.minecraft.item.ItemStack;

public class EtcherRecipeHandler extends LazyMachineRecipeHandler {

    // JEI category background is 113x62; top (4,4), bottom (4,40), middle (27,22),
    // output (87,22), shifted by the centering offset (26,1) plus the 1px slot border.
    private static final int[][] INPUT_POS = { { 31, 6 }, { 31, 42 }, { 54, 24 } };
    private static final int OUT_X = 114;
    private static final int OUT_Y = 24;

    @Override
    protected String getTitleKey() {
        return "container.laziestae2.etcher";
    }

    @Override
    public String getGuiTexture() {
        return "laziestae2:textures/jei/etcher.png";
    }

    @Override
    protected int getBackgroundWidth() {
        return 113;
    }

    @Override
    protected int getBackgroundHeight() {
        return 62;
    }

    @Override
    protected int getMachineMetadata() {
        return BlockMachine.ETCHER;
    }

    @Override
    public String getOverlayIdentifier() {
        return "laziestae2.etcher";
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (getOverlayIdentifier().equals(outputId)) {
            for (EtcherRecipe recipe : ProcessingRecipeRegistry.getEtcherRecipes()) {
                arecipes.add(createCached(recipe));
            }
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        boolean isMachine = isMachineStack(result);
        for (EtcherRecipe recipe : ProcessingRecipeRegistry.getEtcherRecipes()) {
            if (isMachine || isSameOutput(recipe.getOutput(), result))
                arecipes.add(createCached(recipe));
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        boolean isMachine = isMachineStack(ingredient);
        for (EtcherRecipe recipe : ProcessingRecipeRegistry.getEtcherRecipes()) {
            CachedMachineRecipe cached = createCached(recipe);
            if (isMachine || cached.usesIngredient(ingredient))
                arecipes.add(cached);
        }
    }

    private CachedMachineRecipe createCached(EtcherRecipe recipe) {
        CachedMachineRecipe cached = new CachedMachineRecipe(recipe.getOutput(), OUT_X, OUT_Y);
        ItemStackMatcher[] inputs = recipe.getInputs();
        for (int i = 0; i < inputs.length && i < INPUT_POS.length; i++) {
            cached.addIngredient(inputs[i], INPUT_POS[i][0], INPUT_POS[i][1]);
        }
        return cached;
    }
}
