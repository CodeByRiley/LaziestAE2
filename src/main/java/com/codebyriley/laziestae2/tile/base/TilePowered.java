package com.codebyriley.laziestae2.tile.base;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.util.AECableType;
import com.codebyriley.laziestae2.components.EnergyBuffer;
import com.codebyriley.laziestae2.config.LaziestConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class TilePowered extends TileEntity implements IActionHost, IRedstoneConfigurable {

    private static final String NODE_NBT_KEY = "AENode";

    protected final EnergyBuffer energy;

    private final PoweredGridBlock gridBlock = createGridBlock();
    private IGridNode gridNode;
    private NBTTagCompound pendingNodeNBT;

    private RedstoneMode redstoneMode = RedstoneMode.IGNORE;

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

        if (worldObj == null || isInvalid())
            return;

        if (worldObj.isRemote) {
            updateClient();
        } else {
            ensureGridNode();
            chargeFromNetwork();
            updateServer();
        }
    }

    protected void updateServer() { }

    protected void updateClient() { }

    // Grid nodes must only exist server-side, and only once the tile has a world
    // and position, so creation is deferred to the first server tick.
    private void ensureGridNode() {
        if (gridNode != null)
            return;

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
        if (needed <= 0D || gridNode == null || !gridNode.isActive())
            return;

        IGrid grid = gridNode.getGrid();
        if (grid == null)
            return;

        IEnergyGrid energyGrid = grid.getCache(IEnergyGrid.class);
        if (energyGrid == null)
            return;

        double extracted = energyGrid.extractAEPower(needed, Actionable.MODULATE, PowerMultiplier.CONFIG);
        if (extracted > 0D)
            energy.receive(extracted, false);
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

    // === Redstone control ===

    /** Tiles that do no work of their own, such as multiblock casing, override this. */
    @Override
    public boolean supportsRedstoneControl() {
        return true;
    }

    @Override
    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    public void setRedstoneMode(RedstoneMode mode) {
        if (mode == null || redstoneMode == mode)
            return;

        redstoneMode = mode;
        markDirty();
        markForUpdate();
    }

    /**
     * The signal is only sampled when a mode actually cares about it, so the
     * default configuration costs nothing per tick.
     */
    @Override
    public boolean isRedstoneActive() {
        if (!redstoneMode.usesSignal() || worldObj == null)
            return redstoneMode.allowsWork(false);

        return redstoneMode.allowsWork(worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord));
    }

    /**
     * Whether a player may act on this device, according to the ME Security
     * Terminal on the network it is attached to.
     * <p>
     * A network with no security terminal grants everything, and a device that is
     * not on a network has nothing to protect, so both cases return true. Rights
     * are resolved from server-side player data, and this is called from both
     * sides, so the client always answers true and lets the server decide.
     */
    public boolean hasPermission(EntityPlayer player, SecurityPermissions permission) {
        if (worldObj == null || worldObj.isRemote || player == null)
            return true;

        IGridNode node = getActionableNode();
        IGrid grid = node == null ? null : node.getGrid();
        if (grid == null)
            return true;

        ISecurityGrid security = grid.getCache(ISecurityGrid.class);
        return security == null || security.hasPermission(player, permission);
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
        if (worldObj != null && !worldObj.isRemote)
            worldObj.func_147480_a(xCoord, yCoord, zCoord, true);
    }

    @Override
    public IGridNode getActionableNode() {
        return gridNode;
    }

    protected void markForUpdate() {
        if (worldObj != null)
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        energy.writeToNBT(tag, "Energy");
        writeRedstoneNBT(tag);

        if (gridNode != null) {
            gridNode.saveToNBT(NODE_NBT_KEY, tag);
        } else if (pendingNodeNBT != null) {
            // Preserve node data that was loaded but never applied (tile never ticked).
            for (Object keyObj : pendingNodeNBT.func_150296_c()) {
                String key = (String)keyObj;
                if (key.startsWith(NODE_NBT_KEY))
                    tag.setTag(key, pendingNodeNBT.getTag(key).copy());
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        energy.readFromNBT(tag, "Energy");
        readRedstoneNBT(tag);
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
        writeRedstoneNBT(tag);
    }

    protected void readSyncNBT(NBTTagCompound tag) {
        energy.readFromNBT(tag, "Energy");
        readRedstoneNBT(tag);
    }

    private void writeRedstoneNBT(NBTTagCompound tag) {
        // Absent means IGNORE, so the default costs nothing on disk or in the
        // description packet — which matters for a 256-block assembler structure.
        if (redstoneMode != RedstoneMode.IGNORE)
            tag.setByte("RedstoneMode", (byte)redstoneMode.ordinal());
    }

    private void readRedstoneNBT(NBTTagCompound tag) {
        redstoneMode = RedstoneMode.byIndex(tag.getByte("RedstoneMode"));
    }

    public double getStoredEnergy() {
        return energy.getStored();
    }

    public double getEnergyCapacity() {
        return energy.getCapacity();
    }
}
