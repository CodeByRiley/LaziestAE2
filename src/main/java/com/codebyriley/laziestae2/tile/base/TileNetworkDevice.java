package com.codebyriley.laziestae2.tile.base;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Base for grid devices that are not processing machines but still expose
 * per-face item IO, such as the assembly unit and level maintainer.
 */
public abstract class TileNetworkDevice extends TilePowered implements ISideConfigurable {

    private final SideConfiguration sides = new SideConfiguration();

    protected TileNetworkDevice(double energyCapacity) {
        super(energyCapacity);
    }

    @Override
    public MachineSideMode getSideMode(int side) {
        return sides.getMode(side);
    }

    @Override
    public void setSideMode(int side, MachineSideMode mode) {
        if (sides.setMode(side, mode)) {
            markDirty();
            markForUpdate();
        }
    }

    @Override
    public int getFacing() {
        return sides.getFacing();
    }

    @Override
    public void setFacing(int side) {
        if (sides.setFacing(side)) {
            markDirty();
            markForUpdate();
        }
    }

    @Override
    public int getWidgetSide(int widget) {
        return sides.getWidgetSide(widget);
    }

    /** These devices move items as part of normal operation, so there is no toggle. */
    @Override
    public boolean supportsAutoExport() {
        return false;
    }

    @Override
    public boolean isAutoExporting() {
        return false;
    }

    @Override
    public void setAutoExporting(boolean exporting) {
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        sides.writeToNBT(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        sides.readFromNBT(tag);
    }

    @Override
    protected void writeSyncNBT(NBTTagCompound tag) {
        super.writeSyncNBT(tag);
        sides.writeToNBT(tag);
    }

    @Override
    protected void readSyncNBT(NBTTagCompound tag) {
        super.readSyncNBT(tag);
        sides.readFromNBT(tag);
    }
}
