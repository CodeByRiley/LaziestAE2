package com.codebyriley.laziestae2.integration.waila;

import com.codebyriley.laziestae2.integration.tooltip.FastCrafterTooltip;
import com.codebyriley.laziestae2.integration.tooltip.LevelMaintainerTooltip;
import com.codebyriley.laziestae2.integration.tooltip.MachineTooltip;
import com.codebyriley.laziestae2.integration.tooltip.MassAssemblerTooltip;
import com.codebyriley.laziestae2.integration.tooltip.SideConfigTooltip;
import java.util.List;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Legacy Waila front end for the same tooltip content WDMla draws.
 * <p>
 * Waila has no progress bars and no separate details key, so progress is written
 * as a percentage and sneaking stands in for "show details".
 */
public enum WailaTooltipProvider implements IWailaDataProvider {

    MACHINE("machine") {

        @Override
        void write(NBTTagCompound tag, TileEntity tile, boolean details) {
            MachineTooltip.writeData(tag, tile);
        }

        @Override
        void read(NBTTagCompound tag, List<String> tip, IWailaDataAccessor accessor) {
            if (MachineTooltip.hasData(tag)) {
                MachineTooltip.appendLines(MachineTooltip.read(tag), tip, true);
            }
        }
    },

    FAST_CRAFTER("fast_crafter") {

        @Override
        void write(NBTTagCompound tag, TileEntity tile, boolean details) {
            FastCrafterTooltip.writeData(tag, tile);
        }

        @Override
        void read(NBTTagCompound tag, List<String> tip, IWailaDataAccessor accessor) {
            if (FastCrafterTooltip.hasData(tag)) {
                FastCrafterTooltip.appendLines(FastCrafterTooltip.read(tag), tip);
            }
        }
    },

    LEVEL_MAINTAINER("level_maintainer") {

        @Override
        void write(NBTTagCompound tag, TileEntity tile, boolean details) {
            LevelMaintainerTooltip.writeData(tag, tile, details);
        }

        @Override
        void read(NBTTagCompound tag, List<String> tip, IWailaDataAccessor accessor) {
            if (LevelMaintainerTooltip.hasData(tag)) {
                LevelMaintainerTooltip.appendLines(LevelMaintainerTooltip.read(tag), tip);
            }
        }
    },

    MASS_ASSEMBLER("mass_assembler") {

        @Override
        void write(NBTTagCompound tag, TileEntity tile, boolean details) {
            MassAssemblerTooltip.writeData(tag, tile);
        }

        @Override
        void read(NBTTagCompound tag, List<String> tip, IWailaDataAccessor accessor) {
            if (MassAssemblerTooltip.hasData(tag)) {
                MassAssemblerTooltip.appendLines(MassAssemblerTooltip.read(tag), tip, isDetailed(accessor), true);
            }
        }
    },

    SIDE_CONFIG("side_config") {

        @Override
        void write(NBTTagCompound tag, TileEntity tile, boolean details) {
        }

        @Override
        void read(NBTTagCompound tag, List<String> tip, IWailaDataAccessor accessor) {
            if (isDetailed(accessor)) {
                SideConfigTooltip.appendLines(accessor.getTileEntity(), accessor.getSide().ordinal(), tip);
            }
        }
    };

    /** Waila config keys are shared across all mods, so they carry our mod id. */
    private final String configKey;

    WailaTooltipProvider(String name) {
        this.configKey = WailaIntegration.CONFIG_PREFIX + name;
    }

    String getConfigKey() {
        return configKey;
    }

    abstract void write(NBTTagCompound tag, TileEntity tile, boolean details);

    abstract void read(NBTTagCompound tag, List<String> tip, IWailaDataAccessor accessor);

    /** Waila predates a dedicated details key; sneaking is the established stand-in. */
    static boolean isDetailed(IWailaDataAccessor accessor) {
        return accessor.getPlayer() != null && accessor.getPlayer().isSneaking();
    }

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
            IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
            IWailaConfigHandler config) {
        if (!config.getConfig(configKey, true)) {
            return currenttip;
        }

        NBTTagCompound tag = accessor.getNBTData();
        read(tag == null ? new NBTTagCompound() : tag, currenttip, accessor);
        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
            IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x,
            int y, int z) {
        write(tag, te, player != null && player.isSneaking());
        return tag;
    }
}
