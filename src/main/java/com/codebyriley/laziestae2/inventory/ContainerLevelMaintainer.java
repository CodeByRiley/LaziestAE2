package com.codebyriley.laziestae2.inventory;

import com.codebyriley.laziestae2.gui.MachineGuiDefinition;
import com.codebyriley.laziestae2.tile.machines.TileLevelMaintainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerLevelMaintainer extends Container {

    public static final int SLOT_X = 17;
    public static final int SLOT_Y_BASE = 19;
    public static final int SLOT_Y_STEP = 20;

    private final TileLevelMaintainer tile;

    public ContainerLevelMaintainer(InventoryPlayer playerInventory, TileLevelMaintainer tile) {
        this.tile = tile;

        for (int i = 0; i < TileLevelMaintainer.REQ_COUNT; i++) {
            addSlotToContainer(new SlotFake(tile.getRequestInventory(), i, SLOT_X, SLOT_Y_BASE + SLOT_Y_STEP * i));
        }

        MachineGuiDefinition definition = MachineGuiDefinition.LEVEL_MAINTAINER;
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

    public TileLevelMaintainer getTile() {
        return tile;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUseableByPlayer(player);
    }

    @Override
    public ItemStack slotClick(int slotId, int button, int mode, EntityPlayer player) {
        if (slotId >= 0 && slotId < TileLevelMaintainer.REQ_COUNT) {
            if (!player.worldObj.isRemote) {
                if (mode == 1) {
                    // Shift-click clears the request.
                    tile.setRequest(slotId, null);
                } else if (mode == 0) {
                    tile.setRequest(slotId, player.inventory.getItemStack());
                }
            }

            return null;
        }

        return super.slotClick(slotId, button, mode, player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return null;
    }

    public static class SlotFake extends Slot {

        public SlotFake(net.minecraft.inventory.IInventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }

        @Override
        public boolean canTakeStack(EntityPlayer player) {
            return false;
        }
    }
}
