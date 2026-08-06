package com.codebyriley.laziestae2.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockMetadata extends ItemBlock {

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

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        BlockTooltip.append(tooltip, getUnlocalizedName(stack));
    }
}
