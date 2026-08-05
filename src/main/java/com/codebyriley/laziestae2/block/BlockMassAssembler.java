package com.codebyriley.laziestae2.block;

import com.codebyriley.laziestae2.Constants;
import com.codebyriley.laziestae2.LaziestAE2;
import com.codebyriley.laziestae2.gui.GuiIds;
import com.codebyriley.laziestae2.init.ModCreativeTabs;
import com.codebyriley.laziestae2.inventory.InventoryDropHelper;
import com.codebyriley.laziestae2.tile.massassembler.MassAssemblerStructure;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerController;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerCraftingCoprocessor;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerFrame;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerIoPort;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerPart;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerPatternProvider;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerVent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockMassAssembler extends Block implements IMetadataBlock {

    public static final int FRAME = 0;
    public static final int VENT = 1;
    public static final int CONTROLLER = 2;
    public static final int PATTERN_PROVIDER = 3;
    public static final int CRAFTING_COPROCESSOR = 4;
    public static final int IO_PORT = 5;

    private static final String[] VARIANTS = {
            "frame",
            "vent",
            "controller",
            "module_pattern",
            "module_cpu",
            "io_port"
    };

    @SideOnly(Side.CLIENT)
    private IIcon[] icons;

    @SideOnly(Side.CLIENT)
    private IIcon[] activeIcons;

    public BlockMassAssembler() {
        super(Material.iron);
        setBlockName(Constants.MOD_ID + ".big_assembler");
        setCreativeTab(ModCreativeTabs.tab);
        setHardness(4.0F);
        setResistance(12.0F);
        setStepSound(soundTypeMetal);
    }

    @Override
    public int damageDropped(int metadata) {
        return metadata;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side,
            float hitX, float hitY, float hitZ) {
        if (player.isSneaking())
            return false;

        if (world.getBlockMetadata(x, y, z) == PATTERN_PROVIDER) {
            if (!world.isRemote)
                player.openGui(LaziestAE2.instance, GuiIds.PATTERN_PROVIDER, world, x, y, z);
            return true;
        }

        TileMassAssemblerController controller = MassAssemblerStructure.findConnectedController(world, x, y, z);
        if (controller == null)
            return false;

        if (!world.isRemote) {
            player.openGui(
                    LaziestAE2.instance,
                    GuiIds.BIG_ASSEMBLER,
                    world,
                    controller.xCoord,
                    controller.yCoord,
                    controller.zCoord);
        }

        return true;
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        super.onBlockAdded(world, x, y, z);
        MassAssemblerStructure.refreshConnectedController(world, x, y, z);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        super.onNeighborBlockChange(world, x, y, z, neighbor);
        MassAssemblerStructure.refreshConnectedController(world, x, y, z);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int metadata) {
        TileMassAssemblerController controller = MassAssemblerStructure.findConnectedController(world, x, y, z);

        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof net.minecraft.inventory.IInventory)
            InventoryDropHelper.dropInventoryItems(world, x, y, z, (net.minecraft.inventory.IInventory)tile);
        if (tile instanceof TileMassAssemblerController)
            InventoryDropHelper.dropStacks(world, x, y, z, ((TileMassAssemblerController)tile).getDropStacks());

        super.breakBlock(world, x, y, z, block, metadata);

        if (controller != null && !controller.isInvalid())
            controller.refreshStructure();
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return metadata >= 0 && metadata < VARIANTS.length;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        switch (metadata) {
            case FRAME:
                return new TileMassAssemblerFrame();
            case VENT:
                return new TileMassAssemblerVent();
            case CONTROLLER:
                return new TileMassAssemblerController();
            case PATTERN_PROVIDER:
                return new TileMassAssemblerPatternProvider();
            case CRAFTING_COPROCESSOR:
                return new TileMassAssemblerCraftingCoprocessor();
            case IO_PORT:
                return new TileMassAssemblerIoPort();
            default:
                return null;
        }
    }

    @Override
    public int getVariantCount() {
        return VARIANTS.length;
    }

    @Override
    public String getVariantName(int metadata) {
        return VARIANTS[MathHelper.clamp_int(metadata, 0, VARIANTS.length - 1)];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int metadata) {
        return icons[MathHelper.clamp_int(metadata, 0, VARIANTS.length - 1)];
    }

    /** In-world variant: parts light up once the multiblock is formed. */
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        int variant = MathHelper.clamp_int(world.getBlockMetadata(x, y, z), 0, VARIANTS.length - 1);
        return isActive(world, x, y, z) ? activeIcons[variant] : icons[variant];
    }

    @SideOnly(Side.CLIENT)
    private static boolean isActive(IBlockAccess world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);

        if (tile instanceof TileMassAssemblerController)
            return ((TileMassAssemblerController)tile).isFormed();

        return tile instanceof TileMassAssemblerPart && ((TileMassAssemblerPart)tile).isActive();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        icons = new IIcon[VARIANTS.length];
        activeIcons = new IIcon[VARIANTS.length];

        for (int i = 0; i < VARIANTS.length; i++) {
            icons[i] = register.registerIcon(Constants.MOD_ID + ":big_assembler/" + VARIANTS[i]);
            activeIcons[i] = register.registerIcon(Constants.MOD_ID + ":big_assembler/" + VARIANTS[i] + "_active");
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for (int i = 0; i < VARIANTS.length; i++) {
            list.add(new ItemStack(item, 1, i));
        }
    }
}
