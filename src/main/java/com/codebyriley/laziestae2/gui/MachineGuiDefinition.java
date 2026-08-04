package com.codebyriley.laziestae2.gui;

import com.codebyriley.laziestae2.Constants;
import net.minecraft.util.ResourceLocation;

public final class MachineGuiDefinition {

    public static final MachineGuiDefinition AGGREGATOR = new MachineGuiDefinition(
            GuiIds.AGGREGATOR,
            "aggregator",
            "container." + Constants.MOD_ID + ".aggregator",
            "Fluix Aggregator",
            176,
            166,
            new SlotDefinition[] {
                    input(28, 35),
                    input(48, 35),
                    input(68, 35),
                    output(128, 35),
                    upgrade(146, 62)
            });

    public static final MachineGuiDefinition CENTRIFUGE = new MachineGuiDefinition(
            GuiIds.CENTRIFUGE,
            "centrifuge",
            "container." + Constants.MOD_ID + ".centrifuge",
            "Pulse Centrifuge",
            176,
            166,
            new SlotDefinition[] {
                    input(56, 35),
                    output(116, 35),
                    upgrade(146, 62)
            });

    public static final MachineGuiDefinition ETCHER = new MachineGuiDefinition(
            GuiIds.ETCHER,
            "etcher",
            "container." + Constants.MOD_ID + ".etcher",
            "ME Circuit Etcher",
            176,
            166,
            new SlotDefinition[] {
                    input(37, 17),
                    input(37, 53),
                    input(60, 35),
                    output(120, 35),
                    upgrade(146, 62)
            });

    public static final MachineGuiDefinition FAST_CRAFTER = new MachineGuiDefinition(
            GuiIds.FAST_CRAFTER,
            "fast_crafter",
            "container." + Constants.MOD_ID + ".fast_crafter",
            "Preemptive Assembly Unit",
            176,
            166,
            new SlotDefinition[0]);

    public static final MachineGuiDefinition LEVEL_MAINTAINER = new MachineGuiDefinition(
            GuiIds.LEVEL_MAINTAINER,
            "level_maintainer",
            "container." + Constants.MOD_ID + ".level_maintainer",
            "ME Level Maintainer",
            176,
            214,
            new SlotDefinition[0]);

    public static final MachineGuiDefinition ENERGIZER = new MachineGuiDefinition(
            GuiIds.ENERGIZER,
            "energizer",
            "container." + Constants.MOD_ID + ".energizer",
            "Crystal Energizer",
            176,
            166,
            new SlotDefinition[] {
                    input(56, 35),
                    output(116, 35),
                    upgrade(146, 62)
            });

    public static final MachineGuiDefinition BIG_ASSEMBLER = new MachineGuiDefinition(
            GuiIds.BIG_ASSEMBLER,
            "big_assembler",
            "container." + Constants.MOD_ID + ".big_assembler",
            "Mass Assembler",
            176,
            217,
            new SlotDefinition[0]);

    private static final MachineGuiDefinition[] BY_ID = new MachineGuiDefinition[7];

    static {
        // Progress arrow regions double as NEI recipe lookup buttons.
        // Source UVs live at u=176 on each machine's GUI texture.
        AGGREGATOR.withProgress(92, 36, 24, 14).withNeiIdentifier("laziestae2.aggregator");
        CENTRIFUGE.withProgress(84, 36, 22, 14).withNeiIdentifier("laziestae2.centrifuge");
        ETCHER.withProgress(88, 36, 22, 14).withNeiIdentifier("laziestae2.etcher");
        ENERGIZER.withProgress(84, 29, 22, 29).withNeiIdentifier("laziestae2.energizer");

        register(AGGREGATOR);
        register(CENTRIFUGE);
        register(ETCHER);
        register(FAST_CRAFTER);
        register(LEVEL_MAINTAINER);
        register(ENERGIZER);
        register(BIG_ASSEMBLER);
    }

    private final int guiId;
    private final String textureName;
    private final String titleKey;
    private final String fallbackTitle;
    private final int width;
    private final int height;
    private final SlotDefinition[] slots;
    private final ResourceLocation texture;

    private int progressX = -1;
    private int progressY;
    private int progressWidth;
    private int progressHeight;
    private int progressU = 176;
    private int progressV;
    private String neiIdentifier;

    private MachineGuiDefinition(int guiId, String textureName, String titleKey, String fallbackTitle, int width, int height,
            SlotDefinition[] slots) {
        this.guiId = guiId;
        this.textureName = textureName;
        this.titleKey = titleKey;
        this.fallbackTitle = fallbackTitle;
        this.width = width;
        this.height = height;
        this.slots = slots;
        this.texture = new ResourceLocation(Constants.MOD_ID, "textures/gui/" + textureName + ".png");
    }

    private MachineGuiDefinition withProgress(int x, int y, int width, int height) {
        this.progressX = x;
        this.progressY = y;
        this.progressWidth = width;
        this.progressHeight = height;
        return this;
    }

    private MachineGuiDefinition withNeiIdentifier(String identifier) {
        this.neiIdentifier = identifier;
        return this;
    }

    public boolean hasProgressArea() {
        return progressX >= 0;
    }

    public int getProgressX() {
        return progressX;
    }

    public int getProgressY() {
        return progressY;
    }

    public int getProgressWidth() {
        return progressWidth;
    }

    public int getProgressHeight() {
        return progressHeight;
    }

    public int getProgressU() {
        return progressU;
    }

    public int getProgressV() {
        return progressV;
    }

    /** NEI recipe handler identifier, or null if this machine has no NEI handler. */
    public String getNeiIdentifier() {
        return neiIdentifier;
    }

    public static MachineGuiDefinition byGuiId(int guiId) {
        return guiId >= 0 && guiId < BY_ID.length ? BY_ID[guiId] : null;
    }

    public int getGuiId() {
        return guiId;
    }

    public String getTextureName() {
        return textureName;
    }

    public String getTitleKey() {
        return titleKey;
    }

    public String getFallbackTitle() {
        return fallbackTitle;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getPlayerInventoryY() {
        return height - 82;
    }

    public int getPlayerHotbarY() {
        return height - 24;
    }

    public SlotDefinition[] getSlots() {
        return slots;
    }

    public int getSlotCount() {
        return slots.length;
    }

    public boolean isOutputSlot(int slotIndex) {
        return slotIndex >= 0 && slotIndex < slots.length && slots[slotIndex].isOutput();
    }

    public boolean isUpgradeSlot(int slotIndex) {
        return slotIndex >= 0 && slotIndex < slots.length && slots[slotIndex].isUpgrade();
    }

    /** Index of the upgrade slot, or -1 if this machine has none. */
    public int getUpgradeSlotIndex() {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i].isUpgrade()) {
                return i;
            }
        }
        return -1;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    private static void register(MachineGuiDefinition definition) {
        BY_ID[definition.getGuiId()] = definition;
    }

    private static SlotDefinition input(int x, int y) {
        return new SlotDefinition(x, y, false, false);
    }

    private static SlotDefinition output(int x, int y) {
        return new SlotDefinition(x, y, true, false);
    }

    private static SlotDefinition upgrade(int x, int y) {
        return new SlotDefinition(x, y, false, true);
    }

    public static final class SlotDefinition {

        private final int x;
        private final int y;
        private final boolean output;
        private final boolean upgrade;

        private SlotDefinition(int x, int y, boolean output, boolean upgrade) {
            this.x = x;
            this.y = y;
            this.output = output;
            this.upgrade = upgrade;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public boolean isOutput() {
            return output;
        }

        public boolean isUpgrade() {
            return upgrade;
        }
    }
}
