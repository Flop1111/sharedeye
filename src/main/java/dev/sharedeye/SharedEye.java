package dev.sharedeye;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SharedEye implements ClientModInitializer {
    public static final String MOD_ID = "sharedeye";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static SharedEyeConfig config;
    public static Scanner scanner;

    @Override
    public void onInitializeClient() {
        config = SharedEyeConfig.load();
        scanner = new Scanner();

        Keybinds.register();

        WorldRenderEvents.AFTER_TRANSLUCENT.register(EspRenderer::render);
        HudRenderCallback.EVENT.register(HudRenderer::render);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Keybinds.tick(client);
            Freecam.tick(client);
            scanner.tick(client);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            Freecam.stop();
            scanner.clear();
        });

        LOGGER.info("Shared Eye ready");
    }

    public static void saveConfig() {
        if (config != null) {
            SharedEyeConfig.save(config);
        }
    }
}
