package com.codebyriley.laziestae2.init;

import com.codebyriley.laziestae2.Constants;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public final class ModCreativeTabs {

    public static CreativeTabs tab;

    private ModCreativeTabs() { }

    public static void init() {
        tab = new CreativeTabs(Constants.MOD_ID) {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {
                return ModItems.material;
            }
        };
    }
}
