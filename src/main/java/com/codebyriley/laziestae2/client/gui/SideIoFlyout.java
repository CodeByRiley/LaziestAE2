package com.codebyriley.laziestae2.client.gui;

import java.awt.Rectangle;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

/**
 * A tab on the right edge of a machine GUI that expands on hover to reveal the
 * side-IO pad and auto-export toggle, in the style of Thermal Expansion's
 * configuration tab.
 */
public class SideIoFlyout extends Gui {

    /** Controls render at twice their native size inside the tab. */
    private static final float CONTROL_SCALE = 2F;
    private static final int CONTROL_SIZE = Math.round(SideIoWidget.SIZE * CONTROL_SCALE);
    private static final int MARGIN = 6;
    private static final int TITLE_HEIGHT = 12;

    /**
     * Collapsed the tab is about the size of an inventory slot. The margin has to
     * clear the panel frame, which is 2px of outline and bevel, or the pad drawn
     * inside it overlaps the border.
     */
    private static final int COLLAPSED_MARGIN = 3;
    private static final int COLLAPSED_WIDTH = SideIoWidget.SIZE + COLLAPSED_MARGIN * 2;
    private static final int COLLAPSED_HEIGHT = SideIoWidget.SIZE + COLLAPSED_MARGIN * 2;

    private static final int TEXT_COLOR = 0x404040;

    // flyout_panel.png is an 18x18 nine-patch of 6x6 tiles: corners at the grid
    // corners, edges between them, and a flat centre. Edges and centre stretch.
    private static final net.minecraft.util.ResourceLocation PANEL_TEXTURE =
            new net.minecraft.util.ResourceLocation(
                    com.codebyriley.laziestae2.Constants.MOD_ID, "textures/gui/component/flyout_panel.png");
    private static final int PANEL_TILE = 6;
    private static final int PANEL_TEX_SIZE = 18;

    /** Fraction of the open/close transition covered per frame. */
    private static final float ANIMATION_STEP = 0.4F;

    /** How far the cursor must move while held before a click becomes a drag. */
    private static final int DRAG_THRESHOLD = 3;

    /** Remembers where each GUI's tab was dragged to, for the rest of the session. */
    private static final java.util.Map<String, int[]> POSITIONS = new java.util.HashMap<String, int[]>();

    private final SideIoWidget widget;
    private final String key;
    private final int expandedWidth;
    private final int expandedHeight;

    /** Which edge the tab is attached to, and therefore which way it opens. */
    private enum Edge {
        LEFT, RIGHT, TOP, BOTTOM
    }

    private int anchorX;
    private int anchorY;
    private Edge edge = Edge.RIGHT;
    private boolean open;
    private float progress;

    private boolean dragging;
    /** Mouse is held on the tab, but has not moved far enough to be a drag yet. */
    private boolean dragArmed;
    private int pressX;
    private int pressY;
    private int dragOffsetX;
    private int dragOffsetY;

    public SideIoFlyout(SideIoWidget widget, String key, int anchorX, int anchorY) {
        this.widget = widget;
        this.key = key;

        int[] saved = POSITIONS.get(key);
        this.anchorX = saved != null ? saved[0] : anchorX;
        this.anchorY = saved != null ? saved[1] : anchorY;

        if (saved != null)
            this.edge = Edge.values()[saved[2]];

        // Controls are packed at their own drawn width, not in uniform slots, so
        // the gap between them stays MARGIN whatever size each sprite is.
        int contentWidth = CONTROL_SIZE;

        if (widget.hasAutoExport())
            contentWidth += MARGIN + Math.round(SideIoWidget.AUTO_EXPORT_SIZE * CONTROL_SCALE);

        if (widget.hasRedstoneControl())
            contentWidth += MARGIN + Math.round(SideIoWidget.REDSTONE_SIZE * CONTROL_SCALE);

        this.expandedWidth = MARGIN * 2 + contentWidth;
        this.expandedHeight = MARGIN * 2 + TITLE_HEIGHT + CONTROL_SIZE;
    }

    private int getWidth() {
        return Math.round(COLLAPSED_WIDTH + (expandedWidth - COLLAPSED_WIDTH) * progress);
    }

    private int getHeight() {
        return Math.round(COLLAPSED_HEIGHT + (expandedHeight - COLLAPSED_HEIGHT) * progress);
    }

    /**
     * Top-left of the panel. It expands away from the edge it is stuck to, so a
     * tab on the left opens leftwards and one on the top opens upwards.
     */
    private int getPanelX() {
        return edge == Edge.LEFT ? anchorX + COLLAPSED_WIDTH - getWidth() : anchorX;
    }

