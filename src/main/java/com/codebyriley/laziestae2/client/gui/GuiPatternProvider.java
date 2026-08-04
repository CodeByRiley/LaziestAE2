package com.codebyriley.laziestae2.client.gui;

import com.codebyriley.laziestae2.inventory.ContainerPatternProvider;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerPatternProvider;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

public class GuiPatternProvider extends GuiContainer {

    private static final ResourceLocation CHEST_TEXTURE =
            new ResourceLocation("textures/gui/container/generic_54.png");

    private static final int ROWS = ContainerPatternProvider.ROWS;

    public GuiPatternProvider(InventoryPlayer playerInventory, TileMassAssemblerPatternProvider tile) {
        super(new ContainerPatternProvider(playerInventory, tile));
        this.xSize = 176;
        this.ySize = 114 + ROWS * 18;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj.drawString(
                StatCollector.translateToLocal("container.laziestae2.big_assembler.pattern_provider"), 8, 6, 4210752);
        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 94, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        mc.getTextureManager().bindTexture(CHEST_TEXTURE);

        int left = (width - xSize) / 2;
        int top = (height - ySize) / 2;
        drawTexturedModalRect(left, top, 0, 0, xSize, ROWS * 18 + 17);
        drawTexturedModalRect(left, top + ROWS * 18 + 17, 0, 126, xSize, 96);
    }
}
