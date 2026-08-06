package com.codebyriley.laziestae2.client.gui;

import com.codebyriley.laziestae2.gui.MachineGuiDefinition;
import com.codebyriley.laziestae2.inventory.ContainerFastCrafter;
import com.codebyriley.laziestae2.tile.machines.TileFastCrafter;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

public class GuiFastCrafter extends GuiContainer implements ISideIoGui {

    private static final int TAB_X = 176;
    private static final int TAB_Y = 60;
    private static final int TITLE_MAX_WIDTH = 52;
    private static final int TITLE_LINE_HEIGHT = 9;

    private final MachineGuiDefinition definition;
    private final SideIoFlyout sideIo;

    public GuiFastCrafter(InventoryPlayer playerInventory, TileFastCrafter tile) {
        super(new ContainerFastCrafter(playerInventory, tile));
        this.definition = MachineGuiDefinition.FAST_CRAFTER;
        // Matches the tab on the other machine GUIs.
        this.sideIo = new SideIoFlyout(new SideIoWidget(tile), "fast_crafter", TAB_X, TAB_Y);
        this.xSize = definition.getWidth();
        this.ySize = definition.getHeight();
    }

    @Override
    public java.awt.Rectangle getSideIoBounds() {
        return sideIo.getBounds(guiLeft, guiTop);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int left = (width - xSize) / 2;
        int top = (height - ySize) / 2;
        sideIo.update(mouseX, mouseY, left, top);

        super.drawScreen(mouseX, mouseY, partialTicks);

        java.util.List<String> tooltip = sideIo.getTooltip(mouseX, mouseY, left, top);
        if (tooltip != null)
            func_146283_a(tooltip, mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (sideIo.handleClick(mouseX, mouseY, button, guiLeft, guiTop)) {
            GuiSounds.playClick();
            return;
        }

        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long timeSinceClick) {
        sideIo.handleDrag(mouseX, mouseY, guiLeft, guiTop);
        super.mouseClickMove(mouseX, mouseY, button, timeSinceClick);
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        if (state == 0)
            sideIo.handleRelease(xSize, ySize);

        super.mouseMovedOrUp(mouseX, mouseY, state);
    }

    /**
     * The slot grids leave only a narrow column free, so the title is wrapped
     * into it and the inventory label is dropped entirely.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        GL11.glPushMatrix();
        GL11.glTranslatef(-guiLeft, -guiTop, 0F);
        GL11.glColor4f(1F, 1F, 1F, 1F);
        sideIo.draw(mc, fontRendererObj, guiLeft, guiTop);
        GL11.glPopMatrix();

        java.util.List<String> lines = fontRendererObj.listFormattedStringToWidth(
                StatCollector.translateToLocal(definition.getTitleKey()), TITLE_MAX_WIDTH);

        for (int i = 0; i < lines.size() && i < 3; i++) {
            fontRendererObj.drawString(lines.get(i), 8, 8 + i * TITLE_LINE_HEIGHT, 4210752);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        mc.getTextureManager().bindTexture(definition.getTexture());

        int left = (width - xSize) / 2;
        int top = (height - ySize) / 2;
        drawTexturedModalRect(left, top, 0, 0, xSize, ySize);
    }
}
