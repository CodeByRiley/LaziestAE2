package com.codebyriley.laziestae2.client.gui;

import com.codebyriley.laziestae2.Constants;
import com.codebyriley.laziestae2.gui.MachineGuiDefinition;
import com.codebyriley.laziestae2.inventory.ContainerMachine;
import com.codebyriley.laziestae2.network.LazyNetwork;
import com.codebyriley.laziestae2.network.MessageMachineConfig;
import com.codebyriley.laziestae2.tile.base.MachineSideMode;
import com.codebyriley.laziestae2.tile.base.TileMachine;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

public class GuiMachine extends GuiContainer implements ISideIoGui {

    private static final ResourceLocation ENERGY_TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/component/energy.png");

    // energy.png is 6x72: full-height track at (0,0,4,72), fill strip at
    // (4,0,2,70) drawn bottom-up, inset 1px into the track.
    private static final int ENERGY_TEX_WIDTH = 6;
    private static final int ENERGY_TEX_HEIGHT = 72;
    private static final int ENERGY_BAR_X = 165;
    private static final int ENERGY_BAR_Y = 7;
    private static final int ENERGY_BG_WIDTH = 4;
    private static final int ENERGY_BG_HEIGHT = 72;
    private static final int ENERGY_FILL_U = 4;
    private static final int ENERGY_FILL_WIDTH = 2;
    private static final int ENERGY_FILL_HEIGHT = 70;

    // Tab hangs off the right edge near the bottom, clear of the panel.
    private static final int TAB_X = 176;
    private static final int TAB_Y = 60;

    private final ContainerMachine container;
    private final MachineGuiDefinition definition;
    private final SideIoFlyout sideIo;

    public GuiMachine(InventoryPlayer playerInventory, TileMachine tile) {
        this(new ContainerMachine(playerInventory, tile));
    }

    private GuiMachine(ContainerMachine container) {
        super(container);
        this.container = container;
        this.definition = container.getDefinition();
        this.sideIo = new SideIoFlyout(new SideIoWidget(container.getTile()), definition.getTextureName(), TAB_X, TAB_Y);
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

        java.util.List<String> flyoutTooltip = sideIo.getTooltip(mouseX, mouseY, left, top);

        if (flyoutTooltip != null) {
            func_146283_a(flyoutTooltip, mouseX, mouseY);
        } else if (isOverProgressArea(mouseX, mouseY)) {
            java.util.List<String> tooltip = new java.util.ArrayList<String>();
            tooltip.add(String.format("%d%%", Math.round(container.getWorkFraction() * 100F)));

            func_146283_a(tooltip, mouseX, mouseY);
        } else if (mouseX >= left + ENERGY_BAR_X && mouseX < left + ENERGY_BAR_X + ENERGY_BG_WIDTH
                && mouseY >= top + ENERGY_BAR_Y && mouseY < top + ENERGY_BAR_Y + ENERGY_BG_HEIGHT) {
            long capacity = Math.round(container.getTile().getEnergyCapacity());
            long stored = Math.round(container.getEnergyFraction() * capacity);
            func_146283_a(
                    java.util.Collections.singletonList(String.format("%,d / %,d AE", stored, capacity)),
                    mouseX, mouseY);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // Tabs live outside the panel, so undo the layer's origin translation.
        GL11.glPushMatrix();
        GL11.glTranslatef(-guiLeft, -guiTop, 0F);
        GL11.glColor4f(1F, 1F, 1F, 1F);
        sideIo.draw(mc, fontRendererObj, guiLeft, guiTop);
        GL11.glPopMatrix();

        String title = getTitle();
        fontRendererObj.drawString(title, (xSize - fontRendererObj.getStringWidth(title)) / 2, 6, 4210752);
        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 94, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        mc.getTextureManager().bindTexture(definition.getTexture());

        int left = (width - xSize) / 2;
        int top = (height - ySize) / 2;
        drawTexturedModalRect(left, top, 0, 0, xSize, ySize);
        drawEnergyBar(left, top);
        drawWorkProgress(left, top);
    }

    private void drawEnergyBar(int left, int top) {
        mc.getTextureManager().bindTexture(ENERGY_TEXTURE);

        // Track, always drawn at full size.
        func_146110_a(
                left + ENERGY_BAR_X,
                top + ENERGY_BAR_Y,
                0F,
                0F,
                ENERGY_BG_WIDTH,
                ENERGY_BG_HEIGHT,
                ENERGY_TEX_WIDTH,
                ENERGY_TEX_HEIGHT);

        // Fill, growing from the bottom.
        int filled = Math.round(container.getEnergyFraction() * ENERGY_FILL_HEIGHT);
        if (filled > 0) {
            int empty = ENERGY_FILL_HEIGHT - filled;
            func_146110_a(
                    left + ENERGY_BAR_X + 1,
                    top + ENERGY_BAR_Y + 1 + empty,
                    ENERGY_FILL_U,
                    empty,
                    ENERGY_FILL_WIDTH,
                    filled,
                    ENERGY_TEX_WIDTH,
                    ENERGY_TEX_HEIGHT);
        }
    }

    private void drawWorkProgress(int left, int top) {
        if (!definition.hasProgressArea()) {
            drawFallbackWorkBar(left, top);
            return;
        }

        int filled = Math.round(container.getWorkFraction() * definition.getProgressWidth());
        if (filled > 0) {
            mc.getTextureManager().bindTexture(definition.getTexture());
            drawTexturedModalRect(
                    left + definition.getProgressX(),
                    top + definition.getProgressY(),
                    definition.getProgressU(),
                    definition.getProgressV(),
                    filled,
                    definition.getProgressHeight());
        }
    }

    private boolean isOverProgressArea(int mouseX, int mouseY) {
        if (!definition.hasProgressArea()) {
            return false;
        }

        int left = (width - xSize) / 2 + definition.getProgressX();
        int top = (height - ySize) / 2 + definition.getProgressY();

        return mouseX >= left && mouseX < left + definition.getProgressWidth()
                && mouseY >= top && mouseY < top + definition.getProgressHeight();
    }



    /** Left-click the progress arrow for recipes, right-click for uses. */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (sideIo.handleClick(mouseX, mouseY, button, guiLeft, guiTop)) {
            playClick();
            return;
        }

        if ((button == 0 || button == 1) && isOverProgressArea(mouseX, mouseY)) {
            String identifier = definition.getNeiIdentifier();
            boolean opened = button == 0
                    ? NEIGuiBridge.openRecipes(identifier)
                    : NEIGuiBridge.openUsage(identifier);

            if (opened) {
                playClick();
                return;
            }
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
        if (state == 0) {
            sideIo.handleRelease(xSize, ySize);
        }

        super.mouseMovedOrUp(mouseX, mouseY, state);
    }

    private void playClick() {
        mc.getSoundHandler().playSound(
                net.minecraft.client.audio.PositionedSoundRecord.func_147674_a(
                        new ResourceLocation("gui.button.press"), 1F));
    }

    private void drawFallbackWorkBar(int left, int top) {
        int barWidth = 160;
        int barY = definition.getPlayerInventoryY() - 4;
        int filled = Math.round(container.getWorkFraction() * barWidth);

        drawRect(left + 8, top + barY, left + 8 + barWidth, top + barY + 3, 0xFF3B3B3B);
        if (filled > 0) {
            drawRect(left + 8, top + barY, left + 8 + filled, top + barY + 3, 0xFF63B36F);
        }
    }

    private String getTitle() {
        String title = StatCollector.translateToLocal(definition.getTitleKey());
        return definition.getTitleKey().equals(title) ? definition.getFallbackTitle() : title;
    }
}
