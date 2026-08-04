package com.codebyriley.laziestae2.tile.base;

/** Per-face item IO configuration for a machine. */
public enum MachineSideMode {

    NONE(false, false),
    INPUT(true, false),
    OUTPUT(false, true),
    OMNI(true, true);

    private static final MachineSideMode[] VALUES = values();

    private final boolean allowsInput;
    private final boolean allowsOutput;

    MachineSideMode(boolean allowsInput, boolean allowsOutput) {
        this.allowsInput = allowsInput;
        this.allowsOutput = allowsOutput;
    }

    public static MachineSideMode byIndex(int index) {
        return index >= 0 && index < VALUES.length ? VALUES[index] : NONE;
    }

    public boolean allowsInput() {
        return allowsInput;
    }

    public boolean allowsOutput() {
        return allowsOutput;
    }

    public MachineSideMode next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    public MachineSideMode prev() {
        return VALUES[(ordinal() + VALUES.length - 1) % VALUES.length];
    }

    public String getUnlocalizedName() {
        return "gui.laziestae2.side_io." + name().toLowerCase();
    }
}
