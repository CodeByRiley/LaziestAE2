package com.codebyriley.laziestae2.tile.base;

/**
 * When a machine is allowed to run, relative to the redstone signal reaching it.
 * <p>
 * Declaration order is the order the GUI control cycles through and the value
 * stored in NBT, so IGNORE stays first and new modes go on the end. The sprite
 * each mode uses is given separately, since the texture is laid out for reading
 * rather than for this order.
 */
public enum RedstoneMode {

    /** Redstone is not consulted; the machine always runs. */
    IGNORE(2, false),
    /** Runs only while powered. */
    HIGH(1, true),
    /** Runs only while unpowered. */
    LOW(0, true),
    /** Never runs, whatever the signal. */
    NEVER(3, false);

    private static final RedstoneMode[] VALUES = values();

    private final int frame;
    private final boolean usesSignal;

    RedstoneMode(int frame, boolean usesSignal) {
        this.frame = frame;
        this.usesSignal = usesSignal;
    }

    public static RedstoneMode byIndex(int index) {
        return index >= 0 && index < VALUES.length ? VALUES[index] : IGNORE;
    }

    /** Index of this mode's 16x16 frame in redstone_mode.png. */
    public int getFrame() {
        return frame;
    }

    /** False for modes whose answer does not depend on the signal. */
    public boolean usesSignal() {
        return usesSignal;
    }

    /** Whether a machine in this mode may run given the signal reaching it. */
    public boolean allowsWork(boolean powered) {
        switch (this) {
            case HIGH:
                return powered;
            case LOW:
                return !powered;
            case NEVER:
                return false;
            default:
                return true;
        }
    }

    public RedstoneMode next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    public RedstoneMode prev() {
        return VALUES[(ordinal() + VALUES.length - 1) % VALUES.length];
    }

    public String getUnlocalizedName() {
        return "gui.laziestae2.redstone." + name().toLowerCase();
    }
}
