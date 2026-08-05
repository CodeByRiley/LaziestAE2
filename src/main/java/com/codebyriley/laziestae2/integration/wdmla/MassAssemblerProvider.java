package com.codebyriley.laziestae2.integration.wdmla;

import com.codebyriley.laziestae2.integration.tooltip.MassAssemblerTooltip;
import com.gtnewhorizons.wdmla.api.accessor.BlockAccessor;
import com.gtnewhorizons.wdmla.api.provider.IBlockComponentProvider;
import com.gtnewhorizons.wdmla.api.provider.IServerDataProvider;
import com.gtnewhorizons.wdmla.api.ui.ITooltip;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

/** Draws {@link MassAssemblerTooltip} with a real progress bar for the active job. */
public enum MassAssemblerProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    INSTANCE;

    @Override
    public void appendServerData(NBTTagCompound data, BlockAccessor accessor) {
        MassAssemblerTooltip.writeData(data, accessor.getTileEntity());
    }

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        return MassAssemblerTooltip.supports(accessor.getTileEntity());
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor) {
        if (!MassAssemblerTooltip.hasData(accessor.getServerData())) {
            return;
        }

        NBTTagCompound tag = MassAssemblerTooltip.read(accessor.getServerData());
        List<String> lines = new ArrayList<String>();
        MassAssemblerTooltip.appendLines(tag, lines, accessor.showDetails(), false);

        for (String line : lines) {
            tooltip.text(line);
        }

        int workPerJob = MassAssemblerTooltip.getWorkPerJob(tag);
        String activeJob = MassAssemblerTooltip.getActiveJob(tag);

        if (MassAssemblerTooltip.isFormed(tag) && workPerJob > 0 && !activeJob.isEmpty()) {
            tooltip.progress(MassAssemblerTooltip.getWork(tag), workPerJob, activeJob);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return WdmlaIdentifiers.MASS_ASSEMBLER;
    }
}
