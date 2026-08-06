package com.codebyriley.laziestae2.tile.massassembler;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
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
import com.codebyriley.laziestae2.config.LaziestConfig;
import com.codebyriley.laziestae2.tile.base.PoweredGridBlock;
import com.codebyriley.laziestae2.tile.base.TilePowered;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

public class TileMassAssemblerController extends TilePowered implements IMassAssemblerPart, ICraftingProvider {

    private static final double ENERGY_CAPACITY = 100000D;
    private static final int SCAN_INTERVAL_TICKS = 20;
    private static final int JOB_INPUT_SLOTS = 9;
    private static final int JOB_SYNC_INTERVAL = 10;

    private boolean formed;
    private boolean structureTooLarge;
    private int scanTimer;
    private int partCount;
    private int frameCount;
    private int ventCount;
    private int patternProviderCount;
    private int craftingCoprocessorCount;
    private int ioPortCount;

    private final Deque<AssemblyJob> jobQueue = new ArrayDeque<AssemblyJob>();
    private final Deque<ItemStack> outputBuffer = new ArrayDeque<ItemStack>();
    private final BaseActionSource actionSource = new MachineSource(this);

    private List<int[]> patternProviderPositions = new ArrayList<int[]>();
    private int work;
    private boolean patternsDirty;

    /** Head-of-queue snapshot, synced to clients for the GUI job preview. */
    private ItemStack activeJobResult;
    private final ItemStack[] activeJobInputs = new ItemStack[JOB_INPUT_SLOTS];
    private int jobSyncTimer;

    public TileMassAssemblerController() {
        super(ENERGY_CAPACITY);
    }

    @Override
    public MassAssemblerPartType getPartType() {
        return MassAssemblerPartType.CONTROLLER;
    }

    @Override
    protected double getIdlePowerUsage() {
        return Math.max(0D, LaziestConfig.idlePower);
    }

    @Override
    protected PoweredGridBlock createGridBlock() {
        return new MassAssemblerGridBlock(this);
    }

    @Override
    protected EnumSet<GridFlags> getGridFlags() {
        // MULTIBLOCK keeps the whole structure on the controller's single channel.
        return EnumSet.of(GridFlags.REQUIRE_CHANNEL, GridFlags.MULTIBLOCK);
    }

    @Override
    protected void updateServer() {
        if (scanTimer-- <= 0) {
            scanTimer = SCAN_INTERVAL_TICKS;
            refreshStructure();
        }

        IGridNode node = getActionableNode();
        if (node == null || !node.isActive())
            return;

        IGrid grid = node.getGrid();
        if (grid == null)
            return;

        if (patternsDirty) {
            patternsDirty = false;
            grid.postEvent(new MENetworkCraftingPatternChange(this, node));
        }

        drainOutputBuffer(grid);
        processJobs(grid);
        syncActiveJob();
    }

    /**
     * Refreshes the client-visible job preview. Throttled, since the head of the
     * queue can change every tick on a well-upgraded structure.
     */
    private void syncActiveJob() {
        if (--jobSyncTimer > 0)
            return;

        jobSyncTimer = JOB_SYNC_INTERVAL;

        AssemblyJob head = jobQueue.peek();
        ItemStack newResult = head != null ? head.result : null;

        if (!ItemStack.areItemStacksEqual(activeJobResult, newResult)) {
            activeJobResult = newResult == null ? null : newResult.copy();

            for (int i = 0; i < JOB_INPUT_SLOTS; i++) {
                ItemStack input = head == null ? null : head.inputs[i];
                activeJobInputs[i] = input == null ? null : input.copy();
            }

            markForUpdate();
        }
    }

    public ItemStack getActiveJobResult() {
        return activeJobResult;
    }

    public ItemStack getActiveJobInput(int index) {
        return index >= 0 && index < JOB_INPUT_SLOTS ? activeJobInputs[index] : null;
    }

