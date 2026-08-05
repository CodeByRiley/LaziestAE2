package com.codebyriley.laziestae2.tile.machines;

import com.codebyriley.laziestae2.config.LaziestConfig;
import com.codebyriley.laziestae2.gui.MachineGuiDefinition;
import com.codebyriley.laziestae2.recipe.AggregatorRecipe;
import com.codebyriley.laziestae2.recipe.ProcessingRecipeRegistry;
import com.codebyriley.laziestae2.tile.base.TileMachine;
import net.minecraft.item.ItemStack;

public class TileAggregator extends TileMachine {

    private static final int INPUT_A = 0;
    private static final int INPUT_B = 1;
    private static final int INPUT_C = 2;
    private static final int OUTPUT = 3;
    private static final int WORK_PER_TICK = 1;

    private AggregatorRecipe activeRecipe;

    public TileAggregator() {
        super(Math.max(1D, (double)LaziestConfig.aggregatorEnergyBuffer), MachineGuiDefinition.AGGREGATOR);
    }

    @Override
    protected boolean canWork() {
        AggregatorRecipe recipe = ProcessingRecipeRegistry.findAggregatorRecipe(
                getStackInSlot(INPUT_A),
                getStackInSlot(INPUT_B),
                getStackInSlot(INPUT_C));

        if (recipe != activeRecipe) {
            activeRecipe = recipe;
            resetWork();
        }

        return recipe != null && canAcceptOutput(recipe.getOutput(), OUTPUT);
    }

    @Override
    protected double getEnergyCostPerTick() {
        double energyPerOperation = Math.max(1D, (double)LaziestConfig.aggregatorEnergyCostBase)
                + Math.max(0D, (double)LaziestConfig.aggregatorEnergyCostUpgrade) * getUpgradeCount();
        return energyPerOperation / (double)getMaxWork();
    }

    @Override
    protected int getWorkPerTick() {
        return WORK_PER_TICK;
    }

    @Override
    protected int getMaxWork() {
        return Math.max(1, Math.max(1, LaziestConfig.aggregatorWorkTicksBase)
                - Math.max(1, LaziestConfig.aggregatorWorkTicksUpgrade) * getUpgradeCount());
    }

    @Override
    protected void onWorkFinished() {
        AggregatorRecipe recipe = activeRecipe;

        if (recipe == null || !recipe.matches(getStackInSlot(INPUT_A), getStackInSlot(INPUT_B), getStackInSlot(INPUT_C))) {
            recipe = ProcessingRecipeRegistry.findAggregatorRecipe(
                    getStackInSlot(INPUT_A),
                    getStackInSlot(INPUT_B),
                    getStackInSlot(INPUT_C));
        }

        if (recipe == null)
            return;

        ItemStack output = recipe.getOutput();
        if (!canAcceptOutput(output, OUTPUT))
            return;

        int[] matchedSlots = recipe.getMatchingSlots(
                getStackInSlot(INPUT_A),
                getStackInSlot(INPUT_B),
                getStackInSlot(INPUT_C));
        if (matchedSlots == null)
            return;

        for (int matchedSlot : matchedSlots) {
            decrStackSize(matchedSlot, 1);
        }

        insertOutput(output, OUTPUT);
        activeRecipe = null;
    }
}
