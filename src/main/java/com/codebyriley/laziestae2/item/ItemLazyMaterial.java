package com.codebyriley.laziestae2.item;

import appeng.items.materials.MaterialType;
import com.codebyriley.laziestae2.Constants;
import com.codebyriley.laziestae2.init.ModCreativeTabs;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;

public class ItemLazyMaterial extends Item {

    public static final int FLUIX_STEEL = 0;
    public static final int CARBONIC_FLUIX_COMPLEX = 1;
    public static final int FLUIX_PLATED_IRON = 2;
    public static final int COAL_DUST = 3;
    public static final int FLUIX_LOGIC_UNIT = 4;
    public static final int RESONATING_CRYSTAL = 5;
    public static final int PARALLEL_PROCESSOR = 6;
    public static final int SPEC_CORE = 7;
    public static final int SPEC_CORE_2 = 8;
    public static final int SPEC_CORE_4 = 9;
    public static final int SPEC_CORE_8 = 10;
    public static final int SPEC_CORE_16 = 11;
    public static final int SPEC_CORE_32 = 12;
    public static final int SPEC_CORE_64 = 13;
    public static final int SPECULATIVE_PROCESSOR = 14;

    private static final String[] VARIANTS = {
            "fluix_steel",
            "steel_process_dust",
            "steel_process_ingot",
            "coal_dust",
            "machine_core",
            "space_gem",
            "parallel_processor",
            "spec_core",
            "spec_core_2",
            "spec_core_4",
            "spec_core_8",
            "spec_core_16",
            "spec_core_32",
            "spec_core_64",
            "spec_processor"
    };

    // Texture paths; spec core tiers live in the spec_core subfolder named by multiplier.
    private static final String[] TEXTURES = {
            "fluix_steel",
            "steel_process_dust",
            "steel_process_ingot",
            "coal_dust",
            "machine_core",
            "space_gem",
            "parallel_processor",
            "spec_core/1",
            "spec_core/2",
            "spec_core/4",
            "spec_core/8",
            "spec_core/16",
            "spec_core/32",
            "spec_core/64",
            "spec_processor"
    };

    @SideOnly(Side.CLIENT)
    private IIcon[] icons;

    public ItemLazyMaterial() {
        setHasSubtypes(true);
        setMaxDamage(0);
        setCreativeTab(ModCreativeTabs.tab);
        setUnlocalizedName(Constants.MOD_ID + ".material");
        setTextureName(Constants.MOD_ID + ":" + VARIANTS[0]);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return "item." + Constants.MOD_ID + ".material." + getVariantName(stack.getItemDamage());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int damage) {
        return icons[MathHelper.clamp_int(damage, 0, VARIANTS.length - 1)];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        icons = new IIcon[TEXTURES.length];
        for (int i = 0; i < TEXTURES.length; i++) {
            icons[i] = register.registerIcon(Constants.MOD_ID + ":" + TEXTURES[i]);
        }
    }
    /** Mirrors the block tooltip scheme: "&lt;unlocalized name&gt;" plus numbered lines. */
    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, net.minecraft.entity.player.EntityPlayer player, List tooltip,
            boolean advanced) {
        String base = getUnlocalizedName(stack);
        String first = translateOrNull(base);
        if (first == null) {
            return;
        }

        tooltip.add(net.minecraft.util.EnumChatFormatting.GRAY + first);

        for (int line = 2; line <= 8; line++) {
            String extra = translateOrNull(base + "." + line);
            if (extra == null) {
                break;
            }

            tooltip.add(net.minecraft.util.EnumChatFormatting.GRAY + extra);
        }
    }

    private static String translateOrNull(String key) {
        String translated = net.minecraft.util.StatCollector.translateToLocal(key);
        return translated == null || translated.equals(key) ? null : translated;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        for (int i = 0; i < VARIANTS.length; i++) {
            list.add(new ItemStack(item, 1, i));
        }
    }

    public static String getVariantName(int metadata) {
        return VARIANTS[MathHelper.clamp_int(metadata, 0, VARIANTS.length - 1)];
    }
}