    private void drainOutputBuffer(IGrid grid) {
        if (outputBuffer.isEmpty())
            return;

        IEnergyGrid energyGrid = grid.getCache(IEnergyGrid.class);
        IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
        if (energyGrid == null || storageGrid == null)
            return;

        IMEMonitor<IAEItemStack> inventory = storageGrid.getItemInventory();

        for (int i = outputBuffer.size(); i > 0; i--) {
            ItemStack stack = outputBuffer.poll();
            if (stack == null)
                continue;

            IAEItemStack aeStack = AEApi.instance().storage().createItemStack(stack);
            IAEItemStack remainder = AEApi.instance().storage()
                    .poweredInsert(energyGrid, inventory, aeStack, actionSource);

            if (remainder != null && remainder.getStackSize() > 0) {
                ItemStack rest = stack.copy();
                rest.stackSize = (int)remainder.getStackSize();
                outputBuffer.offer(rest);
            }

            markDirty();
        }
    }

    private void processJobs(IGrid grid) {
        if (!formed)
            return;

        int jobs = jobQueue.size();
        if (jobs == 0) {
            if (work != 0) {
                work = 0;
                markDirty();
            }
            return;
        }

        IEnergyGrid energyGrid = grid.getCache(IEnergyGrid.class);
        if (energyGrid == null)
            return;

        int previousWork = work;
        int workPerJob = getWorkPerJob();
        int workToDo = Math.min(getWorkRate(), jobs * workPerJob - work);

        if (workToDo > 0) {
            double energyUnitCost = getEnergyCostPerWork();
            if (energyUnitCost > 0D) {
                double extracted = energyGrid.extractAEPower(
                        energyUnitCost * workToDo, Actionable.MODULATE, PowerMultiplier.CONFIG);
                if (extracted > 0D)
                    work += (int)Math.ceil(extracted / energyUnitCost);
            } else {
                work += workToDo;
            }
        }

        while (work >= workPerJob && !jobQueue.isEmpty()) {
            AssemblyJob job = jobQueue.poll();
            work -= workPerJob;

            outputBuffer.offer(job.result);
            for (ItemStack remainder : job.remainders) {
                if (remainder != null && remainder.stackSize > 0)
                    outputBuffer.offer(remainder);
            }
        }

        if (previousWork != work)
            markDirty();
    }

    // === ICraftingProvider ===

    @Override
    public void provideCrafting(ICraftingProviderHelper helper) {
        if (!formed || worldObj == null)
            return;

        for (int[] pos : patternProviderPositions) {
            TileEntity tile = worldObj.getTileEntity(pos[0], pos[1], pos[2]);
            if (tile instanceof TileMassAssemblerPatternProvider)
                ((TileMassAssemblerPatternProvider)tile).providePatterns(this, helper);
        }
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails pattern, InventoryCrafting table) {
        if (!formed || !pattern.isCraftable() || isBusy())
            return false;

        ItemStack output = pattern.getOutput(table, worldObj);
        if (output == null || output.stackSize <= 0)
            return false;

        ItemStack[] inputs = new ItemStack[JOB_INPUT_SLOTS];
        ItemStack[] remainders = new ItemStack[JOB_INPUT_SLOTS];
        int max = Math.min(table.getSizeInventory(), JOB_INPUT_SLOTS);

        for (int i = 0; i < max; i++) {
            ItemStack stack = table.getStackInSlot(i);
            if (stack == null)
                continue;

            inputs[i] = stack.copy();
            if (stack.getItem() != null && stack.getItem().hasContainerItem(stack))
                remainders[i] = stack.getItem().getContainerItem(stack.copy());
        }

        jobQueue.offer(new AssemblyJob(inputs, remainders, output.copy()));
        markDirty();
        return true;
    }

    @Override
    public boolean isBusy() {
        return jobQueue.size() >= Math.max(1, LaziestConfig.jobQueueSize);
    }

    /**
     * The chamber has no side-IO flyout to host the control, so it is left out
     * rather than carrying a mode nothing can change.
     */
    @Override
    public boolean supportsRedstoneControl() {
        return false;
    }

    public void notifyPatternUpdate() {
        patternsDirty = true;
    }

    // === Stats ===

    public int getJobCount() {
        return jobQueue.size();
    }

    public int getWork() {
        return work;
    }

