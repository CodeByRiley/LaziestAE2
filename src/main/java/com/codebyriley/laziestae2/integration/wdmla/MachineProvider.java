package com.codebyriley.laziestae2.integration.wdmla;

import com.codebyriley.laziestae2.integration.tooltip.MachineTooltip;
import com.codebyriley.laziestae2.integration.tooltip.TooltipText;
import com.gtnewhorizons.wdmla.api.accessor.BlockAccessor;
import com.gtnewhorizons.wdmla.api.provider.IBlockComponentProvider;
import com.gtnewhorizons.wdmla.api.provider.IServerDataProvider;
import com.gtnewhorizons.wdmla.api.ui.ITooltip;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

/** Draws {@link MachineTooltip} with a real progress bar instead of a percentage. */
public enum MachineProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    INSTANCE;

    @Override
    public void appendServerData(NBTTagCompound data, BlockAccessor accessor) {
        MachineTooltip.writeData(data, accessor.getTileEntity());
    }

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        return MachineTooltip.supports(accessor.getTileEntity());
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor) {
        if (!MachineTooltip.hasData(accessor.getServerData()))
            return;

        NBTTagCompound tag = MachineTooltip.read(accessor.getServerData());

        if (MachineTooltip.isWorking(tag)) {
            tooltip.progress(MachineTooltip.getWork(tag), MachineTooltip.getMaxWork(tag),
                    TooltipText.translate("working"));
        }

        List<String> lines = new ArrayList<String>();
        MachineTooltip.appendLines(tag, lines, false);

        for (String line : lines) {
            tooltip.text(line);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return WdmlaIdentifiers.MACHINE;
    }
}
