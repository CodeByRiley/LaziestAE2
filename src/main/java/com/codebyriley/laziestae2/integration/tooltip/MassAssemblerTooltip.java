package com.codebyriley.laziestae2.integration.tooltip;

import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerController;
import com.codebyriley.laziestae2.tile.massassembler.TileMassAssemblerPart;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

/**
 * Structure and job state for the Mass Assembly Chamber.
 * <p>
 * The controller reports directly. Other parts follow the controller position
 * stamped on them during the last structure scan, so any block of the chamber
 * shows the same information without scanning the cluster again.
 */
public final class MassAssemblerTooltip {

    private static final String STRUCTURE_KEY_PREFIX = "container.laziestae2.big_assembler.";

    private MassAssemblerTooltip() {
    }

    public static boolean supports(TileEntity tile) {
        return tile instanceof TileMassAssemblerController || tile instanceof TileMassAssemblerPart;
    }

    public static void writeData(NBTTagCompound data, TileEntity tile) {
        NBTTagCompound tag = new NBTTagCompound();
        TileMassAssemblerController controller;

        if (tile instanceof TileMassAssemblerController) {
            controller = (TileMassAssemblerController)tile;
        } else if (tile instanceof TileMassAssemblerPart) {
            TileMassAssemblerPart part = (TileMassAssemblerPart)tile;
            tag.setBoolean("IsPart", true);
            tag.setBoolean("Active", part.isActive());
            controller = part.getController();
        } else {
            return;
        }

        if (controller != null) {
            writeControllerData(tag, controller);
        }

        data.setTag(TooltipKeys.MASS_ASSEMBLER, tag);
    }

    private static void writeControllerData(NBTTagCompound tag, TileMassAssemblerController controller) {
        tag.setBoolean("HasController", true);
        tag.setBoolean("Formed", controller.isFormed());
        tag.setBoolean("TooLarge", controller.isStructureTooLarge());
        tag.setInteger("Parts", controller.getPartCount());
        tag.setInteger("Frames", controller.getFrameCount());
        tag.setInteger("Vents", controller.getVentCount());
        tag.setInteger("Patterns", controller.getPatternProviderCount());
        tag.setInteger("Coprocessors", controller.getCraftingCoprocessorCount());
        tag.setInteger("IoPorts", controller.getIoPortCount());
        tag.setInteger("Jobs", controller.getJobCount());
        tag.setInteger("JobQueueSize", controller.getJobQueueSize());
        tag.setInteger("Work", controller.getWork());
        tag.setInteger("WorkPerJob", controller.getWorkPerJob());
        tag.setInteger("WorkRate", controller.getWorkRate());
        tag.setDouble("Energy", controller.getStoredEnergy());
        tag.setDouble("MaxEnergy", controller.getEnergyCapacity());
        tag.setBoolean("Connected", controller.isGridConnected());

        ItemStack activeJob = controller.getActiveJobResult();
        if (activeJob != null) {
            tag.setString("ActiveJob", activeJob.getDisplayName());
        }
    }

    public static boolean hasData(NBTTagCompound data) {
        return data.hasKey(TooltipKeys.MASS_ASSEMBLER);
    }

    public static NBTTagCompound read(NBTTagCompound data) {
        return data.getCompoundTag(TooltipKeys.MASS_ASSEMBLER);
    }

    public static boolean hasController(NBTTagCompound tag) {
        return tag.getBoolean("HasController");
    }

    public static boolean isFormed(NBTTagCompound tag) {
        return tag.getBoolean("Formed");
    }

    public static int getWorkPerJob(NBTTagCompound tag) {
        return tag.getInteger("WorkPerJob");
    }

    public static int getWork(NBTTagCompound tag) {
        return Math.min(tag.getInteger("Work"), Math.max(getWorkPerJob(tag), 0));
    }

    public static String getActiveJob(NBTTagCompound tag) {
        return tag.getString("ActiveJob");
    }

    /**
     * @param withProgress false for front ends that draw the job progress bar themselves.
     */
    public static void appendLines(NBTTagCompound tag, List<String> lines, boolean details, boolean withProgress) {
        if (!hasController(tag)) {
            // A part with no reachable controller, or details were not requested.
            if (tag.getBoolean("IsPart") && !tag.getBoolean("Active")) {
                lines.add(EnumChatFormatting.RED + structureText("incomplete"));
            }
            return;
        }

        appendStructureState(tag, lines);

        if (!isFormed(tag)) {
            appendExtraData(tag, lines);
            return;
        }

        String activeJob = getActiveJob(tag);
        if (withProgress && getWorkPerJob(tag) > 0 && !activeJob.isEmpty()) {
            lines.add(TooltipText.format("job_progress",
                    activeJob,
                    TooltipText.percent(getWork(tag), getWorkPerJob(tag))));
        }

        lines.add(TooltipText.format("energy",
                TooltipText.ae(tag.getDouble("Energy")),
                TooltipText.ae(tag.getDouble("MaxEnergy"))));

        if (!tag.getBoolean("Connected")) {
            lines.add(EnumChatFormatting.RED + TooltipText.translate("no_channel"));
        }

        if (details) {
            appendExtraData(tag, lines);
        }
    }

    private static void appendStructureState(NBTTagCompound tag, List<String> lines) {
        if (tag.getBoolean("TooLarge")) {
            lines.add(EnumChatFormatting.RED + structureText("too_large"));
        } else if (isFormed(tag)) {
            lines.add(structureText("formed"));
        } else {
            lines.add(EnumChatFormatting.RED + structureText("incomplete"));
        }
    }

    private static void appendExtraData(NBTTagCompound tag, List<String> lines) {
        lines.add(TooltipText.format("jobs", tag.getInteger("Jobs"), tag.getInteger("JobQueueSize")));
        lines.add(TooltipText.format("work_rate", tag.getInteger("WorkRate")));
        lines.add(TooltipText.format("coprocessors", tag.getInteger("Coprocessors")));
        lines.add(partCount("parts", tag.getInteger("Parts")));
        lines.add(partCount("frames", tag.getInteger("Frames")));
        lines.add(partCount("patterns", tag.getInteger("Patterns")));
        lines.add(partCount("cpus", tag.getInteger("Coprocessors")));
        lines.add(partCount("io_ports", tag.getInteger("IoPorts")));
    }

    private static String structureText(String key) {
        return StatCollector.translateToLocal(STRUCTURE_KEY_PREFIX + key);
    }

    private static String partCount(String key, int count) {
        return StatCollector.translateToLocalFormatted(STRUCTURE_KEY_PREFIX + key, count);
    }
}
