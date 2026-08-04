package com.codebyriley.laziestae2.tile.machines;

import appeng.api.AEApi;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import com.codebyriley.laziestae2.LaziestAE2;
import com.codebyriley.laziestae2.config.LaziestConfig;
import com.codebyriley.laziestae2.integration.ae2.CraftingCpuInternals;
import com.codebyriley.laziestae2.inventory.AdjacentInventoryHelper;
import com.codebyriley.laziestae2.tile.base.MachineSideMode;
import com.codebyriley.laziestae2.tile.base.SideConfiguration;
import com.codebyriley.laziestae2.tile.base.TileNetworkDevice;
import java.util.EnumSet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Preemptive Assembly Unit: an ME crafting provider with nine pattern slots.
 * Pattern inputs are staged in the export buffer and pushed into adjacent
 * inventories; the import buffer injects items back into the network.
 */
public class TileFastCrafter extends TileNetworkDevice
        implements ICraftingProvider, net.minecraft.inventory.ISidedInventory {

    public static final int PATTERN_COUNT = 9;
    public static final int BUFFER_SIZE = 9;

    public static final int PATTERN_START = 0;
    public static final int IMPORT_START = PATTERN_COUNT;
    public static final int EXPORT_START = PATTERN_COUNT + BUFFER_SIZE;
    public static final int SLOT_COUNT = PATTERN_COUNT + BUFFER_SIZE * 2;

    private static final double ENERGY_CAPACITY = 10000D;

    /** Ticks of undeliverable output before the buffer is returned to the network. */
    private static final int EXPORT_STALL_TICKS = 100;

    private final ItemStack[] inventory = new ItemStack[SLOT_COUNT];
    private int exportStallTicks;
    private final BaseActionSource actionSource = new MachineSource(this);

    public TileFastCrafter() {
        super(ENERGY_CAPACITY);
    }

    @Override
    protected double getIdlePowerUsage() {
        return Math.max(0D, LaziestConfig.fastCrafterIdlePower);
    }

    @Override
    protected EnumSet<GridFlags> getGridFlags() {
        return EnumSet.of(GridFlags.REQUIRE_CHANNEL);
    }

    @Override
    protected void updateServer() {
        importIntoNetwork();
        flushExportBuffer();
    }

    private void importIntoNetwork() {
        insertRangeIntoNetwork(IMPORT_START);
    }

    /** Pushes a buffer's contents into network storage. */
    private void insertRangeIntoNetwork(int start) {
        IGridNode node = getActionableNode();
        if (node == null || !node.isActive()) {
            return;
        }

        IGrid grid = node.getGrid();
        if (grid == null) {
            return;
        }

        IEnergyGrid energyGrid = grid.getCache(IEnergyGrid.class);
        IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
        if (energyGrid == null || storageGrid == null) {
            return;
        }

        IMEMonitor<IAEItemStack> networkInventory = storageGrid.getItemInventory();

        for (int i = start; i < start + BUFFER_SIZE; i++) {
            if (inventory[i] == null) {
                continue;
            }

            IAEItemStack aeStack = AEApi.instance().storage().createItemStack(inventory[i]);
            IAEItemStack remainder = AEApi.instance().storage()
                    .poweredInsert(energyGrid, networkInventory, aeStack, actionSource);

            if (remainder == null) {
                inventory[i] = null;
                markDirty();
            } else if (remainder.getStackSize() < aeStack.getStackSize()) {
                inventory[i].stackSize = (int)remainder.getStackSize();
                markDirty();
            }
        }
    }

    private void flushExportBuffer() {
        boolean movedAny = false;

        for (int i = EXPORT_START; i < EXPORT_START + BUFFER_SIZE; i++) {
            if (inventory[i] == null) {
                continue;
            }

            // Only push through faces the player configured for output.
            for (int side = 0; side < SideConfiguration.FACE_COUNT && inventory[i] != null; side++) {
                if (!getSideMode(side).allowsOutput()) {
                    continue;
                }

                ItemStack remainder = AdjacentInventoryHelper.insertIntoSide(
                        worldObj, xCoord, yCoord, zCoord, side, inventory[i]);

                if (remainder == null || remainder.stackSize <= 0) {
                    inventory[i] = null;
                    movedAny = true;
                    markDirty();
                } else if (remainder.stackSize != inventory[i].stackSize) {
                    inventory[i] = remainder;
                    movedAny = true;
                    markDirty();
                }
            }
        }

        recoverStalledExports(movedAny);
    }

    /**
     * Staged ingredients are unrecoverable if nothing ever accepts them: the
     * buffer keeps the unit busy and the crafting task was already consumed.
     * When there is nowhere to deliver, hand them back to the network so the
     * job can be reissued.
     */
    private void recoverStalledExports(boolean movedAny) {
        if (!isBusy()) {
            exportStallTicks = 0;
            return;
        }

        if (movedAny) {
            exportStallTicks = 0;
            return;
        }

        // No point waiting if there is no inventory on an output face at all.
        if (!hasOutputTarget()) {
            exportStallTicks = EXPORT_STALL_TICKS;
        }

        if (++exportStallTicks < EXPORT_STALL_TICKS) {
            return;
        }

        exportStallTicks = 0;
        insertRangeIntoNetwork(EXPORT_START);

        if (!isBusy()) {
            LaziestAE2.logger.debug(
                    "Assembly unit at {},{},{} returned undeliverable ingredients to the network",
                    xCoord, yCoord, zCoord);
        }
    }

    /** True if any face configured for output has an inventory next to it. */
    private boolean hasOutputTarget() {
        for (int side = 0; side < SideConfiguration.FACE_COUNT; side++) {
            if (!getSideMode(side).allowsOutput()) {
                continue;
            }

            ForgeDirection dir = ForgeDirection.getOrientation(side);
            net.minecraft.tileentity.TileEntity neighbour = worldObj.getTileEntity(
                    xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);

            if (neighbour instanceof net.minecraft.inventory.IInventory) {
                return true;
            }
        }

        return false;
    }

    // === ICraftingProvider ===

    @Override
    public void provideCrafting(ICraftingProviderHelper helper) {
        World world = getWorldObj();
        if (world == null) {
            return;
        }

        for (int i = PATTERN_START; i < PATTERN_START + PATTERN_COUNT; i++) {
            ItemStack stack = inventory[i];
            if (stack == null || !(stack.getItem() instanceof ICraftingPatternItem)) {
                continue;
            }

            ICraftingPatternDetails pattern =
                    ((ICraftingPatternItem)stack.getItem()).getPatternForItem(stack, world);
            if (pattern != null) {
                helper.addCraftingOption(this, pattern);
            }
        }
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails pattern, InventoryCrafting table) {
        if (isBusy()) {
            return false;
        }

        // Stage the pattern inputs into the export buffer; the tick handler pushes
        // them into adjacent inventories.
        int bufferSlot = EXPORT_START;

        for (int i = 0; i < table.getSizeInventory(); i++) {
            ItemStack stack = table.getStackInSlot(i);
            if (stack == null || stack.stackSize <= 0) {
                continue;
            }

            if (bufferSlot >= EXPORT_START + BUFFER_SIZE) {
                return false;
            }

            inventory[bufferSlot++] = stack.copy();
        }

        preemptivelyBatch(pattern);

        markDirty();
        return true;
    }

    /**
     * Pulls ingredients for this pattern's remaining iterations out of the CPU
     * ahead of time, so a whole batch leaves in one go instead of one craft per
     * tick. This is what makes the unit "preemptive".
     */
    private void preemptivelyBatch(ICraftingPatternDetails pattern) {
        if (!LaziestConfig.fastCrafterPreemptiveBatching || !CraftingCpuInternals.isAvailable()) {
            return;
        }

        IGridNode node = getActionableNode();
        if (node == null || !node.isActive()) {
            return;
        }

        IGrid grid = node.getGrid();
        if (grid == null) {
            return;
        }

        ICraftingGrid crafting = grid.getCache(ICraftingGrid.class);
        if (crafting == null) {
            return;
        }

        for (ICraftingCPU cpu : crafting.getCpus()) {
            for (CraftingCpuInternals.CraftingTask task : CraftingCpuInternals.getTasks(cpu)) {
                if (!pattern.equals(task.getPattern())) {
                    continue;
                }

                // The invocation being pushed right now is already accounted for.
                long remaining = task.getInvocations() - 1L;

                while (remaining > 0L) {
                    ItemStack[] snapshot = copyExportBuffer();

                    if (!stageInputs(pattern) || !task.tryExtractItems(actionSource)) {
                        restoreExportBuffer(snapshot);
                        return;
                    }

                    task.decrement();
                    remaining--;
                }

                return;
            }
        }
    }

    /** Adds one iteration's ingredients to the export buffer, or fails if it will not fit. */
    private boolean stageInputs(ICraftingPatternDetails pattern) {
        for (IAEItemStack input : pattern.getCondensedInputs()) {
            if (input == null) {
                continue;
            }

            ItemStack stack = input.getItemStack();
            if (stack != null && !insertIntoExportBuffer(stack)) {
                return false;
            }
        }

        return true;
    }

    private boolean insertIntoExportBuffer(ItemStack stack) {
        ItemStack remaining = stack.copy();

        for (int i = EXPORT_START; i < EXPORT_START + BUFFER_SIZE; i++) {
            ItemStack existing = inventory[i];

            if (existing == null) {
                inventory[i] = remaining;
                return true;
            }

            if (existing.isItemEqual(remaining) && ItemStack.areItemStackTagsEqual(existing, remaining)) {
                int space = Math.min(existing.getMaxStackSize(), getInventoryStackLimit()) - existing.stackSize;
                int moved = Math.min(space, remaining.stackSize);

                if (moved > 0) {
                    existing.stackSize += moved;
                    remaining.stackSize -= moved;

                    if (remaining.stackSize <= 0) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private ItemStack[] copyExportBuffer() {
        ItemStack[] snapshot = new ItemStack[BUFFER_SIZE];

        for (int i = 0; i < BUFFER_SIZE; i++) {
            ItemStack stack = inventory[EXPORT_START + i];
            snapshot[i] = stack == null ? null : stack.copy();
        }

        return snapshot;
    }

    private void restoreExportBuffer(ItemStack[] snapshot) {
        for (int i = 0; i < BUFFER_SIZE; i++) {
            inventory[EXPORT_START + i] = snapshot[i];
        }
    }

    @Override
    public boolean isBusy() {
        for (int i = EXPORT_START; i < EXPORT_START + BUFFER_SIZE; i++) {
            if (inventory[i] != null) {
                return true;
            }
        }
        return false;
    }

    private void notifyPatternChange() {
        IGridNode node = getActionableNode();
        if (node == null || worldObj == null || worldObj.isRemote) {
            return;
        }

        IGrid grid = node.getGrid();
        if (grid != null) {
            grid.postEvent(new MENetworkCraftingPatternChange(this, node));
        }
    }

    public static boolean isPatternStack(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ICraftingPatternItem;
    }

    public static boolean isPatternSlot(int slot) {
        return slot >= PATTERN_START && slot < PATTERN_START + PATTERN_COUNT;
    }

    public static boolean isImportSlot(int slot) {
        return slot >= IMPORT_START && slot < IMPORT_START + BUFFER_SIZE;
    }

    public static boolean isExportSlot(int slot) {
        return slot >= EXPORT_START && slot < EXPORT_START + BUFFER_SIZE;
    }

    // === NBT ===

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);

        NBTTagList items = new NBTTagList();
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (inventory[i] != null) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setByte("Slot", (byte)i);
                inventory[i].writeToNBT(itemTag);
                items.appendTag(itemTag);
            }
        }
        tag.setTag("Items", items);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        for (int i = 0; i < SLOT_COUNT; i++) {
            inventory[i] = null;
        }

        NBTTagList items = tag.getTagList("Items", 10);
        for (int i = 0; i < items.tagCount(); i++) {
            NBTTagCompound itemTag = items.getCompoundTagAt(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot >= 0 && slot < SLOT_COUNT) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(itemTag);
            }
        }
    }

    // === IInventory ===

    @Override
    public int getSizeInventory() {
        return SLOT_COUNT;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return slot >= 0 && slot < SLOT_COUNT ? inventory[slot] : null;
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (slot < 0 || slot >= SLOT_COUNT || inventory[slot] == null || amount <= 0) {
            return null;
        }

        ItemStack stack = inventory[slot];
        ItemStack result;

        if (stack.stackSize <= amount) {
            inventory[slot] = null;
            result = stack;
        } else {
            result = stack.splitStack(amount);
            if (stack.stackSize == 0) {
                inventory[slot] = null;
            }
        }

        markDirty();
        if (isPatternSlot(slot)) {
            notifyPatternChange();
        }
        return result;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return null;
        }

        ItemStack stack = inventory[slot];
        inventory[slot] = null;
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return;
        }

        inventory[slot] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }

        markDirty();
        if (isPatternSlot(slot)) {
            notifyPatternChange();
        }
    }

    @Override
    public String getInventoryName() {
        return "container.laziestae2.fast_crafter";
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
        return worldObj != null
                && worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
                && player.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64D;
    }

    @Override
    public void openInventory() {
    }

    @Override
    public void closeInventory() {
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (isPatternSlot(slot)) {
            return isPatternStack(stack);
        }

        return isImportSlot(slot);
    }

    // === ISidedInventory ===

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        MachineSideMode mode = getSideMode(side);
        if (mode == MachineSideMode.NONE) {
            return new int[0];
        }

        int count = 0;
        int[] slots = new int[SLOT_COUNT];

        if (mode.allowsInput()) {
            for (int slot = IMPORT_START; slot < IMPORT_START + BUFFER_SIZE; slot++) {
                slots[count++] = slot;
            }
        }

        if (mode.allowsOutput()) {
            for (int slot = EXPORT_START; slot < EXPORT_START + BUFFER_SIZE; slot++) {
                slots[count++] = slot;
            }
        }

        int[] trimmed = new int[count];
        System.arraycopy(slots, 0, trimmed, 0, count);
        return trimmed;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        return getSideMode(side).allowsInput() && isImportSlot(slot) && isItemValidForSlot(slot, stack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return getSideMode(side).allowsOutput() && isExportSlot(slot);
    }
}
