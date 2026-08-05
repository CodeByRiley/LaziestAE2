package com.codebyriley.laziestae2.integration.wdmla;

import com.codebyriley.laziestae2.integration.tooltip.FastCrafterTooltip;
import com.gtnewhorizons.wdmla.api.accessor.BlockAccessor;
import com.gtnewhorizons.wdmla.api.provider.IBlockComponentProvider;
import com.gtnewhorizons.wdmla.api.provider.IServerDataProvider;
import com.gtnewhorizons.wdmla.api.ui.ITooltip;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

/** Draws {@link FastCrafterTooltip}. */
public enum FastCrafterProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    INSTANCE;

    @Override
    public void appendServerData(NBTTagCompound data, BlockAccessor accessor) {
        FastCrafterTooltip.writeData(data, accessor.getTileEntity());
    }

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        return FastCrafterTooltip.supports(accessor.getTileEntity());
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor) {
        if (!FastCrafterTooltip.hasData(accessor.getServerData()))
            return;

        List<String> lines = new ArrayList<String>();
        FastCrafterTooltip.appendLines(FastCrafterTooltip.read(accessor.getServerData()), lines);

        for (String line : lines) {
            tooltip.text(line);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return WdmlaIdentifiers.FAST_CRAFTER;
    }
}
