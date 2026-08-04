package com.codebyriley.laziestae2.tile.base;

/** A tile whose faces can be configured for item IO from its GUI. */
public interface ISideConfigurable {

    MachineSideMode getSideMode(int side);

    void setSideMode(int side, MachineSideMode mode);

    int getFacing();

    void setFacing(int side);

    /** Absolute block side for a position on the GUI's side-IO pad. */
    int getWidgetSide(int widget);

    /** False for tiles that push items as part of their normal operation. */
    boolean supportsAutoExport();

    boolean isAutoExporting();

    void setAutoExporting(boolean exporting);
}
