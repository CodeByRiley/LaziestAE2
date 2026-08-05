package com.codebyriley.laziestae2.integration.tooltip;

import com.codebyriley.laziestae2.tile.base.TileMachine;
import java.util.List;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;

/**
 * Progress, buffered power and installed acceleration cards for the four
 * processing machines. The upgrade count is not in the tile's description
 * packet, so this is synchronised from the server rather than read client-side.
 */
public final class MachineTooltip {

    private MachineTooltip() { }

    public static boolean supports(TileEntity tile) {
        return tile instanceof TileMachine;
    }

    public static void writeData(NBTTagCompound data, TileEntity tile) {
        if (!supports(tile))
            return;

        TileMachine machine = (TileMachine)tile;
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("Work", machine.getWork());
        tag.setInteger("MaxWork", machine.getMaxWorkForDisplay());
        tag.setBoolean("Working", machine.isWorking());
        tag.setDouble("Energy", machine.getStoredEnergy());
        tag.setDouble("MaxEnergy", machine.getEnergyCapacity());
        tag.setInteger("Upgrades", machine.getUpgradeCount());
        tag.setBoolean("Connected", machine.isGridConnected());
        data.setTag(TooltipKeys.MACHINE, tag);
    }

    public static boolean hasData(NBTTagCompound data) {
        return data.hasKey(TooltipKeys.MACHINE);
    }

    public static NBTTagCompound read(NBTTagCompound data) {
        return data.getCompoundTag(TooltipKeys.MACHINE);
    }

    public static int getMaxWork(NBTTagCompound tag) {
        return tag.getInteger("MaxWork");
    }

    public static int getWork(NBTTagCompound tag) {
        return Math.min(tag.getInteger("Work"), Math.max(getMaxWork(tag), 0));
    }

    public static boolean isWorking(NBTTagCompound tag) {
        return tag.getBoolean("Working") && getMaxWork(tag) > 0;
    }

    /**
     * @param withProgress false for front ends that draw the progress bar themselves.
     */
    public static void appendLines(NBTTagCompound tag, List<String> lines, boolean withProgress) {
        if (withProgress) {
            lines.add(isWorking(tag)
                    ? TooltipText.format("working_percent", TooltipText.percent(getWork(tag), getMaxWork(tag)))
                    : TooltipText.translate("idle"));
        } else if (!isWorking(tag)) {
            lines.add(TooltipText.translate("idle"));
        }

        lines.add(TooltipText.format("energy",
                TooltipText.ae(tag.getDouble("Energy")),
                TooltipText.ae(tag.getDouble("MaxEnergy"))));

        int upgrades = tag.getInteger("Upgrades");
        if (upgrades > 0)
            lines.add(TooltipText.format("cards", upgrades, TileMachine.MAX_UPGRADES));

        if (!tag.getBoolean("Connected"))
            lines.add(EnumChatFormatting.RED + TooltipText.translate("no_channel"));
    }
}