    private int getPanelY() {
        return edge == Edge.TOP ? anchorY + COLLAPSED_HEIGHT - getHeight() : anchorY;
    }

    public Rectangle getBounds(int guiLeft, int guiTop) {
        return new Rectangle(guiLeft + getPanelX(), guiTop + getPanelY(), getWidth(), getHeight());
    }

    /** Expanded far enough that the controls inside are usable. */
    private boolean isOpen() {
        return progress > 0.99F;
    }

    private boolean isOverTab(int mouseX, int mouseY, int guiLeft, int guiTop) {
        return getBounds(guiLeft, guiTop).contains(mouseX, mouseY);
    }

    public void update(int mouseX, int mouseY, int guiLeft, int guiTop) {
        widget.setMousePosition(mouseX, mouseY);

        // While being dragged the tab stays collapsed, so it is easy to place.
        open = !dragging && isOverTab(mouseX, mouseY, guiLeft, guiTop);
        progress = Math.max(0F, Math.min(1F, progress + (open ? ANIMATION_STEP : -ANIMATION_STEP)));

        if (isOpen()) {
            widget.setScale(CONTROL_SCALE);
            layoutControls(guiLeft, guiTop);
        }
    }

    /** Keeps the controls in step with the panel as it grows and shrinks. */
    private void layoutControls(int guiLeft, int guiTop) {
        int padSize = widget.getScaledSize();
        int padX = getPanelX() + MARGIN;
        int padY = getPanelY() + getHeight() - padSize - MARGIN;

        // Controls sit in a row after the pad, each taking only its own width and
        // centred against the taller pad. Any control the tile lacks is skipped.
        int exportSize = widget.getScaledAutoExportSize();
        int redstoneSize = widget.getScaledRedstoneSize();

        int next = padX + padSize + MARGIN;
        int exportX = next;

        if (widget.hasAutoExport())
            next += exportSize + MARGIN;

        widget.setPosition(
                padX, padY,
                exportX, padY + (padSize - exportSize) / 2,
                next, padY + (padSize - redstoneSize) / 2);
    }

    public void draw(Minecraft mc, FontRenderer font, int guiLeft, int guiTop) {
        int x = guiLeft + getPanelX();
        int y = guiTop + getPanelY();
        int width = getWidth();
        int height = getHeight();

        GL11.glDisable(GL11.GL_LIGHTING);

        GL11.glColor4f(1F, 1F, 1F, 1F);
        mc.getTextureManager().bindTexture(PANEL_TEXTURE);
        drawPanel(x, y, width, height);

        if (isOpen()) {
            font.drawString(StatCollector.translateToLocal("gui.laziestae2.side_io.title"),
                    x + MARGIN, y + MARGIN, TEXT_COLOR);
            GL11.glColor4f(1F, 1F, 1F, 1F);
            widget.setScale(CONTROL_SCALE);
            layoutControls(guiLeft, guiTop);
            widget.draw(mc, guiLeft, guiTop);
        } else if (progress <= 0.01F) {
            // Collapsed: the pad alone, at native size, doubles as the tab icon.
            GL11.glColor4f(1F, 1F, 1F, 1F);
            widget.setScale(1F);
            widget.setPosition(
                    anchorX + COLLAPSED_MARGIN, anchorY + COLLAPSED_MARGIN,
                    anchorX + COLLAPSED_MARGIN, anchorY + COLLAPSED_MARGIN);
            widget.draw(mc, guiLeft, guiTop);
        }

        GL11.glEnable(GL11.GL_LIGHTING);
    }

    /**
     * Draws the panel from the nine-patch. Corners keep their size, the edges
     * stretch along their run, and the centre fills what is left, so the same
     * sheet serves the collapsed tab and every step of the open animation.
     */
    private void drawPanel(int x, int y, int width, int height) {
        int tile = PANEL_TILE;
        int midWidth = Math.max(0, width - tile * 2);
        int midHeight = Math.max(0, height - tile * 2);
        int right = x + width - tile;
        int bottom = y + height - tile;
        int far = PANEL_TEX_SIZE - tile;

        drawTile(x, y, 0, 0, 1F, 1F);
        drawTile(right, y, far, 0, 1F, 1F);
        drawTile(x, bottom, 0, far, 1F, 1F);
        drawTile(right, bottom, far, far, 1F, 1F);

        if (midWidth > 0) {
            float scaleX = (float)midWidth / tile;
            drawTile(x + tile, y, tile, 0, scaleX, 1F);
            drawTile(x + tile, bottom, tile, far, scaleX, 1F);
        }

        if (midHeight > 0) {
            float scaleY = (float)midHeight / tile;
            drawTile(x, y + tile, 0, tile, 1F, scaleY);
            drawTile(right, y + tile, far, tile, 1F, scaleY);
        }

        if (midWidth > 0 && midHeight > 0)
            drawTile(x + tile, y + tile, tile, tile, (float)midWidth / tile, (float)midHeight / tile);
    }

