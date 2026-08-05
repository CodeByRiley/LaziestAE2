package com.codebyriley.laziestae2.integration.tooltip;

import com.codebyriley.laziestae2.tile.machines.TileFastCrafter;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;

/**
 * Pattern count and buffer state for the preemptive assembly unit, so a stalled
 * unit can be spotted without opening its GUI.
 */
public final class FastCrafterTooltip {

    private FastCrafterTooltip() {
    }

    public static boolean supports(TileEntity tile) {
        return tile instanceof TileFastCrafter;
    }

    public static void writeData(NBTTagCompound data, TileEntity tile) {
        if (!supports(tile)) {
            return;
        }

        TileFastCrafter crafter = (TileFastCrafter)tile;
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("Patterns", countStacks(crafter, TileFastCrafter.PATTERN_START, TileFastCrafter.PATTERN_COUNT));
        tag.setInteger("Import", countStacks(crafter, TileFastCrafter.IMPORT_START, TileFastCrafter.BUFFER_SIZE));
        tag.setInteger("Export", countStacks(crafter, TileFastCrafter.EXPORT_START, TileFastCrafter.BUFFER_SIZE));
        tag.setBoolean("Connected", crafter.isGridConnected());
        data.setTag(TooltipKeys.FAST_CRAFTER, tag);
    }

    public static boolean hasData(NBTTagCompound data) {
        return data.hasKey(TooltipKeys.FAST_CRAFTER);
    }

    public static NBTTagCompound read(NBTTagCompound data) {
        return data.getCompoundTag(TooltipKeys.FAST_CRAFTER);
    }

    public static void appendLines(NBTTagCompound tag, List<String> lines) {
        lines.add(TooltipText.format("patterns", tag.getInteger("Patterns"), TileFastCrafter.PATTERN_COUNT));

        // A non-empty export buffer means ingredients are staged but the adjacent
        // crafter has not taken them yet, which is what "busy" means here.
        lines.add(tag.getInteger("Export") > 0
                ? TooltipText.translate("staging")
                : TooltipText.translate("ready"));

        int imported = tag.getInteger("Import");
        if (imported > 0) {
            lines.add(TooltipText.format("returning", imported));
        }

        if (!tag.getBoolean("Connected")) {
            lines.add(EnumChatFormatting.RED + TooltipText.translate("no_channel"));
        }
    }

    private static int countStacks(TileFastCrafter crafter, int start, int length) {
        int count = 0;
        for (int slot = start; slot < start + length; slot++) {
            ItemStack stack = crafter.getStackInSlot(slot);
            if (stack != null) {
                count++;
            }
        }
        return count;
    }
}
