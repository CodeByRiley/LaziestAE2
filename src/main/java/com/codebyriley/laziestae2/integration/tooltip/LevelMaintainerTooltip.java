package com.codebyriley.laziestae2.integration.tooltip;

import com.codebyriley.laziestae2.tile.machines.TileLevelMaintainer;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;

/**
 * Per-line stock for the level maintainer. The line breakdown is only sent while
 * the player asks for details, since it is five extra rows of text.
 */
public final class LevelMaintainerTooltip {

    private LevelMaintainerTooltip() { }

    public static boolean supports(TileEntity tile) {
        return tile instanceof TileLevelMaintainer;
    }

    public static void writeData(NBTTagCompound data, TileEntity tile, boolean details) {
        if (!supports(tile))
            return;

        TileLevelMaintainer maintainer = (TileLevelMaintainer)tile;
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList lines = new NBTTagList();
        int active = 0;

        for (int slot = 0; slot < TileLevelMaintainer.REQ_COUNT; slot++) {
            if (!maintainer.isRequesting(slot))
                continue;

            active++;

            if (!details)
                continue;

            ItemStack stack = maintainer.getRequestStack(slot);
            NBTTagCompound line = new NBTTagCompound();
            line.setString("Name", stack == null ? "" : stack.getDisplayName());
            line.setLong("Target", maintainer.getRequestQuantity(slot));
            line.setLong("Stock", maintainer.getKnownCount(slot));
            line.setBoolean("Crafting", maintainer.isCrafting(slot));
            lines.appendTag(line);
        }

        tag.setInteger("Active", active);
        tag.setTag("Lines", lines);
        tag.setBoolean("Connected", maintainer.isGridConnected());
        data.setTag(TooltipKeys.LEVEL_MAINTAINER, tag);
    }

    public static boolean hasData(NBTTagCompound data) {
        return data.hasKey(TooltipKeys.LEVEL_MAINTAINER);
    }

    public static NBTTagCompound read(NBTTagCompound data) {
        return data.getCompoundTag(TooltipKeys.LEVEL_MAINTAINER);
    }

    public static void appendLines(NBTTagCompound tag, List<String> lines) {
        lines.add(TooltipText.format("lines", tag.getInteger("Active"), TileLevelMaintainer.REQ_COUNT));

        NBTTagList rows = tag.getTagList("Lines", 10);
        for (int i = 0; i < rows.tagCount(); i++) {
            NBTTagCompound row = rows.getCompoundTagAt(i);
            long stock = row.getLong("Stock");

            String text = TooltipText.format("line",
                    row.getString("Name"),
                    stock < 0L ? TooltipText.translate("unknown") : TooltipText.count(stock),
                    TooltipText.count(row.getLong("Target")));

            if (row.getBoolean("Crafting"))
                text = text + " " + EnumChatFormatting.AQUA + TooltipText.translate("crafting");

            lines.add(text);
        }

        if (!tag.getBoolean("Connected"))
            lines.add(EnumChatFormatting.RED + TooltipText.translate("no_channel"));
    }
}
