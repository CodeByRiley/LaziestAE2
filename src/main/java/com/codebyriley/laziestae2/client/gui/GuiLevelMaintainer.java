package com.codebyriley.laziestae2.client.gui;

import com.codebyriley.laziestae2.gui.MachineGuiDefinition;
import com.codebyriley.laziestae2.inventory.ContainerLevelMaintainer;
import com.codebyriley.laziestae2.network.LazyNetwork;
import com.codebyriley.laziestae2.network.MessageLevelMaintainerRequest;
import com.codebyriley.laziestae2.tile.machines.TileLevelMaintainer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class GuiLevelMaintainer extends GuiContainer implements ISideIoGui {

    // Matches the tab on the other machine GUIs.
    private static final int TAB_X = 176;
    private static final int TAB_Y = 60;

    // Measured from level_maintainer.png. Per row (pitch 20, first row y=21):
    // quantity well x38-82, its submit x84-96, batch well x101-145, submit x147-159.
    private static final int FIELD_QTY_X = 40;
    private static final int FIELD_BATCH_X = 103;
    private static final int FIELD_Y_BASE = 24;
    private static final int FIELD_Y_STEP = 20;
    private static final int FIELD_WIDTH = 41;
    private static final int FIELD_HEIGHT = 8;
    private static final int FIELD_TEXT_COLOR = 0xFFFFFF;

    private static final int SUBMIT_QTY_X = 84;
    private static final int SUBMIT_BATCH_X = 147;
    private static final int SUBMIT_Y_BASE = 21;

    private static final net.minecraft.util.ResourceLocation SUBMIT_TEXTURE =
            new net.minecraft.util.ResourceLocation(
                    com.codebyriley.laziestae2.Constants.MOD_ID, "textures/gui/component/submit.png");

    // submit.png is atlas of all button types
    // normal at 0,0, disabled at 16,0, hovered at 32,0
    private static final int SUBMIT_TEX_WIDTH = 48;
    private static final int SUBMIT_TEX_HEIGHT = 16;
    private static final int SUBMIT_SIZE = 16;

    private final TileLevelMaintainer tile;
    private final MachineGuiDefinition definition;

    private final GuiTextField[] qtyFields = new GuiTextField[TileLevelMaintainer.REQ_COUNT];
    private final GuiTextField[] batchFields = new GuiTextField[TileLevelMaintainer.REQ_COUNT];
    private final SideIoFlyout sideIo;

    public GuiLevelMaintainer(InventoryPlayer playerInventory, TileLevelMaintainer tile) {
        super(new ContainerLevelMaintainer(playerInventory, tile));
        this.tile = tile;
        this.definition = MachineGuiDefinition.LEVEL_MAINTAINER;
        this.sideIo = new SideIoFlyout(new SideIoWidget(tile), "level_maintainer", TAB_X, TAB_Y);
        this.xSize = definition.getWidth();
        this.ySize = definition.getHeight();
    }

    @Override
    public java.awt.Rectangle getSideIoBounds() {
        return sideIo.getBounds(guiLeft, guiTop);
    }

    @Override
    public void initGui() {
        super.initGui();

        for (int i = 0; i < TileLevelMaintainer.REQ_COUNT; i++) {
            qtyFields[i] = createField(FIELD_QTY_X, FIELD_Y_BASE + FIELD_Y_STEP * i, tile.getRequestQuantity(i));
            batchFields[i] = createField(FIELD_BATCH_X, FIELD_Y_BASE + FIELD_Y_STEP * i, tile.getRequestBatch(i));
        }
    }

    private int getSubmitX(boolean batch) {
        return batch ? SUBMIT_BATCH_X : SUBMIT_QTY_X;
    }

    private GuiTextField createField(int x, int y, long initial) {
        GuiTextField field = new GuiTextField(fontRendererObj, guiLeft + x, guiTop + y, FIELD_WIDTH, FIELD_HEIGHT);
        field.setMaxStringLength(12);
        // The GUI texture already provides a recessed well behind each field.
        field.setEnableBackgroundDrawing(false);
        field.setTextColor(FIELD_TEXT_COLOR);
        field.setText(Long.toString(initial));
        return field;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        for (int i = 0; i < TileLevelMaintainer.REQ_COUNT; i++) {
            qtyFields[i].updateCursorCounter();
            batchFields[i].updateCursorCounter();

            if (!qtyFields[i].isFocused())
                qtyFields[i].setText(Long.toString(tile.getRequestQuantity(i)));

            if (!batchFields[i].isFocused())
                batchFields[i].setText(Long.toString(tile.getRequestBatch(i)));
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        for (int i = 0; i < TileLevelMaintainer.REQ_COUNT; i++) {
            if (handleFieldKey(qtyFields[i], i, MessageLevelMaintainerRequest.MODE_QUANTITY, typedChar, keyCode))
                return;

            if (handleFieldKey(batchFields[i], i, MessageLevelMaintainerRequest.MODE_BATCH, typedChar, keyCode))
                return;
        }

        super.keyTyped(typedChar, keyCode);
    }

    private boolean handleFieldKey(GuiTextField field, int slot, int mode, char typedChar, int keyCode) {
        if (!field.isFocused())
            return false;

        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            submitField(field, slot, mode);
            field.setFocused(false);
            return true;
        }

        if (keyCode == Keyboard.KEY_ESCAPE) {
            field.setFocused(false);
            return true;
        }

        // Digits only, let the field handle navigation/backspace itself.
        if (Character.isDigit(typedChar) || typedChar < ' ')
            field.textboxKeyTyped(typedChar, keyCode);

        return true;
    }

    private void submitField(GuiTextField field, int slot, int mode) {
        long value;
        try {
            value = Long.parseLong(field.getText().trim());
        } catch (NumberFormatException e) {
            return;
        }

        if (value < 0L)
            return;

        LazyNetwork.CHANNEL.sendToServer(new MessageLevelMaintainerRequest(tile, slot, mode, value));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (sideIo.handleClick(mouseX, mouseY, button, guiLeft, guiTop)) {
            GuiSounds.playClick();
            return;
        }

        if (button == 0) {
            for (int i = 0; i < TileLevelMaintainer.REQ_COUNT; i++) {
                if (isOverSubmit(i, false, mouseX, mouseY)) {
                    submitField(qtyFields[i], i, MessageLevelMaintainerRequest.MODE_QUANTITY);
                    return;
                }

                if (isOverSubmit(i, true, mouseX, mouseY)) {
                    submitField(batchFields[i], i, MessageLevelMaintainerRequest.MODE_BATCH);
                    return;
                }
            }
        }

        super.mouseClicked(mouseX, mouseY, button);

        for (int i = 0; i < TileLevelMaintainer.REQ_COUNT; i++) {
            qtyFields[i].mouseClicked(mouseX, mouseY, button);
            batchFields[i].mouseClicked(mouseX, mouseY, button);
        }
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
            return;
        }

        drawFieldTooltip(mouseX, mouseY);
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
     * Fields and buttons are drawn here rather than in drawScreen so they stay
     * beneath tooltips. This layer is translated by the GUI origin, and the
     * fields hold absolute positions, so the translation is undone first.
     */
    private void drawFields(int mouseX, int mouseY) {
        GL11.glPushMatrix();
        GL11.glTranslatef(-guiLeft, -guiTop, 0F);
        GL11.glDisable(GL11.GL_LIGHTING);

        for (int i = 0; i < TileLevelMaintainer.REQ_COUNT; i++) {
            qtyFields[i].drawTextBox();
            batchFields[i].drawTextBox();
        }

        GL11.glColor4f(1F, 1F, 1F, 1F);
        for (int i = 0; i < TileLevelMaintainer.REQ_COUNT; i++) {
            drawSubmit(qtyFields[i], i, false, mouseX, mouseY);
            drawSubmit(batchFields[i], i, true, mouseX, mouseY);
        }

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }

    /** The columns have no room for headings, so name them on hover instead. */
    private void drawFieldTooltip(int mouseX, int mouseY) {
        for (int i = 0; i < TileLevelMaintainer.REQ_COUNT; i++) {
            if (isOverField(qtyFields[i], mouseX, mouseY) || isOverSubmit(i, false, mouseX, mouseY)) {
                func_146283_a(java.util.Collections.singletonList(
                        StatCollector.translateToLocal("gui.laziestae2.level_maintainer.quantity")), mouseX, mouseY);
                return;
            }

            if (isOverField(batchFields[i], mouseX, mouseY) || isOverSubmit(i, true, mouseX, mouseY)) {
                func_146283_a(java.util.Collections.singletonList(
                        StatCollector.translateToLocal("gui.laziestae2.level_maintainer.batch")), mouseX, mouseY);
                return;
            }
        }
    }

    private static boolean isOverField(GuiTextField field, int mouseX, int mouseY) {
        return mouseX >= field.xPosition && mouseX < field.xPosition + FIELD_WIDTH
                && mouseY >= field.yPosition - 3 && mouseY < field.yPosition + FIELD_HEIGHT + 2;
    }

    /** Submit button beside a field: disabled unless the field holds a valid new value. */
    private void drawSubmit(GuiTextField field, int row, boolean batch, int mouseX, int mouseY) {
        int x = guiLeft + getSubmitX(batch);
        int y = guiTop + SUBMIT_Y_BASE + FIELD_Y_STEP * row;

        int state;
        if (!isValidValue(field))
            state = 1;
        else
            state = isOverSubmit(row, batch, mouseX, mouseY) ? 2 : 0;

        mc.getTextureManager().bindTexture(SUBMIT_TEXTURE);
        func_146110_a(x, y, state * SUBMIT_SIZE, 0F, SUBMIT_SIZE, SUBMIT_SIZE,
                SUBMIT_TEX_WIDTH, SUBMIT_TEX_HEIGHT);
    }

    private static boolean isValidValue(GuiTextField field) {
        try {
            return Long.parseLong(field.getText().trim()) >= 0L;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isOverSubmit(int row, boolean batch, int mouseX, int mouseY) {
        int x = guiLeft + getSubmitX(batch);
        int y = guiTop + SUBMIT_Y_BASE + FIELD_Y_STEP * row;

        return mouseX >= x && mouseX < x + SUBMIT_SIZE && mouseY >= y && mouseY < y + SUBMIT_SIZE;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // The tab lives outside the panel, so undo the layer's origin translation.
        GL11.glPushMatrix();
        GL11.glTranslatef(-guiLeft, -guiTop, 0F);
        GL11.glColor4f(1F, 1F, 1F, 1F);
        sideIo.draw(mc, fontRendererObj, guiLeft, guiTop);
        GL11.glPopMatrix();

        fontRendererObj.drawString(StatCollector.translateToLocal(definition.getTitleKey()), 8, 6, 4210752);
        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 94, 4210752);

        drawFields(mouseX, mouseY);
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
