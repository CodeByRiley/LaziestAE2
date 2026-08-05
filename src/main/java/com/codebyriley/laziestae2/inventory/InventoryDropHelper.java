package com.codebyriley.laziestae2.inventory;

import java.util.Random;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public final class InventoryDropHelper {

    private static final Random RANDOM = new Random();

    private InventoryDropHelper() { }

    public static void dropInventoryItems(World world, int x, int y, int z, IInventory inventory) {
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);

            if (stack != null) {
                dropStack(world, x, y, z, stack);
                inventory.setInventorySlotContents(slot, null);
            }
        }
    }

    public static void dropStacks(World world, int x, int y, int z, java.util.List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            if (stack != null && stack.stackSize > 0)
                dropStack(world, x, y, z, stack);
        }
    }

    private static void dropStack(World world, int x, int y, int z, ItemStack stack) {
        float offsetX = RANDOM.nextFloat() * 0.8F + 0.1F;
        float offsetY = RANDOM.nextFloat() * 0.8F + 0.1F;
        float offsetZ = RANDOM.nextFloat() * 0.8F + 0.1F;

        while (stack.stackSize > 0) {
            int amount = RANDOM.nextInt(21) + 10;
            if (amount > stack.stackSize)
                amount = stack.stackSize;

            stack.stackSize -= amount;
            ItemStack droppedStack = new ItemStack(stack.getItem(), amount, stack.getItemDamage());

            if (stack.hasTagCompound())
                droppedStack.setTagCompound((NBTTagCompound)stack.getTagCompound().copy());

            EntityItem entityItem = new EntityItem(
                    world,
                    (double)x + offsetX,
                    (double)y + offsetY,
                    (double)z + offsetZ,
                    droppedStack);

            entityItem.motionX = RANDOM.nextGaussian() * 0.05000000074505806D;
            entityItem.motionY = RANDOM.nextGaussian() * 0.05000000074505806D + 0.20000000298023224D;
            entityItem.motionZ = RANDOM.nextGaussian() * 0.05000000074505806D;
            world.spawnEntityInWorld(entityItem);
        }
    }
}
