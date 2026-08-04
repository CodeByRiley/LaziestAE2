package com.codebyriley.laziestae2.client.gui;

import com.codebyriley.laziestae2.Constants;
import com.codebyriley.laziestae2.network.LazyNetwork;
import com.codebyriley.laziestae2.network.MessageMachineConfig;
import com.codebyriley.laziestae2.tile.base.ISideConfigurable;
import com.codebyriley.laziestae2.tile.base.MachineSideMode;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

/** The face-configuration pad and auto-export toggle shared by machine GUIs. */
public class SideIoWidget extends Gui {

    public static final int PAD_X = 146;
    public static final int PAD_Y = 7;
    public static final int AUTO_EXPORT_X = 127;
    public static final int AUTO_EXPORT_Y = 7;
    public static final int SIZE = 17;

    private static final ResourceLocation SIDE_IO_TEXTURE =
            new ResourceLocation(Constants.MOD_ID, "textures/gui/component/side_io.png");
    private static final ResourceLocation AUTO_EXPORT_TEXTURE =
            new ResourceLocation(Constants.MOD_ID, "textures/gui/component/auto_export.png");

    // side_io.png is 22x17: panel (0,0,17,17), then 5x5 markers for
    // input (17,0), output (17,5) and omni (17,10).
    private static final int SIDE_IO_TEX_WIDTH = 22;
    private static final int SIDE_IO_TEX_HEIGHT = 17;
    private static final int MARKER_U = 17;
    private static final int MARKER_SIZE = 5;

    // auto_export.png is 34x17: off (0,0), on (17,0).
    private static final int AUTO_EXPORT_TEX_WIDTH = 34;
    private static final int AUTO_EXPORT_TEX_HEIGHT = 17;

    private static final int[] FACE_X = { 6, 1, 6, 11, 6, 11 };
    private static final int[] FACE_Y = { 1, 6, 6, 6, 11, 11 };
    private static final String[] FACE_NAMES = { "up", "left", "front", "right", "down", "back" };
    private static final int FACE_COUNT = 6;

    private final ISideConfigurable tile;
    private int padX;
    private int padY;
    private int exportX;
    private int exportY;
    private float scale = 1F;

    public SideIoWidget(ISideConfigurable tile) {
        this(tile, PAD_X, PAD_Y, AUTO_EXPORT_X, AUTO_EXPORT_Y);
    }

    /**
     * Positions are relative to the GUI origin and may sit outside the panel,
     * which is how the machine GUIs hang these as tabs off the right edge.
     */
    public SideIoWidget(ISideConfigurable tile, int padX, int padY, int exportX, int exportY) {
        this.tile = tile;
        this.padX = padX;
        this.padY = padY;
        this.exportX = exportX;
        this.exportY = exportY;
    }

    /** Lets a container reposition the controls each frame, e.g. while animating. */
    public void setPosition(int padX, int padY, int exportX, int exportY) {
        this.padX = padX;
        this.padY = padY;
        this.exportX = exportX;
        this.exportY = exportY;
    }

    /** Renders the pad and toggle at this multiple of their native 17x17 size. */
    public void setScale(float scale) {
        this.scale = scale;
    }

    public int getScaledSize() {
        return Math.round(SIZE * scale);
    }

    private int getScaledMarker() {
        return Math.round(MARKER_SIZE * scale);
    }

    public boolean hasAutoExport() {
        return tile.supportsAutoExport();
    }

    /** Screen-space bounds of everything this widget draws, for NEI to avoid. */
    public java.awt.Rectangle getBounds(int left, int top) {
        int size = getScaledSize();

        if (!tile.supportsAutoExport()) {
            return new java.awt.Rectangle(left + padX, top + padY, size, size);
        }

        int minX = Math.min(padX, exportX);
        int minY = Math.min(padY, exportY);
        int maxX = Math.max(padX, exportX) + size;
        int maxY = Math.max(padY, exportY) + size;

        return new java.awt.Rectangle(left + minX, top + minY, maxX - minX, maxY - minY);
    }

