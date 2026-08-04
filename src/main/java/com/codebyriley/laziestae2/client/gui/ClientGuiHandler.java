package com.codebyriley.laziestae2.client.gui;

import com.codebyriley.laziestae2.gui.GuiIds;
import com.codebyriley.laziestae2.gui.GuiHandler;
import com.codebyriley.laziestae2.tile.base.TileMachine;
import com.codebyriley.laziestae2.tile.machines.TileFastCrafter;
import com.codebyriley.laziestae2.tile.machines.TileLevelMaintainer;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerController;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerPatternProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class ClientGuiHandler extends GuiHandler {

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == GuiIds.BIG_ASSEMBLER) {
            TileMassAssemblerController controller = getMassAssemblerController(world, x, y, z);
            return controller != null ? new GuiMassAssembler(player.inventory, controller) : null;
        }

        if (id == GuiIds.LEVEL_MAINTAINER) {
            TileLevelMaintainer maintainer = getLevelMaintainer(world, x, y, z);
            return maintainer != null ? new GuiLevelMaintainer(player.inventory, maintainer) : null;
        }

        if (id == GuiIds.FAST_CRAFTER) {
            TileFastCrafter crafter = getFastCrafter(world, x, y, z);
            return crafter != null ? new GuiFastCrafter(player.inventory, crafter) : null;
        }

        if (id == GuiIds.PATTERN_PROVIDER) {
            TileMassAssemblerPatternProvider provider = getPatternProvider(world, x, y, z);
            return provider != null ? new GuiPatternProvider(player.inventory, provider) : null;
        }

        TileMachine tile = getMachineTile(id, world, x, y, z);
        return tile != null ? new GuiMachine(player.inventory, tile) : null;
    }
}
