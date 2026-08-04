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

    private final MassAssemblerPartType partType;

    /** Set by the controller when the structure forms; drives the lit texture. */
    private boolean active;

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
    protected void onActiveChanged() {
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("Active", active);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        active = tag.getBoolean("Active");
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

        if (wasActive != active && worldObj != null) {
            worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
        }
    }
}
