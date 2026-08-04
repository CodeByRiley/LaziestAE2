package com.codebyriley.laziestae2.inventory;

import com.codebyriley.laziestae2.gui.MachineGuiDefinition;
import com.codebyriley.laziestae2.tile.machines.TileFastCrafter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerFastCrafter extends Container {

    private final TileFastCrafter tile;

    public ContainerFastCrafter(InventoryPlayer playerInventory, TileFastCrafter tile) {
        this.tile = tile;

        // Pattern slots along the bottom row of the machine area.
        for (int i = 0; i < TileFastCrafter.PATTERN_COUNT; i++) {
            addSlotToContainer(new SlotPattern(tile, TileFastCrafter.PATTERN_START + i, 8 + 18 * i, 62));
        }

        // Import buffer, 3x3 on the left.
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlotToContainer(new Slot(tile,
                        TileFastCrafter.IMPORT_START + row * 3 + column, 62 + column * 18, 8 + row * 18));
            }
        }

        // Export buffer, 3x3 on the right, output only.
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlotToContainer(new SlotOutput(tile,
                        TileFastCrafter.EXPORT_START + row * 3 + column, 116 + column * 18, 8 + row * 18));
            }
        }

        MachineGuiDefinition definition = MachineGuiDefinition.FAST_CRAFTER;
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
            addSlotToContainer(new Slot(playerInventory, column, 8 + column * 18, definition.getPlayerHotbarY()));
        }
    }

    public TileFastCrafter getTile() {
        return tile;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUseableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack original = null;
        Slot slot = (Slot)inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            original = stack.copy();

            if (index < TileFastCrafter.SLOT_COUNT) {
                if (!mergeItemStack(stack, TileFastCrafter.SLOT_COUNT, inventorySlots.size(), true)) {
                    return null;
                }

                slot.onSlotChange(stack, original);
            } else if (TileFastCrafter.isPatternStack(stack)) {
                if (!mergeItemStack(stack, TileFastCrafter.PATTERN_START,
                        TileFastCrafter.PATTERN_START + TileFastCrafter.PATTERN_COUNT, false)) {
                    return null;
                }
            } else if (!mergeItemStack(stack, TileFastCrafter.IMPORT_START,
                    TileFastCrafter.IMPORT_START + TileFastCrafter.BUFFER_SIZE, false)) {
                return null;
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

    private static class SlotPattern extends Slot {

        public SlotPattern(net.minecraft.inventory.IInventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return TileFastCrafter.isPatternStack(stack);
        }

        @Override
        public int getSlotStackLimit() {
            return 1;
        }
    }
}
