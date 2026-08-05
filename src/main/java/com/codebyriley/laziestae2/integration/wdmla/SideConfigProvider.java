package com.codebyriley.laziestae2.integration.wdmla;

import com.codebyriley.laziestae2.integration.tooltip.SideConfigTooltip;
import com.gtnewhorizons.wdmla.api.accessor.BlockAccessor;
import com.gtnewhorizons.wdmla.api.provider.IBlockComponentProvider;
import com.gtnewhorizons.wdmla.api.ui.ITooltip;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;

/** Draws {@link SideConfigTooltip}, gated behind the "show details" key. */
public enum SideConfigProvider implements IBlockComponentProvider {

    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor) {
        if (!accessor.showDetails())
            return;

        MovingObjectPosition hit = accessor.getHitResult();
        List<String> lines = new ArrayList<String>();
        SideConfigTooltip.appendLines(accessor.getTileEntity(), hit == null ? -1 : hit.sideHit, lines);

        for (String line : lines) {
            tooltip.text(line);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return WdmlaIdentifiers.SIDE_CONFIG;
    }
}
