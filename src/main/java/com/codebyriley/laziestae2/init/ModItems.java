package com.codebyriley.laziestae2.init;

import com.codebyriley.laziestae2.item.ItemLazyMaterial;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public final class ModItems {

    public static ItemLazyMaterial material;

    private ModItems() {
    }

    public static void register() {
        material = new ItemLazyMaterial();
        GameRegistry.registerItem(material, "material");
        registerOreDictionary();
    }

    private static void registerOreDictionary() {
        // Standard names, so other mods' dusts/ingots interoperate with ours.
        register("ingotFluixSteel", ItemLazyMaterial.FLUIX_STEEL);
        register("dustCoal", ItemLazyMaterial.COAL_DUST);
        register("ingotFluixPlatedIron", ItemLazyMaterial.FLUIX_PLATED_IRON);
        register("dustCarbonicFluix", ItemLazyMaterial.CARBONIC_FLUIX_COMPLEX);
        register("gemResonatingCrystal", ItemLazyMaterial.RESONATING_CRYSTAL);
        register("crystalResonating", ItemLazyMaterial.RESONATING_CRYSTAL);

        // Processors, following AE2's "itemProcessor"-style naming.
        register("itemFluixLogicUnit", ItemLazyMaterial.FLUIX_LOGIC_UNIT);
        register("itemParallelProcessor", ItemLazyMaterial.PARALLEL_PROCESSOR);
        register("itemSpeculativeProcessor", ItemLazyMaterial.SPECULATIVE_PROCESSOR);

        // Speculation cores, both tier-specific and a shared name for "any tier".
        register("itemSpeculationCore1", ItemLazyMaterial.SPEC_CORE);
        register("itemSpeculationCore2", ItemLazyMaterial.SPEC_CORE_2);
        register("itemSpeculationCore4", ItemLazyMaterial.SPEC_CORE_4);
        register("itemSpeculationCore8", ItemLazyMaterial.SPEC_CORE_8);
        register("itemSpeculationCore16", ItemLazyMaterial.SPEC_CORE_16);
        register("itemSpeculationCore32", ItemLazyMaterial.SPEC_CORE_32);
        register("itemSpeculationCore64", ItemLazyMaterial.SPEC_CORE_64);

        for (int metadata = ItemLazyMaterial.SPEC_CORE; metadata <= ItemLazyMaterial.SPEC_CORE_64; metadata++) {
            register("itemSpeculationCore", metadata);
        }
    }

    private static void register(String oreName, int metadata) {
        OreDictionary.registerOre(oreName, new ItemStack(material, 1, metadata));
    }
}
