package com.codebyriley.laziestae2.integration.nei;

import codechicken.nei.VisiblityData;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.api.TaggedInventoryArea;
import com.codebyriley.laziestae2.client.gui.ISideIoGui;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

/**
 * Keeps NEI's item panel away from the IO tabs that hang off the right edge of
 * our machine GUIs.
 */
public class NEIGuiSpaceHandler implements INEIGuiHandler {

    @Override
    public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int width, int height) {
        if (!(gui instanceof ISideIoGui)) {
            return false;
        }

        Rectangle tabs = ((ISideIoGui)gui).getSideIoBounds();
        return tabs != null && tabs.intersects(new Rectangle(x, y, width, height));
    }

    @Override
    public VisiblityData modifyVisiblity(GuiContainer gui, VisiblityData visibility) {
        return visibility;
    }

    @Override
    public Iterable<Integer> getItemSpawnSlots(GuiContainer gui, ItemStack stack) {
        return Collections.emptyList();
    }

    @Override
    public List<TaggedInventoryArea> getInventoryAreas(GuiContainer gui) {
        return null;
    }

    @Override
    public boolean handleDragNDrop(GuiContainer gui, int mouseX, int mouseY, ItemStack draggedStack, int button) {
        return false;
    }
}
