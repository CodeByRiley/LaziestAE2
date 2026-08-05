package com.codebyriley.laziestae2.integration.ae2;

import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.data.IAEItemStack;
import com.codebyriley.laziestae2.LaziestAE2;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reflective access to a crafting CPU's outstanding task list. AE2 exposes no
 * API for this, so the preemptive assembly unit reaches into the cluster to
 * pull ingredients for future iterations of a pattern.
 *
 * Every entry point fails soft: if AE2's internals ever change shape, batching
 * switches itself off and the machine falls back to one craft per push.
 */
public final class CraftingCpuInternals {

    private static final String CLUSTER_CLASS = "appeng.me.cluster.implementations.CraftingCPUCluster";
    private static final String TASK_PROGRESS_CLASS = CLUSTER_CLASS + "$TaskProgress";

    private static Class<?> clusterClass;
    private static Field tasksField;
    private static Field inventoryField;
    private static Field taskValueField;
    private static Method extractMethod;
    private static Method injectMethod;

    private static boolean available;

    static {
        try {
            clusterClass = Class.forName(CLUSTER_CLASS);
            tasksField = clusterClass.getDeclaredField("tasks");
            tasksField.setAccessible(true);
            inventoryField = clusterClass.getDeclaredField("inventory");
            inventoryField.setAccessible(true);

            taskValueField = Class.forName(TASK_PROGRESS_CLASS).getDeclaredField("value");
            taskValueField.setAccessible(true);

            Class<?> inventoryClass = Class.forName("appeng.crafting.MECraftingInventory");
            extractMethod = inventoryClass.getMethod(
                    "extractItems", IAEItemStack.class, Actionable.class, BaseActionSource.class);
            injectMethod = inventoryClass.getMethod(
                    "injectItems", IAEItemStack.class, Actionable.class, BaseActionSource.class);

            available = true;
        } catch (Throwable t) {
            available = false;
            LaziestAE2.logger.warn("AE2 crafting internals unavailable; "
                    + "preemptive batching disabled", t);
        }
    }

    private CraftingCpuInternals() { }

    public static boolean isAvailable() {
        return available;
    }

    /** Outstanding tasks on a CPU, or an empty list if it is not a standard cluster. */
    public static List<CraftingTask> getTasks(ICraftingCPU cpu) {
        List<CraftingTask> tasks = new ArrayList<CraftingTask>();

        if (!available || cpu == null || !clusterClass.isInstance(cpu))
            return tasks;

        try {
            Map<?, ?> taskMap = (Map<?, ?>)tasksField.get(cpu);
            for (Map.Entry<?, ?> entry : taskMap.entrySet()) {
                if (entry.getKey() instanceof ICraftingPatternDetails)
                    tasks.add(new CraftingTask(cpu, (ICraftingPatternDetails)entry.getKey(), entry.getValue()));
            }
        } catch (Throwable t) {
            disable(t);
        }

        return tasks;
    }

    private static void disable(Throwable t) {
        available = false;
        LaziestAE2.logger.warn("Disabling preemptive batching after an AE2 internals failure", t);
    }

    /** One pattern's outstanding invocation count on a CPU. */
    public static class CraftingTask {

        private final ICraftingCPU cpu;
        private final ICraftingPatternDetails pattern;
        private final Object progress;

        CraftingTask(ICraftingCPU cpu, ICraftingPatternDetails pattern, Object progress) {
            this.cpu = cpu;
            this.pattern = pattern;
            this.progress = progress;
        }

        public ICraftingPatternDetails getPattern() {
            return pattern;
        }

        public long getInvocations() {
            try {
                return taskValueField.getLong(progress);
            } catch (Throwable t) {
                disable(t);
                return 0L;
            }
        }

        public void decrement() {
            try {
                taskValueField.setLong(progress, Math.max(0L, taskValueField.getLong(progress) - 1L));
            } catch (Throwable t) {
                disable(t);
            }
        }

        /**
         * Takes one invocation's ingredients out of the CPU's staging inventory.
         * Restores anything already taken if the full set is not available.
         */
        public boolean tryExtractItems(BaseActionSource source) {
            if (!available)
                return false;

            List<IAEItemStack> extracted = new ArrayList<IAEItemStack>();

            try {
                Object inventory = inventoryField.get(cpu);

                for (IAEItemStack input : pattern.getCondensedInputs()) {
                    if (input == null)
                        continue;

                    IAEItemStack taken = (IAEItemStack)extractMethod.invoke(
                            inventory, input, Actionable.MODULATE, source);

                    if (taken == null || taken.getStackSize() != input.getStackSize()) {
                        if (taken != null)
                            extracted.add(taken);

                        for (IAEItemStack rollback : extracted) {
                            injectMethod.invoke(inventory, rollback, Actionable.MODULATE, source);
                        }

                        return false;
                    }

                    extracted.add(taken);
                }

                return true;
            } catch (Throwable t) {
                disable(t);
                return false;
            }
        }
    }
}
