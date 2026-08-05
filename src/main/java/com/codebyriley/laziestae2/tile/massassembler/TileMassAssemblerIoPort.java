package com.codebyriley.laziestae2.tile.massassembler;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;

/**
 * Exposes every pattern provider in the structure as one inventory, so hoppers
 * and pipes can load or retrieve patterns without opening a GUI.
 */
public class TileMassAssemblerIoPort extends TileMassAssemblerPart implements ISidedInventory {

    private static final int CACHE_REFRESH_TICKS = 40;

    private List<TileMassAssemblerPatternProvider> providerCache;
    private int[] slotCache;
    private int cacheTimer;

    public TileMassAssemblerIoPort() {
        super(MassAssemblerPartType.IO_PORT);
    }

    @Override
    protected void updateServer() {
        super.updateServer();

        // Providers can be added or removed without the formed state changing.
        if (--cacheTimer <= 0) {
            cacheTimer = CACHE_REFRESH_TICKS;
            invalidateCache();
        }
    }

    @Override
    protected void onActiveChanged() {
        invalidateCache();
    }

    public void invalidateCache() {
        providerCache = null;
        slotCache = null;
    }

    private List<TileMassAssemblerPatternProvider> getProviders() {
        if (providerCache == null) {
            providerCache = new ArrayList<TileMassAssemblerPatternProvider>();

            if (isActive() && worldObj != null) {
                TileMassAssemblerController controller =
                        MassAssemblerStructure.findConnectedController(worldObj, xCoord, yCoord, zCoord);
                if (controller != null)
                    providerCache = controller.getPatternProviders();
            }
        }

        return providerCache;
    }

    private TileMassAssemblerPatternProvider getProviderForSlot(int slot) {
        if (slot < 0)
            return null;

        List<TileMassAssemblerPatternProvider> providers = getProviders();
        int index = slot / TileMassAssemblerPatternProvider.PATTERN_SLOTS;

        if (index >= providers.size())
            return null;

        TileMassAssemblerPatternProvider provider = providers.get(index);
        return provider.isInvalid() ? null : provider;
    }

    private static int getLocalSlot(int slot) {
        return slot % TileMassAssemblerPatternProvider.PATTERN_SLOTS;
    }

    @Override
    public int getSizeInventory() {
        return getProviders().size() * TileMassAssemblerPatternProvider.PATTERN_SLOTS;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        TileMassAssemblerPatternProvider provider = getProviderForSlot(slot);
        return provider == null ? null : provider.getStackInSlot(getLocalSlot(slot));
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        TileMassAssemblerPatternProvider provider = getProviderForSlot(slot);
        return provider == null ? null : provider.decrStackSize(getLocalSlot(slot), amount);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        TileMassAssemblerPatternProvider provider = getProviderForSlot(slot);
        if (provider != null)
            provider.setInventorySlotContents(getLocalSlot(slot), stack);
    }

    @Override
    public String getInventoryName() {
        return "tile.laziestae2.big_assembler.io_port";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj != null
                && worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
                && player.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64D;
    }

    @Override
    public void openInventory() { }

    @Override
    public void closeInventory() { }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return isActive()
                && TileMassAssemblerPatternProvider.isPatternStack(stack)
                && getProviderForSlot(slot) != null;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        int size = getSizeInventory();

        if (slotCache == null || slotCache.length != size) {
            slotCache = new int[size];
            for (int i = 0; i < size; i++) {
                slotCache[i] = i;
            }
        }

        return slotCache;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        return isItemValidForSlot(slot, stack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return isActive();
    }
}
