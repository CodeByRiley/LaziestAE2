package com.codebyriley.laziestae2.integration.nei;

import codechicken.nei.LayoutManager;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.LayoutStyle;
import codechicken.nei.recipe.TemplateRecipeHandler;
import codechicken.lib.gui.GuiDraw;
import com.codebyriley.laziestae2.Constants;
import com.codebyriley.laziestae2.client.gui.GuiMachine;
import com.codebyriley.laziestae2.gui.GuiIds;
import com.codebyriley.laziestae2.gui.MachineGuiDefinition;
import com.codebyriley.laziestae2.init.ModBlocks;
import com.codebyriley.laziestae2.inventory.ContainerMachine;
import com.codebyriley.laziestae2.recipe.AggregatorRecipe;
import com.codebyriley.laziestae2.recipe.ItemStackMatcher;
import com.codebyriley.laziestae2.recipe.ProcessingRecipeRegistry;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

public class NEIAggregatorRecipeHandler extends TemplateRecipeHandler {

    public static final String RECIPE_ID = Constants.MOD_ID + ".aggregator";

    private static final MachineGuiDefinition DEFINITION = MachineGuiDefinition.AGGREGATOR;
    private static final int TEXTURE_WIDTH = 130;
    private static final int TEXTURE_HEIGHT = 34;
    private static final int RECIPE_WIDTH = 166;
    private static final int RECIPE_HEIGHT = 65;
    private static final int ORIGIN_X = (RECIPE_WIDTH - TEXTURE_WIDTH) / 2;
    private static final int ORIGIN_Y = (RECIPE_HEIGHT - TEXTURE_HEIGHT) / 2;

    @Override
    public void loadTransferRects() {
        transferRects.add(new RecipeTransferRect(offset(new Rectangle(68, 8, 28, 18)), RECIPE_ID));
    }

    @Override
    public Class<? extends GuiContainer> getGuiClass() {
        return GuiMachine.class;
    }

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal(DEFINITION.getTitleKey());
    }

    @Override
    public String getGuiTexture() {
        return Constants.MOD_ID + ":textures/jei/aggregator.png";
    }

    @Override
    public String getOverlayIdentifier() {
        return RECIPE_ID;
    }

    @Override
    public boolean hasOverlay(GuiContainer gui, Container container, int recipe) {
        return container instanceof ContainerMachine
                && ((ContainerMachine)container).getDefinition().getGuiId() == GuiIds.AGGREGATOR
                && super.hasOverlay(gui, container, recipe);
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (RECIPE_ID.equals(outputId)) {
            loadAllRecipes();
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        for (AggregatorRecipe recipe : ProcessingRecipeRegistry.getAggregatorRecipes()) {
            if (NEIServerUtils.areStacksSameType(recipe.getOutput(), result)) {
                arecipes.add(new CachedAggregatorRecipe(recipe));
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        for (AggregatorRecipe recipe : ProcessingRecipeRegistry.getAggregatorRecipes()) {
            CachedAggregatorRecipe cachedRecipe = new CachedAggregatorRecipe(recipe);
            if (cachedRecipe.contains(cachedRecipe.getIngredients(), ingredient)) {
                cachedRecipe.setIngredientPermutation(cachedRecipe.getIngredients(), ingredient);
                arecipes.add(cachedRecipe);
            }
        }
    }

    @Override
    public void drawBackground(int recipe) {
        GL11.glColor4f(1, 1, 1, 1);
        GuiDraw.changeTexture(getGuiTexture());
        GuiDraw.drawTexturedModalRect(ORIGIN_X, ORIGIN_Y, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void drawExtras(int recipe) {
    }

    private void loadAllRecipes() {
        for (AggregatorRecipe recipe : ProcessingRecipeRegistry.getAggregatorRecipes()) {
            arecipes.add(new CachedAggregatorRecipe(recipe));
        }
    }

    public static ItemStack getMachineStack() {
        return new ItemStack(ModBlocks.machine, 1, GuiIds.AGGREGATOR);
    }

    private static Rectangle offset(Rectangle rectangle) {
        return new Rectangle(rectangle.x + ORIGIN_X, rectangle.y + ORIGIN_Y, rectangle.width, rectangle.height);
    }

    private class CachedAggregatorRecipe extends CachedRecipe {

        private final ArrayList<PositionedStack> ingredients;
        private final PositionedStack result;

        CachedAggregatorRecipe(AggregatorRecipe recipe) {
            this.ingredients = new ArrayList<PositionedStack>();

            ItemStackMatcher[] inputs = recipe.getInputs();
            int[][] slotPositions = {
                    { ORIGIN_X + 5, ORIGIN_Y + 9 },
                    { ORIGIN_X + 25, ORIGIN_Y + 9 },
                    { ORIGIN_X + 45, ORIGIN_Y + 9 }
            };

            for (int i = 0; i < inputs.length; i++) {
                Object displayInput = getDisplayInput(inputs[i]);
                if (displayInput != null) {
                    ingredients.add(new PositionedStack(displayInput, slotPositions[i][0], slotPositions[i][1]));
                }
            }

            this.result = new PositionedStack(recipe.getOutput(), ORIGIN_X + 105, ORIGIN_Y + 9);
        }

        @Override
        public List<PositionedStack> getIngredients() {
            return getCycledIngredients(cycleticks / 48, ingredients);
        }

        @Override
        public PositionedStack getResult() {
            return result;
        }

        private Object getDisplayInput(ItemStackMatcher matcher) {
            ItemStack displayStack = matcher.getDisplayStack();
            return displayStack != null ? displayStack : Arrays.asList(new ItemStack[0]);
        }
    }
}
