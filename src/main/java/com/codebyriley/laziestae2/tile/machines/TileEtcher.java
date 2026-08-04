package com.codebyriley.laziestae2.tile.machines;

import com.codebyriley.laziestae2.config.LaziestConfig;
import com.codebyriley.laziestae2.gui.MachineGuiDefinition;
import com.codebyriley.laziestae2.recipe.EtcherRecipe;
import com.codebyriley.laziestae2.recipe.ProcessingRecipeRegistry;
import com.codebyriley.laziestae2.tile.base.TileMachine;
import net.minecraft.item.ItemStack;

public class TileEtcher extends TileMachine {

    private static final int INPUT_TOP = 0;
    private static final int INPUT_BOTTOM = 1;
    private static final int INPUT_MIDDLE = 2;
    private static final int OUTPUT = 3;
    private static final int WORK_PER_TICK = 1;

    private EtcherRecipe activeRecipe;

    public TileEtcher() {
        super(Math.max(1D, (double)LaziestConfig.etcherEnergyBuffer), MachineGuiDefinition.ETCHER);
    }

    @Override
    protected boolean canWork() {
        EtcherRecipe recipe = ProcessingRecipeRegistry.findEtcherRecipe(
                getStackInSlot(INPUT_TOP),
                getStackInSlot(INPUT_BOTTOM),
                getStackInSlot(INPUT_MIDDLE));

        if (recipe != activeRecipe) {
            activeRecipe = recipe;
            resetWork();
        }

        return recipe != null && canAcceptOutput(recipe.getOutput(), OUTPUT);
    }

    @Override
    protected double getEnergyCostPerTick() {
        double energyPerOperation = Math.max(1D, (double)LaziestConfig.etcherEnergyCostBase)
                + Math.max(0D, (double)LaziestConfig.etcherEnergyCostUpgrade) * getUpgradeCount();
        return energyPerOperation / (double)getMaxWork();
    }

    @Override
    protected int getWorkPerTick() {
        return WORK_PER_TICK;
    }

    @Override
    protected int getMaxWork() {
        return Math.max(1, Math.max(1, LaziestConfig.etcherWorkTicksBase)
                - Math.max(1, LaziestConfig.etcherWorkTicksUpgrade) * getUpgradeCount());
    }

    @Override
    protected void onWorkFinished() {
        EtcherRecipe recipe = activeRecipe;

        if (recipe == null
                || !recipe.matches(getStackInSlot(INPUT_TOP), getStackInSlot(INPUT_BOTTOM), getStackInSlot(INPUT_MIDDLE))) {
            recipe = ProcessingRecipeRegistry.findEtcherRecipe(
                    getStackInSlot(INPUT_TOP),
                    getStackInSlot(INPUT_BOTTOM),
                    getStackInSlot(INPUT_MIDDLE));
        }

        if (recipe == null) {
            return;
        }

        ItemStack output = recipe.getOutput();
        if (!canAcceptOutput(output, OUTPUT)) {
            return;
        }

        decrStackSize(INPUT_TOP, 1);
        decrStackSize(INPUT_BOTTOM, 1);
        decrStackSize(INPUT_MIDDLE, 1);
        insertOutput(output, OUTPUT);
        activeRecipe = null;
    }

    @Override
    protected boolean isValidInput(int slot, ItemStack stack) {
        return ProcessingRecipeRegistry.isValidEtcherInput(slot, stack);
    }
}
