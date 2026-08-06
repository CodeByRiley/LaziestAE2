package com.codebyriley.laziestae2.block;

import appeng.block.AEBaseItemBlock;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * AE2's item block, plus this mod's tooltip convention. AE2 makes
 * {@code addInformation} final and routes it through {@code addCheckedInformation},
 * so the extra lines go on there.
 */
public class ItemBlockCompressedAccelerator extends AEBaseItemBlock {

    public ItemBlockCompressedAccelerator(Block block) {
        super(block);
    }

    @Override
    public void addCheckedInformation(ItemStack stack, EntityPlayer player, List<String> tooltip,
            boolean advanced) {
        super.addCheckedInformation(stack, player, tooltip, advanced);
        BlockTooltip.append(tooltip, field_150939_a.getUnlocalizedName());
    }
}
