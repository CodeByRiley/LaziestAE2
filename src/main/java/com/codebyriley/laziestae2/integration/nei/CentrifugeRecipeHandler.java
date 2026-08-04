package com.codebyriley.laziestae2.integration.nei;

import com.codebyriley.laziestae2.block.BlockMachine;
import com.codebyriley.laziestae2.recipe.CentrifugeRecipe;
import com.codebyriley.laziestae2.recipe.ProcessingRecipeRegistry;
import net.minecraft.item.ItemStack;

public class CentrifugeRecipeHandler extends LazyMachineRecipeHandler {

    // JEI category background is 90x34; input (4,8), output (64,8),
    // shifted by the centering offset (38,15) plus the 1px slot border.
    private static final int IN_X = 43;
    private static final int IN_Y = 24;
    private static final int OUT_X = 103;
    private static final int OUT_Y = 24;

    @Override
    protected String getTitleKey() {
        return "container.laziestae2.centrifuge";
    }

    @Override
    public String getGuiTexture() {
        return "laziestae2:textures/jei/centrifuge.png";
    }

    @Override
    protected int getBackgroundWidth() {
        return 90;
    }

    @Override
    protected int getBackgroundHeight() {
        return 34;
    }

    @Override
    protected int getMachineMetadata() {
        return BlockMachine.CENTRIFUGE;
    }

    @Override
    public String getOverlayIdentifier() {
        return "laziestae2.centrifuge";
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (getOverlayIdentifier().equals(outputId)) {
            for (CentrifugeRecipe recipe : ProcessingRecipeRegistry.getCentrifugeRecipes()) {
                arecipes.add(createCached(recipe));
            }
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        boolean isMachine = isMachineStack(result);
        for (CentrifugeRecipe recipe : ProcessingRecipeRegistry.getCentrifugeRecipes()) {
            if (isMachine || isSameOutput(recipe.getOutput(), result)) {
                arecipes.add(createCached(recipe));
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        boolean isMachine = isMachineStack(ingredient);
        for (CentrifugeRecipe recipe : ProcessingRecipeRegistry.getCentrifugeRecipes()) {
            CachedMachineRecipe cached = createCached(recipe);
            if (isMachine || cached.usesIngredient(ingredient)) {
                arecipes.add(cached);
            }
        }
    }

    private CachedMachineRecipe createCached(CentrifugeRecipe recipe) {
        CachedMachineRecipe cached = new CachedMachineRecipe(recipe.getOutput(), OUT_X, OUT_Y);
        cached.addIngredient(recipe.getInput(), IN_X, IN_Y);
        return cached;
    }
}
