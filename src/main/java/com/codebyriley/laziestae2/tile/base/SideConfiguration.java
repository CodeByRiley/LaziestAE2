package com.codebyriley.laziestae2.tile.base;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Per-face item IO state plus the block's facing, shared by every tile that
 * exposes a side-IO pad in its GUI.
 */
public class SideConfiguration {

    public static final int FACE_COUNT = 6;
    public static final int DEFAULT_FACING = 2;

    /** Widget pad order: up, left, front, right, down, back. */
    public static final int WIDGET_UP = 0;
    public static final int WIDGET_LEFT = 1;
    public static final int WIDGET_FRONT = 2;
    public static final int WIDGET_RIGHT = 3;
    public static final int WIDGET_DOWN = 4;

    private final MachineSideMode[] modes = new MachineSideMode[FACE_COUNT];
    private int facing = DEFAULT_FACING;

    public SideConfiguration() {
        this(MachineSideMode.OMNI);
    }

    public SideConfiguration(MachineSideMode defaultMode) {
        for (int i = 0; i < modes.length; i++) {
            modes[i] = defaultMode;
        }
    }

    public MachineSideMode getMode(int side) {
        return side >= 0 && side < modes.length ? modes[side] : MachineSideMode.NONE;
    }

    /** Returns true if the mode actually changed. */
    public boolean setMode(int side, MachineSideMode mode) {
        if (side < 0 || side >= modes.length || mode == null || modes[side] == mode)
            return false;

        modes[side] = mode;
        return true;
    }

    public int getFacing() {
        return facing;
    }

    public boolean setFacing(int side) {
        if (side < 2 || side > 5 || facing == side)
            return false;

        facing = side;
        return true;
    }

    /** Absolute block side shown at a position on the side-IO pad. */
    public int getWidgetSide(int widget) {
        switch (widget) {
            case WIDGET_UP:
                return 1;
            case WIDGET_DOWN:
                return 0;
            case WIDGET_FRONT:
                return facing;
            case WIDGET_LEFT:
                return getLeftSide();
            case WIDGET_RIGHT:
                return ForgeDirection.getOrientation(getLeftSide()).getOpposite().ordinal();
            default:
                return ForgeDirection.getOrientation(facing).getOpposite().ordinal();
        }
    }

    /** Left as seen by a player looking at the front face. */
    private int getLeftSide() {
        switch (facing) {
            case 2:
                return 5;
            case 3:
                return 4;
            case 4:
                return 2;
            default:
                return 3;
        }
    }

    public void writeToNBT(NBTTagCompound tag) {
        byte[] packed = new byte[modes.length];
        for (int i = 0; i < modes.length; i++) {
            packed[i] = (byte)modes[i].ordinal();
        }

        tag.setByteArray("SideModes", packed);
        tag.setByte("Facing", (byte)facing);
    }

    public void readFromNBT(NBTTagCompound tag) {
        byte[] packed = tag.getByteArray("SideModes");
        for (int i = 0; i < modes.length && i < packed.length; i++) {
            modes[i] = MachineSideMode.byIndex(packed[i]);
        }

        if (tag.hasKey("Facing"))
            facing = tag.getByte("Facing");
    }
}
