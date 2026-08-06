package com.codebyriley.laziestae2.components;

import net.minecraft.nbt.NBTTagCompound;

public final class EnergyBuffer {
    private final double capacity;
    private double stored;
    private final Runnable onChanged;

    public EnergyBuffer(double capacity, Runnable onChanged) {
        if (Double.isNaN(capacity) || Double.isInfinite(capacity) || capacity < 0D)
            throw new IllegalArgumentException("Energy capacity must be finite and non-negative");

        this.capacity = capacity;
        this.onChanged = onChanged;
    }

    public double getStored() {
        return stored;
    }

    public double getCapacity() {
        return capacity;
    }

    public double getRemainingCapacity() {
        return capacity - stored;
    }

    public void setStored(double amount) {
        double clamped = clampToCapacity(amount);

        if (clamped != stored) {
            stored = clamped;
            notifyChanged();
        }
    }

    public double receive(double amount, boolean simulate) {
        if (!isFiniteAmount(amount) || amount <= 0D)
            return 0D;

        double accepted = Math.min(amount, getRemainingCapacity());

        if (!simulate && accepted > 0D)
            setStored(stored + accepted);

        return accepted;
    }

    public boolean consume(double amount, boolean simulate) {
        if (!isFiniteAmount(amount) || amount < 0D)
            return false;

        if (stored < amount)
            return false;

        if (!simulate)
            setStored(stored - amount);

        return true;
    }

    public void writeToNBT(NBTTagCompound tag, String key) {
        tag.setDouble(key, stored);
    }

    public void readFromNBT(NBTTagCompound tag, String key) {
        setStored(tag.getDouble(key));
    }

    private void notifyChanged() {
        if (onChanged != null)
            onChanged.run();
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double clampToCapacity(double value) {
        if (Double.isNaN(value))
            return 0D;

        if (value == Double.POSITIVE_INFINITY)
            return capacity;

        if (value == Double.NEGATIVE_INFINITY)
            return 0D;

        return clamp(value, 0D, capacity);
    }

    private static boolean isFiniteAmount(double amount) {
        return !Double.isNaN(amount) && !Double.isInfinite(amount);
    }
}
