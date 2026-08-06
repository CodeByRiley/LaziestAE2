package com.codebyriley.laziestae2.init;

import appeng.api.AEApi;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IMaterials;
import appeng.api.definitions.IParts;
import appeng.api.features.IInscriberRegistry;
import appeng.api.features.InscriberProcessType;
import appeng.api.util.AEColor;
import com.codebyriley.laziestae2.LaziestAE2;
import com.codebyriley.laziestae2.block.BlockMachine;
import com.codebyriley.laziestae2.block.AcceleratorTiles;
import com.codebyriley.laziestae2.block.BlockMassAssembler;
import com.codebyriley.laziestae2.integration.ae2.Ae2Fork;
import com.codebyriley.laziestae2.item.ItemLazyMaterial;
import com.codebyriley.laziestae2.recipe.ProcessingRecipeRegistry;
import com.google.common.base.Optional;
import cpw.mods.fml.common.registry.GameRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public final class ModRecipes {

    // Sky stone block metadata: 0 raw, 1 smooth, 2 brick, 3 small brick.
    private static final int SKY_STONE_SMOOTH_META = 1;

    private ModRecipes() { }

    public static void register() {
        ProcessingRecipeRegistry.initDefaults();
        registerSteelmaking();
        registerMaterialRecipes();
        registerMachineRecipes();
        registerMassAssemblerRecipes();
        registerAcceleratorRecipes();
    }

    /**
     * Four of the tier below, shapeless and with no catalyst — the same ladder
     * GTNH's fork uses, so a pack swapping between the two crafts identically.
     * Only registered on stock rv3, where the blocks themselves exist.
     */
    private static void registerAcceleratorRecipes() {
        if (ModBlocks.accelerators.isEmpty())
            return;

        ItemStack previous = Ae2Fork.getCraftingAccelerator(1);
        if (previous == null) {
            LaziestAE2.logger.warn("No AE2 crafting accelerator; skipping compressed accelerator recipes");
            return;
        }

        for (int tier : AcceleratorTiles.TIERS) {
            Block block = ModBlocks.accelerators.get(tier);
            if (block == null)
                continue;

            addShapeless(new ItemStack(block), previous, previous, previous, previous);
            previous = new ItemStack(block);
        }
    }

    /**
     * Iron ingot pressed with a Carbonic Fluix Complex and sky dust yields
     * Fluix-Plated Iron, which smelts into Fluix Steel.
     */
    private static void registerSteelmaking() {
        IInscriberRegistry inscriber = AEApi.instance().registries().inscriber();
        ItemStack skyDust = stack(materials().skyDust());

        if (skyDust != null) {
            inscriber.addRecipe(inscriber.builder()
                    .withProcessType(InscriberProcessType.Press)
                    .withInputs(Collections.singleton(new ItemStack(Items.iron_ingot)))
                    .withTopOptional(material(ItemLazyMaterial.CARBONIC_FLUIX_COMPLEX))
                    .withBottomOptional(skyDust)
                    .withOutput(material(ItemLazyMaterial.FLUIX_PLATED_IRON))
                    .build());
        }

        GameRegistry.addSmelting(
                material(ItemLazyMaterial.FLUIX_PLATED_IRON), material(ItemLazyMaterial.FLUIX_STEEL), 0F);
    }

    private static void registerMaterialRecipes() {
        // Carbonic Fluix Complex: 2 fluix dust + 2 coal dust + silicon.
        addShapeless(material(ItemLazyMaterial.CARBONIC_FLUIX_COMPLEX),
                "dustFluix", "dustFluix", "dustCoal", "dustCoal", "itemSilicon");

        // Fluix Logic Unit.
        addShaped(material(ItemLazyMaterial.FLUIX_LOGIC_UNIT),
                new String[] { "igi", "dpd", "igi" },
                'i', "ingotFluixSteel",
                'g', block(blocks().quartzGlass()),
                'd', material(ItemLazyMaterial.CARBONIC_FLUIX_COMPLEX),
                'p', stack(materials().engProcessor()));

        registerSpecCoreRecipes();
    }

    /** Each speculation core tier doubles the previous one with a better catalyst. */
    private static void registerSpecCoreRecipes() {
        addSpecCore(ItemLazyMaterial.SPEC_CORE, ItemLazyMaterial.SPEC_CORE_2, "dustRedstone");
        addSpecCore(ItemLazyMaterial.SPEC_CORE_2, ItemLazyMaterial.SPEC_CORE_4, "itemSilicon");
        addSpecCore(ItemLazyMaterial.SPEC_CORE_4, ItemLazyMaterial.SPEC_CORE_8, stack(materials().logicProcessor()));
        addSpecCore(ItemLazyMaterial.SPEC_CORE_8, ItemLazyMaterial.SPEC_CORE_16, stack(materials().calcProcessor()));
        addSpecCore(ItemLazyMaterial.SPEC_CORE_16, ItemLazyMaterial.SPEC_CORE_32, stack(materials().engProcessor()));
        addSpecCore(ItemLazyMaterial.SPEC_CORE_32, ItemLazyMaterial.SPEC_CORE_64,
                material(ItemLazyMaterial.PARALLEL_PROCESSOR));
    }

    private static void addSpecCore(int inputMeta, int outputMeta, Object catalyst) {
        addShaped(material(outputMeta),
                new String[] { "cpc" },
                'c', material(inputMeta),
                'p', catalyst);
    }

    private static void registerMachineRecipes() {
        IMaterials materials = materials();
        IBlocks blocks = blocks();

        // Fluix Aggregator.
        addShaped(machine(BlockMachine.AGGREGATOR),
                new String[] { "hhh", "rcr", "lxl" },
                'h', new ItemStack(Blocks.hopper),
                'r', "dustRedstone",
                'c', material(ItemLazyMaterial.FLUIX_LOGIC_UNIT),
                'l', stack(materials.logicProcessor()),
                'x', block(blocks.condenser()));

        // Pulse Centrifuge.
        addShaped(machine(BlockMachine.CENTRIFUGE),
                new String[] { "sas", "lcl", "sps" },
                's', "ingotFluixSteel",
                'a', block(blocks.molecularAssembler()),
                'l', stack(materials.logicProcessor()),
                'c', material(ItemLazyMaterial.FLUIX_LOGIC_UNIT),
                'p', material(ItemLazyMaterial.PARALLEL_PROCESSOR));

        // ME Circuit Etcher.
        addShaped(machine(BlockMachine.ETCHER),
                new String[] { "xiy", "gcg", "zfw" },
                'i', block(blocks.inscriber()),
                'g', block(blocks.quartzGlass()),
                'c', material(ItemLazyMaterial.FLUIX_LOGIC_UNIT),
                'f', stack(materials.formationCore()),
                'x', stack(materials.siliconPress()),
                'y', stack(materials.calcProcessorPress()),
                'z', stack(materials.logicProcessorPress()),
                'w', stack(materials.engProcessorPress()));

        // Preemptive Assembly Unit.
        addShaped(machine(BlockMachine.FAST_CRAFTER),
                new String[] { "sis", "xcx", "sps" },
                's', "ingotFluixSteel",
                'i', block(blocks.iface()),
                'x', block(blocks.craftingAccelerator()),
                'c', material(ItemLazyMaterial.FLUIX_LOGIC_UNIT),
                'p', material(ItemLazyMaterial.SPECULATIVE_PROCESSOR));

        // ME Level Maintainer.
        addShaped(machine(BlockMachine.LEVEL_MAINTAINER),
                new String[] { "ses", "pcp", "sks" },
                's', "ingotFluixSteel",
                'e', stack(parts().levelEmitter()),
                'p', stack(materials.logicProcessor()),
                'c', material(ItemLazyMaterial.FLUIX_LOGIC_UNIT),
                'k', stack(materials.cardCrafting()));

        // Crystal Energizer.
        addShaped(machine(BlockMachine.ENERGIZER),
                new String[] { "sis", "fcf", "sbs" },
                's', "ingotFluixSteel",
                'i', block(blocks.charger()),
                'f', stack(parts().quartzFiber()),
                'c', material(ItemLazyMaterial.FLUIX_LOGIC_UNIT),
                'b', block(blocks.energyCellDense()));
    }

    private static void registerMassAssemblerRecipes() {
        IBlocks blocks = blocks();
        ItemStack frame = massAssembler(BlockMassAssembler.FRAME);
        ItemStack fluixCable = fluixGlassCable();

        // Frames come four at a time.
        addShaped(massAssembler(BlockMassAssembler.FRAME, 4),
                new String[] { "FSF", "STS", "FSF" },
                'F', "ingotFluixSteel",
                'S', smoothSkyStone(),
                'T', new ItemStack(Blocks.crafting_table));

        addShaped(massAssembler(BlockMassAssembler.VENT),
                new String[] { " B ", "BFB", " B " },
                'B', new ItemStack(Blocks.iron_bars),
                'F', frame);

        addShaped(massAssembler(BlockMassAssembler.CONTROLLER),
                new String[] { "FAF", "CUC", "FIF" },
                'F', frame,
                'A', block(blocks.molecularAssembler()),
                'C', fluixCable,
                'U', material(ItemLazyMaterial.FLUIX_LOGIC_UNIT),
                'I', block(blocks.iface()));

        addShaped(massAssembler(BlockMassAssembler.PATTERN_PROVIDER),
                new String[] { "FSF", "CPC", "FIF" },
                'F', frame,
                'S', stack(materials().cell1kPart()),
                'C', fluixCable,
                'P', stack(materials().engProcessor()),
                'I', block(blocks.iface()));

        addShaped(massAssembler(BlockMassAssembler.CRAFTING_COPROCESSOR),
                new String[] { "FAF", "CPC", "FXF" },
                'F', frame,
                'A', block(blocks.molecularAssembler()),
                'C', fluixCable,
                'P', material(ItemLazyMaterial.PARALLEL_PROCESSOR),
                'X', block(blocks.craftingAccelerator()));

        addShaped(massAssembler(BlockMassAssembler.IO_PORT),
                new String[] { "FHF", "CPC", "FBF" },
                'F', frame,
                'H', new ItemStack(Blocks.hopper),
                'C', fluixCable,
                'P', block(blocks.iOPort()),
                'B', new ItemStack(Blocks.chest));

        registerCoprocessorTierRecipes();
    }

    /**
     * Coprocessor tiers follow AE2's storage ladder, quadrupling each step. Each
     * one fuses four of the tier below with a catalyst, so a 256x costs 256 base
     * units and every catalyst below it.
     *
     * On GTNH's AE2 Unofficial the catalyst is that fork's matching compressed
     * crafting accelerator, mirroring the base coprocessor's own recipe. Stock
     * rv3 has no such block, so there we climb our own speculation core ladder.
     */
    private static void registerCoprocessorTierRecipes() {
        addCoprocessorTier(BlockMassAssembler.CRAFTING_COPROCESSOR,
                BlockMassAssembler.CRAFTING_COPROCESSOR_4, 4, ItemLazyMaterial.SPEC_CORE_4);
        addCoprocessorTier(BlockMassAssembler.CRAFTING_COPROCESSOR_4,
                BlockMassAssembler.CRAFTING_COPROCESSOR_16, 16, ItemLazyMaterial.SPEC_CORE_16);
        addCoprocessorTier(BlockMassAssembler.CRAFTING_COPROCESSOR_16,
                BlockMassAssembler.CRAFTING_COPROCESSOR_64, 64, ItemLazyMaterial.SPEC_CORE_64);

        // Our core ladder stops at 64, so the top tier falls back to the processor.
        addCoprocessorTier(BlockMassAssembler.CRAFTING_COPROCESSOR_64,
                BlockMassAssembler.CRAFTING_COPROCESSOR_256, 256, ItemLazyMaterial.SPECULATIVE_PROCESSOR);
    }

    private static void addCoprocessorTier(int inputMeta, int outputMeta, int multiplier, int fallbackCoreMeta) {
        ItemStack accelerator = Ae2Fork.getCraftingAccelerator(multiplier);
        Object catalyst = accelerator != null ? accelerator : material(fallbackCoreMeta);

        addShaped(massAssembler(outputMeta),
                new String[] { " c ", "cpc", " c " },
                'c', massAssembler(inputMeta),
                'p', catalyst);
    }

    /**
     * Registers a shaped recipe, skipping it if any ingredient is unavailable
     * (an AE2 feature may be disabled in its config).
     */
    private static void addShaped(ItemStack output, String[] pattern, Object... keyed) {
        if (output == null)
            return;

        List<Object> args = new ArrayList<Object>();
        Collections.addAll(args, (Object[])pattern);

        for (int i = 0; i + 1 < keyed.length; i += 2) {
            Object ingredient = keyed[i + 1];
            if (ingredient == null) {
                logSkipped(output);
                return;
            }

            args.add(keyed[i]);
            args.add(ingredient);
        }

        GameRegistry.addRecipe(new ShapedOreRecipe(output, args.toArray()));
    }

    private static void addShapeless(ItemStack output, Object... ingredients) {
        if (output == null)
            return;

        for (Object ingredient : ingredients) {
            if (ingredient == null) {
                logSkipped(output);
                return;
            }
        }

        GameRegistry.addRecipe(new ShapelessOreRecipe(output, ingredients));
    }

    private static void logSkipped(ItemStack output) {
        LaziestAE2.logger.warn("Skipping recipe for {}: a required ingredient is unavailable",
                output.getDisplayName());
    }

    private static IMaterials materials() {
        return AEApi.instance().definitions().materials();
    }

    private static IBlocks blocks() {
        return AEApi.instance().definitions().blocks();
    }

    private static IParts parts() {
        return AEApi.instance().definitions().parts();
    }

    private static ItemStack stack(IItemDefinition definition) {
        Optional<ItemStack> stack = definition.maybeStack(1);
        return stack.isPresent() ? stack.get() : null;
    }

    private static ItemStack block(IItemDefinition definition) {
        return stack(definition);
    }

    private static ItemStack smoothSkyStone() {
        Optional<Block> skyStone = blocks().skyStone().maybeBlock();
        return skyStone.isPresent() ? new ItemStack(skyStone.get(), 1, SKY_STONE_SMOOTH_META) : null;
    }

    private static ItemStack fluixGlassCable() {
        return parts().cableGlass().stack(AEColor.Transparent, 1);
    }

    private static ItemStack material(int metadata) {
        return new ItemStack(ModItems.material, 1, metadata);
    }

    private static ItemStack machine(int metadata) {
        return new ItemStack(ModBlocks.machine, 1, metadata);
    }

    private static ItemStack massAssembler(int metadata) {
        return massAssembler(metadata, 1);
    }

    private static ItemStack massAssembler(int metadata, int amount) {
        return new ItemStack(ModBlocks.bigAssembler, amount, metadata);
    }
}
