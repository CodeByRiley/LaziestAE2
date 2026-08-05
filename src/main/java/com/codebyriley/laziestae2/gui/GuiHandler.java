package com.codebyriley.laziestae2.gui;

import com.codebyriley.laziestae2.inventory.ContainerFastCrafter;
import com.codebyriley.laziestae2.inventory.ContainerLevelMaintainer;
import com.codebyriley.laziestae2.inventory.ContainerPatternProvider;
import com.codebyriley.laziestae2.inventory.ContainerMassAssembler;
import com.codebyriley.laziestae2.inventory.ContainerMachine;
import com.codebyriley.laziestae2.tile.base.TileMachine;
import com.codebyriley.laziestae2.tile.machines.TileFastCrafter;
import com.codebyriley.laziestae2.tile.machines.TileLevelMaintainer;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerController;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerPatternProvider;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GuiHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == GuiIds.BIG_ASSEMBLER) {
            TileMassAssemblerController controller = getMassAssemblerController(world, x, y, z);
            return controller != null ? new ContainerMassAssembler(player.inventory, controller) : null;
        }

        if (id == GuiIds.LEVEL_MAINTAINER) {
            TileLevelMaintainer maintainer = getLevelMaintainer(world, x, y, z);
            return maintainer != null ? new ContainerLevelMaintainer(player.inventory, maintainer) : null;
        }

        if (id == GuiIds.FAST_CRAFTER) {
            TileFastCrafter crafter = getFastCrafter(world, x, y, z);
            return crafter != null ? new ContainerFastCrafter(player.inventory, crafter) : null;
        }

        if (id == GuiIds.PATTERN_PROVIDER) {
            TileMassAssemblerPatternProvider provider = getPatternProvider(world, x, y, z);
            return provider != null ? new ContainerPatternProvider(player.inventory, provider) : null;
        }

        TileMachine tile = getMachineTile(id, world, x, y, z);
        return tile != null ? new ContainerMachine(player.inventory, tile) : null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    protected TileMachine getMachineTile(int id, World world, int x, int y, int z) {
        if (MachineGuiDefinition.byGuiId(id) == null || id == GuiIds.BIG_ASSEMBLER)
            return null;

        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TileMachine))
            return null;

        TileMachine machine = (TileMachine)tile;
        return machine.getGuiDefinition().getGuiId() == id ? machine : null;
    }

    protected TileMassAssemblerController getMassAssemblerController(World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        return tile instanceof TileMassAssemblerController ? (TileMassAssemblerController)tile : null;
    }

    protected TileLevelMaintainer getLevelMaintainer(World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        return tile instanceof TileLevelMaintainer ? (TileLevelMaintainer)tile : null;
    }

    protected TileFastCrafter getFastCrafter(World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        return tile instanceof TileFastCrafter ? (TileFastCrafter)tile : null;
    }

    protected TileMassAssemblerPatternProvider getPatternProvider(World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        return tile instanceof TileMassAssemblerPatternProvider ? (TileMassAssemblerPatternProvider)tile : null;
    }
}
