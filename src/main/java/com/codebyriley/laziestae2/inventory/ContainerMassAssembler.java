package com.codebyriley.laziestae2.inventory;

import com.codebyriley.laziestae2.gui.MachineGuiDefinition;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerController;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerPatternProvider;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;

public class ContainerMassAssembler extends Container {

    private static final int ENERGY_SCALE = 10000;
    private static final int PROGRESS_FORMED = 0;
    private static final int PROGRESS_TOO_LARGE = 1;
    private static final int PROGRESS_PARTS = 2;
    private static final int PROGRESS_FRAMES = 3;
    private static final int PROGRESS_VENTS = 4;
    private static final int PROGRESS_PATTERN_PROVIDERS = 5;
    private static final int PROGRESS_CRAFTING_COPROCESSORS = 6;
    private static final int PROGRESS_IO_PORTS = 7;
    private static final int PROGRESS_ENERGY = 8;
    private static final int PROGRESS_JOBS = 9;
    private static final int PROGRESS_WORK = 10;

    private static final int PATTERN_ROWS = 4;
    private static final int PATTERN_COLUMNS = 9;
    private static final int PATTERNS_PER_PAGE = PATTERN_ROWS * PATTERN_COLUMNS;

    private final TileMassAssemblerController controller;

    private int formed;
    private int tooLarge;
    private int partCount;
    private int frameCount;
    private int ventCount;
    private int patternProviderCount;
    private int craftingCoprocessorCount;
    private int ioPortCount;
    private int energyScaled;
    private int jobCount;
    private int work;

    private final List<PatternPage> pages = new ArrayList<PatternPage>();
    private int currentPage;

    public ContainerMassAssembler(InventoryPlayer playerInventory, TileMassAssemblerController controller) {
        this.controller = controller;

        addPatternPages();
        addPlayerInventory(playerInventory);
        refreshFromController();
    }

