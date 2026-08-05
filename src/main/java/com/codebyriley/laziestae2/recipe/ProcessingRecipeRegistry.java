package com.codebyriley.laziestae2.recipe;

import appeng.api.AEApi;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IMaterials;
import com.codebyriley.laziestae2.init.ModItems;
import com.codebyriley.laziestae2.item.ItemLazyMaterial;
import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public final class ProcessingRecipeRegistry {

    private static final List<AggregatorRecipe> aggregatorRecipes = new ArrayList<AggregatorRecipe>();
    private static final List<CentrifugeRecipe> centrifugeRecipes = new ArrayList<CentrifugeRecipe>();
    private static final List<EtcherRecipe> etcherRecipes = new ArrayList<EtcherRecipe>();
    private static final List<EnergizerRecipe> energizerRecipes = new ArrayList<EnergizerRecipe>();

    private ProcessingRecipeRegistry() { }

    public static void initDefaults() {
        aggregatorRecipes.clear();
        centrifugeRecipes.clear();
        etcherRecipes.clear();
        energizerRecipes.clear();

        registerDefaultAggregatorRecipes();
        registerDefaultCentrifugeRecipes();
        registerDefaultEtcherRecipes();
        registerDefaultEnergizerRecipes();
    }

    public static List<AggregatorRecipe> getAggregatorRecipes() {
        return Collections.unmodifiableList(aggregatorRecipes);
    }

    public static void addAggregatorRecipe(AggregatorRecipe recipe) {
        if (recipe == null)
            throw new IllegalArgumentException("Aggregator recipe cannot be null");

        aggregatorRecipes.add(recipe);
    }

    /** Removes every aggregator recipe producing the given output. Returns what was removed. */
    public static List<AggregatorRecipe> removeAggregatorRecipes(ItemStack output) {
        List<AggregatorRecipe> removed = new ArrayList<AggregatorRecipe>();

        for (int i = aggregatorRecipes.size() - 1; i >= 0; i--) {
            if (matchesOutput(aggregatorRecipes.get(i).getOutput(), output))
                removed.add(aggregatorRecipes.remove(i));
        }

        return removed;
    }

    public static List<CentrifugeRecipe> removeCentrifugeRecipes(ItemStack output) {
        List<CentrifugeRecipe> removed = new ArrayList<CentrifugeRecipe>();

        for (int i = centrifugeRecipes.size() - 1; i >= 0; i--) {
            if (matchesOutput(centrifugeRecipes.get(i).getOutput(), output))
                removed.add(centrifugeRecipes.remove(i));
        }

        return removed;
    }

    public static List<EtcherRecipe> removeEtcherRecipes(ItemStack output) {
        List<EtcherRecipe> removed = new ArrayList<EtcherRecipe>();

        for (int i = etcherRecipes.size() - 1; i >= 0; i--) {
            if (matchesOutput(etcherRecipes.get(i).getOutput(), output))
                removed.add(etcherRecipes.remove(i));
        }

        return removed;
    }

    public static List<EnergizerRecipe> removeEnergizerRecipes(ItemStack output) {
        List<EnergizerRecipe> removed = new ArrayList<EnergizerRecipe>();

        for (int i = energizerRecipes.size() - 1; i >= 0; i--) {
            if (matchesOutput(energizerRecipes.get(i).getOutput(), output))
                removed.add(energizerRecipes.remove(i));
        }

        return removed;
    }

    private static boolean matchesOutput(ItemStack recipeOutput, ItemStack query) {
        return recipeOutput != null && query != null
                && recipeOutput.getItem() == query.getItem()
                && (query.getItemDamage() == net.minecraftforge.oredict.OreDictionary.WILDCARD_VALUE
                        || recipeOutput.getItemDamage() == query.getItemDamage());
    }

    public static AggregatorRecipe findAggregatorRecipe(ItemStack inputA, ItemStack inputB, ItemStack inputC) {
        for (AggregatorRecipe recipe : aggregatorRecipes) {
            if (recipe.matches(inputA, inputB, inputC))
                return recipe;
        }

        return null;
    }

    public static List<CentrifugeRecipe> getCentrifugeRecipes() {
        return Collections.unmodifiableList(centrifugeRecipes);
    }

    public static void addCentrifugeRecipe(CentrifugeRecipe recipe) {
        if (recipe == null)
            throw new IllegalArgumentException("Centrifuge recipe cannot be null");

        centrifugeRecipes.add(recipe);
    }

    public static CentrifugeRecipe findCentrifugeRecipe(ItemStack input) {
        for (CentrifugeRecipe recipe : centrifugeRecipes) {
            if (recipe.matches(input))
                return recipe;
        }

        return null;
    }

    public static List<EtcherRecipe> getEtcherRecipes() {
        return Collections.unmodifiableList(etcherRecipes);
    }

    public static void addEtcherRecipe(EtcherRecipe recipe) {
        if (recipe == null)
            throw new IllegalArgumentException("Etcher recipe cannot be null");

        etcherRecipes.add(recipe);
    }

    public static EtcherRecipe findEtcherRecipe(ItemStack top, ItemStack bottom, ItemStack middle) {
        for (EtcherRecipe recipe : etcherRecipes) {
            if (recipe.matches(top, bottom, middle))
                return recipe;
        }

        return null;
    }

    public static boolean isValidEtcherInput(int slot, ItemStack stack) {
        for (EtcherRecipe recipe : etcherRecipes) {
            if (recipe.matchesSlot(slot, stack))
                return true;
        }

        return false;
    }

    public static List<EnergizerRecipe> getEnergizerRecipes() {
        return Collections.unmodifiableList(energizerRecipes);
    }

    public static void addEnergizerRecipe(EnergizerRecipe recipe) {
        if (recipe == null)
            throw new IllegalArgumentException("Energizer recipe cannot be null");

        energizerRecipes.add(recipe);
    }

    public static EnergizerRecipe findEnergizerRecipe(ItemStack input) {
        for (EnergizerRecipe recipe : energizerRecipes) {
            if (recipe.matches(input))
                return recipe;
        }

        return null;
    }

    private static void registerDefaultAggregatorRecipes() {
        addAggregatorRecipeIfComplete(
                ore("dustCoal"),
                ae(AEApi.instance().definitions().materials().fluixDust()),
                ore("ingotIron"),
                lazyMaterial(ItemLazyMaterial.FLUIX_STEEL, 1));

        addAggregatorRecipeIfComplete(
                ore("dustCoal"),
                ae(AEApi.instance().definitions().materials().fluixDust()),
                ore("itemSilicon"),
                lazyMaterial(ItemLazyMaterial.CARBONIC_FLUIX_COMPLEX, 1));

        addAggregatorRecipeIfComplete(
                ore("gemQuartz"),
                ore("dustRedstone"),
                ae(AEApi.instance().definitions().materials().certusQuartzCrystalCharged()),
                aeStack(AEApi.instance().definitions().materials().fluixCrystal(), 2));

        addAggregatorRecipeIfComplete(
                exact(new ItemStack(Items.diamond)),
                ae(AEApi.instance().definitions().materials().skyDust()),
                ae(AEApi.instance().definitions().materials().enderDust()),
                lazyMaterial(ItemLazyMaterial.RESONATING_CRYSTAL, 1));

        addAggregatorRecipeIfComplete(
                ae(AEApi.instance().definitions().materials().skyDust()),
                ae(AEApi.instance().definitions().materials().matterBall()),
                exact(lazyMaterial(ItemLazyMaterial.CARBONIC_FLUIX_COMPLEX, 1)),
                lazyMaterial(ItemLazyMaterial.SPEC_CORE, 1));
    }

    private static void registerDefaultCentrifugeRecipes() {
        IMaterials materials = AEApi.instance().definitions().materials();

        addCentrifugeRecipeIfComplete(
                ae(materials.certusQuartzCrystal()),
                aeStack(materials.purifiedCertusQuartzCrystal(), 2));

        addCentrifugeRecipeIfComplete(
                exact(new ItemStack(Items.quartz)),
                aeStack(materials.purifiedNetherQuartzCrystal(), 2));

        addCentrifugeRecipeIfComplete(
                ae(materials.fluixCrystal()),
                aeStack(materials.purifiedFluixCrystal(), 2));

        addCentrifugeRecipeIfComplete(
                ae(AEApi.instance().definitions().blocks().skyStone()),
                aeStack(materials.skyDust(), 1));

        addCentrifugeRecipeIfComplete(
                exact(new ItemStack(Items.ender_pearl)),
                aeStack(materials.enderDust(), 1));

        addCentrifugeRecipeIfComplete(
                exact(new ItemStack(Items.wheat)),
                aeStack(materials.flour(), 1));
    }

    private static void registerDefaultEtcherRecipes() {
        IMaterials materials = AEApi.instance().definitions().materials();

        addEtcherRecipeIfComplete(
                ore("ingotGold"),
                aeStack(materials.logicProcessor(), 1));

        addEtcherRecipeIfComplete(
                ae(materials.purifiedCertusQuartzCrystal()),
                aeStack(materials.calcProcessor(), 1));

        addEtcherRecipeIfComplete(
                ore("gemDiamond"),
                aeStack(materials.engProcessor(), 1));

        addEtcherRecipeIfComplete(
                exact(lazyMaterial(ItemLazyMaterial.RESONATING_CRYSTAL, 1)),
                lazyMaterial(ItemLazyMaterial.PARALLEL_PROCESSOR, 1));

        addEtcherRecipeIfComplete(
                exact(lazyMaterial(ItemLazyMaterial.SPEC_CORE_64, 1)),
                lazyMaterial(ItemLazyMaterial.SPECULATIVE_PROCESSOR, 1));
    }

    private static void registerDefaultEnergizerRecipes() {
        IMaterials materials = AEApi.instance().definitions().materials();

        ItemStackMatcher input = ae(materials.certusQuartzCrystal());
        ItemStack output = aeStack(materials.certusQuartzCrystalCharged(), 1);
        if (input != null && output != null)
            addEnergizerRecipe(new EnergizerRecipe(input, 12000, output));
    }

    private static void addAggregatorRecipeIfComplete(ItemStackMatcher inputA, ItemStackMatcher inputB,
            ItemStackMatcher inputC, ItemStack output) {
        if (inputA != null && inputB != null && inputC != null && output != null)
            addAggregatorRecipe(new AggregatorRecipe(inputA, inputB, inputC, output));
    }

    private static void addCentrifugeRecipeIfComplete(ItemStackMatcher input, ItemStack output) {
        if (input != null && output != null)
            addCentrifugeRecipe(new CentrifugeRecipe(input, output));
    }

    // Etcher top/bottom are fixed redstone/silicon, matching the upstream mod.
    private static void addEtcherRecipeIfComplete(ItemStackMatcher middle, ItemStack output) {
        ItemStackMatcher top = ore("dustRedstone");
        ItemStackMatcher bottom = ore("itemSilicon");

        if (middle != null && output != null)
            addEtcherRecipe(new EtcherRecipe(top, bottom, middle, output));
    }

    private static ItemStackMatcher ore(String oreName) {
        return new OreDictionaryItemStackMatcher(oreName);
    }

    private static ItemStackMatcher exact(ItemStack stack) {
        return stack != null ? new ExactItemStackMatcher(stack) : null;
    }

    private static ItemStackMatcher ae(IItemDefinition definition) {
        ItemStack stack = aeStack(definition, 1);
        return stack != null ? exact(stack) : null;
    }

    private static ItemStack aeStack(IItemDefinition definition, int amount) {
        Optional<ItemStack> stack = definition.maybeStack(amount);
        return stack.isPresent() ? stack.get() : null;
    }

    private static ItemStack lazyMaterial(int metadata, int amount) {
        return new ItemStack(ModItems.material, amount, metadata);
    }
}
