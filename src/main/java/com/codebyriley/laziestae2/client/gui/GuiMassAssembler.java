package com.codebyriley.laziestae2.client.gui;

import com.codebyriley.laziestae2.gui.MachineGuiDefinition;
import com.codebyriley.laziestae2.inventory.ContainerMassAssembler;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerController;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class GuiMassAssembler extends GuiContainer {

    // Bar sprites live to the right of the 176px-wide GUI panel.
    private static final int QUEUE_U = 176;
    private static final int CPU_U = 179;
    private static final int BAR_WIDTH = 3;
    private static final int BAR_HEIGHT = 25;
    private static final int QUEUE_X = 58;
    private static final int CPU_X = 64;
    private static final int BAR_Y = 94;

    private static final int PROGRESS_U = 182;
    private static final int PROGRESS_V = 0;
    private static final int PROGRESS_X = 36;
    private static final int PROGRESS_Y = 113;
    private static final int PROGRESS_WIDTH = 16;
    private static final int PROGRESS_HEIGHT = 2;

    private static final int PAGE_TEXT_X = 108;
    private static final int PAGE_TEXT_Y = 108;

    // Recessed well in the texture reserved for the pattern search field.
    private static final int SEARCH_X = 80;
    private static final int SEARCH_Y = 5;
    private static final int SEARCH_WIDTH = 88;
    private static final int SEARCH_HEIGHT = 10;

    /** Title must stop before the search well at x=79. */
    private static final int TITLE_MAX_WIDTH = 68;

    // Job preview: 3x3 ingredient well at (7,93) with 9px cells (items drawn at
    // half scale), and the result slot at (36,94).
    private static final int PREVIEW_X = 15;
    private static final int PREVIEW_Y = 187;
    private static final int PREVIEW_PITCH = 18;
    private static final int RESULT_X = 36;
    private static final int RESULT_Y = 94;

    private static final net.minecraft.util.ResourceLocation NEXT_PREV_TEXTURE =
            new net.minecraft.util.ResourceLocation(
                    com.codebyriley.laziestae2.Constants.MOD_ID, "textures/gui/component/next_prev.png");

    // next_prev.png is 58x33: prev (0,0,18,11), next (18,0,18,11),
    // first (36,0,11,11), last (47,0,11,11). Rows are normal / disabled / hovered.
    private static final int NAV_TEX_WIDTH = 58;
    private static final int NAV_TEX_HEIGHT = 33;
    private static final int NAV_BUTTON_HEIGHT = 11;
    private static final int NAV_X = 108;
    private static final int NAV_Y = 93;

    private static final int STATE_NORMAL = 0;
    private static final int STATE_DISABLED = 1;
    private static final int STATE_HOVERED = 2;

    // Widget-relative x, width, and source u for first / prev / next / last.
    private static final int[] NAV_X_OFFSET = { 0, 12, 31, 50 };
    private static final int[] NAV_WIDTH = { 11, 18, 18, 11 };
    private static final int[] NAV_U = { 36, 0, 18, 47 };

    private static final String[] NAV_TOOLTIPS = {
            "gui.laziestae2.page_first",
            "gui.laziestae2.page_prev",
            "gui.laziestae2.page_next",
            "gui.laziestae2.page_last"
    };

    private final ContainerMassAssembler container;
    private final TileMassAssemblerController controller;
    private final MachineGuiDefinition definition = MachineGuiDefinition.BIG_ASSEMBLER;

    private net.minecraft.client.gui.GuiTextField searchField;
    private String searchQuery = "";

    public GuiMassAssembler(InventoryPlayer playerInventory, TileMassAssemblerController controller) {
        this(new ContainerMassAssembler(playerInventory, controller), controller);
    }

    private GuiMassAssembler(ContainerMassAssembler container, TileMassAssemblerController controller) {
        super(container);
        this.container = container;
        this.controller = controller;
        this.xSize = definition.getWidth();
        this.ySize = definition.getHeight();
    }

    @Override
    public void initGui() {
        super.initGui();

        searchField = new net.minecraft.client.gui.GuiTextField(
                fontRendererObj, guiLeft + SEARCH_X, guiTop + SEARCH_Y, SEARCH_WIDTH, SEARCH_HEIGHT);
        searchField.setMaxStringLength(32);
        searchField.setEnableBackgroundDrawing(false);
        searchField.setText(searchQuery);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        searchField.updateCursorCounter();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (searchField.isFocused()) {
            if (keyCode == org.lwjgl.input.Keyboard.KEY_ESCAPE) {
                searchField.setFocused(false);
                return;
            }

            searchField.textboxKeyTyped(typedChar, keyCode);
            searchQuery = searchField.getText().trim().toLowerCase();
            return;
        }

        super.keyTyped(typedChar, keyCode);
    }

    /**
     * Dims visible pattern slots that do not match the search query. Drawn as an
     * overlay because GuiContainer's per-slot hook is private in 1.7.10.
     */
    private void drawSearchDimming() {
        if (searchQuery.isEmpty()) {
            return;
        }

        int left = (width - xSize) / 2;
        int top = (height - ySize) / 2;

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        for (Object slotObject : inventorySlots.inventorySlots) {
            net.minecraft.inventory.Slot slot = (net.minecraft.inventory.Slot)slotObject;

            if (!container.isPatternSlot(slot.slotNumber) || !slot.func_111238_b() || matchesSearch(slot.getStack())) {
                continue;
            }

            drawRect(
                    left + slot.xDisplayPosition,
                    top + slot.yDisplayPosition,
                    left + slot.xDisplayPosition + 16,
                    top + slot.yDisplayPosition + 16,
                    0x7F000000);
        }

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }

    @SuppressWarnings("unchecked")
    private boolean matchesSearch(net.minecraft.item.ItemStack stack) {
        if (stack == null) {
            return false;
        }

        if (stack.getDisplayName().toLowerCase().contains(searchQuery)) {
            return true;
        }

        // Encoded patterns list their inputs and outputs in the tooltip.
        try {
            java.util.List<String> tooltip = stack.getTooltip(mc.thePlayer, false);
            for (String line : tooltip) {
                if (net.minecraft.util.EnumChatFormatting.getTextWithoutFormattingCodes(line)
                        .toLowerCase().contains(searchQuery)) {
                    return true;
                }
            }
        } catch (Exception e) {
            // Some patterns throw when their recipe is unavailable; treat as no match.
        }

        return false;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = fontRendererObj.trimStringToWidth(
                StatCollector.translateToLocal(definition.getTitleKey()), TITLE_MAX_WIDTH);
        fontRendererObj.drawString(title, 8, 6, 4210752);
        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 94, 4210752);

        fontRendererObj.drawString(
                String.format("%d/%d", container.getCurrentPage() + 1, container.getPageCount()),
                PAGE_TEXT_X, PAGE_TEXT_Y, 4210752);

        if (searchQuery.isEmpty() && !searchField.isFocused()) {
            fontRendererObj.drawString(
                    StatCollector.translateToLocal("gui.laziestae2.search"), SEARCH_X + 1, SEARCH_Y + 1, 0x808080);
        }

        drawJobPreview();

        // Drawn in this layer so tooltips render above them.
        GL11.glPushMatrix();
        GL11.glTranslatef(-guiLeft, -guiTop, 0F);
        drawSearchDimming();
        GL11.glDisable(GL11.GL_LIGHTING);
        searchField.drawTextBox();
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();

        if (!container.isFormed()) {
            String status = StatCollector.translateToLocal(container.isTooLarge()
                    ? "container.laziestae2.big_assembler.too_large"
                    : "container.laziestae2.big_assembler.incomplete");
            fontRendererObj.drawString(status, 79, 6, container.isTooLarge() ? 0xAA0000 : 0xAA6600);
        }
    }

    /** Ingredients of the job currently being worked on, plus its result. */
    private void drawJobPreview() {
        net.minecraft.item.ItemStack result = controller.getActiveJobResult();
        if (result == null) {
            return;
        }

        net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
        zLevel = 100F;
        itemRender.zLevel = 100F;

        GL11.glPushMatrix();
        GL11.glScalef(0.5F, 0.5F, 1F);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                drawStack(controller.getActiveJobInput(row * 3 + column),
                        PREVIEW_X + column * PREVIEW_PITCH, PREVIEW_Y + row * PREVIEW_PITCH);
            }
        }

        GL11.glPopMatrix();

        drawStack(result, RESULT_X, RESULT_Y);

        itemRender.zLevel = 0F;
        zLevel = 0F;
        net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }

    private void drawStack(net.minecraft.item.ItemStack stack, int x, int y) {
        if (stack == null) {
            return;
        }

        itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), stack, x, y);
        itemRender.renderItemOverlayIntoGUI(fontRendererObj, mc.getTextureManager(), stack, x, y);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        mc.getTextureManager().bindTexture(definition.getTexture());

        int left = (width - xSize) / 2;
        int top = (height - ySize) / 2;
        drawTexturedModalRect(left, top, 0, 0, xSize, ySize);

        drawVerticalBar(left + QUEUE_X, top + BAR_Y, QUEUE_U, getQueueFraction());
        drawVerticalBar(left + CPU_X, top + BAR_Y, CPU_U, getCpuFraction());
        drawProgress(left, top);

        // Always drawn: the texture has wells for these, so hiding them leaves holes.
        drawPageNav(left, top, mouseX, mouseY);
    }

    private void drawPageNav(int left, int top, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(NEXT_PREV_TEXTURE);

        int page = container.getCurrentPage();
        int lastPage = container.getPageCount() - 1;
        int hovered = getHoveredNavButton(mouseX, mouseY);

        for (int button = 0; button < NAV_X_OFFSET.length; button++) {
            // Buttons 0-1 move backwards, 2-3 forwards.
            boolean disabled = button < 2 ? page == 0 : page == lastPage;
            int state = disabled ? STATE_DISABLED : (hovered == button ? STATE_HOVERED : STATE_NORMAL);

            func_146110_a(
                    left + NAV_X + NAV_X_OFFSET[button],
                    top + NAV_Y,
                    NAV_U[button],
                    state * NAV_BUTTON_HEIGHT,
                    NAV_WIDTH[button],
                    NAV_BUTTON_HEIGHT,
                    NAV_TEX_WIDTH,
                    NAV_TEX_HEIGHT);
        }
    }

    private int getHoveredNavButton(int mouseX, int mouseY) {
        int left = (width - xSize) / 2 + NAV_X;
        int top = (height - ySize) / 2 + NAV_Y;

        if (mouseY < top || mouseY >= top + NAV_BUTTON_HEIGHT) {
            return -1;
        }

        for (int button = 0; button < NAV_X_OFFSET.length; button++) {
            int x = left + NAV_X_OFFSET[button];
            if (mouseX >= x && mouseX < x + NAV_WIDTH[button]) {
                return button;
            }
        }

        return -1;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        searchField.mouseClicked(mouseX, mouseY, button);

        if (button == 0 && container.getPageCount() > 1) {
            int navButton = getHoveredNavButton(mouseX, mouseY);
            int page = container.getCurrentPage();
            int lastPage = container.getPageCount() - 1;
            int target = page;

            switch (navButton) {
                case 0:
                    target = 0;
                    break;
                case 1:
                    target = page - 1;
                    break;
                case 2:
                    target = page + 1;
                    break;
                case 3:
                    target = lastPage;
                    break;
                default:
                    break;
            }

            if (navButton >= 0 && target != page && target >= 0 && target <= lastPage) {
                container.setCurrentPage(target);
                mc.getSoundHandler().playSound(
                        net.minecraft.client.audio.PositionedSoundRecord.func_147674_a(
                                new net.minecraft.util.ResourceLocation("gui.button.press"), 1F));
                return;
            }

            if (navButton >= 0) {
                return;
            }
        }

        super.mouseClicked(mouseX, mouseY, button);
    }

    /** Bars fill upward, so both the screen and source Y shift down as they empty. */
    private void drawVerticalBar(int x, int y, int u, float fraction) {
        int filled = Math.round(fraction * BAR_HEIGHT);
        if (filled <= 0) {
            return;
        }

        int empty = BAR_HEIGHT - filled;
        drawTexturedModalRect(x, y + empty, u, empty, BAR_WIDTH, filled);
    }

    private void drawProgress(int left, int top) {
        int workPerJob = controller.getWorkPerJob();
        if (workPerJob <= 0) {
            return;
        }

        float fraction = Math.min(container.getWork() / (float)workPerJob, 1F);
        int filled = Math.round(fraction * PROGRESS_WIDTH);

        if (filled > 0) {
            drawTexturedModalRect(
                    left + PROGRESS_X, top + PROGRESS_Y, PROGRESS_U, PROGRESS_V, filled, PROGRESS_HEIGHT);
        }
    }

    private float getQueueFraction() {
        int capacity = controller.getJobQueueSize();
        return capacity <= 0 ? 0F : Math.min(container.getJobCount() / (float)capacity, 1F);
    }

    /** Coprocessor scaling is logarithmic, matching the diminishing returns in throughput. */
    private float getCpuFraction() {
        int cpus = container.getCraftingCoprocessorCount();
        if (cpus <= 0) {
            return 0F;
        }

        int maxEffective = getMaxEffectiveCpus();
        if (cpus >= maxEffective) {
            return 1F;
        }

        return (float)(Math.log1p(cpus) / Math.log1p(maxEffective));
    }

    private int getMaxEffectiveCpus() {
        int perTickUpgrade = Math.max(1, com.codebyriley.laziestae2.config.LaziestConfig.workPerTickUpgrade);
        int base = Math.max(0, com.codebyriley.laziestae2.config.LaziestConfig.workPerTickBase);
        int ceiling = controller.getWorkPerJob() * controller.getJobQueueSize() - base;
        return Math.max(1, (int)Math.ceil(ceiling / (float)perTickUpgrade));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        int left = (width - xSize) / 2;
        int top = (height - ySize) / 2;

        int navButton = getHoveredNavButton(mouseX, mouseY);

        if (navButton >= 0) {
            drawHoverText(StatCollector.translateToLocal(NAV_TOOLTIPS[navButton]), mouseX, mouseY);
        } else if (isOver(mouseX, mouseY, left + PROGRESS_X - 1, top + PROGRESS_Y - 1, 18, 4)) {
            drawHoverText(String.format("%d / %d", container.getWork(), controller.getWorkPerJob()), mouseX, mouseY);
        } else if (isOver(mouseX, mouseY, left + QUEUE_X - 1, top + BAR_Y - 1, 5, 27)) {
            drawHoverText(format("jobs", container.getJobCount()), mouseX, mouseY);
        } else if (isOver(mouseX, mouseY, left + CPU_X - 1, top + BAR_Y - 1, 5, 27)) {
            List<String> lines = new ArrayList<String>();
            lines.add(format("cpus", container.getCraftingCoprocessorCount()));
            lines.add(format("work_rate", controller.getWorkRate()));
            func_146283_a(lines, mouseX, mouseY);
        } else if (isOver(mouseX, mouseY, left + 8, top + 6, 60, 10)) {
            // Structure breakdown, moved off the slot grid into a tooltip.
            List<String> lines = new ArrayList<String>();
            lines.add(format("parts", container.getPartCount()));
            lines.add(format("frames", container.getFrameCount()));
            lines.add(format("vents", container.getVentCount()));
            lines.add(format("patterns", container.getPatternProviderCount()));
            lines.add(format("cpus", container.getCraftingCoprocessorCount()));
            lines.add(format("io_ports", container.getIoPortCount()));
            func_146283_a(lines, mouseX, mouseY);
        }
    }

    private void drawHoverText(String text, int mouseX, int mouseY) {
        func_146283_a(java.util.Collections.singletonList(text), mouseX, mouseY);
    }

    private static boolean isOver(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    /** Scroll wheel flips between pattern provider pages. */
    @Override
    public void handleMouseInput() {
        super.handleMouseInput();

        int scroll = Mouse.getEventDWheel();
        if (scroll == 0 || container.getPageCount() <= 1) {
            return;
        }

        if (scroll < 0) {
            container.setCurrentPage(container.getCurrentPage() + 1);
        } else {
            container.setCurrentPage(container.getCurrentPage() - 1);
        }
    }

    private static String format(String key, int value) {
        return String.format(
                StatCollector.translateToLocal("container.laziestae2.big_assembler." + key), value);
    }
}
