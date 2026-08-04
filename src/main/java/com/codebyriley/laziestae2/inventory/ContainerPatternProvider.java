package com.codebyriley.laziestae2.inventory;

import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerPatternProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerPatternProvider extends Container {

    public static final int ROWS = 4;
    public static final int COLUMNS = 9;

    private final TileMassAssemblerPatternProvider tile;

    public ContainerPatternProvider(InventoryPlayer playerInventory, TileMassAssemblerPatternProvider tile) {
        this.tile = tile;

        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                addSlotToContainer(new SlotPattern(tile, column + row * COLUMNS, 8 + column * 18, 18 + row * 18));
            }
        }

        int playerInventoryY = 18 + ROWS * 18 + 13;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlotToContainer(new Slot(
                        playerInventory,
                        column + row * 9 + 9,
                        8 + column * 18,
                        playerInventoryY + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlotToContainer(new Slot(playerInventory, column, 8 + column * 18, playerInventoryY + 58));
        }
    }

    public TileMassAssemblerPatternProvider getTile() {
        return tile;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUseableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        int patternSlots = ROWS * COLUMNS;
        ItemStack original = null;
        Slot slot = (Slot)inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            original = stack.copy();

            if (index < patternSlots) {
                if (!mergeItemStack(stack, patternSlots, inventorySlots.size(), true)) {
                    return null;
                }
            } else {
                if (!TileMassAssemblerPatternProvider.isPatternStack(stack)) {
                    return null;
                }

                if (!mergeItemStack(stack, 0, patternSlots, false)) {
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

    private static class SlotPattern extends Slot {

        public SlotPattern(net.minecraft.inventory.IInventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return TileMassAssemblerPatternProvider.isPatternStack(stack);
        }

        @Override
        public int getSlotStackLimit() {
            return 1;
        }
    }
}
