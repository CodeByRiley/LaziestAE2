package com.codebyriley.laziestae2.network;

import com.codebyriley.laziestae2.tile.machines.TileLevelMaintainer;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

/**
 * Client -> server update of a level maintainer request line's quantity or batch size.
 */
public class MessageLevelMaintainerRequest implements IMessage {

    public static final int MODE_QUANTITY = 0;
    public static final int MODE_BATCH = 1;

    private int x;
    private int y;
    private int z;
    private int slot;
    private int mode;
    private long value;

    public MessageLevelMaintainerRequest() { }

    public MessageLevelMaintainerRequest(TileLevelMaintainer tile, int slot, int mode, long value) {
        this.x = tile.xCoord;
        this.y = tile.yCoord;
        this.z = tile.zCoord;
        this.slot = slot;
        this.mode = mode;
        this.value = value;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        slot = buf.readByte();
        mode = buf.readByte();
        value = buf.readLong();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(slot);
        buf.writeByte(mode);
        buf.writeLong(value);
    }

    public static class Handler implements IMessageHandler<MessageLevelMaintainerRequest, IMessage> {

        @Override
        public IMessage onMessage(MessageLevelMaintainerRequest message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player == null || player.worldObj == null)
                return null;

            TileEntity tile = player.worldObj.getTileEntity(message.x, message.y, message.z);
            if (!(tile instanceof TileLevelMaintainer))
                return null;

            TileLevelMaintainer maintainer = (TileLevelMaintainer)tile;

            // Covers reach and ME security in one check, so a client that sends this
            // packet without a legitimately open GUI gets nothing.
            if (!maintainer.isUseableByPlayer(player))
                return null;
            if (message.mode == MODE_QUANTITY)
                maintainer.setRequestQuantity(message.slot, message.value);
            else if (message.mode == MODE_BATCH)
                maintainer.setRequestBatch(message.slot, message.value);

            return null;
        }
    }
}
