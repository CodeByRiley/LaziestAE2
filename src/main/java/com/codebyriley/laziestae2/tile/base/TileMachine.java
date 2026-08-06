package com.codebyriley.laziestae2.tile.base;

import appeng.api.config.SecurityPermissions;
import com.codebyriley.laziestae2.gui.MachineGuiDefinition;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public abstract class TileMachine extends TilePowered
        implements net.minecraft.inventory.ISidedInventory, ISideConfigurable {

    private static final int AUTO_EXPORT_INTERVAL = 16;

    private final SideConfiguration sides = new SideConfiguration();
    private boolean autoExporting;
    private int autoExportTimer;

    private final MachineGuiDefinition guiDefinition;
    private final ItemStack[] inventory;
    private int work;
    private boolean working;


    protected TileMachine(double energyCapacity, MachineGuiDefinition guiDefinition) {
        super(energyCapacity);

        if (guiDefinition == null)
            throw new IllegalArgumentException("Machine GUI definition cannot be null");

        this.guiDefinition = guiDefinition;
        this.inventory = new ItemStack[guiDefinition.getSlotCount()];

    }

    @Override
    public int getFacing() {
        return sides.getFacing();
    }

    @Override
    public void setFacing(int side) {
        if (sides.setFacing(side)) {
            markDirty();
            markForUpdate();
        }
    }

    @Override
    public int getWidgetSide(int widget) {
        return sides.getWidgetSide(widget);
    }

    // Sided IO configuration

    @Override
    public MachineSideMode getSideMode(int side) {
        return sides.getMode(side);
    }

    @Override
    public void setSideMode(int side, MachineSideMode mode) {
        if (sides.setMode(side, mode)) {
            markDirty();
            markForUpdate();
        }
    }

    @Override
    public boolean supportsAutoExport() {
        return true;
    }

    @Override
    public boolean isAutoExporting() {
        return autoExporting;
    }

    @Override
    public void setAutoExporting(boolean exporting) {
        if (autoExporting != exporting) {
            autoExporting = exporting;
            markDirty();
            markForUpdate();
        }
    }

    // Pushes finished output into inventories on faces configured to output.
    private void doAutoExport() {
        for (int slot = 0; slot < inventory.length; slot++) {
            if (!guiDefinition.isOutputSlot(slot) || inventory[slot] == null)
                continue;

            for (int side = 0; side < SideConfiguration.FACE_COUNT; side++) {
                if (!sides.getMode(side).allowsOutput() || inventory[slot] == null)
                    continue;

                ItemStack remainder = com.codebyriley.laziestae2.inventory.AdjacentInventoryHelper
                        .insertIntoSide(worldObj, xCoord, yCoord, zCoord, side, inventory[slot]);

                if (remainder == null || remainder.stackSize <= 0) {
                    inventory[slot] = null;
                    markDirty();
                } else if (remainder.stackSize != inventory[slot].stackSize) {
                    inventory[slot] = remainder;
                    markDirty();
                }
            }
        }
    }

    @Override
    protected void updateServer() {
        // A redstone-disabled machine holds everything, including auto-export.
        // Progress is kept so toggling the signal resumes rather than restarts.
        if (!isRedstoneActive()) {
            setWorking(false);
            return;
        }

        if (autoExporting && ++autoExportTimer >= AUTO_EXPORT_INTERVAL) {
            autoExportTimer = 0;
            doAutoExport();
        }

        if (!canWork()) {
            resetWorkIfNeeded();
            return;
        }

        int maxWork = getMaxWork();
        int workPerTick = getWorkPerTick();
        double energyCost = getEnergyCostPerTick();

        if (maxWork <= 0 || workPerTick <= 0 || !isValidEnergyCost(energyCost)) {
            resetWorkIfNeeded();
            return;
        }

        if (energyCost > 0D && !energy.consume(energyCost, false)) {
            setWorking(false);
            return;
        }

        setWorking(true);
        work += workPerTick;

        if (work >= maxWork) {
            work = 0;
            onWorkFinished();
        }

        markDirty();
    }

    protected abstract boolean canWork();

    protected abstract double getEnergyCostPerTick();

    protected abstract int getWorkPerTick();

    protected abstract int getMaxWork();

    protected abstract void onWorkFinished();

    public int getWork() {
        return work;
    }

    public int getMaxWorkForDisplay() {
        return getMaxWork();
    }

    public boolean isWorking() {
        return working;
    }

    public MachineGuiDefinition getGuiDefinition() {
        return guiDefinition;
    }

    protected void resetWork() {
        if (work != 0) {
            work = 0;
            markDirty();
        }
    }

    protected void setWorking(boolean working) {
        if (this.working != working) {
            this.working = working;
            markDirty();
            markForUpdate();
        }
    }

    private void resetWorkIfNeeded() {
        resetWork();
        setWorking(false);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        writeMachineNBT(tag);
        writeInventoryNBT(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        readMachineNBT(tag);
        readInventoryNBT(tag);
    }

    @Override
    protected void writeSyncNBT(NBTTagCompound tag) {
        super.writeSyncNBT(tag);
        writeMachineNBT(tag);
    }

    @Override
    protected void readSyncNBT(NBTTagCompound tag) {
        super.readSyncNBT(tag);
        readMachineNBT(tag);
    }

    private void writeMachineNBT(NBTTagCompound tag) {
        tag.setInteger("Work", work);
        tag.setBoolean("Working", working);
        tag.setBoolean("AutoExport", autoExporting);
        sides.writeToNBT(tag);
    }

    private void readMachineNBT(NBTTagCompound tag) {
        work = Math.max(0, tag.getInteger("Work"));
        working = tag.getBoolean("Working");
        autoExporting = tag.getBoolean("AutoExport");
        sides.readFromNBT(tag);
    }

    private void writeInventoryNBT(NBTTagCompound tag) {
        NBTTagList items = new NBTTagList();

        for (int i = 0; i < inventory.length; i++) {
            ItemStack stack = inventory[i];
            if (stack != null) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setByte("Slot", (byte)i);
                stack.writeToNBT(itemTag);
                items.appendTag(itemTag);
            }
        }

        tag.setTag("Items", items);
    }

    private void readInventoryNBT(NBTTagCompound tag) {
        for (int i = 0; i < inventory.length; i++) {
            inventory[i] = null;
        }

        NBTTagList items = tag.getTagList("Items", 10);
        for (int i = 0; i < items.tagCount(); i++) {
            NBTTagCompound itemTag = items.getCompoundTagAt(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot >= 0 && slot < inventory.length)
                inventory[slot] = ItemStack.loadItemStackFromNBT(itemTag);
        }
    }

    @Override
    public int getSizeInventory() {
        return inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return isValidSlot(slot) ? inventory[slot] : null;
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (!isValidSlot(slot) || inventory[slot] == null || amount <= 0)
            return null;

        ItemStack stack = inventory[slot];
        if (stack.stackSize <= amount) {
            inventory[slot] = null;
            markDirty();
            return stack;
        }

        ItemStack split = stack.splitStack(amount);
        if (stack.stackSize == 0)
            inventory[slot] = null;

        markDirty();
        return split;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        if (!isValidSlot(slot))
            return null;

        ItemStack stack = inventory[slot];
        inventory[slot] = null;
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (!isValidSlot(slot))
            return;

        inventory[slot] = stack;

        int limit = guiDefinition.isUpgradeSlot(slot) ? MAX_UPGRADES : getInventoryStackLimit();
        if (stack != null && stack.stackSize > limit)
            stack.stackSize = limit;

        markDirty();
    }

    @Override
    public String getInventoryName() {
        return guiDefinition.getTitleKey();
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj != null &&
                worldObj.getTileEntity(xCoord, yCoord, zCoord) == this &&
                player.getDistanceSq(
                        (double) xCoord + 0.5D,
                        (double) yCoord + 0.5D,
                        (double) zCoord + 0.5D) <= 64D &&
                hasPermission(player, SecurityPermissions.BUILD);
    }

    @Override
    public void openInventory() { }

    @Override
    public void closeInventory() { }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (!isValidSlot(slot) || guiDefinition.isOutputSlot(slot))
            return false;

        if (guiDefinition.isUpgradeSlot(slot))
            return isAccelerationCard(stack);

        if (preventsDuplicateInputs() && isHeldInAnotherInputSlot(slot, stack))
            return false;

        return isValidInput(slot, stack);
    }

    /**
     * Keeps one item type to a single input slot, so a full slot does not spill
     * the same item into the slots meant for the recipe's other ingredients.
     * Machines whose recipes take the same item twice should override this.
     */
    protected boolean preventsDuplicateInputs() {
        return true;
    }

    private boolean isHeldInAnotherInputSlot(int slot, ItemStack stack) {
        if (stack == null)
            return false;

        for (int other = 0; other < inventory.length; other++) {
            if (other == slot
                    || inventory[other] == null
                    || guiDefinition.isOutputSlot(other)
                    || guiDefinition.isUpgradeSlot(other)) {
                continue;
            }

            if (inventory[other].isItemEqual(stack)
                    && ItemStack.areItemStackTagsEqual(inventory[other], stack)) {
                return true;
            }
        }

        return false;
    }

    /** Input-slot filtering hook for machine subclasses; upgrade/output handled by the base. */
    protected boolean isValidInput(int slot, ItemStack stack) {
        return true;
    }

    public static boolean isAccelerationCard(ItemStack stack) {
        return stack != null
                && appeng.api.AEApi.instance().definitions().materials().cardSpeed().isSameAs(stack);
    }

    public static final int MAX_UPGRADES = 8;

    /** Number of installed acceleration cards, capped at {@link #MAX_UPGRADES}. */
    public int getUpgradeCount() {
        int slot = guiDefinition.getUpgradeSlotIndex();
        if (slot == -1)
            return 0;

        ItemStack stack = getStackInSlot(slot);
        return stack == null ? 0 : Math.min(stack.stackSize, MAX_UPGRADES);
    }

    private boolean isValidSlot(int slot) {
        return slot >= 0 && slot < inventory.length;
    }

    // === ISidedInventory ===

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        MachineSideMode mode = getSideMode(side);
        if (mode == MachineSideMode.NONE)
            return new int[0];

        int count = 0;
        int[] slots = new int[inventory.length];

        for (int slot = 0; slot < inventory.length; slot++) {
            if (guiDefinition.isUpgradeSlot(slot))
                continue;

            boolean output = guiDefinition.isOutputSlot(slot);
            if ((output && mode.allowsOutput()) || (!output && mode.allowsInput()))
                slots[count++] = slot;
        }

        int[] trimmed = new int[count];
        System.arraycopy(slots, 0, trimmed, 0, count);
        return trimmed;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        // Check other slots for duplicate items,
        return getSideMode(side).allowsInput()
                && !guiDefinition.isOutputSlot(slot)
                && !guiDefinition.isUpgradeSlot(slot)
                && isItemValidForSlot(slot, stack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return getSideMode(side).allowsOutput() && guiDefinition.isOutputSlot(slot);
    }

    protected boolean canAcceptOutput(ItemStack output, int outputSlot) {
        if (output == null)
            return false;

        ItemStack existing = getStackInSlot(outputSlot);
        if (existing == null)
            return true;

        if (!existing.isItemEqual(output) || !ItemStack.areItemStackTagsEqual(existing, output))
            return false;

        int maxStackSize = Math.min(existing.getMaxStackSize(), getInventoryStackLimit());
        return existing.stackSize + output.stackSize <= maxStackSize;
    }

    protected void insertOutput(ItemStack output, int outputSlot) {
        ItemStack existing = getStackInSlot(outputSlot);
        if (existing == null) {
            setInventorySlotContents(outputSlot, output.copy());
        } else {
            existing.stackSize += output.stackSize;
            setInventorySlotContents(outputSlot, existing);
        }
    }

    private static boolean isValidEnergyCost(double energyCost) {
        return !Double.isNaN(energyCost) && !Double.isInfinite(energyCost) && energyCost >= 0D;
    }
}
