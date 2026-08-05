package com.codebyriley.laziestae2.integration.wdmla;

import com.codebyriley.laziestae2.integration.tooltip.LevelMaintainerTooltip;
import com.gtnewhorizons.wdmla.api.accessor.BlockAccessor;
import com.gtnewhorizons.wdmla.api.provider.IBlockComponentProvider;
import com.gtnewhorizons.wdmla.api.provider.IServerDataProvider;
import com.gtnewhorizons.wdmla.api.ui.ITooltip;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

/** Draws {@link LevelMaintainerTooltip}. */
public enum LevelMaintainerProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    INSTANCE;

    @Override
    public void appendServerData(NBTTagCompound data, BlockAccessor accessor) {
        LevelMaintainerTooltip.writeData(data, accessor.getTileEntity(), accessor.showDetails());
    }

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        return LevelMaintainerTooltip.supports(accessor.getTileEntity());
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor) {
        if (!LevelMaintainerTooltip.hasData(accessor.getServerData()))
            return;

        List<String> lines = new ArrayList<String>();
        LevelMaintainerTooltip.appendLines(LevelMaintainerTooltip.read(accessor.getServerData()), lines);

        for (String line : lines) {
            tooltip.text(line);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return WdmlaIdentifiers.LEVEL_MAINTAINER;
    }
}
