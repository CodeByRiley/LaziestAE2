package com.codebyriley.laziestae2.tile.base;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridNotification;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import java.util.EnumSet;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

public class PoweredGridBlock implements IGridBlock {

    private final TilePowered tile;

    public PoweredGridBlock(TilePowered tile) {
        this.tile = tile;
    }

    @Override
    public double getIdlePowerUsage() {
        return tile.getIdlePowerUsage();
    }

    @Override
    public EnumSet<GridFlags> getFlags() {
        return tile.getGridFlags();
    }

    @Override
    public boolean isWorldAccessible() {
        return true;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(tile);
    }

    @Override
    public AEColor getGridColor() {
        return AEColor.Transparent;
    }

    @Override
    public void onGridNotification(GridNotification notification) {
    }

    @Override
    public void setNetworkStatus(IGrid grid, int channelsInUse) {
    }

    @Override
    public EnumSet<ForgeDirection> getConnectableSides() {
        return EnumSet.complementOf(EnumSet.of(ForgeDirection.UNKNOWN));
    }

    @Override
    public IGridHost getMachine() {
        return tile;
    }

    @Override
    public void gridChanged() {
    }

    @Override
    public ItemStack getMachineRepresentation() {
        Block block = tile.getBlockType();
        return block != null ? new ItemStack(block, 1, tile.getBlockMetadata()) : null;
    }
}
