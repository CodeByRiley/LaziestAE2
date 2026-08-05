package com.codebyriley.laziestae2.block;

import appeng.facade.IFacadeItem;
import com.codebyriley.laziestae2.Constants;
import com.codebyriley.laziestae2.LaziestAE2;
import com.codebyriley.laziestae2.gui.MachineGuiDefinition;
import com.codebyriley.laziestae2.init.ModCreativeTabs;
import com.codebyriley.laziestae2.inventory.InventoryDropHelper;
import com.codebyriley.laziestae2.tile.machines.TileAggregator;
import com.codebyriley.laziestae2.tile.machines.TileCentrifuge;
import com.codebyriley.laziestae2.tile.machines.TileEnergizer;
import com.codebyriley.laziestae2.tile.machines.TileEtcher;
import com.codebyriley.laziestae2.tile.machines.TileFastCrafter;
import com.codebyriley.laziestae2.tile.base.ISideConfigurable;
import com.codebyriley.laziestae2.tile.base.TileMachine;
import com.codebyriley.laziestae2.tile.base.TilePowered;
import com.codebyriley.laziestae2.tile.machines.TileLevelMaintainer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockMachine extends Block implements IMetadataBlock {

    public static final int AGGREGATOR = 0;
    public static final int CENTRIFUGE = 1;
    public static final int ETCHER = 2;
    public static final int FAST_CRAFTER = 3;
    public static final int LEVEL_MAINTAINER = 4;
    public static final int ENERGIZER = 5;

    private static final String[] VARIANTS = {
            "aggregator",
            "centrifuge",
            "etcher",
            "fast_crafter",
            "level_maintainer",
            "energizer"
    };


    private static final int FRONT_SIDE = 3;

    @SideOnly(Side.CLIENT)
    private IIcon sideIcon;

    @SideOnly(Side.CLIENT)
    private IIcon[] frontIcons;

    @SideOnly(Side.CLIENT)
    private IIcon[] activeFrontIcons;

    public BlockMachine() {
        super(Material.iron);
        setBlockName(Constants.MOD_ID + ".machine");
        setCreativeTab(ModCreativeTabs.tab);
        setHardness(3.0F);
        setResistance(8.0F);
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

        MachineGuiDefinition definition = MachineGuiDefinition.byGuiId(world.getBlockMetadata(x, y, z));
        if (definition == null)
            return false;

        if (!world.isRemote)
            player.openGui(LaziestAE2.instance, definition.getGuiId(), world, x, y, z);

        return true;
    }

    /** Points the machine's front at the player who placed it. */
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, net.minecraft.entity.EntityLivingBase placer,
            ItemStack stack) {
        super.onBlockPlacedBy(world, x, y, z, placer, stack);

        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof ISideConfigurable))
            return;

        int rotation = MathHelper.floor_double((placer.rotationYaw * 4F / 360F) + 0.5D) & 3;
        // Rotation 0 means the player faces south, so the front looks back at them.
        int[] facings = { 2, 5, 3, 4 };
        ((ISideConfigurable)tile).setFacing(facings[rotation]);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int metadata) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof IInventory)
            InventoryDropHelper.dropInventoryItems(world, x, y, z, (IInventory)tile);

        super.breakBlock(world, x, y, z, block, metadata);
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return metadata >= 0 && metadata < VARIANTS.length;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        switch (metadata) {
            case AGGREGATOR:
                return new TileAggregator();
            case CENTRIFUGE:
                return new TileCentrifuge();
            case ETCHER:
                return new TileEtcher();
            case FAST_CRAFTER:
                return new TileFastCrafter();
            case LEVEL_MAINTAINER:
                return new TileLevelMaintainer();
            case ENERGIZER:
                return new TileEnergizer();
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
        if (side == FRONT_SIDE)
            return frontIcons[MathHelper.clamp_int(metadata, 0, VARIANTS.length - 1)];
        return sideIcon;
    }

    /** swaps the front face to the lit texture while the machine runs. */
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        TileEntity tile = world.getTileEntity(x, y, z);
        int facing = tile instanceof ISideConfigurable ? ((ISideConfigurable)tile).getFacing() : FRONT_SIDE;

        if (side != facing)
            return sideIcon;

        int variant = MathHelper.clamp_int(world.getBlockMetadata(x, y, z), 0, VARIANTS.length - 1);
        return isActive(world, x, y, z) ? activeFrontIcons[variant] : frontIcons[variant];
    }

    @SideOnly(Side.CLIENT)
    private static boolean isActive(IBlockAccess world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);

        if (tile instanceof TileMachine)
            return ((TileMachine)tile).isWorking();

        if (tile instanceof TilePowered) {
            // Network devices have no work cycle; light them up while grid-connected.
            return ((TilePowered)tile).isGridConnected();
        }

        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        sideIcon = register.registerIcon(Constants.MOD_ID + ":machine/machine_side");
        frontIcons = new IIcon[VARIANTS.length];
        activeFrontIcons = new IIcon[VARIANTS.length];

        for (int i = 0; i < VARIANTS.length; i++) {
            frontIcons[i] = register.registerIcon(Constants.MOD_ID + ":machine/" + VARIANTS[i]);
            activeFrontIcons[i] = register.registerIcon(Constants.MOD_ID + ":machine/" + VARIANTS[i] + "_active");
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "unchecked" })
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for (int i = 0; i < VARIANTS.length; i++) {
            list.add(new ItemStack(item, 1, i));
        }
    }
}
