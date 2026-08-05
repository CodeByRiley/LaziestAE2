package com.codebyriley.laziestae2.tile.massassembler;

import appeng.api.networking.GridFlags;
import com.codebyriley.laziestae2.tile.base.PoweredGridBlock;
import com.codebyriley.laziestae2.tile.base.TilePowered;
import java.util.EnumSet;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Structural assembler block. Carries a grid node purely so ME cables can
 * attach to any face of the structure; it stores no power and does no work.
 */
public abstract class TileMassAssemblerPart extends TilePowered implements IMassAssemblerPart {

    /** Sentinel for {@link #controllerY}; world Y is never negative. */
    private static final int NO_CONTROLLER = -1;

    private final MassAssemblerPartType partType;

    /** Set by the controller when the structure forms; drives the lit texture. */
    private boolean active;

    // Stamped by the controller during its structure scan so a part can reach it
    // without scanning the whole cluster itself. Server-side only.
    private int controllerX;
    private int controllerY = NO_CONTROLLER;
    private int controllerZ;

    protected TileMassAssemblerPart(MassAssemblerPartType partType) {
        super(0D);
        this.partType = partType;
    }

    @Override
    protected PoweredGridBlock createGridBlock() {
        return new MassAssemblerGridBlock(this);
    }

    @Override
    protected EnumSet<GridFlags> getGridFlags() {
        // Parts never require a channel of their own; the controller does that.
        return EnumSet.of(GridFlags.MULTIBLOCK);
    }

    @Override
    protected double getIdlePowerUsage() {
        return 0D;
    }

    /** Parts do no work of their own; the controller carries the redstone control. */
    @Override
    public boolean supportsRedstoneControl() {
        return false;
    }

    @Override
    public MassAssemblerPartType getPartType() {
        return partType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        if (this.active != active) {
            this.active = active;
            onActiveChanged();
            markDirty();
            markForUpdate();
        }
    }

    /** Hook for parts that cache structure-derived state. */
    protected void onActiveChanged() { }

    /** Records which controller scanned this part into its structure. */
    public void setControllerPosition(int x, int y, int z) {
        if (controllerX == x && controllerY == y && controllerZ == z)
            return;

        controllerX = x;
        controllerY = y;
        controllerZ = z;
        markDirty();
    }

    /**
     * Controller of the structure this part belongs to, or null when it is not
     * part of one. Like {@link #isActive()}, this reflects the last scan rather
     * than the present moment, so it is re-checked against the world.
     */
    public TileMassAssemblerController getController() {
        if (worldObj == null || controllerY < 0)
            return null;

        net.minecraft.tileentity.TileEntity tile = worldObj.getTileEntity(controllerX, controllerY, controllerZ);
        return tile instanceof TileMassAssemblerController ? (TileMassAssemblerController)tile : null;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("Active", active);
        tag.setIntArray("ControllerPos", new int[] { controllerX, controllerY, controllerZ });
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        active = tag.getBoolean("Active");

        int[] pos = tag.getIntArray("ControllerPos");
        if (pos.length == 3) {
            controllerX = pos[0];
            controllerY = pos[1];
            controllerZ = pos[2];
        } else {
            controllerY = NO_CONTROLLER;
        }
    }

    @Override
    protected void writeSyncNBT(NBTTagCompound tag) {
        super.writeSyncNBT(tag);
        tag.setBoolean("Active", active);
    }

    @Override
    protected void readSyncNBT(NBTTagCompound tag) {
        super.readSyncNBT(tag);
        boolean wasActive = active;
        active = tag.getBoolean("Active");

        if (wasActive != active && worldObj != null)
            worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
    }
}