    /**
     * One page of 4x9 pattern slots per pattern provider. The page count comes
     * from the synced provider count, and unresolved tiles fall back to a dummy
     * inventory, so the client and server always build identical slot lists.
     */
    private void addPatternPages() {
        List<TileMassAssemblerPatternProvider> providers = controller.getPatternProviders();
        int pageCount = Math.max(providers.size(), controller.getPatternProviderCount());

        for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
            IInventory inventory = pageIndex < providers.size()
                    ? providers.get(pageIndex)
                    : new InventoryBasic("", false, TileMassAssemblerPatternProvider.PATTERN_SLOTS);

            PatternPage page = new PatternPage(pageIndex);

            for (int row = 0; row < PATTERN_ROWS; row++) {
                for (int column = 0; column < PATTERN_COLUMNS; column++) {
                    int index = row * PATTERN_COLUMNS + column;
                    page.slots.add((SlotPattern)addSlotToContainer(
                            new SlotPattern(inventory, index, 8 + column * 18, 18 + row * 18, page)));
                }
            }

            pages.add(page);
        }
    }

    public int getPageCount() {
        return pages.size();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int page) {
        if (page >= 0 && page < pages.size()) {
            currentPage = page;
        }
    }

    private boolean isPageActive(int pageIndex) {
        return pageIndex == currentPage;
    }

    public boolean isPatternSlot(int slotIndex) {
        return slotIndex >= 0 && slotIndex < getPatternSlotCount();
    }

    private int getPatternSlotCount() {
        return pages.size() * PATTERNS_PER_PAGE;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return controller.isUseableByPlayer(player);
    }

    @Override
    public void addCraftingToCrafters(ICrafting listener) {
        super.addCraftingToCrafters(listener);
        sendProgress(listener);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        int currentFormed = controller.isFormed() ? 1 : 0;
        int currentTooLarge = controller.isStructureTooLarge() ? 1 : 0;
        int currentPartCount = controller.getPartCount();
        int currentFrameCount = controller.getFrameCount();
        int currentVentCount = controller.getVentCount();
        int currentPatternProviderCount = controller.getPatternProviderCount();
        int currentCraftingCoprocessorCount = controller.getCraftingCoprocessorCount();
        int currentIoPortCount = controller.getIoPortCount();
        int currentEnergy = getEnergyScaledFromController();
        int currentJobCount = controller.getJobCount();
        int currentWork = controller.getWork();

        for (Object crafterObject : crafters) {
            ICrafting crafter = (ICrafting)crafterObject;

            sendIfChanged(crafter, PROGRESS_FORMED, formed, currentFormed);
            sendIfChanged(crafter, PROGRESS_TOO_LARGE, tooLarge, currentTooLarge);
            sendIfChanged(crafter, PROGRESS_PARTS, partCount, currentPartCount);
            sendIfChanged(crafter, PROGRESS_FRAMES, frameCount, currentFrameCount);
            sendIfChanged(crafter, PROGRESS_VENTS, ventCount, currentVentCount);
            sendIfChanged(crafter, PROGRESS_PATTERN_PROVIDERS, patternProviderCount, currentPatternProviderCount);
            sendIfChanged(crafter, PROGRESS_CRAFTING_COPROCESSORS, craftingCoprocessorCount, currentCraftingCoprocessorCount);
            sendIfChanged(crafter, PROGRESS_IO_PORTS, ioPortCount, currentIoPortCount);
            sendIfChanged(crafter, PROGRESS_ENERGY, energyScaled, currentEnergy);
            sendIfChanged(crafter, PROGRESS_JOBS, jobCount, currentJobCount);
            sendIfChanged(crafter, PROGRESS_WORK, work, currentWork);
        }

        formed = currentFormed;
        tooLarge = currentTooLarge;
        partCount = currentPartCount;
        frameCount = currentFrameCount;
        ventCount = currentVentCount;
        patternProviderCount = currentPatternProviderCount;
        craftingCoprocessorCount = currentCraftingCoprocessorCount;
        ioPortCount = currentIoPortCount;
        energyScaled = currentEnergy;
        jobCount = currentJobCount;
        work = currentWork;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int value) {
        switch (id) {
            case PROGRESS_FORMED:
                formed = value;
                break;
            case PROGRESS_TOO_LARGE:
                tooLarge = value;
                break;
            case PROGRESS_PARTS:
                partCount = value;
                break;
            case PROGRESS_FRAMES:
                frameCount = value;
                break;
            case PROGRESS_VENTS:
                ventCount = value;
                break;
            case PROGRESS_PATTERN_PROVIDERS:
                patternProviderCount = value;
                break;
            case PROGRESS_CRAFTING_COPROCESSORS:
                craftingCoprocessorCount = value;
                break;
            case PROGRESS_IO_PORTS:
                ioPortCount = value;
                break;
            case PROGRESS_ENERGY:
                energyScaled = value;
                break;
            case PROGRESS_JOBS:
                jobCount = value;
                break;
            case PROGRESS_WORK:
                work = value;
                break;
            default:
                break;
        }
    }

    public TileMassAssemblerController getController() {
        return controller;
    }

    public boolean isFormed() {
        return formed != 0;
    }

    public boolean isTooLarge() {
        return tooLarge != 0;
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

    public int getJobCount() {
        return jobCount;
    }

    public int getWork() {
        return work;
    }

    public float getEnergyFraction() {
        return Math.min((float)energyScaled / (float)ENERGY_SCALE, 1F);
    }

    @Override
    public ItemStack slotClick(int slotId, int button, int mode, EntityPlayer player) {
        // Slots on hidden pages are still part of the container; ignore clicks on them.
        if (slotId >= 0 && slotId < getPatternSlotCount() && !isPageActive(slotId / PATTERNS_PER_PAGE)) {
            return null;
        }

        return super.slotClick(slotId, button, mode, player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        int patternSlots = getPatternSlotCount();
        ItemStack original = null;
        Slot slot = (Slot)inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            original = stack.copy();

            if (index < patternSlots) {
                if (!isPageActive(index / PATTERNS_PER_PAGE)
                        || !mergeItemStack(stack, patternSlots, inventorySlots.size(), true)) {
                    return null;
                }
            } else {
                if (!TileMassAssemblerPatternProvider.isPatternStack(stack)) {
                    return null;
                }

                // Only the visible page accepts shift-clicked patterns.
                int pageStart = currentPage * PATTERNS_PER_PAGE;
                if (!mergeItemStack(stack, pageStart, pageStart + PATTERNS_PER_PAGE, false)) {
                    return null;
                }
            }

            if (stack.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }

            if (stack.stackSize == original.stackSize) {
                return null;
            }

            slot.onPickupFromSlot(player, stack);
        }

        return original;
    }

    private class PatternPage {

        private final int index;
        private final List<SlotPattern> slots = new ArrayList<SlotPattern>();

        private PatternPage(int index) {
            this.index = index;
        }

        private boolean isActive() {
            return isPageActive(index);
        }
    }

    private static class SlotPattern extends Slot {

        private final PatternPage page;

        private SlotPattern(IInventory inventory, int index, int x, int y, PatternPage page) {
            super(inventory, index, x, y);
            this.page = page;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return page.isActive() && TileMassAssemblerPatternProvider.isPatternStack(stack);
        }

        @Override
        public boolean canTakeStack(EntityPlayer player) {
            return page.isActive();
        }

        @Override
        public int getSlotStackLimit() {
            return 1;
        }

        /** Hides slots belonging to pages that are not on screen. */
        @Override
        public boolean func_111238_b() {
            return page.isActive();
        }
    }

    private void addPlayerInventory(InventoryPlayer playerInventory) {
        MachineGuiDefinition definition = MachineGuiDefinition.BIG_ASSEMBLER;

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

    private void refreshFromController() {
        formed = controller.isFormed() ? 1 : 0;
        tooLarge = controller.isStructureTooLarge() ? 1 : 0;
        partCount = controller.getPartCount();
        frameCount = controller.getFrameCount();
        ventCount = controller.getVentCount();
        patternProviderCount = controller.getPatternProviderCount();
        craftingCoprocessorCount = controller.getCraftingCoprocessorCount();
        ioPortCount = controller.getIoPortCount();
        energyScaled = getEnergyScaledFromController();
        jobCount = controller.getJobCount();
        work = controller.getWork();
    }

    private void sendProgress(ICrafting crafter) {
        crafter.sendProgressBarUpdate(this, PROGRESS_FORMED, formed);
        crafter.sendProgressBarUpdate(this, PROGRESS_TOO_LARGE, tooLarge);
        crafter.sendProgressBarUpdate(this, PROGRESS_PARTS, partCount);
        crafter.sendProgressBarUpdate(this, PROGRESS_FRAMES, frameCount);
        crafter.sendProgressBarUpdate(this, PROGRESS_VENTS, ventCount);
        crafter.sendProgressBarUpdate(this, PROGRESS_PATTERN_PROVIDERS, patternProviderCount);
        crafter.sendProgressBarUpdate(this, PROGRESS_CRAFTING_COPROCESSORS, craftingCoprocessorCount);
        crafter.sendProgressBarUpdate(this, PROGRESS_IO_PORTS, ioPortCount);
        crafter.sendProgressBarUpdate(this, PROGRESS_ENERGY, energyScaled);
        crafter.sendProgressBarUpdate(this, PROGRESS_JOBS, jobCount);
        crafter.sendProgressBarUpdate(this, PROGRESS_WORK, work);
    }

    private void sendIfChanged(ICrafting crafter, int id, int oldValue, int newValue) {
        if (oldValue != newValue) {
            crafter.sendProgressBarUpdate(this, id, newValue);
        }
    }

    private int getEnergyScaledFromController() {
        double capacity = controller.getEnergyCapacity();
        if (capacity <= 0D) {
            return 0;
        }

        return Math.min(ENERGY_SCALE, (int)Math.round(controller.getStoredEnergy() * ENERGY_SCALE / capacity));
    }
}
