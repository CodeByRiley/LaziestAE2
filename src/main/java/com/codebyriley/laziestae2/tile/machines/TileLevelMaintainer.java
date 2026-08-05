package com.codebyriley.laziestae2.tile.machines;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import com.codebyriley.laziestae2.LaziestAE2;
import com.codebyriley.laziestae2.config.LaziestConfig;
import com.codebyriley.laziestae2.tile.base.TileNetworkDevice;
import com.google.common.collect.ImmutableSet;
import java.util.EnumSet;
import java.util.concurrent.Future;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class TileLevelMaintainer extends TileNetworkDevice
        implements ICraftingRequester, IStackWatcherHost, IInventory {

    public static final int REQ_COUNT = 5;

    private static final double ENERGY_CAPACITY = 10000D;

    private final ItemStack[] requestStacks = new ItemStack[REQ_COUNT];
    private final long[] requestQtys = new long[REQ_COUNT];
    private final long[] requestBatches = new long[REQ_COUNT];

    // Crafted items are buffered here until they can be pushed into the network.
    private final ItemStack[] results = new ItemStack[REQ_COUNT];

    @SuppressWarnings("unchecked")
    private final Future<ICraftingJob>[] pendingJobs = new Future[REQ_COUNT];
    private final ICraftingLink[] links = new ICraftingLink[REQ_COUNT];

    private final BaseActionSource actionSource = new MachineSource(this);
    private final RequestInventory requestInventory = new RequestInventory();

    private int sleepTicks;
    private int sleepIncrement = Math.max(0, LaziestConfig.levelMaintainerSleepMax);

    /** Network stock per request line; -1 means "not yet known". */
    private final long[] knownCounts = new long[REQ_COUNT];
    private IStackWatcher watcher;

    public TileLevelMaintainer() {
        super(ENERGY_CAPACITY);

        for (int i = 0; i < REQ_COUNT; i++) {
            requestBatches[i] = 1;
            knownCounts[i] = -1L;
        }
    }

    // === IStackWatcherHost ===

    /** AE2 hands us a watcher when our node joins a grid. */
    @Override
    public void updateWatcher(IStackWatcher newWatcher) {
        watcher = newWatcher;
        resetWatcher();
    }

    private void resetWatcher() {
        if (watcher == null)
            return;

        watcher.clear();

        for (int i = 0; i < REQ_COUNT; i++) {
            if (requestStacks[i] != null)
                watcher.add(AEApi.instance().storage().createItemStack(requestStacks[i]));
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void onStackChange(IItemList items, IAEStack fullStack, IAEStack diffStack,
            BaseActionSource source, StorageChannel channel) {
        if (channel != StorageChannel.ITEMS || !(fullStack instanceof IAEItemStack))
            return;

        IAEItemStack changed = (IAEItemStack)fullStack;

        for (int i = 0; i < REQ_COUNT; i++) {
            if (requestStacks[i] != null && changed.isSameType(requestStacks[i]))
                knownCounts[i] = changed.getStackSize();
        }
    }

    /** Forces the next work tick to re-read stock for a line. */
    private void clearKnownCount(int slot) {
        knownCounts[slot] = -1L;
        resetWatcher();
    }

    @Override
    protected double getIdlePowerUsage() {
        return Math.max(0D, LaziestConfig.levelMaintainerIdlePower);
    }

    @Override
    protected EnumSet<GridFlags> getGridFlags() {
        return EnumSet.of(GridFlags.REQUIRE_CHANNEL);
    }

    @Override
    protected void updateServer() {
        if (sleepTicks-- > 0)
            return;

        if (!isRedstoneActive()) {
            sleepTicks = sleepIncrement;
            return;
        }

        IGridNode node = getActionableNode();
        if (node == null || !node.isActive()) {
            sleepTicks = sleepIncrement;
            return;
        }

        IGrid grid = node.getGrid();
        if (grid == null) {
            sleepTicks = sleepIncrement;
            return;
        }

        boolean workDone = pushResults(grid);
        workDone |= pollPendingJobs(grid);
        workDone |= requestCrafting(grid);

        int sleepMin = Math.max(0, LaziestConfig.levelMaintainerSleepMin);
        int sleepMax = Math.max(sleepMin, LaziestConfig.levelMaintainerSleepMax);

        if (workDone) {
            markDirty();
            if (sleepIncrement > sleepMin)
                sleepIncrement = Math.max(sleepIncrement - 20, sleepMin);
        } else if (sleepIncrement < sleepMax) {
            sleepIncrement = Math.min(sleepIncrement + 30, sleepMax);
        }

        sleepTicks = sleepIncrement;
    }

    private boolean pushResults(IGrid grid) {
        IEnergyGrid energyGrid = grid.getCache(IEnergyGrid.class);
        IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
        if (energyGrid == null || storageGrid == null)
            return false;

        IMEMonitor<IAEItemStack> inventory = storageGrid.getItemInventory();
        boolean workDone = false;

        for (int i = 0; i < REQ_COUNT; i++) {
            if (results[i] == null)
                continue;

            IAEItemStack aeStack = AEApi.instance().storage().createItemStack(results[i]);
            IAEItemStack remainder = AEApi.instance().storage().poweredInsert(energyGrid, inventory, aeStack, actionSource);

            if (remainder == null) {
                results[i] = null;
                workDone = true;
            } else if (remainder.getStackSize() < aeStack.getStackSize()) {
                results[i].stackSize = (int)remainder.getStackSize();
                workDone = true;
            }
        }

        return workDone;
    }

    private boolean pollPendingJobs(IGrid grid) {
        ICraftingGrid crafting = grid.getCache(ICraftingGrid.class);
        if (crafting == null)
            return false;

        boolean workDone = false;

        for (int i = 0; i < REQ_COUNT; i++) {
            if (pendingJobs[i] == null || !pendingJobs[i].isDone())
                continue;

            ICraftingJob job = null;
            try {
                job = pendingJobs[i].get();
            } catch (Exception e) {
                LaziestAE2.logger.warn("Level maintainer crafting job computation failed", e);
            }

            pendingJobs[i] = null;

            if (job != null && !job.isSimulation()) {
                ICraftingLink link = crafting.submitJob(job, this, null, false, actionSource);
                if (link != null) {
                    links[i] = link;
                    workDone = true;
                }
            }
        }

        return workDone;
    }

    private boolean requestCrafting(IGrid grid) {
        ICraftingGrid crafting = grid.getCache(ICraftingGrid.class);
        IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
        ISecurityGrid securityGrid = grid.getCache(ISecurityGrid.class);
        if (crafting == null || storageGrid == null || securityGrid == null)
            return false;


        IMEMonitor<IAEItemStack> inventory = storageGrid.getItemInventory();
        boolean workDone = false;

        for (int i = 0; i < REQ_COUNT; i++) {
            if (requestStacks[i] == null || links[i] != null || pendingJobs[i] != null || results[i] != null)
                continue;

            IAEItemStack requested = AEApi.instance().storage().createItemStack(requestStacks[i]);

            // The watcher keeps this current; only re-read when it is unknown.
            if (knownCounts[i] < 0L) {
                IAEItemStack stored = inventory.getStorageList().findPrecise(requested);
                knownCounts[i] = stored == null ? 0L : stored.getStackSize();
                workDone = true;
            }

            long missing = requestQtys[i] - knownCounts[i];
            long toCraft = Math.min(Math.max(missing, 0L), Math.max(requestBatches[i], 0L));

            if (toCraft > 0L) {
                requested.setStackSize(toCraft);
                pendingJobs[i] = crafting.beginCraftingJob(worldObj, grid, actionSource, requested, null);
                workDone = true;
            }
        }

        return workDone;
    }

    // === ICraftingRequester ===

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        ImmutableSet.Builder<ICraftingLink> builder = ImmutableSet.builder();
        for (int i = 0; i < REQ_COUNT; i++) {
            if (links[i] != null)
                builder.add(links[i]);
        }
        return builder.build();
    }

    @Override
    public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack stack, Actionable mode) {
        if (stack == null)
            return null;

        int slot = getSlotForLink(link);
        if (slot == -1)
            return stack;

        ItemStack incoming = stack.getItemStack();
        if (incoming == null)
            return stack;

        int space;
        ItemStack existing = results[slot];
        if (existing == null)
            space = Math.min(incoming.getMaxStackSize(), 64);
        else if (existing.isItemEqual(incoming) && ItemStack.areItemStackTagsEqual(existing, incoming))
            space = Math.min(existing.getMaxStackSize(), 64) - existing.stackSize;
        else
            space = 0;

        long accepted = Math.min((long)space, stack.getStackSize());
        if (accepted <= 0L)
            return stack;

        if (mode == Actionable.MODULATE) {
            if (existing == null) {
                ItemStack stored = incoming.copy();
                stored.stackSize = (int)accepted;
                results[slot] = stored;
            } else {
                existing.stackSize += (int)accepted;
            }

            markDirty();

            // Wake up so the buffered result gets pushed into the network promptly.
            sleepIncrement = Math.max(0, LaziestConfig.levelMaintainerSleepMin);
            sleepTicks = 0;
        }

        if (accepted >= stack.getStackSize())
            return null;

        IAEItemStack remainder = stack.copy();
        remainder.setStackSize(stack.getStackSize() - accepted);
        return remainder;
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        int slot = getSlotForLink(link);
        if (slot != -1) {
            links[slot] = null;
            markDirty();
        }
    }

    private int getSlotForLink(ICraftingLink link) {
        for (int i = 0; i < REQ_COUNT; i++) {
            if (links[i] != null && links[i].getCraftingID().equals(link.getCraftingID()))
                return i;
        }
        return -1;
    }

    // === Request configuration (called from container/packets, server side) ===

    public IInventory getRequestInventory() {
        return requestInventory;
    }

    public ItemStack getRequestStack(int slot) {
        return isRequestSlot(slot) ? requestStacks[slot] : null;
    }

    public long getRequestQuantity(int slot) {
        return isRequestSlot(slot) ? requestQtys[slot] : 0L;
    }

    public long getRequestBatch(int slot) {
        return isRequestSlot(slot) ? requestBatches[slot] : 0L;
    }

    public boolean isRequesting(int slot) {
        return isRequestSlot(slot) && requestStacks[slot] != null;
    }

    /** Last known network stock for a request line; -1 when it has not been read yet. */
    public long getKnownCount(int slot) {
        return isRequestSlot(slot) ? knownCounts[slot] : -1L;
    }

    /** Whether a crafting job is planned or running for a request line. */
    public boolean isCrafting(int slot) {
        return isRequestSlot(slot) && (links[slot] != null || pendingJobs[slot] != null);
    }

    public void setRequest(int slot, ItemStack held) {
        if (!isRequestSlot(slot))
            return;

        if (held == null) {
            requestStacks[slot] = null;
            requestQtys[slot] = 0L;
            cancelSlot(slot);
        } else {
            ItemStack ghost = held.copy();
            requestQtys[slot] = ghost.stackSize;
            ghost.stackSize = 1;
            requestStacks[slot] = ghost;
            cancelSlot(slot);
        }

        clearKnownCount(slot);

        markDirty();
        markForUpdate();
    }

    public void setRequestQuantity(int slot, long quantity) {
        if (!isRequestSlot(slot))
            return;

        if (quantity <= 0L) {
            setRequest(slot, null);
            return;
        }

        if (requestStacks[slot] != null) {
            requestQtys[slot] = quantity;
            markDirty();
            markForUpdate();
        }
    }

    public void setRequestBatch(int slot, long batch) {
        if (isRequestSlot(slot) && batch >= 0L) {
            requestBatches[slot] = batch;
            markDirty();
            markForUpdate();
        }
    }

    private void cancelSlot(int slot) {
        if (pendingJobs[slot] != null) {
            pendingJobs[slot].cancel(true);
            pendingJobs[slot] = null;
        }

        if (links[slot] != null) {
            links[slot].cancel();
            links[slot] = null;
        }
    }

    private static boolean isRequestSlot(int slot) {
        return slot >= 0 && slot < REQ_COUNT;
    }

    // === NBT ===

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        writeRequestNBT(tag);

        NBTTagList resultList = new NBTTagList();
        for (int i = 0; i < REQ_COUNT; i++) {
            NBTTagCompound entry = new NBTTagCompound();
            if (results[i] != null)
                results[i].writeToNBT(entry);
            resultList.appendTag(entry);
        }
        tag.setTag("Results", resultList);

        NBTTagList linkList = new NBTTagList();
        for (int i = 0; i < REQ_COUNT; i++) {
            NBTTagCompound entry = new NBTTagCompound();
            if (links[i] != null)
                links[i].writeToNBT(entry);
            linkList.appendTag(entry);
        }
        tag.setTag("CraftingLinks", linkList);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        readRequestNBT(tag);

        NBTTagList resultList = tag.getTagList("Results", 10);
        for (int i = 0; i < REQ_COUNT && i < resultList.tagCount(); i++) {
            NBTTagCompound entry = resultList.getCompoundTagAt(i);
            results[i] = entry.hasNoTags() ? null : ItemStack.loadItemStackFromNBT(entry);
        }

        NBTTagList linkList = tag.getTagList("CraftingLinks", 10);
        for (int i = 0; i < REQ_COUNT && i < linkList.tagCount(); i++) {
            NBTTagCompound entry = linkList.getCompoundTagAt(i);
            links[i] = entry.hasNoTags() ? null : AEApi.instance().storage().loadCraftingLink(entry, this);
        }
    }

    @Override
    protected void writeSyncNBT(NBTTagCompound tag) {
        super.writeSyncNBT(tag);
        writeRequestNBT(tag);
    }

    @Override
    protected void readSyncNBT(NBTTagCompound tag) {
        super.readSyncNBT(tag);
        readRequestNBT(tag);
    }

    private void writeRequestNBT(NBTTagCompound tag) {
        NBTTagList requestList = new NBTTagList();
        for (int i = 0; i < REQ_COUNT; i++) {
            NBTTagCompound entry = new NBTTagCompound();
            if (requestStacks[i] != null) {
                NBTTagCompound stackTag = new NBTTagCompound();
                requestStacks[i].writeToNBT(stackTag);
                entry.setTag("Stack", stackTag);
                entry.setLong("Count", requestQtys[i]);
            }
            entry.setLong("Batch", requestBatches[i]);
            requestList.appendTag(entry);
        }
        tag.setTag("Requests", requestList);
    }

    private void readRequestNBT(NBTTagCompound tag) {
        NBTTagList requestList = tag.getTagList("Requests", 10);
        for (int i = 0; i < REQ_COUNT && i < requestList.tagCount(); i++) {
            NBTTagCompound entry = requestList.getCompoundTagAt(i);
            if (entry.hasKey("Stack")) {
                requestStacks[i] = ItemStack.loadItemStackFromNBT(entry.getCompoundTag("Stack"));
                requestQtys[i] = entry.getLong("Count");
            } else {
                requestStacks[i] = null;
                requestQtys[i] = 0L;
            }
            requestBatches[i] = entry.getLong("Batch");
        }

        // Stock is re-read on the next work tick; the watcher repopulates after that.
        for (int i = 0; i < REQ_COUNT; i++) {
            knownCounts[i] = -1L;
        }
    }

    // === IInventory over the result buffer (so items drop on break / can be extracted) ===

    @Override
    public int getSizeInventory() {
        return REQ_COUNT;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return isRequestSlot(slot) ? results[slot] : null;
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (!isRequestSlot(slot) || results[slot] == null || amount <= 0)
            return null;

        ItemStack stack = results[slot];
        if (stack.stackSize <= amount) {
            results[slot] = null;
            markDirty();
            return stack;
        }

        ItemStack split = stack.splitStack(amount);
        markDirty();
        return split;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        if (!isRequestSlot(slot))
            return null;

        ItemStack stack = results[slot];
        results[slot] = null;
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (isRequestSlot(slot)) {
            results[slot] = stack;
            markDirty();
        }
    }

    @Override
    public String getInventoryName() {
        return "container.laziestae2.level_maintainer";
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
                        (double)xCoord + 0.5D,
                        (double)yCoord + 0.5D,
                        (double)zCoord + 0.5D) <= 64D &&
                hasPermission(player, SecurityPermissions.BUILD);
    }

    @Override
    public void openInventory() { }

    @Override
    public void closeInventory() { }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return false;
    }

    /**
     * Ghost-slot view over the request stacks, used by the container's fake slots.
     * Vanilla container sync pushes these to the client automatically.
     */
    private class RequestInventory implements IInventory {

        @Override
        public int getSizeInventory() {
            return REQ_COUNT;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return getRequestStack(slot);
        }

        @Override
        public ItemStack decrStackSize(int slot, int amount) {
            return null;
        }

        @Override
        public ItemStack getStackInSlotOnClosing(int slot) {
            return null;
        }

        @Override
        public void setInventorySlotContents(int slot, ItemStack stack) {
            // Client-side vanilla sync lands here; server-side config goes through setRequest.
            if (worldObj != null && worldObj.isRemote && isRequestSlot(slot))
                requestStacks[slot] = stack;
        }

        @Override
        public String getInventoryName() {
            return "container.laziestae2.level_maintainer.requests";
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
            return TileLevelMaintainer.this.isUseableByPlayer(player);
        }

        @Override
        public void openInventory() { }

        @Override
        public void closeInventory() { }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack) {
            return false;
        }

        @Override
        public void markDirty() {
            TileLevelMaintainer.this.markDirty();
        }
    }
}
