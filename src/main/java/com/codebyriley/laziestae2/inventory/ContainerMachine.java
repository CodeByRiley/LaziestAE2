package com.codebyriley.laziestae2.inventory;

import com.codebyriley.laziestae2.gui.MachineGuiDefinition;
import com.codebyriley.laziestae2.tile.base.TileMachine;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerMachine extends Container {

    private static final int ENERGY_SCALE = 10000;
    private static final int PROGRESS_WORK = 0;
    private static final int PROGRESS_MAX_WORK = 1;
    private static final int PROGRESS_ENERGY = 2;
    private static final int PROGRESS_WORKING = 3;

    private final TileMachine tile;
    private final MachineGuiDefinition definition;
    private final int machineSlotCount;

    private int work;
    private int maxWork;
    private int energyScaled;
    private int working;

    public ContainerMachine(InventoryPlayer playerInventory, TileMachine tile) {
        this.tile = tile;
        this.definition = tile.getGuiDefinition();
        this.machineSlotCount = definition.getSlotCount();

        addMachineSlots();
        addPlayerInventory(playerInventory);
        refreshProgressFromTile();
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUseableByPlayer(player);
    }

    @Override
    public void addCraftingToCrafters(ICrafting listener) {
        super.addCraftingToCrafters(listener);
        sendProgress(listener);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        int currentWork = tile.getWork();
        int currentMaxWork = tile.getMaxWorkForDisplay();
        int currentEnergy = getEnergyScaledFromTile();
        int currentWorking = tile.isWorking() ? 1 : 0;

        for (Object crafterObject : crafters) {
            ICrafting crafter = (ICrafting)crafterObject;

            if (work != currentWork)
                crafter.sendProgressBarUpdate(this, PROGRESS_WORK, currentWork);

            if (maxWork != currentMaxWork)
                crafter.sendProgressBarUpdate(this, PROGRESS_MAX_WORK, currentMaxWork);

            if (energyScaled != currentEnergy)
                crafter.sendProgressBarUpdate(this, PROGRESS_ENERGY, currentEnergy);

            if (working != currentWorking)
                crafter.sendProgressBarUpdate(this, PROGRESS_WORKING, currentWorking);
        }

        work = currentWork;
        maxWork = currentMaxWork;
        energyScaled = currentEnergy;
        working = currentWorking;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int value) {
        switch (id) {
            case PROGRESS_WORK:
                work = value;
                break;
            case PROGRESS_MAX_WORK:
                maxWork = value;
                break;
            case PROGRESS_ENERGY:
                energyScaled = value;
                break;
            case PROGRESS_WORKING:
                working = value;
                break;
            default:
                break;
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack original = null;
        Slot slot = (Slot)inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            original = stack.copy();

            if (index < machineSlotCount) {
                if (!mergeItemStack(stack, machineSlotCount, inventorySlots.size(), true))
                    return null;

                slot.onSlotChange(stack, original);
            } else {
                boolean moved = false;

                if (TileMachine.isAccelerationCard(stack))
                    moved = mergeIntoUpgradeSlot(stack);

                if (stack.stackSize > 0 && mergeItemStack(stack, 0, getInputSlotEnd(), false))
                    moved = true;

                if (!moved)
                    return null;
            }

            if (stack.stackSize == 0)
                slot.putStack(null);
            else
                slot.onSlotChanged();

            if (stack.stackSize == original.stackSize)
                return null;

            slot.onPickupFromSlot(player, stack);
        }

        return original;
    }

    public MachineGuiDefinition getDefinition() {
        return definition;
    }

    public TileMachine getTile() {
        return tile;
    }

    public float getWorkFraction() {
        return maxWork <= 0 ? 0F : Math.min((float)work / (float)maxWork, 1F);
    }

    public float getEnergyFraction() {
        return Math.min((float)energyScaled / (float)ENERGY_SCALE, 1F);
    }

    public boolean isWorking() {
        return working != 0;
    }

    /** Machine slots are laid out inputs-first, so inputs end at the first output/upgrade slot. */
    private int getInputSlotEnd() {
        for (int i = 0; i < machineSlotCount; i++) {
            if (definition.isOutputSlot(i) || definition.isUpgradeSlot(i))
                return i;
        }
        return machineSlotCount;
    }

    // Vanilla mergeItemStack ignores per-slot stack limits, so the 8-card upgrade
    // slot gets its own merge.
    private boolean mergeIntoUpgradeSlot(ItemStack stack) {
        int upgradeIndex = definition.getUpgradeSlotIndex();
        if (upgradeIndex == -1)
            return false;

        ItemStack existing = tile.getStackInSlot(upgradeIndex);
        int space;

        if (existing == null)
            space = TileMachine.MAX_UPGRADES;
        else if (existing.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(existing, stack))
            space = TileMachine.MAX_UPGRADES - existing.stackSize;
        else
            return false;

        int moved = Math.min(space, stack.stackSize);
        if (moved <= 0)
            return false;

        if (existing == null) {
            ItemStack placed = stack.copy();
            placed.stackSize = moved;
            tile.setInventorySlotContents(upgradeIndex, placed);
        } else {
            existing.stackSize += moved;
            tile.setInventorySlotContents(upgradeIndex, existing);
        }

        stack.stackSize -= moved;
        return true;
    }

    private void addMachineSlots() {
        MachineGuiDefinition.SlotDefinition[] slots = definition.getSlots();

        for (int i = 0; i < slots.length; i++) {
            MachineGuiDefinition.SlotDefinition slot = slots[i];

            if (slot.isOutput())
                addSlotToContainer(new SlotOutput(tile, i, slot.getX(), slot.getY()));
            else if (slot.isUpgrade())
                addSlotToContainer(new SlotUpgrade(tile, i, slot.getX(), slot.getY()));
            else
                addSlotToContainer(new Slot(tile, i, slot.getX(), slot.getY()));
        }
    }

    private static class SlotUpgrade extends Slot {

        public SlotUpgrade(TileMachine tile, int index, int x, int y) {
            super(tile, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return TileMachine.isAccelerationCard(stack);
        }

        @Override
        public int getSlotStackLimit() {
            return TileMachine.MAX_UPGRADES;
        }
    }

    private void addPlayerInventory(InventoryPlayer playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlotToContainer(new Slot(
                        playerInventory,
                        column + row * 9 + 9,
                        8 + column * 18,
                        definition.getPlayerInventoryY() + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlotToContainer(new Slot(
                    playerInventory,
                    column,
                    8 + column * 18,
                    definition.getPlayerHotbarY()));
        }
    }

    private void refreshProgressFromTile() {
        work = tile.getWork();
        maxWork = tile.getMaxWorkForDisplay();
        energyScaled = getEnergyScaledFromTile();
        working = tile.isWorking() ? 1 : 0;
    }

    private void sendProgress(ICrafting crafter) {
        crafter.sendProgressBarUpdate(this, PROGRESS_WORK, work);
        crafter.sendProgressBarUpdate(this, PROGRESS_MAX_WORK, maxWork);
        crafter.sendProgressBarUpdate(this, PROGRESS_ENERGY, energyScaled);
        crafter.sendProgressBarUpdate(this, PROGRESS_WORKING, working);
    }

    private int getEnergyScaledFromTile() {
        double capacity = tile.getEnergyCapacity();
        if (capacity <= 0D)
            return 0;

        return Math.min(ENERGY_SCALE, (int)Math.round(tile.getStoredEnergy() * ENERGY_SCALE / capacity));
    }
}
