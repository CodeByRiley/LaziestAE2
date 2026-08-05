package com.codebyriley.laziestae2;

import com.codebyriley.laziestae2.block.BlockMachine;
import com.codebyriley.laziestae2.block.BlockMassAssembler;
import com.codebyriley.laziestae2.config.LaziestConfig;
import com.codebyriley.laziestae2.init.ModBlocks;
import com.codebyriley.laziestae2.init.ModCreativeTabs;
import com.codebyriley.laziestae2.init.ModItems;
import com.codebyriley.laziestae2.init.ModRecipes;
import com.codebyriley.laziestae2.init.ModTiles;
import com.codebyriley.laziestae2.proxy.CommonProxy;
import com.codebyriley.laziestae2.tile.base.TileMachine;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(
        modid = Constants.MOD_ID,
        name = Constants.NAME,
        version = Constants.VERSION,
        acceptedMinecraftVersions = "[1.7.10]",
        dependencies = "required-after:appliedenergistics2;after:NotEnoughItems;after:wdmla",
        guiFactory = "com.codebyriley.laziestae2.client.config.GuiFactory"
)
public class LaziestAE2 {

    @Instance(Constants.MOD_ID)
    public static LaziestAE2 instance;

    @SidedProxy(clientSide = Constants.CLIENT_PROXY, serverSide = Constants.SERVER_PROXY)
    public static CommonProxy proxy;

    public static Logger logger = LogManager.getLogger("LaziestAE2");

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LaziestConfig.init(event.getModConfigurationDirectory().toString());
        FMLCommonHandler.instance().bus().register(new LaziestConfig());
        ModCreativeTabs.init();
        ModItems.register();
        ModBlocks.register();
        ModTiles.register();
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        ModRecipes.register();
        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

}
