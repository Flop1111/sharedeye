package dev.sharedeye;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public final class EspRenderer {
    private static final int CONTAINER_COLOR = 0xFF3BE0FF;
    private static final int LIGHT_COLOR = 0xFFFFD24A;
    private static final int ARTIFICIAL_COLOR = 0xFFFF7A3D;
    private static final int GEODE_COLOR = 0xFFC77DFF;
    private static final int AMETHYST_COLOR = 0xFFE9C6FF;
    private static final int CLUSTER_COLOR = 0xFFFF6CE0;

    public static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        SharedEyeConfig config = SharedEye.config;
        if (config == null || client.world == null || client.player == null) {
            return;
        }
        if (!config.espContainers && !config.espLights && !config.espArtificialLights
                && !config.espAmethyst && !config.espBaseSignals) {
            return;
        }

        MatrixStack matrices = context.matrixStack();
        if (matrices == null) {
            return;
        }
        Vec3d cameraPos = context.camera().getPos();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.lineWidth(2.0f);

        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexBuilders();
        VertexConsumer lines = immediate.getBuffer(RenderLayer.getLines());

        if (config.espContainers) {
            for (Scanner.Hit hit : SharedEye.scanner.containers()) {
                box(lines, matrix, hit.pos(), 0.06, CONTAINER_COLOR);
            }
        }
        if (config.espLights) {
            for (Scanner.Hit hit : SharedEye.scanner.lights()) {
                box(lines, matrix, hit.pos(), 0.02, LIGHT_COLOR);
            }
        }
        if (config.espArtificialLights) {
            for (Scanner.Hit hit : SharedEye.scanner.artificialLights()) {
                beam(lines, matrix, hit.pos(), ARTIFICIAL_COLOR, 0.35f);
            }
        }
        if (config.espAmethyst) {
            for (Scanner.Hit hit : SharedEye.scanner.amethyst()) {
                box(lines, matrix, hit.pos(), 0.02, AMETHYST_COLOR);
            }
            for (Scanner.Hit hit : SharedEye.scanner.geodes()) {
                beam(lines, matrix, hit.pos(), GEODE_COLOR, 0.5f);
            }
        }
        if (config.espBaseSignals) {
            for (Scanner.Cluster cluster : SharedEye.scanner.clusters()) {
                beam(lines, matrix, cluster.center(), CLUSTER_COLOR, 0.55f);
                box(lines, matrix, cluster.center(), 0.5, CLUSTER_COLOR);
            }
        }

        immediate.draw(RenderLayer.getLines());
        matrices.pop();

        RenderSystem.lineWidth(1.0f);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void box(VertexConsumer lines, Matrix4f matrix, BlockPos pos, double grow, int argb) {
        float a = ((argb >> 24) & 0xFF) / 255.0f;
        float r = ((argb >> 16) & 0xFF) / 255.0f;
        float g = ((argb >> 8) & 0xFF) / 255.0f;
        float b = (argb & 0xFF) / 255.0f;

        float x1 = (float) (pos.getX() - grow);
        float y1 = (float) (pos.getY() - grow);
        float z1 = (float) (pos.getZ() - grow);
        float x2 = (float) (pos.getX() + 1.0 + grow);
        float y2 = (float) (pos.getY() + 1.0 + grow);
        float z2 = (float) (pos.getZ() + 1.0 + grow);

        line(lines, matrix, x1, y1, z1, x2, y1, z1, r, g, b, a);
        line(lines, matrix, x2, y1, z1, x2, y1, z2, r, g, b, a);
        line(lines, matrix, x2, y1, z2, x1, y1, z2, r, g, b, a);
        line(lines, matrix, x1, y1, z2, x1, y1, z1, r, g, b, a);

        line(lines, matrix, x1, y2, z1, x2, y2, z1, r, g, b, a);
        line(lines, matrix, x2, y2, z1, x2, y2, z2, r, g, b, a);
        line(lines, matrix, x2, y2, z2, x1, y2, z2, r, g, b, a);
        line(lines, matrix, x1, y2, z2, x1, y2, z1, r, g, b, a);

        line(lines, matrix, x1, y1, z1, x1, y2, z1, r, g, b, a);
        line(lines, matrix, x2, y1, z1, x2, y2, z1, r, g, b, a);
        line(lines, matrix, x2, y1, z2, x2, y2, z2, r, g, b, a);
        line(lines, matrix, x1, y1, z2, x1, y2, z2, r, g, b, a);
    }

    private static void beam(VertexConsumer lines, Matrix4f matrix, BlockPos pos, int argb, float alpha) {
        float r = ((argb >> 16) & 0xFF) / 255.0f;
        float g = ((argb >> 8) & 0xFF) / 255.0f;
        float b = (argb & 0xFF) / 255.0f;
        float x = pos.getX() + 0.5f;
        float z = pos.getZ() + 0.5f;
        line(lines, matrix, x, pos.getY() - 40.0f, z, x, pos.getY() + 120.0f, z, r, g, b, alpha);
        box(lines, matrix, pos, 0.1, argb);
    }

    private static void line(VertexConsumer lines, Matrix4f matrix,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float r, float g, float b, float a) {
        lines.vertex(matrix, x1, y1, z1).color(r, g, b, a);
        lines.vertex(matrix, x2, y2, z2).color(r, g, b, a);
    }
}