    public void draw(Minecraft mc, int left, int top) {
        if (tile.supportsAutoExport()) {
            mc.getTextureManager().bindTexture(AUTO_EXPORT_TEXTURE);
            drawScaled(left + exportX, top + exportY,
                    tile.isAutoExporting() ? SIZE : 0, 0F, SIZE, SIZE,
                    AUTO_EXPORT_TEX_WIDTH, AUTO_EXPORT_TEX_HEIGHT);
        }

        mc.getTextureManager().bindTexture(SIDE_IO_TEXTURE);
        drawScaled(left + padX, top + padY, 0F, 0F, SIZE, SIZE,
                SIDE_IO_TEX_WIDTH, SIDE_IO_TEX_HEIGHT);

        for (int i = 0; i < FACE_COUNT; i++) {
            int markerV = getMarkerV(tile.getSideMode(tile.getWidgetSide(i)));
            if (markerV < 0) {
                continue;
            }

            drawScaled(
                    left + padX + Math.round(FACE_X[i] * scale),
                    top + padY + Math.round(FACE_Y[i] * scale),
                    MARKER_U, markerV, MARKER_SIZE, MARKER_SIZE,
                    SIDE_IO_TEX_WIDTH, SIDE_IO_TEX_HEIGHT);
        }
    }

    /** Draws a texture region enlarged about its top-left corner. */
    private void drawScaled(int x, int y, float u, float v, int width, int height, int texWidth, int texHeight) {
        if (scale == 1F) {
            func_146110_a(x, y, u, v, width, height, texWidth, texHeight);
            return;
        }

        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0F);
        GL11.glScalef(scale, scale, 1F);
        func_146110_a(0, 0, u, v, width, height, texWidth, texHeight);
        GL11.glPopMatrix();
    }

    private static int getMarkerV(MachineSideMode mode) {
        switch (mode) {
            case INPUT:
                return 0;
            case OUTPUT:
                return MARKER_SIZE;
            case OMNI:
                return MARKER_SIZE * 2;
            default:
                return -1;
        }
    }

    public int getHoveredFace(int mouseX, int mouseY, int left, int top) {
        for (int i = 0; i < FACE_COUNT; i++) {
            int x = left + padX + Math.round(FACE_X[i] * scale);
            int y = top + padY + Math.round(FACE_Y[i] * scale);
            int size = getScaledMarker();

            if (mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size) {
                return i;
            }
        }

        return -1;
    }

    public boolean isOverAutoExport(int mouseX, int mouseY, int left, int top) {
        if (!tile.supportsAutoExport()) {
            return false;
        }

        int x = left + exportX;
        int y = top + exportY;
        int size = getScaledSize();
        return mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size;
    }

    public List<String> getFaceTooltip(int face) {
        List<String> tooltip = new ArrayList<String>();
        tooltip.add(EnumChatFormatting.YELLOW
                + StatCollector.translateToLocal("gui.laziestae2.side." + FACE_NAMES[face]));
        tooltip.add(StatCollector.translateToLocal(
                tile.getSideMode(tile.getWidgetSide(face)).getUnlocalizedName()));
        return tooltip;
    }

    public String getAutoExportTooltip() {
        return StatCollector.translateToLocal(tile.isAutoExporting()
                ? "gui.laziestae2.auto_export.on"
                : "gui.laziestae2.auto_export.off");
    }

    /** Left-click cycles a face forwards, right-click backwards. Returns true if handled. */
    public boolean handleClick(int mouseX, int mouseY, int button, int left, int top) {
        if (button != 0 && button != 1) {
            return false;
        }

        TileEntity entity = (TileEntity)tile;
        int face = getHoveredFace(mouseX, mouseY, left, top);

        if (face >= 0) {
            LazyNetwork.CHANNEL.sendToServer(new MessageMachineConfig(
                    entity, MessageMachineConfig.MODE_CYCLE_SIDE, tile.getWidgetSide(face), button == 1));
            return true;
        }

        if (isOverAutoExport(mouseX, mouseY, left, top)) {
            LazyNetwork.CHANNEL.sendToServer(new MessageMachineConfig(
                    entity, MessageMachineConfig.MODE_TOGGLE_EXPORT, 0, false));
            return true;
        }

        return false;
    }
}
