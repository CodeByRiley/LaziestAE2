package com.codebyriley.laziestae2.inventory;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

/** Shared layout every screen in this mod repeats. */
public abstract class ContainerBase extends Container {

    private static final int COLUMNS = 9;
    private static final int SLOT_SIZE = 18;
    private static final int LEFT = 8;

    /** The player's 3x9 main inventory, then the hotbar below it. */
    protected void addPlayerInventory(InventoryPlayer playerInventory, int inventoryY, int hotbarY) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                addSlotToContainer(new Slot(
                        playerInventory,
                        column + row * COLUMNS + COLUMNS,
                        LEFT + column * SLOT_SIZE,
                        inventoryY + row * SLOT_SIZE));
            }
        }

        for (int column = 0; column < COLUMNS; column++) {
            addSlotToContainer(new Slot(
                    playerInventory,
                    column,
                    LEFT + column * SLOT_SIZE,
                    hotbarY));
        }
    }
}
