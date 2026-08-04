package com.codebyriley.laziestae2.network;

import com.codebyriley.laziestae2.tile.base.ISideConfigurable;
import com.codebyriley.laziestae2.tile.base.MachineSideMode;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

/** Client -> server machine IO configuration: cycle a face, or toggle auto-export. */
public class MessageMachineConfig implements IMessage {

    public static final int MODE_CYCLE_SIDE = 0;
    public static final int MODE_TOGGLE_EXPORT = 1;

    private int x;
    private int y;
    private int z;
    private int mode;
    private int side;
    private boolean backwards;

    public MessageMachineConfig() {
    }

    public MessageMachineConfig(net.minecraft.tileentity.TileEntity tile, int mode, int side, boolean backwards) {
        this.x = tile.xCoord;
        this.y = tile.yCoord;
        this.z = tile.zCoord;
        this.mode = mode;
        this.side = side;
        this.backwards = backwards;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        mode = buf.readByte();
        side = buf.readByte();
        backwards = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(mode);
        buf.writeByte(side);
        buf.writeBoolean(backwards);
    }

    public static class Handler implements IMessageHandler<MessageMachineConfig, IMessage> {

        @Override
        public IMessage onMessage(MessageMachineConfig message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player == null || player.worldObj == null) {
                return null;
            }

            if (player.getDistanceSq((double)message.x + 0.5D, (double)message.y + 0.5D, (double)message.z + 0.5D) > 64D) {
                return null;
            }

            TileEntity tile = player.worldObj.getTileEntity(message.x, message.y, message.z);
            if (!(tile instanceof ISideConfigurable)) {
                return null;
            }

            ISideConfigurable machine = (ISideConfigurable)tile;

            if (message.mode == MODE_CYCLE_SIDE) {
                MachineSideMode current = machine.getSideMode(message.side);
                machine.setSideMode(message.side, message.backwards ? current.prev() : current.next());
            } else if (message.mode == MODE_TOGGLE_EXPORT && machine.supportsAutoExport()) {
                machine.setAutoExporting(!machine.isAutoExporting());
            }

            return null;
        }
    }
}
