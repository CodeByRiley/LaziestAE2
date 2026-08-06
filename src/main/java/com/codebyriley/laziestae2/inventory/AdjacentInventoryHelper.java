package com.codebyriley.laziestae2.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public final class AdjacentInventoryHelper {

    private AdjacentInventoryHelper() { }

    /** Inserts into the inventory on one specific face. Returns the remainder, or null. */
    public static ItemStack insertIntoSide(World world, int x, int y, int z, int side, ItemStack stack) {
        if (world == null || stack == null || stack.stackSize <= 0)
            return stack;

        ForgeDirection dir = ForgeDirection.getOrientation(side);
        if (dir == ForgeDirection.UNKNOWN)
            return stack;

        TileEntity tile = world.getTileEntity(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
        if (!(tile instanceof IInventory))
            return stack;

        return insert((IInventory)tile, stack, dir.getOpposite());
    }

    private static ItemStack insert(IInventory inventory, ItemStack stack, ForgeDirection fromSide) {
        ItemStack remaining = stack.copy();

        for (int slot : getAccessibleSlots(inventory, fromSide)) {
            if (!canPlaceInSlot(inventory, slot, remaining, fromSide))
                continue;

            ItemStack existing = inventory.getStackInSlot(slot);
            int limit = Math.min(inventory.getInventoryStackLimit(), remaining.getMaxStackSize());

            if (existing == null) {
                ItemStack placed = remaining.copy();
                placed.stackSize = Math.min(remaining.stackSize, limit);
                inventory.setInventorySlotContents(slot, placed);
                remaining.stackSize -= placed.stackSize;
            } else if (existing.isItemEqual(remaining) && ItemStack.areItemStackTagsEqual(existing, remaining)) {
                int space = limit - existing.stackSize;
                if (space > 0) {
                    int moved = Math.min(space, remaining.stackSize);
                    existing.stackSize += moved;
                    remaining.stackSize -= moved;
                    inventory.markDirty();
                }
            }

            if (remaining.stackSize <= 0)
                return null;
        }

        return remaining;
    }

    private static int simulateInsert(IInventory inventory, ItemStack stack, int amount, ForgeDirection fromSide) {
        int accepted = 0;

        for (int slot : getAccessibleSlots(inventory, fromSide)) {
            if (!canPlaceInSlot(inventory, slot, stack, fromSide))
                continue;

            ItemStack existing = inventory.getStackInSlot(slot);
            int limit = Math.min(inventory.getInventoryStackLimit(), stack.getMaxStackSize());

            if (existing == null)
                accepted += limit;
            else if (existing.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(existing, stack))
                accepted += Math.max(0, limit - existing.stackSize);

            if (accepted >= amount)
                return amount;
        }

        return accepted;
    }

    private static int[] getAccessibleSlots(IInventory inventory, ForgeDirection fromSide) {
        if (inventory instanceof ISidedInventory)
            return ((ISidedInventory)inventory).getAccessibleSlotsFromSide(fromSide.ordinal());

        int[] slots = new int[inventory.getSizeInventory()];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = i;
        }
        return slots;
    }

    private static boolean canPlaceInSlot(IInventory inventory, int slot, ItemStack stack, ForgeDirection fromSide) {
        if (!inventory.isItemValidForSlot(slot, stack))
            return false;

        return !(inventory instanceof ISidedInventory)
                || ((ISidedInventory)inventory).canInsertItem(slot, stack, fromSide.ordinal());
    }
}