    /** Pattern provider tiles in the formed structure, in scan order. */
    public List<TileMassAssemblerPatternProvider> getPatternProviders() {
        List<TileMassAssemblerPatternProvider> providers = new ArrayList<TileMassAssemblerPatternProvider>();

        if (worldObj == null)
            return providers;

        for (int[] pos : patternProviderPositions) {
            TileEntity tile = worldObj.getTileEntity(pos[0], pos[1], pos[2]);
            if (tile instanceof TileMassAssemblerPatternProvider)
                providers.add((TileMassAssemblerPatternProvider)tile);
        }

        return providers;
    }

    public int getJobQueueSize() {
        return Math.max(1, LaziestConfig.jobQueueSize);
    }

    public int getWorkPerJob() {
        return Math.max(1, LaziestConfig.workPerJob);
    }

    public int getWorkRate() {
        return Math.max(0, LaziestConfig.workPerTickBase)
                + Math.max(1, LaziestConfig.workPerTickUpgrade) * craftingCoprocessorCount;
    }

    public double getEnergyCostPerWork() {
        return Math.max(0D, LaziestConfig.energyPerWorkBase)
                + Math.max(0D, LaziestConfig.energyPerWorkUpgrade) * craftingCoprocessorCount;
    }

    public void refreshStructure() {
        MassAssemblerStructure.ScanResult result = MassAssemblerStructure.scan(worldObj, xCoord, yCoord, zCoord);
        applyScanResult(result);
    }

    public boolean isFormed() {
        return formed;
    }

    public boolean isStructureTooLarge() {
        return structureTooLarge;
    }

    public int getPartCount() {
        return partCount;
    }

    public int getFrameCount() {
        return frameCount;
    }

    public int getVentCount() {
        return ventCount;
    }

    public int getPatternProviderCount() {
        return patternProviderCount;
    }

    public int getCraftingCoprocessorCount() {
        return craftingCoprocessorCount;
    }

    public int getIoPortCount() {
        return ioPortCount;
    }

    /** Spills queued job inputs and buffered outputs; called when the block is broken. */
    public List<ItemStack> getDropStacks() {
        List<ItemStack> drops = new ArrayList<ItemStack>();

        for (AssemblyJob job : jobQueue) {
            for (ItemStack input : job.inputs) {
                if (input != null && input.stackSize > 0)
                    drops.add(input);
            }
        }

        drops.addAll(outputBuffer);

        jobQueue.clear();
        outputBuffer.clear();
        return drops;
    }

    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj != null
                && worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
                && player.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64D
                && hasPermission(player, SecurityPermissions.BUILD);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        writeStructureNBT(tag);
        tag.setInteger("AssemblyWork", work);

        NBTTagList jobList = new NBTTagList();
        for (AssemblyJob job : jobQueue) {
            jobList.appendTag(job.toNBT());
        }
        tag.setTag("Jobs", jobList);

        NBTTagList outputList = new NBTTagList();
        for (ItemStack stack : outputBuffer) {
            NBTTagCompound entry = new NBTTagCompound();
            stack.writeToNBT(entry);
            outputList.appendTag(entry);
        }
        tag.setTag("OutputBuffer", outputList);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        readStructureNBT(tag);
        work = Math.max(0, tag.getInteger("AssemblyWork"));

        jobQueue.clear();
        NBTTagList jobList = tag.getTagList("Jobs", 10);
        for (int i = 0; i < jobList.tagCount(); i++) {
            AssemblyJob job = AssemblyJob.fromNBT(jobList.getCompoundTagAt(i));
            if (job != null)
                jobQueue.offer(job);
        }

