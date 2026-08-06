package com.codebyriley.laziestae2.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

/** The vanilla button click, which every screen in this mod plays by hand. */
@SideOnly(Side.CLIENT)
public final class GuiSounds {

    private static final ResourceLocation CLICK = new ResourceLocation("gui.button.press");

    private GuiSounds() { }

    public static void playClick() {
        Minecraft.getMinecraft().getSoundHandler().playSound(
                PositionedSoundRecord.func_147674_a(CLICK, 1F));
    }
}
