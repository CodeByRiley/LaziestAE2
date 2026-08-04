package com.codebyriley.laziestae2.tile.massassembler;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridMultiblock;
import appeng.api.networking.IGridNode;
import com.codebyriley.laziestae2.tile.base.PoweredGridBlock;
import com.codebyriley.laziestae2.tile.base.TilePowered;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Grid block for mass assembler blocks. Every part carries its own node so a
 * cable may attach anywhere on the structure; reporting all of them as one
 * multiblock keeps the whole cluster on a single channel.
 */
public class MassAssemblerGridBlock extends PoweredGridBlock implements IGridMultiblock {

    private final TilePowered tile;

    public MassAssemblerGridBlock(TilePowered tile) {
        super(tile);
        this.tile = tile;
    }

    @Override
    public Iterator<IGridNode> getMultiblockNodes() {
        List<IGridNode> nodes = new ArrayList<IGridNode>();

        if (tile.getWorldObj() == null) {
            return nodes.iterator();
        }

        MassAssemblerStructure.ScanResult result = MassAssemblerStructure.scan(
                tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);

        for (int[] pos : result.getPartPositions()) {
            TileEntity other = tile.getWorldObj().getTileEntity(pos[0], pos[1], pos[2]);
            if (!(other instanceof IGridHost)) {
                continue;
            }

            IGridNode node = ((IGridHost)other).getGridNode(ForgeDirection.UNKNOWN);
            if (node != null) {
                nodes.add(node);
            }
        }

        return nodes.iterator();
    }
}
