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

    /** Collapsed the tab is about the size of an inventory slot. */
    private static final int COLLAPSED_MARGIN = 1;
    private static final int COLLAPSED_WIDTH = SideIoWidget.SIZE + COLLAPSED_MARGIN * 2;
    private static final int COLLAPSED_HEIGHT = SideIoWidget.SIZE + COLLAPSED_MARGIN * 2;

    private static final int BORDER_COLOR = 0xFF000000;
    private static final int PANEL_COLOR = 0xFFC6C6C6;
    private static final int SHADE_COLOR = 0xFF8B8B8B;
    private static final int TEXT_COLOR = 0x404040;

    /** Fraction of the open/close transition covered per frame. */
    private static final float ANIMATION_STEP = 0.4F;

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
    private int dragOffsetX;
    private int dragOffsetY;

    public SideIoFlyout(SideIoWidget widget, String key, int anchorX, int anchorY) {
        this.widget = widget;
        this.key = key;

        int[] saved = POSITIONS.get(key);
        this.anchorX = saved != null ? saved[0] : anchorX;
        this.anchorY = saved != null ? saved[1] : anchorY;

        if (saved != null) {
            this.edge = Edge.values()[saved[2]];
        }

        // Two controls side by side when the tile has an auto-export toggle.
        int controls = widget.hasAutoExport() ? 2 : 1;
        this.expandedWidth = MARGIN * 2 + CONTROL_SIZE * controls + (controls - 1) * MARGIN;
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
        int padX = getPanelX() + MARGIN;
        int padY = getPanelY() + getHeight() - CONTROL_SIZE - MARGIN;
        int exportX = padX + CONTROL_SIZE + MARGIN;

        widget.setPosition(padX, padY, exportX, padY);
    }

    public void draw(Minecraft mc, FontRenderer font, int guiLeft, int guiTop) {
        int x = guiLeft + getPanelX();
        int y = guiTop + getPanelY();
        int width = getWidth();
        int height = getHeight();

        GL11.glDisable(GL11.GL_LIGHTING);

        drawRect(x, y, x + width, y + height, BORDER_COLOR);
        drawRect(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_COLOR);
        drawRect(x + 1, y + height - 2, x + width - 1, y + height - 1, SHADE_COLOR);

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
        if (face >= 0) {
            return widget.getFaceTooltip(face);
        }

        if (widget.isOverAutoExport(mouseX, mouseY, guiLeft, guiTop)) {
            return Collections.singletonList(widget.getAutoExportTooltip());
        }

        return null;
    }

    /**
     * Consumes clicks on the tab. Hitting a control operates it; anywhere else
     * starts a drag.
     */
    public boolean handleClick(int mouseX, int mouseY, int button, int guiLeft, int guiTop) {
        if (!isOverTab(mouseX, mouseY, guiLeft, guiTop)) {
            return false;
        }

        if (isOpen() && widget.handleClick(mouseX, mouseY, button, guiLeft, guiTop)) {
            return true;
        }

        if (button == 0) {
            dragging = true;
            dragOffsetX = mouseX - (guiLeft + anchorX);
            dragOffsetY = mouseY - (guiTop + anchorY);
        }

        return true;
    }

    public void handleDrag(int mouseX, int mouseY, int guiLeft, int guiTop) {
        if (!dragging) {
            return;
        }

        anchorX = mouseX - guiLeft - dragOffsetX;
        anchorY = mouseY - guiTop - dragOffsetY;
    }

    /** On release the tab snaps flush against the nearest edge of the panel. */
    public void handleRelease(int guiWidth, int guiHeight) {
        if (!dragging) {
            return;
        }

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
