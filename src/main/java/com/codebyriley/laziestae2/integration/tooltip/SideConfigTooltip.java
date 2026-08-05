package com.codebyriley.laziestae2.integration.tooltip;

import com.codebyriley.laziestae2.tile.base.IRedstoneConfigurable;
import com.codebyriley.laziestae2.tile.base.ISideConfigurable;
import com.codebyriley.laziestae2.tile.base.MachineSideMode;
import com.codebyriley.laziestae2.tile.base.RedstoneMode;
import com.codebyriley.laziestae2.tile.base.SideConfiguration;
import java.util.List;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;

/**
 * IO mode of the face the player is pointing at, plus the auto-export toggle.
 * <p>
 * Both are already part of the tile's description packet, so this reads the
 * client tile and needs no server synchronisation. The face is named the way the
 * side-IO pad in the GUI names it, rather than by compass direction.
 */
public final class SideConfigTooltip {

    /** Widget pad index to the label used for it in the machine GUI. */
    private static final String[] WIDGET_KEYS = { "up", "left", "front", "right", "down", "back" };

    private SideConfigTooltip() { }

    public static boolean supports(TileEntity tile) {
        return tile instanceof ISideConfigurable;
    }

    public static void appendLines(TileEntity tile, int sideHit, List<String> lines) {
        if (!supports(tile))
            return;

        ISideConfigurable configurable = (ISideConfigurable)tile;

        if (sideHit >= 0 && sideHit < SideConfiguration.FACE_COUNT) {
            MachineSideMode mode = configurable.getSideMode(sideHit);
            lines.add(TooltipText.format("face",
                    StatCollector.translateToLocal(faceKey(configurable, sideHit)),
                    StatCollector.translateToLocal(mode.getUnlocalizedName())));
        }

        if (configurable.supportsAutoExport())
            lines.add(TooltipText.format("auto_export", TooltipText.onOff(configurable.isAutoExporting())));

        if (tile instanceof IRedstoneConfigurable) {
            IRedstoneConfigurable redstone = (IRedstoneConfigurable)tile;
            if (redstone.supportsRedstoneControl() && redstone.getRedstoneMode() != RedstoneMode.IGNORE) {
                lines.add(TooltipText.format("redstone",
                        StatCollector.translateToLocal(redstone.getRedstoneMode().getUnlocalizedName())));
            }
        }
    }

    private static String faceKey(ISideConfigurable configurable, int side) {
        for (int widget = 0; widget < WIDGET_KEYS.length; widget++) {
            if (configurable.getWidgetSide(widget) == side)
                return "gui.laziestae2.side." + WIDGET_KEYS[widget];
        }

        return "gui.laziestae2.side.back";
    }
}
