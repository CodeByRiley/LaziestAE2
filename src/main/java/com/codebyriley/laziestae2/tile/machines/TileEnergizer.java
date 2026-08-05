package com.codebyriley.laziestae2.tile.machines;

import com.codebyriley.laziestae2.config.LaziestConfig;
import com.codebyriley.laziestae2.gui.MachineGuiDefinition;
import com.codebyriley.laziestae2.recipe.EnergizerRecipe;
import com.codebyriley.laziestae2.recipe.ProcessingRecipeRegistry;
import com.codebyriley.laziestae2.tile.base.TileMachine;
import net.minecraft.item.ItemStack;

public class TileEnergizer extends TileMachine {

    private static final int INPUT = 0;
    private static final int OUTPUT = 1;
    private static final int WORK_PER_TICK = 1;
    private static final double DEFAULT_ENERGY_PER_OPERATION = 8100D;

    private EnergizerRecipe activeRecipe;

    public TileEnergizer() {
        super(Math.max(1D, (double)LaziestConfig.energizerEnergyBuffer), MachineGuiDefinition.ENERGIZER);
    }

    @Override
    protected boolean canWork() {
        EnergizerRecipe recipe = ProcessingRecipeRegistry.findEnergizerRecipe(getStackInSlot(INPUT));

        if (recipe != activeRecipe) {
            activeRecipe = recipe;
            resetWork();
        }

        return recipe != null && canAcceptOutput(recipe.getOutput(), OUTPUT);
    }

    @Override
    protected double getEnergyCostPerTick() {
        // The energizer's operation cost comes from the recipe itself rather than config.
        EnergizerRecipe recipe = activeRecipe;
        double energyPerOperation = recipe != null ? (double)recipe.getEnergyRequired() : DEFAULT_ENERGY_PER_OPERATION;
        energyPerOperation += Math.max(0D, (double)LaziestConfig.energizerEnergyCostUpgrade) * getUpgradeCount();
        return energyPerOperation / (double)getMaxWork();
    }

    @Override
    protected int getWorkPerTick() {
        return WORK_PER_TICK;
    }

    @Override
    protected int getMaxWork() {
        return Math.max(1, Math.max(1, LaziestConfig.energizerWorkTicksBase)
                - Math.max(1, LaziestConfig.energizerWorkTicksUpgrade) * getUpgradeCount());
    }

    @Override
    protected void onWorkFinished() {
        EnergizerRecipe recipe = activeRecipe;

        if (recipe == null || !recipe.matches(getStackInSlot(INPUT)))
            recipe = ProcessingRecipeRegistry.findEnergizerRecipe(getStackInSlot(INPUT));

        if (recipe == null)
            return;

        ItemStack output = recipe.getOutput();
        if (!canAcceptOutput(output, OUTPUT))
            return;

        decrStackSize(INPUT, 1);
        insertOutput(output, OUTPUT);
        activeRecipe = null;
    }

    @Override
    protected boolean isValidInput(int slot, ItemStack stack) {
        return ProcessingRecipeRegistry.findEnergizerRecipe(stack) != null;
    }
}
