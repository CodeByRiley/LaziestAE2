package com.codebyriley.laziestae2.config;

import com.codebyriley.laziestae2.Constants;
import com.codebyriley.laziestae2.LaziestAE2;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class LaziestConfig {
    public static Configuration config;

    public static final String CATEGORY_GENERAL = "general";
    public static final String CATEGORY_MACHINES = "machines";
    public static final String CATEGORY_AGGREGATOR = "machines.aggregator";
    public static final String CATEGORY_CENTRIFUGE = "machines.centrifuge";
    public static final String CATEGORY_ENERGISER = "machines.energiser";
    public static final String CATEGORY_ETCHER = "machines.etcher";
    public static final String CATEGORY_FAST_CRAFTER = "machines.fast_crafter";
    public static final String CATEGORY_MASS_ASSEMBLER = "machines.mass_assembler";
    public static final String CATEGORY_LEVEL_MAINTAINER = "machines.level_maintainer";

    public static double networkTransferPerTick = 100000D;

    // Values (defaults are the same as your previous fields)
    // Fluix Aggregator
    public static int aggregatorEnergyBuffer = 100000;
    public static int aggregatorEnergyCostBase = 8100;
    public static int aggregatorEnergyCostUpgrade = 863;
    public static int aggregatorWorkTicksBase = 150;
    public static int aggregatorWorkTicksUpgrade = 18;

    // Pulse Centrifuge
    public static int centrifugeEnergyBuffer = 100000;
    public static int centrifugeEnergyCostBase = 8100;
    public static int centrifugeEnergyCostUpgrade = 863;
    public static int centrifugeWorkTicksBase = 150;
    public static int centrifugeWorkTicksUpgrade = 18;

    // Circuit Etcher
    public static int etcherEnergyBuffer = 100000;
    public static int etcherEnergyCostBase = 8100;
    public static int etcherEnergyCostUpgrade = 863;
    public static int etcherWorkTicksBase = 150;
    public static int etcherWorkTicksUpgrade = 18;

    // Crystal Energizer
    public static int energizerEnergyBuffer = 100000;
    public static int energizerEnergyCostUpgrade = 1625;
    public static int energizerWorkTicksBase = 150;
    public static int energizerWorkTicksUpgrade = 18;

    // Preemptive Assembly / fast crafter
    public static double fastCrafterIdlePower = 6D;
    // Whether the assembly unit pulls ingredients for a crafting job's remaining
    // iterations ahead of time. Requires reaching into AE2 internals.
    public static boolean fastCrafterPreemptiveBatching = true;

    // Level Maintainer
    public static double levelMaintainerIdlePower = 3D;
    // The minimum interval between work ticks for the level maintainer.
    // The level maintainer will gradually increase its work rate while running without obstruction.
    // Setting this too low may cause lag!
    public static int levelMaintainerSleepMin = 12;
    // The maximum interval between work ticks for the level maintainer.
    // The level maintainer will gradually reduce its work rate when something prevents it from progressing.
    // Setting this too low may cause lag!
    public static int levelMaintainerSleepMax = 200;

    // Mass Assembler / chamber
    public static double idlePower = 3D;
    // The size of the mass assembler's crafting job queue.
    // Some crafting job data may be lost if this is decreased!
    public static int jobQueueSize = 64;
    // The amount of work needed to complete one crafting job.
    public static int workPerJob = 16;
    // The base amount of energy consumed to perform one unit of work.
    public static double energyPerWorkBase = 16D;
    // The additional energy consumed per unit of work for each installed coprocessor.
    public static double energyPerWorkUpgrade = 1D;
    // The base amount of work performed per tick.
    // If set to zero, the mass assembler will not do any work without a coprocessor installed.
    public static int workPerTickBase = 1;
    // The additional work performed per tick for each installed coprocessor.
    public static int workPerTickUpgrade = 3;

    public static boolean enableNeiIntegration = true;

    public static void init(String path) {
        if (config == null) {
            File file = new File(path + "/" + Constants.MOD_ID + ".cfg");
            config = new Configuration(file);
            load(config);
        }
    }

    public static void load(Configuration config) {
        try {
            enableNeiIntegration = config.get(CATEGORY_GENERAL, "enableNeiIntegration", true).getBoolean();

            networkTransferPerTick = config.get(CATEGORY_GENERAL, "networkTransferPerTick", 100000D,
                    "The maximum amount of AE each machine may draw from the ME network per tick.\n"
                            + "Lower values throttle machines running with many acceleration cards.\n"
                            + "Set to 0 for no limit."
            ).getDouble();

            // Aggregator
            aggregatorEnergyBuffer = config.get(CATEGORY_AGGREGATOR, "aggregatorEnergyBuffer", 100000,
                    "The size of the fluix aggregator's energy buffer. Values below 1 are treated as 1."
            ).getInt();
            aggregatorEnergyCostBase = config.get(CATEGORY_AGGREGATOR, "aggregatorEnergyCostBase", 8100,
                    "The base energy cost for each fluix aggregation operation performed. Values below 1 are treated as 1."
            ).getInt();
            aggregatorEnergyCostUpgrade = config.get(CATEGORY_AGGREGATOR, "aggregatorEnergyCostUpgrade", 863,
                    "The additional energy cost for fluix aggregation incurred by each acceleration card. Values below 0 are treated as 0."
            ).getInt();
            aggregatorWorkTicksBase = config.get(CATEGORY_AGGREGATOR, "aggregatorWorkTicksBase", 150,
                    "The base number of ticks needed to complete one fluix aggregation operation. Values below 1 are treated as 1."
            ).getInt();
            aggregatorWorkTicksUpgrade = config.get(CATEGORY_AGGREGATOR, "aggregatorWorkTicksUpgrade", 18,
                    "The number of ticks by which each acceleration card hastens a fluix aggregation operation. Values below 1 are treated as 1."
            ).getInt();

            // Centrifuge
            centrifugeEnergyBuffer = config.get(CATEGORY_CENTRIFUGE, "centrifugeEnergyBuffer", 100000,
                    "The size of the pulse centrifuge's energy buffer. Values below 1 are treated as 1."
            ).getInt();
            centrifugeEnergyCostBase = config.get(CATEGORY_CENTRIFUGE, "centrifugeEnergyCostBase", 8100,
                    "The base energy cost for each centrifuging operation performed. Values below 1 are treated as 1."
            ).getInt();
            centrifugeEnergyCostUpgrade = config.get(CATEGORY_CENTRIFUGE, "centrifugeEnergyCostUpgrade", 863,
                    "The additional energy cost for centrifuging incurred by each acceleration card. Values below 0 are treated as 0."
            ).getInt();
            centrifugeWorkTicksBase = config.get(CATEGORY_CENTRIFUGE, "centrifugeWorkTicksBase", 150,
                    "The base number of ticks needed to complete one centrifuging operation. Values below 1 are treated as 1."
            ).getInt();
            centrifugeWorkTicksUpgrade = config.get(CATEGORY_CENTRIFUGE, "centrifugeWorkTicksUpgrade", 18,
                    "The number of ticks by which each acceleration card hastens a centrifuging operation. Values below 1 are treated as 1."
            ).getInt();

            // Etcher
            etcherEnergyBuffer = config.get(CATEGORY_ETCHER, "etcherEnergyBuffer", 100000,
                    "The size of the circuit etcher's energy buffer. Values below 1 are treated as 1."
            ).getInt();
            etcherEnergyCostBase = config.get(CATEGORY_ETCHER, "etcherEnergyCostBase", 8100,
                    "The base energy cost for each circuit etching operation performed. Values below 1 are treated as 1."
            ).getInt();
            etcherEnergyCostUpgrade = config.get(CATEGORY_ETCHER, "etcherEnergyCostUpgrade", 863,
                    "The additional energy cost for circuit etching incurred by each acceleration card. Values below 0 are treated as 0."
            ).getInt();
            etcherWorkTicksBase = config.get(CATEGORY_ETCHER, "etcherWorkTicksBase", 150,
                    "The base number of ticks needed to complete one circuit etching operation. Values below 1 are treated as 1."
            ).getInt();
            etcherWorkTicksUpgrade = config.get(CATEGORY_ETCHER, "etcherWorkTicksUpgrade", 18,
                    "The number of ticks by which each acceleration card hastens a circuit etching operation. Values below 1 are treated as 1."
            ).getInt();

            // Energiser
            energizerEnergyBuffer = config.get(CATEGORY_ENERGISER, "energizerEnergyBuffer", 100000,
                    "The size of the crystal energizer's energy buffer. Values below 1 are treated as 1."
            ).getInt();
            energizerEnergyCostUpgrade = config.get(CATEGORY_ENERGISER, "energizerEnergyCostUpgrade", 1625,
                    "The additional energy cost for crystal energization incurred by each acceleration card. Values below 0 are treated as 0."
            ).getInt();
            energizerWorkTicksBase = config.get(CATEGORY_ENERGISER, "energizerWorkTicksBase", 150,
                    "The base number of ticks needed to complete one crystal energization operation. Values below 1 are treated as 1."
            ).getInt();
            energizerWorkTicksUpgrade = config.get(CATEGORY_ENERGISER, "energizerWorkTicksUpgrade", 18,
                    "The number of ticks by which each acceleration card hastens a crystal energization operation. Values below 1 are treated as 1."
            ).getInt();

            // Network devices (doubles)
            fastCrafterIdlePower = config.get(CATEGORY_FAST_CRAFTER, "fastCrafterIdlePower", 6D,
                    "The idle power consumption of the preemptive assembly unit. Values below 0 are treated as 0."
            ).getDouble();

            fastCrafterPreemptiveBatching = config.get(CATEGORY_FAST_CRAFTER, "preemptiveBatching", true,
                    "Whether the preemptive assembly unit pulls ingredients for a crafting job's\n"
                            + "remaining iterations ahead of time instead of one craft at a time.\n"
                            + "This reaches into Applied Energistics internals; disable it if another\n"
                            + "mod conflicts or a future AE2 build changes them."
            ).getBoolean();

            levelMaintainerIdlePower = config.get(CATEGORY_LEVEL_MAINTAINER, "levelMaintainerIdlePower", 3D,
                    "The idle power consumption of the level maintainer. Values below 0 are treated as 0."
            ).getDouble();

            levelMaintainerSleepMin = config.get(CATEGORY_LEVEL_MAINTAINER, "levelMaintainerSleepMin", 12,
                    "The minimum interval between work ticks for the level maintainer.\n" +
                            "The level maintainer will gradually increase its work rate while running without obstruction.\n" +
                            "Setting this too low may cause lag! Values below 0 are treated as 0."
            ).getInt();

            levelMaintainerSleepMax = config.get(CATEGORY_LEVEL_MAINTAINER, "levelMaintainerSleepMax", 200,
                    "The maximum interval between work ticks for the level maintainer.\n" +
                            "The level maintainer will gradually reduce its work rate when something prevents it from progressing.\n" +
                            "Setting this too low may cause lag! Values below 0 are treated as 0."
            ).getInt();

            // Mass assembler
            idlePower = config.get(CATEGORY_MASS_ASSEMBLER, "idlePower", 3D,
                    "The idle power consumption of the mass assembly chamber. Values below 0 are treated as 0."
            ).getDouble();

            jobQueueSize = config.get(CATEGORY_MASS_ASSEMBLER, "jobQueueSize", 64,
                    "The size of the mass assembler's crafting job queue.\nSome crafting job data may be lost if this is decreased!\nValues below 1 are treated as 1."
            ).getInt();

            workPerJob = config.get(CATEGORY_MASS_ASSEMBLER, "workPerJob", 16,
                    "The amount of work needed to complete one crafting job. Values below 1 are treated as 1."
            ).getInt();

            energyPerWorkBase = config.get(CATEGORY_MASS_ASSEMBLER, "energyPerWorkBase", 16D,
                    "The base amount of energy consumed to perform one unit of work. Values below 0 are treated as 0."
            ).getDouble();

            energyPerWorkUpgrade = config.get(CATEGORY_MASS_ASSEMBLER, "energyPerWorkUpgrade", 1D,
                    "The additional energy consumed per unit of work for each installed coprocessor. Values below 0 are treated as 0."
            ).getDouble();

            workPerTickBase = config.get(CATEGORY_MASS_ASSEMBLER, "workPerTickBase", 1,
                    "The base amount of work performed per tick.\nIf set to zero, the mass assembler will not do any work without a coprocessor installed.\nValues below 0 are treated as 0."
            ).getInt();

            workPerTickUpgrade = config.get(CATEGORY_MASS_ASSEMBLER, "workPerTickUpgrade", 3,
                    "The additional work performed per tick for each installed coprocessor. Values below 1 are treated as 1."
            ).getInt();
        } catch (Exception e) {
            LaziestAE2.logger.error("Failed to load config file", e);
        } finally {
            if (config.hasChanged())
                config.save();
        }
    }

    public static Configuration getConfig() {
        return config;
    }

    @SubscribeEvent
    public void onConfigurationChangeEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equalsIgnoreCase(Constants.MOD_ID)) {
            load(config);
            if (config.hasChanged())
                config.save();
        }
    }
}
