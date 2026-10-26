package dev.sharedeye;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public final class Freecam {
    private static boolean active;
    private static Vec3d pos = Vec3d.ZERO;
    private static float yaw;
    private static float pitch;

    private Freecam() {
    }

    public static boolean isActive() {
        return active;
    }

    public static Vec3d pos() {
        return pos;
    }

    public static float yaw() {
        return yaw;
    }

    public static float pitch() {
        return pitch;
    }

    public static void toggle(MinecraftClient client) {
        if (client.player == null) {
            return;
        }
        if (active) {
            active = false;
            return;
        }
        active = true;
        pos = client.player.getEyePos();
        yaw = client.player.getYaw();
        pitch = client.player.getPitch();
    }

    public static void stop() {
        active = false;
    }

    public static void tick(MinecraftClient client) {
        if (!active) {
            return;
        }
        if (client.player == null || client.world == null) {
            active = false;
            return;
        }

        yaw = client.player.getYaw();
        pitch = client.player.getPitch();

        if (client.currentScreen != null) {
            return;
        }

        long handle = client.getWindow().getHandle();
        double speed = Math.max(0.05, SharedEye.config.freecamSpeed);
        if (down(handle, GLFW.GLFW_KEY_LEFT_CONTROL) || down(handle, GLFW.GLFW_KEY_RIGHT_CONTROL)) {
            speed *= 3.0;
        }

        Vec3d forward = Vec3d.fromPolar(pitch, yaw);
        Vec3d right = Vec3d.fromPolar(0.0f, yaw + 90.0f);
        Vec3d delta = Vec3d.ZERO;

        if (down(handle, GLFW.GLFW_KEY_W)) {
            delta = delta.add(forward);
        }
        if (down(handle, GLFW.GLFW_KEY_S)) {
            delta = delta.subtract(forward);
        }
        if (down(handle, GLFW.GLFW_KEY_D)) {
            delta = delta.add(right);
        }
        if (down(handle, GLFW.GLFW_KEY_A)) {
            delta = delta.subtract(right);
        }
        if (down(handle, GLFW.GLFW_KEY_SPACE)) {
            delta = delta.add(0.0, 1.0, 0.0);
        }
        if (down(handle, GLFW.GLFW_KEY_LEFT_SHIFT) || down(handle, GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            delta = delta.add(0.0, -1.0, 0.0);
        }

        if (delta.lengthSquared() > 1.0E-6) {
            pos = pos.add(delta.normalize().multiply(speed));
        }
    }

    private static boolean down(long handle, int key) {
        return InputUtil.isKeyPressed(handle, key);
    }
}
