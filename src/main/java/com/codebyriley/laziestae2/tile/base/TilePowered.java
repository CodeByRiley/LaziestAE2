package com.codebyriley.laziestae2.tile.base;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import com.codebyriley.laziestae2.components.EnergyBuffer;
import com.codebyriley.laziestae2.config.LaziestConfig;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class TilePowered extends TileEntity implements IActionHost {

    private static final String NODE_NBT_KEY = "AENode";

    protected final EnergyBuffer energy;

    private final PoweredGridBlock gridBlock = createGridBlock();
    private IGridNode gridNode;
    private NBTTagCompound pendingNodeNBT;

    protected TilePowered(double energyCapacity) {
        this.energy = new EnergyBuffer(energyCapacity, this::markDirty);
    }

    /** Override to supply a specialised grid block, e.g. a multiblock-aware one. */
    protected PoweredGridBlock createGridBlock() {
        return new PoweredGridBlock(this);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (worldObj == null || isInvalid()) {
            return;
        }

        if (worldObj.isRemote) {
            updateClient();
        } else {
            ensureGridNode();
            chargeFromNetwork();
            updateServer();
        }
    }

    protected void updateServer() {
    }

    protected void updateClient() {
    }

    // Grid nodes must only exist server-side, and only once the tile has a world
    // and position, so creation is deferred to the first server tick.
    private void ensureGridNode() {
        if (gridNode != null) {
            return;
        }

        gridNode = AEApi.instance().createGridNode(gridBlock);

        if (pendingNodeNBT != null) {
            gridNode.loadFromNBT(NODE_NBT_KEY, pendingNodeNBT);
            pendingNodeNBT = null;
        }

        gridNode.updateState();
    }

    private void destroyGridNode() {
        if (gridNode != null) {
            gridNode.destroy();
            gridNode = null;
        }
    }

    /**
     * Maximum AE this machine pulls out of the network per tick. Read live from
     * config so in-game config edits take effect without a world reload.
     * Zero or negative means unlimited.
     */
    protected double getNetworkTransferRate() {
        double rate = LaziestConfig.networkTransferPerTick;
        return rate > 0D ? rate : Double.MAX_VALUE;
    }

    protected void chargeFromNetwork() {
        double needed = Math.min(energy.getRemainingCapacity(), getNetworkTransferRate());
        if (needed <= 0D || gridNode == null || !gridNode.isActive()) {
            return;
        }

        IGrid grid = gridNode.getGrid();
        if (grid == null) {
            return;
        }

        IEnergyGrid energyGrid = grid.getCache(IEnergyGrid.class);
        if (energyGrid == null) {
            return;
        }

        double extracted = energyGrid.extractAEPower(needed, Actionable.MODULATE, PowerMultiplier.CONFIG);
        if (extracted > 0D) {
            energy.receive(extracted, false);
        }
    }

    protected double getIdlePowerUsage() {
        return 1D;
    }

    protected java.util.EnumSet<appeng.api.networking.GridFlags> getGridFlags() {
        return java.util.EnumSet.noneOf(appeng.api.networking.GridFlags.class);
    }

    public boolean isGridConnected() {
        return gridNode != null && gridNode.isActive();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        destroyGridNode();
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        destroyGridNode();
    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {
        return gridNode;
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection dir) {
        return AECableType.SMART;
    }

    @Override
    public void securityBreak() {
        if (worldObj != null && !worldObj.isRemote) {
            worldObj.func_147480_a(xCoord, yCoord, zCoord, true);
        }
    }

    @Override
    public IGridNode getActionableNode() {
        return gridNode;
    }

    protected void markForUpdate() {
        if (worldObj != null) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        energy.writeToNBT(tag, "Energy");

        if (gridNode != null) {
            gridNode.saveToNBT(NODE_NBT_KEY, tag);
        } else if (pendingNodeNBT != null) {
            // Preserve node data that was loaded but never applied (tile never ticked).
            for (Object keyObj : pendingNodeNBT.func_150296_c()) {
                String key = (String)keyObj;
                if (key.startsWith(NODE_NBT_KEY)) {
                    tag.setTag(key, pendingNodeNBT.getTag(key).copy());
                }
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        energy.readFromNBT(tag, "Energy");
        pendingNodeNBT = (NBTTagCompound)tag.copy();
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeSyncNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
    }

    @Override
    public void onDataPacket(NetworkManager networkManager, S35PacketUpdateTileEntity packet) {
        readSyncNBT(packet.func_148857_g());
    }

    protected void writeSyncNBT(NBTTagCompound tag) {
        energy.writeToNBT(tag, "Energy");
    }

    protected void readSyncNBT(NBTTagCompound tag) {
        energy.readFromNBT(tag, "Energy");
    }

    public EnergyBuffer getEnergyBuffer() {
        return energy;
    }

    public double getStoredEnergy() {
        return energy.getStored();
    }

    public double getEnergyCapacity() {
        return energy.getCapacity();
    }
}