        outputBuffer.clear();
        NBTTagList outputList = tag.getTagList("OutputBuffer", 10);
        for (int i = 0; i < outputList.tagCount(); i++) {
            ItemStack stack = ItemStack.loadItemStackFromNBT(outputList.getCompoundTagAt(i));
            if (stack != null)
                outputBuffer.offer(stack);
        }
    }

    @Override
    protected void writeSyncNBT(NBTTagCompound tag) {
        super.writeSyncNBT(tag);
        writeStructureNBT(tag);
        tag.setInteger("AssemblyWork", work);
        tag.setInteger("JobCount", jobQueue.size());
        writeJobPreviewNBT(tag);
    }

    @Override
    protected void readSyncNBT(NBTTagCompound tag) {
        super.readSyncNBT(tag);
        readStructureNBT(tag);
        work = Math.max(0, tag.getInteger("AssemblyWork"));
        clientJobCount = tag.getInteger("JobCount");
        readJobPreviewNBT(tag);
    }

    private void writeJobPreviewNBT(NBTTagCompound tag) {
        if (activeJobResult != null) {
            NBTTagCompound resultTag = new NBTTagCompound();
            activeJobResult.writeToNBT(resultTag);
            tag.setTag("JobResult", resultTag);
        }

        NBTTagList inputList = new NBTTagList();
        for (int i = 0; i < JOB_INPUT_SLOTS; i++) {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setByte("Slot", (byte)i);
            if (activeJobInputs[i] != null)
                activeJobInputs[i].writeToNBT(entry);
            inputList.appendTag(entry);
        }
        tag.setTag("JobInputs", inputList);
    }

    private void readJobPreviewNBT(NBTTagCompound tag) {
        activeJobResult = tag.hasKey("JobResult")
                ? ItemStack.loadItemStackFromNBT(tag.getCompoundTag("JobResult"))
                : null;

        for (int i = 0; i < JOB_INPUT_SLOTS; i++) {
            activeJobInputs[i] = null;
        }

        NBTTagList inputList = tag.getTagList("JobInputs", 10);
        for (int i = 0; i < inputList.tagCount(); i++) {
            NBTTagCompound entry = inputList.getCompoundTagAt(i);
            int slot = entry.getByte("Slot") & 255;
            if (slot < JOB_INPUT_SLOTS && entry.hasKey("id"))
                activeJobInputs[slot] = ItemStack.loadItemStackFromNBT(entry);
        }
    }

    private int clientJobCount;

    public int getJobCountForDisplay() {
        return worldObj != null && worldObj.isRemote ? clientJobCount : jobQueue.size();
    }

    private void applyScanResult(MassAssemblerStructure.ScanResult result) {
        boolean newFormed = result.isFormed();
        boolean newTooLarge = result.isTooLarge();
        int newPartCount = result.getPartCount();
        int newFrameCount = result.getFrameCount();
        int newVentCount = result.getVentCount();
        int newPatternProviderCount = result.getPatternProviderCount();
        int newCraftingCoprocessorCount = result.getCraftingCoprocessorCount();
        int newIoPortCount = result.getIoPortCount();

        patternProviderPositions = result.getPatternProviderPositions();
        updatePartActivity(result.getPartPositions(), newFormed);

        if (formed != newFormed
                || structureTooLarge != newTooLarge
                || partCount != newPartCount
                || frameCount != newFrameCount
                || ventCount != newVentCount
                || patternProviderCount != newPatternProviderCount
                || craftingCoprocessorCount != newCraftingCoprocessorCount
                || ioPortCount != newIoPortCount) {
            boolean formationChanged = formed != newFormed;

            formed = newFormed;
            structureTooLarge = newTooLarge;
            partCount = newPartCount;
            frameCount = newFrameCount;
            ventCount = newVentCount;
            patternProviderCount = newPatternProviderCount;



            craftingCoprocessorCount = newCraftingCoprocessorCount;
            ioPortCount = newIoPortCount;

            if (formationChanged)
                notifyPatternUpdate();

            markDirty();
            markForUpdate();
        }
    }

    /** Propagates the formed state to every part so they render their lit texture. */
    private void updatePartActivity(List<int[]> positions, boolean formedNow) {
        if (worldObj == null)
            return;

        for (int[] pos : positions) {
            TileEntity tile = worldObj.getTileEntity(pos[0], pos[1], pos[2]);
            if (tile instanceof TileMassAssemblerPart) {
                TileMassAssemblerPart part = (TileMassAssemblerPart)tile;
                part.setActive(formedNow);
                // Lets the part report structure state without scanning the cluster itself.
                part.setControllerPosition(xCoord, yCoord, zCoord);
            }
        }
    }

    private void writeStructureNBT(NBTTagCompound tag) {
        // Synced so the client builds the same pattern pages the server does;
        // a mismatch would desync container slot indices.
        int[] packed = new int[patternProviderPositions.size() * 3];
        for (int i = 0; i < patternProviderPositions.size(); i++) {
            int[] pos = patternProviderPositions.get(i);
            packed[i * 3] = pos[0];
            packed[i * 3 + 1] = pos[1];
            packed[i * 3 + 2] = pos[2];
        }
        tag.setIntArray("PatternProviderPositions", packed);

        tag.setBoolean("Formed", formed);
        tag.setBoolean("StructureTooLarge", structureTooLarge);
        tag.setInteger("PartCount", partCount);
        tag.setInteger("FrameCount", frameCount);
        tag.setInteger("VentCount", ventCount);
        tag.setInteger("PatternProviderCount", patternProviderCount);
        tag.setInteger("CraftingCoprocessorCount", craftingCoprocessorCount);
        tag.setInteger("IoPortCount", ioPortCount);
    }

    private void readStructureNBT(NBTTagCompound tag) {
        int[] packed = tag.getIntArray("PatternProviderPositions");
        List<int[]> positions = new ArrayList<int[]>();
        for (int i = 0; i + 2 < packed.length; i += 3) {
            positions.add(new int[] { packed[i], packed[i + 1], packed[i + 2] });
        }
        patternProviderPositions = positions;

        formed = tag.getBoolean("Formed");
        structureTooLarge = tag.getBoolean("StructureTooLarge");
        partCount = tag.getInteger("PartCount");
        frameCount = tag.getInteger("FrameCount");
        ventCount = tag.getInteger("VentCount");
        patternProviderCount = tag.getInteger("PatternProviderCount");
        craftingCoprocessorCount = tag.getInteger("CraftingCoprocessorCount");
        ioPortCount = tag.getInteger("IoPortCount");
    }

    private static final class AssemblyJob {

        private final ItemStack[] inputs;
        private final ItemStack[] remainders;
        private final ItemStack result;

        private AssemblyJob(ItemStack[] inputs, ItemStack[] remainders, ItemStack result) {
            this.inputs = inputs;
            this.remainders = remainders;
            this.result = result;
        }

        private NBTTagCompound toNBT() {
            NBTTagCompound tag = new NBTTagCompound();

            NBTTagList inputList = new NBTTagList();
            for (int i = 0; i < inputs.length; i++) {
                NBTTagCompound entry = new NBTTagCompound();
                entry.setByte("Slot", (byte)i);
                if (inputs[i] != null)
                    inputs[i].writeToNBT(entry);
                inputList.appendTag(entry);
            }
            tag.setTag("Inputs", inputList);

            NBTTagList remainderList = new NBTTagList();
            for (int i = 0; i < remainders.length; i++) {
                NBTTagCompound entry = new NBTTagCompound();
                entry.setByte("Slot", (byte)i);
                if (remainders[i] != null)
                    remainders[i].writeToNBT(entry);
                remainderList.appendTag(entry);
            }
            tag.setTag("Remainders", remainderList);

            NBTTagCompound resultTag = new NBTTagCompound();
            result.writeToNBT(resultTag);
            tag.setTag("Result", resultTag);
            return tag;
        }

        private static AssemblyJob fromNBT(NBTTagCompound tag) {
            ItemStack result = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("Result"));
            if (result == null)
                return null;

            ItemStack[] inputs = readStackArray(tag.getTagList("Inputs", 10));
            ItemStack[] remainders = readStackArray(tag.getTagList("Remainders", 10));
            return new AssemblyJob(inputs, remainders, result);
        }

        private static ItemStack[] readStackArray(NBTTagList list) {
            ItemStack[] stacks = new ItemStack[JOB_INPUT_SLOTS];
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound entry = list.getCompoundTagAt(i);
                int slot = entry.getByte("Slot") & 255;
                if (slot < JOB_INPUT_SLOTS && entry.hasKey("id"))
                    stacks[slot] = ItemStack.loadItemStackFromNBT(entry);
            }
            return stacks;
        }
    }
}
