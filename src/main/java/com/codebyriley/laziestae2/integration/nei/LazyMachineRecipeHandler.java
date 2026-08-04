package com.codebyriley.laziestae2.integration.nei;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.codebyriley.laziestae2.init.ModBlocks;
import com.codebyriley.laziestae2.recipe.ItemStackMatcher;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

public abstract class LazyMachineRecipeHandler extends TemplateRecipeHandler {

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal(getTitleKey());
    }

    protected abstract String getTitleKey();

    /** Size of the recipe background region within the 256x256 JEI category texture. */
    protected abstract int getBackgroundWidth();

    protected abstract int getBackgroundHeight();

    /** Recipe area is 166x65; backgrounds are centered inside it. */
    protected int getBackgroundOffsetX() {
        return (166 - getBackgroundWidth()) / 2;
    }

    protected int getBackgroundOffsetY() {
        return (65 - getBackgroundHeight()) / 2;
    }

    @Override
    public void drawBackground(int recipe) {
        GuiDraw.changeTexture(getGuiTexture());
        GuiDraw.drawTexturedModalRect(
                getBackgroundOffsetX(), getBackgroundOffsetY(),
                0, 0,
                getBackgroundWidth(), getBackgroundHeight());
    }

    @Override
    public int recipiesPerPage() {
        return 2;
    }

    /** Block metadata of the machine this handler describes. */
    protected abstract int getMachineMetadata();

    public ItemStack getMachineStack() {
        return new ItemStack(ModBlocks.machine, 1, getMachineMetadata());
    }

    /** True when the query is this handler's machine block, so it shows its own recipes. */
    protected boolean isMachineStack(ItemStack stack) {
        return stack != null
                && stack.getItem() == net.minecraft.item.Item.getItemFromBlock(ModBlocks.machine)
                && stack.getItemDamage() == getMachineMetadata();
    }

    /**
     * No transfer rects: they point at a *different* handler's recipes, and ours
     * would only ever re-open the handler already on screen. A rect here also
     * overlays the ingredient slots with a permanent tooltip.
     *
     * Machine lookups are handled by {@link #isMachineStack} in the recipe/usage
     * loaders, and the machine GUI's progress arrow calls NEI directly.
     */
    @Override
    public void loadTransferRects() {
    }

    protected PositionedStack toPositionedStack(ItemStackMatcher matcher, int x, int y) {
        List<ItemStack> stacks = matcher.getDisplayStacks();
        if (stacks.isEmpty()) {
            ItemStack single = matcher.getDisplayStack();
            if (single == null) {
                return null;
            }
            stacks = new ArrayList<ItemStack>();
            stacks.add(single);
        }

        return new PositionedStack(stacks, x, y);
    }

    protected static boolean isSameOutput(ItemStack recipeOutput, ItemStack query) {
        return NEIServerUtils.areStacksSameTypeCrafting(recipeOutput, query);
    }

    public class CachedMachineRecipe extends CachedRecipe {

        private final List<PositionedStack> ingredients = new ArrayList<PositionedStack>();
        private final PositionedStack result;

        public CachedMachineRecipe(ItemStack output, int outX, int outY) {
            this.result = new PositionedStack(output, outX, outY);
        }

        public void addIngredient(ItemStackMatcher matcher, int x, int y) {
            PositionedStack stack = toPositionedStack(matcher, x, y);
            if (stack != null) {
                ingredients.add(stack);
            }
        }

        public boolean usesIngredient(ItemStack query) {
            for (PositionedStack ingredient : ingredients) {
                for (ItemStack option : ingredient.items) {
                    if (NEIServerUtils.areStacksSameTypeCrafting(option, query)) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public List<PositionedStack> getIngredients() {
            return getCycledIngredients(cycleticks / 20, ingredients);
        }

        @Override
        public PositionedStack getResult() {
            return result;
        }
    }
}
