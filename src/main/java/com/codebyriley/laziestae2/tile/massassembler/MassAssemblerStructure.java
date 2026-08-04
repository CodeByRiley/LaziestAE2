package com.codebyriley.laziestae2.tile.massassembler;

import com.codebyriley.laziestae2.init.ModBlocks;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public final class MassAssemblerStructure {

    private static final int MAX_PARTS = 256;
    private static final int[][] NEIGHBORS = {
            { 1, 0, 0 },
            { -1, 0, 0 },
            { 0, 1, 0 },
            { 0, -1, 0 },
            { 0, 0, 1 },
            { 0, 0, -1 }
    };

    private MassAssemblerStructure() {
    }

    public static ScanResult scan(World world, int x, int y, int z) {
        ScanResult result = new ScanResult();

        if (world == null || getPartType(world, x, y, z) == null) {
            return result;
        }

        Queue<Coord> queue = new ArrayDeque<Coord>();
        Set<Long> visited = new HashSet<Long>();
        queue.add(new Coord(x, y, z));

        while (!queue.isEmpty() && result.getPartCount() <= MAX_PARTS) {
            Coord coord = queue.remove();
            long key = pack(coord.x, coord.y, coord.z);

            if (!visited.add(key)) {
                continue;
            }

            MassAssemblerPartType type = getPartType(world, coord.x, coord.y, coord.z);
            if (type == null) {
                continue;
            }

            result.addPart(type, coord.x, coord.y, coord.z);

            for (int[] neighbor : NEIGHBORS) {
                int nextX = coord.x + neighbor[0];
                int nextY = coord.y + neighbor[1];
                int nextZ = coord.z + neighbor[2];

                if (!visited.contains(pack(nextX, nextY, nextZ)) && getPartType(world, nextX, nextY, nextZ) != null) {
                    queue.add(new Coord(nextX, nextY, nextZ));
                }
            }
        }

        result.setTooLarge(result.getPartCount() > MAX_PARTS);
        return result;
    }

    public static TileMassAssemblerController findConnectedController(World world, int x, int y, int z) {
        ScanResult result = scan(world, x, y, z);
        if (result.getControllerCount() != 1) {
            return null;
        }

        TileEntity tile = world.getTileEntity(result.getControllerX(), result.getControllerY(), result.getControllerZ());
        return tile instanceof TileMassAssemblerController ? (TileMassAssemblerController)tile : null;
    }

    public static void refreshConnectedController(World world, int x, int y, int z) {
        if (world == null || world.isRemote) {
            return;
        }

        TileMassAssemblerController controller = findConnectedController(world, x, y, z);
        if (controller != null) {
            controller.refreshStructure();
        }
    }

    private static MassAssemblerPartType getPartType(World world, int x, int y, int z) {
        if (world.getBlock(x, y, z) != ModBlocks.bigAssembler) {
            return null;
        }

        return MassAssemblerPartType.fromMetadata(world.getBlockMetadata(x, y, z));
    }

    private static long pack(int x, int y, int z) {
        return ((long)(x & 0x3FFFFFF) << 38) | ((long)(z & 0x3FFFFFF) << 12) | (long)(y & 0xFFF);
    }

    private static final class Coord {

        private final int x;
        private final int y;
        private final int z;

        private Coord(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static final class ScanResult {

        private final java.util.List<int[]> patternProviderPositions = new java.util.ArrayList<int[]>();
        private final java.util.List<int[]> partPositions = new java.util.ArrayList<int[]>();
        private int partCount;
        private int frameCount;
        private int ventCount;
        private int controllerCount;
        private int patternProviderCount;
        private int craftingCoprocessorCount;
        private int ioPortCount;
        private int controllerX;
        private int controllerY;
        private int controllerZ;
        private boolean tooLarge;

        private void addPart(MassAssemblerPartType type, int x, int y, int z) {
            partCount++;
            partPositions.add(new int[] { x, y, z });

            switch (type) {
                case FRAME:
                    frameCount++;
                    break;
                case VENT:
                    ventCount++;
                    break;
                case CONTROLLER:
                    controllerCount++;
                    controllerX = x;
                    controllerY = y;
                    controllerZ = z;
                    break;
                case PATTERN_PROVIDER:
                    patternProviderCount++;
                    patternProviderPositions.add(new int[] { x, y, z });
                    break;
                case CRAFTING_COPROCESSOR:
                    craftingCoprocessorCount++;
                    break;
                case IO_PORT:
                    ioPortCount++;
                    break;
                default:
                    break;
            }
        }

        public boolean isFormed() {
            return !tooLarge
                    && controllerCount == 1
                    && frameCount > 0
                    && patternProviderCount > 0
                    && craftingCoprocessorCount > 0
                    && ioPortCount > 0;
        }

        public int getPartCount() {
            return partCount;
        }

        public int getFrameCount() {
            return frameCount;
        }

        public int getVentCount() {
            return ventCount;
        }

        public int getControllerCount() {
            return controllerCount;
        }

        public int getPatternProviderCount() {
            return patternProviderCount;
        }

        public int getCraftingCoprocessorCount() {
            return craftingCoprocessorCount;
        }

        public int getIoPortCount() {
            return ioPortCount;
        }

        public int getControllerX() {
            return controllerX;
        }

        public int getControllerY() {
            return controllerY;
        }

        public int getControllerZ() {
            return controllerZ;
        }

        public boolean isTooLarge() {
            return tooLarge;
        }

        public java.util.List<int[]> getPatternProviderPositions() {
            return patternProviderPositions;
        }

        public java.util.List<int[]> getPartPositions() {
            return partPositions;
        }

        private void setTooLarge(boolean tooLarge) {
            this.tooLarge = tooLarge;
        }
    }
}