    /** Blits one 6x6 tile, stretched about its top-left corner. */
    private void drawTile(int x, int y, int u, int v, float scaleX, float scaleY) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0F);
        GL11.glScalef(scaleX, scaleY, 1F);
        func_146110_a(0, 0, u, v, PANEL_TILE, PANEL_TILE, PANEL_TEX_SIZE, PANEL_TEX_SIZE);
        GL11.glPopMatrix();
    }

    /** Tooltip lines for whatever the cursor is over, or null. */
    public List<String> getTooltip(int mouseX, int mouseY, int guiLeft, int guiTop) {
        if (!isOpen()) {
            if (isOverTab(mouseX, mouseY, guiLeft, guiTop)) {
                return Collections.singletonList(
                        StatCollector.translateToLocal("gui.laziestae2.side_io.title"));
            }

            return null;
        }

        int face = widget.getHoveredFace(mouseX, mouseY, guiLeft, guiTop);
        if (face >= 0)
            return widget.getFaceTooltip(face);

        if (widget.isOverAutoExport(mouseX, mouseY, guiLeft, guiTop))
            return Collections.singletonList(widget.getAutoExportTooltip());

        if (widget.isOverRedstone(mouseX, mouseY, guiLeft, guiTop))
            return widget.getRedstoneTooltip();

        return null;
    }

    /**
     * Consumes clicks on the tab. Hitting a control operates it; anywhere else
     * arms a drag, which only starts once the cursor actually moves so that a
     * plain click leaves the panel open.
     */
    public boolean handleClick(int mouseX, int mouseY, int button, int guiLeft, int guiTop) {
        if (!isOverTab(mouseX, mouseY, guiLeft, guiTop))
            return false;

        if (isOpen() && widget.handleClick(mouseX, mouseY, button, guiLeft, guiTop))
            return true;

        if (button == 0) {
            dragArmed = true;
            pressX = mouseX;
            pressY = mouseY;
            dragOffsetX = mouseX - (guiLeft + anchorX);
            dragOffsetY = mouseY - (guiTop + anchorY);
        }

        return true;
    }

    public void handleDrag(int mouseX, int mouseY, int guiLeft, int guiTop) {
        if (!dragging) {
            if (!dragArmed)
                return;

            // Below the threshold this is still a click being held, not a drag.
            if (Math.abs(mouseX - pressX) < DRAG_THRESHOLD && Math.abs(mouseY - pressY) < DRAG_THRESHOLD)
                return;

            dragging = true;
        }

        anchorX = mouseX - guiLeft - dragOffsetX;
        anchorY = mouseY - guiTop - dragOffsetY;
    }

    /** On release the tab snaps flush against the nearest edge of the panel. */
    public void handleRelease(int guiWidth, int guiHeight) {
        dragArmed = false;

        // A press that never became a drag leaves the tab where it was.
        if (!dragging)
            return;

        dragging = false;
        snapToEdge(guiWidth, guiHeight);
        POSITIONS.put(key, new int[] { anchorX, anchorY, edge.ordinal() });
    }

    private void snapToEdge(int guiWidth, int guiHeight) {
        int centerX = anchorX + COLLAPSED_WIDTH / 2;
        int centerY = anchorY + COLLAPSED_HEIGHT / 2;

        int toLeft = Math.abs(centerX);
        int toRight = Math.abs(guiWidth - centerX);
        int toTop = Math.abs(centerY);
        int toBottom = Math.abs(guiHeight - centerY);
        int nearest = Math.min(Math.min(toLeft, toRight), Math.min(toTop, toBottom));

        if (nearest == toLeft) {
            edge = Edge.LEFT;
            anchorX = -COLLAPSED_WIDTH;
            anchorY = clamp(anchorY, 0, guiHeight - COLLAPSED_HEIGHT);
        } else if (nearest == toRight) {
            edge = Edge.RIGHT;
            anchorX = guiWidth;
            anchorY = clamp(anchorY, 0, guiHeight - COLLAPSED_HEIGHT);
        } else if (nearest == toTop) {
            edge = Edge.TOP;
            anchorY = -COLLAPSED_HEIGHT;
            anchorX = clamp(anchorX, 0, guiWidth - COLLAPSED_WIDTH);
        } else {
            edge = Edge.BOTTOM;
            anchorY = guiHeight;
            anchorX = clamp(anchorX, 0, guiWidth - COLLAPSED_WIDTH);
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
