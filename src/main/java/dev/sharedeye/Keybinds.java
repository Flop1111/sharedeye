package dev.sharedeye;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class Keybinds {
    private static final String CATEGORY = "category.sharedeye";

    public static KeyBinding menu;
    public static KeyBinding containers;
    public static KeyBinding lights;
    public static KeyBinding bases;
    public static KeyBinding freecam;
    public static KeyBinding hud;
    public static KeyBinding rescan;

    private Keybinds() {
    }

    public static void register() {
        menu = bind("menu", GLFW.GLFW_KEY_RIGHT_SHIFT);
        containers = bind("containers", GLFW.GLFW_KEY_X);
        lights = bind("lights", GLFW.GLFW_KEY_L);
        bases = bind("bases", GLFW.GLFW_KEY_B);
        freecam = bind("freecam", GLFW.GLFW_KEY_F6);
        hud = bind("hud", GLFW.GLFW_KEY_H);
        rescan = bind("rescan", GLFW.GLFW_KEY_R);
    }

    private static KeyBinding bind(String name, int code) {
        return KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.sharedeye." + name, InputUtil.Type.KEYSYM, code, CATEGORY));
    }

    public static void tick(MinecraftClient client) {
        SharedEyeConfig config = SharedEye.config;
        if (config == null) {
            return;
        }
        boolean changed = false;

        while (menu.wasPressed()) {
            client.setScreen(new MenuScreen());
        }
        while (containers.wasPressed()) {
            config.espContainers = !config.espContainers;
            changed = true;
        }
        while (lights.wasPressed()) {
            config.espLights = !config.espLights;
            changed = true;
        }
        while (bases.wasPressed()) {
            config.baseFinder = !config.baseFinder;
            config.espBaseSignals = config.baseFinder;
            changed = true;
        }
        while (hud.wasPressed()) {
            config.showHud = !config.showHud;
            changed = true;
        }
        while (rescan.wasPressed()) {
            SharedEye.scanner.requestRescan();
        }
        while (freecam.wasPressed()) {
            Freecam.toggle(client);
        }

        if (changed) {
            SharedEye.scanner.requestRescan();
            SharedEye.saveConfig();
        }
    }
}
