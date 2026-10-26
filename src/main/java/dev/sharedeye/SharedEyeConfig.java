package dev.sharedeye;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SharedEyeConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean espContainers = true;
    public boolean espLights = true;
    public boolean espArtificialLights = true;
    public boolean espAmethyst = true;
    public boolean espBaseSignals = true;
    public boolean baseFinder = true;
    public boolean showHud = true;

    /** Radius in chunks scanned around the player. Bigger means more lag. */
    public int radiusChunks = 4;

    /** Ticks between scans. 20 ticks = 1 second. */
    public int scanIntervalTicks = 60;

    /** Minimum cluster score before it is reported as a base hint. */
    public int baseMinWeight = 14;

    /** A light with no scored block within this many blocks counts as "artificial". */
    public int artificialRadius = 6;

    /** Freecam blocks per tick. Ctrl multiplies this by 3. */
    public double freecamSpeed = 0.75;

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("sharedeye.json");
    }

    public static SharedEyeConfig load() {
        Path file = path();
        if (Files.isRegularFile(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                SharedEyeConfig loaded = GSON.fromJson(reader, SharedEyeConfig.class);
                if (loaded != null) {
                    return loaded;
                }
            } catch (Exception e) {
                SharedEye.LOGGER.warn("Could not read sharedeye.json, using defaults", e);
            }
        }
        SharedEyeConfig fresh = new SharedEyeConfig();
        save(fresh);
        return fresh;
    }

    public static void save(SharedEyeConfig config) {
        try {
            Files.createDirectories(path().getParent());
            Files.writeString(path(), GSON.toJson(config));
        } catch (Exception e) {
            SharedEye.LOGGER.warn("Could not write sharedeye.json", e);
        }
    }
}
