package com.codebyriley.laziestae2.tile.massassembler;

import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class TileMassAssemblerPatternProvider extends TileMassAssemblerPart implements IInventory {

    public static final int PATTERN_SLOTS = 36;

    private final ItemStack[] patterns = new ItemStack[PATTERN_SLOTS];
    private List<ICraftingPatternDetails> patternCache;

    public TileMassAssemblerPatternProvider() {
        super(MassAssemblerPartType.PATTERN_PROVIDER);
    }

    public static boolean isPatternStack(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ICraftingPatternItem;
    }

    /** Craftable pattern details held by this provider. */
    public List<ICraftingPatternDetails> getPatterns() {
        if (patternCache == null) {
            patternCache = new ArrayList<ICraftingPatternDetails>();

            if (worldObj != null) {
                for (int i = 0; i < PATTERN_SLOTS; i++) {
                    ItemStack stack = patterns[i];
                    if (!isPatternStack(stack)) {
                        continue;
                    }

                    ICraftingPatternDetails pattern =
                            ((ICraftingPatternItem)stack.getItem()).getPatternForItem(stack, worldObj);
                    if (pattern != null && pattern.isCraftable()) {
                        patternCache.add(pattern);
                    }
                }
            }
        }

        return patternCache;
    }

    public void providePatterns(ICraftingMedium core, ICraftingProviderHelper helper) {
        for (ICraftingPatternDetails pattern : getPatterns()) {
            helper.addCraftingOption(core, pattern);
        }
    }

    private void onPatternsChanged() {
        patternCache = null;
        markDirty();

        if (worldObj != null && !worldObj.isRemote) {
            TileMassAssemblerController controller =
                    MassAssemblerStructure.findConnectedController(worldObj, xCoord, yCoord, zCoord);
            if (controller != null) {
                controller.notifyPatternUpdate();
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);

        NBTTagList items = new NBTTagList();
        for (int i = 0; i < PATTERN_SLOTS; i++) {
            if (patterns[i] != null) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setByte("Slot", (byte)i);
                patterns[i].writeToNBT(itemTag);
                items.appendTag(itemTag);
            }
        }
        tag.setTag("Patterns", items);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        for (int i = 0; i < PATTERN_SLOTS; i++) {
            patterns[i] = null;
        }

        NBTTagList items = tag.getTagList("Patterns", 10);
        for (int i = 0; i < items.tagCount(); i++) {
            NBTTagCompound itemTag = items.getCompoundTagAt(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot >= 0 && slot < PATTERN_SLOTS) {
                patterns[slot] = ItemStack.loadItemStackFromNBT(itemTag);
            }
        }

        patternCache = null;
    }

    @Override
    public int getSizeInventory() {
        return PATTERN_SLOTS;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return slot >= 0 && slot < PATTERN_SLOTS ? patterns[slot] : null;
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (slot < 0 || slot >= PATTERN_SLOTS || patterns[slot] == null || amount <= 0) {
            return null;
        }

        ItemStack stack = patterns[slot];
        ItemStack result;

        if (stack.stackSize <= amount) {
            patterns[slot] = null;
            result = stack;
        } else {
            result = stack.splitStack(amount);
            if (stack.stackSize == 0) {
                patterns[slot] = null;
            }
        }

        onPatternsChanged();
        return result;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        if (slot < 0 || slot >= PATTERN_SLOTS) {
            return null;
        }

        ItemStack stack = patterns[slot];
        patterns[slot] = null;
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot < 0 || slot >= PATTERN_SLOTS) {
            return;
        }

        patterns[slot] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }

        onPatternsChanged();
    }

    @Override
    public String getInventoryName() {
        return "container.laziestae2.big_assembler.patterns";
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
                && player.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64D
                && hasPermission(player, SecurityPermissions.BUILD);
    }

    @Override
    public void openInventory() {
    }

    @Override
    public void closeInventory() {
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return isPatternStack(stack);
    }
}
