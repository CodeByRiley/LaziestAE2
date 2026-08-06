package com.codebyriley.laziestae2.block;

import appeng.block.crafting.BlockCraftingUnit;
import com.codebyriley.laziestae2.Constants;
import com.codebyriley.laziestae2.init.ModCreativeTabs;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

/**
 * Crafting accelerator worth several co-processors, for stock AE2 rv3 — GTNH's
 * fork has its own and these are not registered there.
 *
 * Extends AE2's own crafting unit block so the cluster's connected-texture
 * renderer applies unchanged: it picks edge and corner pieces per neighbour and
 * composites our icon over a solid base, which is why the formed texture is a
 * sparse overlay rather than a whole face. GTNH's BlockAdvancedCraftingUnit
 * takes exactly this route, overriding nothing but the icon.
 *
 * AE2 keeps only bits 0-1 of the metadata — bit 2 is powered, bit 3 formed — so
 * a block holds at most four tiers. Rather than pack them, each tier is its own
 * block and leaves the metadata entirely to AE2.
 */
public class BlockCompressedAccelerator extends BlockCraftingUnit {

    /** AE2 sets this in the metadata once the tile belongs to a formed CPU. */
    private static final int FLAG_FORMED = 8;

    private final int coProcessors;
    private final String texture;

    @SideOnly(Side.CLIENT)
    private IIcon icon;

    /** Overlay AE2's renderer draws over the solid base while the CPU is formed. */
    @SideOnly(Side.CLIENT)
    private IIcon formedIcon;

    public BlockCompressedAccelerator(int coProcessors, Class<? extends TileEntity> tile) {
        this.coProcessors = coProcessors;
        this.texture = "processors/" + coProcessors + "x";

        setTileEntity(tile);
        setBlockName(Constants.MOD_ID + ".accelerator_" + coProcessors + "x");
        setCreativeTab(ModCreativeTabs.tab);
    }

    public int getCoProcessors() {
        return coProcessors;
    }

    /** Metadata is AE2's state, not a variant, so a broken block always drops the plain item. */
    @Override
    public int damageDropped(int metadata) {
        return 0;
    }

    /**
     * One item per tier. The inherited version lists two, having no reason to
     * know its metadata is a tier here rather than AE2's unit/accelerator pair.
     */
    @Override
    public void getCheckedSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list) {
        list.add(new ItemStack(this, 1, 0));
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return getUnlocalizedName();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int direction, int metadata) {
        return (metadata & FLAG_FORMED) != 0 ? formedIcon : icon;
    }

    /** Drives AE2's own icon registration, which fills in the render info its renderer reads. */
    @Override
    public String getTextureName() {
        return Constants.MOD_ID + ":" + texture;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        // Populates AE2's BlockRenderInfo from getTextureName; the renderer needs it.
        super.registerBlockIcons(register);

        icon = register.registerIcon(getTextureName());
        formedIcon = register.registerIcon(getTextureName() + "_fit");
    }
}
