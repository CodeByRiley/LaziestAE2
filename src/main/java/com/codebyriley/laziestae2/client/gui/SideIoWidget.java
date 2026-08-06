package com.codebyriley.laziestae2.client.gui;

import com.codebyriley.laziestae2.Constants;
import com.codebyriley.laziestae2.network.LazyNetwork;
import com.codebyriley.laziestae2.network.MessageMachineConfig;
import com.codebyriley.laziestae2.tile.base.IRedstoneConfigurable;
import com.codebyriley.laziestae2.tile.base.ISideConfigurable;
import com.codebyriley.laziestae2.tile.base.MachineSideMode;
import com.codebyriley.laziestae2.tile.base.RedstoneMode;
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
    /** Control slot size, set by the pad; the smaller sprites centre inside it. */
    public static final int SIZE = 20;

    private static final ResourceLocation  SIDE_IO_TEXTURE =
            new ResourceLocation(Constants.MOD_ID, "textures/gui/component/side_io.png");
    private static final ResourceLocation  IO_STATES_TEXTURE =
            new ResourceLocation(Constants.MOD_ID, "textures/gui/component/io_states.png");
    private static final ResourceLocation  AUTO_EXPORT_TEXTURE =
            new ResourceLocation(Constants.MOD_ID, "textures/gui/component/auto_export.png");
    private static final ResourceLocation  REDSTONE_TEXTURE =
            new ResourceLocation(Constants.MOD_ID, "textures/gui/component/redstone_mode.png");

    // side_io.png is the pad alone, 20x20, holding six 6x6 face cells. The markers
    // live in io_states.png as 6x6 frames — input (0), output (6), omni (12) —
    // sized to fill a cell exactly.
    private static final int SIDE_IO_TEX_WIDTH = 20;
    private static final int SIDE_IO_TEX_HEIGHT = 20;
    private static final int MARKER_TEX_WIDTH = 6;
    private static final int MARKER_TEX_HEIGHT = 18;
    private static final int MARKER_U = 0;
    private static final int MARKER_SIZE = 6;

    // auto_export.png is 64x16, grouped by state with the hovered frame beside it:
    // on (0), on hovered (16), off (32), off hovered (48).
    public static final int  AUTO_EXPORT_SIZE = 16;
    private static final int AUTO_EXPORT_TEX_WIDTH = 64;
    private static final int AUTO_EXPORT_TEX_HEIGHT = 16;
    private static final int AUTO_EXPORT_OFF_FRAME = 2;

    // redstone_mode.png is 64x16: low (0), high (16), ignore (32), never (48).
    // Smaller than the other controls, so it is centred within their 17px slot.
    public static final int  REDSTONE_SIZE = 16;
    private static final int REDSTONE_TEX_WIDTH = 64;
    private static final int REDSTONE_TEX_HEIGHT = 16;

    private static final int[]    FACE_X = { 7, 1, 7, 13, 7, 13 };
    private static final int[]    FACE_Y = { 1, 7, 7, 7, 13, 13 };
    private static final String[] FACE_NAMES = { "up", "left", "front", "right", "down", "back" };
    private static final int      FACE_COUNT = 6;

    private final ISideConfigurable tile;
    private int   padX;
    private int   padY;
    private int   exportX;
    private int   exportY;
    private int   redstoneX;
    private int   redstoneY;
    private float scale = 1F;
    private int   mouseX = Integer.MIN_VALUE;
    private int   mouseY = Integer.MIN_VALUE;

    /**
     * Starting positions only. The flyout lays the controls out every frame, so
     * these are overwritten before anything is drawn.
     */
    public SideIoWidget(ISideConfigurable tile) {
        this.tile    = tile;
        this.padX    = PAD_X;
        this.padY    = PAD_Y;
        this.exportX = AUTO_EXPORT_X;
        this.exportY = AUTO_EXPORT_Y;
    }

    /** Lets the flyout reposition the controls each frame, e.g. while animating. */
    public void setPosition(int padX, int padY, int exportX, int exportY, int redstoneX, int redstoneY) {
        this.padX      = padX;
        this.padY      = padY;
        this.exportX   = exportX;
        this.exportY   = exportY;
        this.redstoneX = redstoneX;
        this.redstoneY = redstoneY;
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

    private int getScaledSize(int nativeSize) {
        return Math.round(nativeSize * scale);
    }

    /** Drawn size of the auto-export toggle, which is smaller than the pad. */
    public int getScaledAutoExportSize() {
        return getScaledSize(AUTO_EXPORT_SIZE);
    }

    /** Drawn size of the redstone control, which is smaller than the pad. */
    public int getScaledRedstoneSize() {
        return getScaledSize(REDSTONE_SIZE);
    }

    /**
     * Latest cursor position, pushed in by the container each frame so controls
     * can draw a hovered frame without the draw call needing the mouse.
     */
    public void setMousePosition(int mouseX, int mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public boolean hasAutoExport() {
        return tile.supportsAutoExport();
    }

    public boolean hasRedstoneControl() {
        return tile instanceof IRedstoneConfigurable && ((IRedstoneConfigurable)tile).supportsRedstoneControl();
    }

    private RedstoneMode getRedstoneMode() {
        return hasRedstoneControl() ? ((IRedstoneConfigurable)tile).getRedstoneMode() : RedstoneMode.IGNORE;
    }

    /** Screen-space bounds of everything this widget draws, for NEI to avoid. */
    public java.awt.Rectangle getBounds(int left, int top) {
        int size = getScaledSize();
        int minX = padX;
        int minY = padY;
        int maxX = padX + size;
        int maxY = padY + size;

        if (tile.supportsAutoExport()) {
            int exportSize = getScaledAutoExportSize();
            minX = Math.min(minX, exportX);
            minY = Math.min(minY, exportY);
            maxX = Math.max(maxX, exportX + exportSize);
            maxY = Math.max(maxY, exportY + exportSize);
        }

        if (hasRedstoneControl()) {
            int redstoneSize = getScaledRedstoneSize();
            minX = Math.min(minX, redstoneX);
            minY = Math.min(minY, redstoneY);
            maxX = Math.max(maxX, redstoneX + redstoneSize);
            maxY = Math.max(maxY, redstoneY + redstoneSize);
        }

        return new java.awt.Rectangle(left + minX, top + minY, maxX - minX, maxY - minY);
    }

    public void draw(Minecraft mc, int left, int top) {
        if (hasRedstoneControl()) {
            mc.getTextureManager().bindTexture(REDSTONE_TEXTURE);
            drawScaled(left + redstoneX, top + redstoneY,
                    getRedstoneMode().getFrame() * REDSTONE_SIZE, 0F, REDSTONE_SIZE, REDSTONE_SIZE,
                    REDSTONE_TEX_WIDTH, REDSTONE_TEX_HEIGHT);
        }

        if (tile.supportsAutoExport()) {
            int frame = (tile.isAutoExporting() ? 0 : AUTO_EXPORT_OFF_FRAME)
                    + (isOverAutoExport(mouseX, mouseY, left, top) ? 1 : 0);

            mc.getTextureManager().bindTexture(AUTO_EXPORT_TEXTURE);
            drawScaled(left + exportX, top + exportY,
                    frame * AUTO_EXPORT_SIZE, 0F, AUTO_EXPORT_SIZE, AUTO_EXPORT_SIZE,
                    AUTO_EXPORT_TEX_WIDTH, AUTO_EXPORT_TEX_HEIGHT);
        }

        mc.getTextureManager().bindTexture(SIDE_IO_TEXTURE);
        drawScaled(left + padX, top + padY, 0F, 0F, SIZE, SIZE,
                SIDE_IO_TEX_WIDTH, SIDE_IO_TEX_HEIGHT);

        mc.getTextureManager().bindTexture(IO_STATES_TEXTURE);

        for (int i = 0; i < FACE_COUNT; i++) {
            int markerV = getMarkerV(tile.getSideMode(tile.getWidgetSide(i)));
            if (markerV < 0)
                continue;

            drawScaled(
                    left + padX + Math.round(FACE_X[i] * scale),
                    top + padY + Math.round(FACE_Y[i] * scale),
                    MARKER_U, markerV, MARKER_SIZE, MARKER_SIZE,
                    MARKER_TEX_WIDTH, MARKER_TEX_HEIGHT);
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

            if (mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size)
                return i;
        }

        return -1;
    }

    public boolean isOverAutoExport(int mouseX, int mouseY, int left, int top) {
        if (!tile.supportsAutoExport())
            return false;

        int x = left + exportX;
        int y = top + exportY;
        int size = getScaledAutoExportSize();
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

    public boolean isOverRedstone(int mouseX, int mouseY, int left, int top) {
        if (!hasRedstoneControl())
            return false;

        int x = left + redstoneX;
        int y = top + redstoneY;
        int size = getScaledRedstoneSize();
        return mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size;
    }

    public List<String> getRedstoneTooltip() {
        List<String> tooltip = new ArrayList<String>();
        tooltip.add(EnumChatFormatting.YELLOW
                + StatCollector.translateToLocal("gui.laziestae2.redstone.title"));
        tooltip.add(StatCollector.translateToLocal(getRedstoneMode().getUnlocalizedName()));
        return tooltip;
    }

    public String getAutoExportTooltip() {
        return StatCollector.translateToLocal(tile.isAutoExporting()
                ? "gui.laziestae2.auto_export.on"
                : "gui.laziestae2.auto_export.off");
    }

    /** Left-click cycles a face forwards, right-click backwards. Returns true if handled. */
    public boolean handleClick(int mouseX, int mouseY, int button, int left, int top) {
        if (button != 0 && button != 1)
            return false;

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

        if (isOverRedstone(mouseX, mouseY, left, top)) {
            LazyNetwork.CHANNEL.sendToServer(new MessageMachineConfig(
                    entity, MessageMachineConfig.MODE_CYCLE_REDSTONE, 0, button == 1));
            return true;
        }

        return false;
    }
}
