package com.codebyriley.laziestae2.tile.machines;

import com.codebyriley.laziestae2.config.LaziestConfig;
import com.codebyriley.laziestae2.gui.MachineGuiDefinition;
import com.codebyriley.laziestae2.recipe.CentrifugeRecipe;
import com.codebyriley.laziestae2.recipe.ProcessingRecipeRegistry;
import com.codebyriley.laziestae2.tile.base.TileMachine;
import net.minecraft.item.ItemStack;

public class TileCentrifuge extends TileMachine {

    private static final int INPUT = 0;
    private static final int OUTPUT = 1;
    private static final int WORK_PER_TICK = 1;

    private CentrifugeRecipe activeRecipe;

    public TileCentrifuge() {
        super(Math.max(1D, (double)LaziestConfig.centrifugeEnergyBuffer), MachineGuiDefinition.CENTRIFUGE);
    }

    @Override
    protected boolean canWork() {
        CentrifugeRecipe recipe = ProcessingRecipeRegistry.findCentrifugeRecipe(getStackInSlot(INPUT));

        if (recipe != activeRecipe) {
            activeRecipe = recipe;
            resetWork();
        }

        return recipe != null && canAcceptOutput(recipe.getOutput(), OUTPUT);
    }

    @Override
    protected double getEnergyCostPerTick() {
        double energyPerOperation = Math.max(1D, (double)LaziestConfig.centrifugeEnergyCostBase)
                + Math.max(0D, (double)LaziestConfig.centrifugeEnergyCostUpgrade) * getUpgradeCount();
        return energyPerOperation / (double)getMaxWork();
    }

    @Override
    protected int getWorkPerTick() {
        return WORK_PER_TICK;
    }

    @Override
    protected int getMaxWork() {
        return Math.max(1, Math.max(1, LaziestConfig.centrifugeWorkTicksBase)
                - Math.max(1, LaziestConfig.centrifugeWorkTicksUpgrade) * getUpgradeCount());
    }

    @Override
    protected void onWorkFinished() {
        CentrifugeRecipe recipe = activeRecipe;

        if (recipe == null || !recipe.matches(getStackInSlot(INPUT)))
            recipe = ProcessingRecipeRegistry.findCentrifugeRecipe(getStackInSlot(INPUT));

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
        return ProcessingRecipeRegistry.findCentrifugeRecipe(stack) != null;
    }
}
