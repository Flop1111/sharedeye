package dev.sharedeye;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Scans loaded chunks for containers, light sources, amethyst geodes and "this looks built" clusters.
 * Everything is read from data the client already has; nothing extra is requested from the server.
 */
public final class Scanner {
    public record Hit(BlockPos pos) {
    }

    public record Cluster(BlockPos center, int weight, int markers) {
    }

    private static final Set<String> CONTAINER_BLOCK_ENTITIES = new HashSet<>(Set.of(
            "chest", "trapped_chest", "ender_chest", "barrel", "hopper",
            "furnace", "blast_furnace", "smoker", "dispenser", "dropper",
            "brewing_stand", "crafter", "decorated_pot",
            "shulker_box", "white_shulker_box", "light_gray_shulker_box", "gray_shulker_box",
            "black_shulker_box", "brown_shulker_box", "red_shulker_box", "orange_shulker_box",
            "yellow_shulker_box", "lime_shulker_box", "green_shulker_box", "cyan_shulker_box",
            "light_blue_shulker_box", "blue_shulker_box", "purple_shulker_box",
            "magenta_shulker_box", "pink_shulker_box"
    ));

    private final List<Hit> containers = new ArrayList<>();
    private final List<Hit> lights = new ArrayList<>();
    private final List<Hit> artificialLights = new ArrayList<>();
    private final List<Hit> geodes = new ArrayList<>();
    private final List<Hit> amethyst = new ArrayList<>();
    private final List<Cluster> clusters = new ArrayList<>();

    private int cooldown;
    private boolean requested = true;

    public List<Hit> containers() {
        return containers;
    }

    public List<Hit> lights() {
        return lights;
    }

    public List<Hit> artificialLights() {
        return artificialLights;
    }

    public List<Hit> geodes() {
        return geodes;
    }

    public List<Hit> amethyst() {
        return amethyst;
    }

    public List<Cluster> clusters() {
        return clusters;
    }

    public void clear() {
        containers.clear();
        lights.clear();
        artificialLights.clear();
        geodes.clear();
        amethyst.clear();
        clusters.clear();
    }

    public void requestRescan() {
        requested = true;
        cooldown = 0;
    }

