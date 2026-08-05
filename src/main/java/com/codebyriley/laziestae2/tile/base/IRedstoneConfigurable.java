package com.codebyriley.laziestae2.tile.base;

/** A tile whose work can be gated on the redstone signal reaching it. */
public interface IRedstoneConfigurable {

    RedstoneMode getRedstoneMode();

    void setRedstoneMode(RedstoneMode mode);

    /** False when the current mode and signal say the machine should hold still. */
    boolean isRedstoneActive();

    /** False for tiles that have no work to gate, so the GUI hides the control. */
    boolean supportsRedstoneControl();
}
