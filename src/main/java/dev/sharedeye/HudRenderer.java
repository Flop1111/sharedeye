package dev.sharedeye;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public final class HudRenderer {
    private static final int ACCENT = 0xFF7CE7FF;
    private static final int CONTAINER = 0xFFBFE9FF;
    private static final int LIGHT = 0xFFFFE7A8;
    private static final int AMETHYST = 0xFFE9C6FF;
    private static final int BASE = 0xFFFFB2F0;

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        SharedEyeConfig config = SharedEye.config;
        if (config == null || client.player == null || client.world == null || !config.showHud) {
            return;
        }

        int x = 6;
        int y = 6;
        int step = 11;

        context.drawTextWithShadow(client.textRenderer, Text.literal("Shared Eye"), x, y, ACCENT);
        y += step;
        context.drawTextWithShadow(client.textRenderer, Text.literal(
                (config.espContainers ? "on  " : "off ") + "containers " + SharedEye.scanner.containers().size()), x, y, CONTAINER);
        y += step;
        context.drawTextWithShadow(client.textRenderer, Text.literal(
                (config.espLights ? "on  " : "off ") + "lights " + SharedEye.scanner.lights().size()
                        + "  isolated " + SharedEye.scanner.artificialLights().size()), x, y, LIGHT);
        y += step;
        context.drawTextWithShadow(client.textRenderer, Text.literal(
                (config.espAmethyst ? "on  " : "off ") + "geodes " + SharedEye.scanner.geodes().size()
                        + "  buds " + SharedEye.scanner.amethyst().size()), x, y, AMETHYST);
        y += step;

        if (!config.baseFinder) {
            return;
        }

        List<Scanner.Cluster> clusters = SharedEye.scanner.clusters();
        context.drawTextWithShadow(client.textRenderer, Text.literal("base hints " + clusters.size()), x, y, BASE);
        y += step;

        int shown = 0;
        for (Scanner.Cluster cluster : clusters) {
            if (shown >= 3) {
                break;
            }
            shown++;
            double distance = Math.sqrt(cluster.center().getSquaredDistance(client.player.getPos()));
            String line = bearing(client, cluster.center())
                    + " " + Math.round(distance) + "m  "
                    + cluster.center().getX() + " " + cluster.center().getY() + " " + cluster.center().getZ()
                    + "  score " + cluster.weight();
            context.drawTextWithShadow(client.textRenderer, Text.literal(line), x + 4, y, BASE);
            y += step;
        }
    }

    private static String bearing(MinecraftClient client, BlockPos target) {
        double dx = target.getX() + 0.5 - client.player.getX();
        double dz = target.getZ() + 0.5 - client.player.getZ();
        float absolute = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float relative = MathHelper.wrapDegrees(absolute - client.player.getYaw());
        int index = Math.round(relative / 45.0f);
        index = ((index % 8) + 8) % 8;
        String[] names = {"ahead", "ahead-right", "right", "behind-right", "behind", "behind-left", "left", "ahead-left"};
        return names[index];
    }
}