    public void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        ClientWorld world = client.world;
        SharedEyeConfig config = SharedEye.config;
        if (player == null || world == null || config == null) {
            clear();
            return;
        }
        if (cooldown > 0) {
            cooldown--;
        }
        if (!requested && cooldown > 0) {
            return;
        }
        requested = false;
        cooldown = Math.max(10, config.scanIntervalTicks);
        scan(world, player, config);
    }

    private void scan(ClientWorld world, ClientPlayerEntity player, SharedEyeConfig config) {
        clear();

        boolean needBlocks = config.espLights || config.espArtificialLights || config.espAmethyst || config.baseFinder;
        if (!config.espContainers && !needBlocks) {
            return;
        }

        Map<Long, double[]> sums = new HashMap<>();
        Map<Long, int[]> stats = new HashMap<>();
        Set<Long> builtBuckets = new HashSet<>();
        List<BlockPos> lightPositions = new ArrayList<>();

        int radius = MathHelper.clamp(config.radiusChunks, 1, 12);
        int centerChunkX = player.getBlockX() >> 4;
        int centerChunkZ = player.getBlockZ() >> 4;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                WorldChunk chunk = world.getChunkManager().getWorldChunk(centerChunkX + dx, centerChunkZ + dz, false);
                if (chunk == null) {
                    continue;
                }

                if (config.espContainers) {
                    for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                        Identifier id = Registries.BLOCK_ENTITY_TYPE.getId(entry.getValue().getType());
                        if (id != null && CONTAINER_BLOCK_ENTITIES.contains(id.getPath())) {
                            containers.add(new Hit(entry.getKey().toImmutable()));
                        }
                    }
                }

                if (!needBlocks) {
                    continue;
                }

                ChunkSection[] sections = chunk.getSectionArray();
                int startX = chunk.getPos().getStartX();
                int startZ = chunk.getPos().getStartZ();
                int bottomY = chunk.getBottomY();

                for (int index = 0; index < sections.length; index++) {
                    ChunkSection section = sections[index];
                    if (section == null || section.isEmpty()) {
                        continue;
                    }
                    if (!section.hasAny(this::isInteresting)) {
                        continue;
                    }
                    int baseY = bottomY + (index << 4);
                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            for (int x = 0; x < 16; x++) {
                                BlockState state = section.getBlockState(x, y, z);
                                String path = pathOf(state);
                                BlockPos pos = new BlockPos(startX + x, baseY + y, startZ + z);

                                if (config.espAmethyst) {
                                    if (isBudding(path)) {
                                        geodes.add(new Hit(pos));
                                    } else if (isAmethystGrowth(path)) {
                                        amethyst.add(new Hit(pos));
                                    }
                                }

                                int weight = weightOf(path);
                                if (weight <= 0) {
                                    continue;
                                }
                                if (isLight(path)) {
                                    lightPositions.add(pos);
                                } else {
                                    builtBuckets.add(bucketKey(pos));
                                }
                                long key = clusterKey(pos);
                                double[] sum = sums.computeIfAbsent(key, k -> new double[3]);
                                sum[0] += pos.getX();
                                sum[1] += pos.getY();
                                sum[2] += pos.getZ();
                                int[] stat = stats.computeIfAbsent(key, k -> new int[2]);
                                stat[0] += weight;
                                stat[1]++;
                            }
                        }
                    }
                }
            }
        }

        int artificialRadius = MathHelper.clamp(config.artificialRadius, 2, 24);
        int steps = Math.max(1, (int) Math.ceil(artificialRadius / 4.0));
        for (BlockPos pos : lightPositions) {
            lights.add(new Hit(pos));
            if (!hasBuiltNear(builtBuckets, pos, steps)) {
                artificialLights.add(new Hit(pos));
            }
        }

        if (!config.baseFinder) {
            return;
        }

        int minWeight = Math.max(1, config.baseMinWeight);
        for (Map.Entry<Long, int[]> entry : stats.entrySet()) {
            int[] stat = entry.getValue();
            double[] sum = sums.get(entry.getKey());
            if (sum == null || stat[1] <= 0 || stat[0] < minWeight) {
                continue;
            }
            int cx = (int) Math.round(sum[0] / stat[1]);
            int cy = (int) Math.round(sum[1] / stat[1]);
            int cz = (int) Math.round(sum[2] / stat[1]);
            clusters.add(new Cluster(new BlockPos(cx, cy, cz), stat[0], stat[1]));
        }
        clusters.sort(Comparator.comparingDouble(c -> c.center().getSquaredDistance(player.getPos())));
    }

    private boolean isInteresting(BlockState state) {
        String path = pathOf(state);
        if (SharedEye.config.espAmethyst && (isBudding(path) || isAmethystGrowth(path))) {
            return true;
        }
        return weightOf(path) > 0;
    }

    /** 32x32 block buckets, so a village becomes one cluster instead of forty. */
    private static long clusterKey(BlockPos pos) {
        long cx = Math.floorDiv(pos.getX(), 32);
        long cz = Math.floorDiv(pos.getZ(), 32);
        return (cx << 32) ^ (cz & 0xFFFFFFFFL);
    }

    /** 4x4x4 cells used to answer "is there anything built near this light". */
    private static long bucketKey(BlockPos pos) {
        long bx = Math.floorDiv(pos.getX(), 4);
        long by = Math.floorDiv(pos.getY(), 4);
        long bz = Math.floorDiv(pos.getZ(), 4);
        return ((bx & 0x3FFFFFL) << 42) | ((by & 0xFFFFFL) << 22) | (bz & 0x3FFFFFL);
    }

    private static boolean hasBuiltNear(Set<Long> buckets, BlockPos pos, int steps) {
        long bx = Math.floorDiv(pos.getX(), 4);
        long by = Math.floorDiv(pos.getY(), 4);
        long bz = Math.floorDiv(pos.getZ(), 4);
        for (int dx = -steps; dx <= steps; dx++) {
            for (int dy = -steps; dy <= steps; dy++) {
                for (int dz = -steps; dz <= steps; dz++) {
                    long key = (((bx + dx) & 0x3FFFFFL) << 42)
                            | (((by + dy) & 0xFFFFFL) << 22)
                            | ((bz + dz) & 0x3FFFFFL);
                    if (buckets.contains(key)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static String pathOf(BlockState state) {
        Identifier id = Registries.BLOCK.getId(state.getBlock());
        return id == null ? "" : id.getPath();
    }

    private static boolean isLight(String path) {
        return switch (path) {
            case "torch", "wall_torch", "soul_torch", "soul_wall_torch",
                 "lantern", "soul_lantern", "campfire", "soul_campfire",
                 "glowstone", "sea_lantern", "shroomlight", "redstone_lamp",
                 "jack_o_lantern", "end_rod", "ochre_froglight", "verdant_froglight",
                 "pearlescent_froglight", "candle", "white_candle", "red_candle" -> true;
            default -> false;
        };
    }

    private static boolean isBudding(String path) {
        return path.equals("budding_amethyst");
    }

    private static boolean isAmethystGrowth(String path) {
        return switch (path) {
            case "amethyst_cluster", "small_amethyst_bud", "medium_amethyst_bud", "large_amethyst_bud" -> true;
            default -> false;
        };
    }

    /**
     * Rough "a player probably built this" score. Villages and other structures score too,
     * which is what makes them show up as candidates.
     */
    private static int weightOf(String path) {
        if (path.endsWith("_bed")) {
            return 3;
        }
        return switch (path) {
            case "bell" -> 8;
            case "smithing_table", "cartography_table", "fletching_table" -> 6;
            case "anvil", "chipped_anvil", "damaged_anvil" -> 5;
            case "respawn_anchor", "enchanting_table" -> 4;
            case "crafting_table", "lectern", "bookshelf", "brewing_stand" -> 3;
            case "furnace", "blast_furnace", "smoker", "chest", "trapped_chest", "barrel",
                 "lantern", "soul_lantern", "campfire", "soul_campfire", "composter" -> 2;
            case "torch", "wall_torch", "soul_torch", "soul_wall_torch",
                 "dirt_path", "hay_block", "glass", "glass_pane" -> 1;
            default -> 0;
        };
    }
}
