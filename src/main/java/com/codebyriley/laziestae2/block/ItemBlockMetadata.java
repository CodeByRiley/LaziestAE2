package com.codebyriley.laziestae2.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

public class ItemBlockMetadata extends ItemBlock {

    private static final int MAX_TOOLTIP_LINES = 8;

    private final IMetadataBlock metadataBlock;

    public ItemBlockMetadata(Block block) {
        super(block);
        setHasSubtypes(true);
        setMaxDamage(0);
        metadataBlock = (IMetadataBlock)block;
    }

    @Override
    public int getMetadata(int metadata) {
        return metadata;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return field_150939_a.getUnlocalizedName() + "." + metadataBlock.getVariantName(stack.getItemDamage());
    }

    /**
     * Appends "&lt;unlocalized name&gt;.tooltip" if present, followed by any
     * numbered continuation lines ("...tooltip.2", "...tooltip.3", and so on).
     */
    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);

        String base = getUnlocalizedName(stack) + ".tooltip";
        String first = translateOrNull(base);
        if (first == null) {
            return;
        }

        tooltip.add(EnumChatFormatting.GRAY + first);

        for (int line = 2; line <= MAX_TOOLTIP_LINES; line++) {
            String extra = translateOrNull(base + "." + line);
            if (extra == null) {
                break;
            }

            tooltip.add(EnumChatFormatting.GRAY + extra);
        }
    }

    private static String translateOrNull(String key) {
        String translated = StatCollector.translateToLocal(key);
        return translated == null || translated.equals(key) ? null : translated;
    }
}
