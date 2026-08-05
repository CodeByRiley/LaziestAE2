package com.codebyriley.laziestae2.client.config;

import com.codebyriley.laziestae2.Constants;
import com.codebyriley.laziestae2.LaziestAE2;
import com.codebyriley.laziestae2.config.LaziestConfig;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;

import java.util.ArrayList;
import java.util.List;

public class LaziestAE2ConfigGui extends GuiConfig {
    public LaziestAE2ConfigGui(GuiScreen screen) {
        super(screen,
                getConfigElements(),
                Constants.MOD_ID, false, false,
                GuiConfig.getAbridgedConfigPath(LaziestConfig.getConfig().getConfigFile().getAbsolutePath()));
    }

    private static List<IConfigElement> getConfigElements() {

        final List<IConfigElement> elements = new ArrayList<IConfigElement>();

        for (final String cat : LaziestConfig.getConfig().getCategoryNames()) {
            final ConfigCategory cc = LaziestConfig.getConfig().getCategory(cat);

            if (cc.isChild())
                continue;

            final ConfigElement ce = new ConfigElement(cc);
            elements.add(ce);
        }
        return elements;
    }
}
