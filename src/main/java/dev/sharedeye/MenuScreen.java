package dev.sharedeye;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

/**
 * Hand-drawn settings panel: flat rects, one accent colour, no vanilla widgets.
 * Everything writes straight into the config object and is saved immediately.
 */
public class MenuScreen extends Screen {
    private static final int SCRIM = 0x99000000;
    private static final int PANEL = 0xFF11151B;
    private static final int HEADER = 0xFF171D26;
    private static final int BORDER = 0xFF2B3543;
    private static final int ROW = 0xFF1A2029;
    private static final int ROW_HOVER = 0xFF232C38;
    private static final int ACCENT = 0xFF4FD6E8;
    private static final int TEXT = 0xFFE8EEF5;
    private static final int MUTED = 0xFF7E8B9C;
    private static final int ON = 0xFF41D68F;
    private static final int OFF = 0xFF39414E;

    private static final int PANEL_W = 424;
    private static final int PANEL_H = 232;
    private static final int ROW_H = 22;
    private static final int ROW_STEP = 27;
    private static final int LEFT_X = 14;
    private static final int LEFT_W = 196;
    private static final int RIGHT_X = 224;
    private static final int RIGHT_W = 186;
    private static final int TOP = 42;
    private static final int SLIDER_STEP = 30;
    private static final int BUTTON_Y = TOP + 4 * SLIDER_STEP + 6;

    private int originX;
    private int originY;
    private int dragSlider = -1;

    public MenuScreen() {
        super(Text.literal("Shared Eye"));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    protected void init() {
        this.originX = (this.width - PANEL_W) / 2;
        this.originY = (this.height - PANEL_H) / 2;
        super.init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        SharedEyeConfig cfg = SharedEye.config;
        if (cfg == null) {
            return;
        }
        int x = this.originX;
        int y = this.originY;

        context.fill(0, 0, this.width, this.height, SCRIM);
        context.fill(x - 4, y - 4, x + PANEL_W + 4, y + PANEL_H + 4, 0x55000000);
        context.fill(x, y, x + PANEL_W, y + PANEL_H, PANEL);
        context.fill(x, y, x + PANEL_W, y + 30, HEADER);
        border(context, x, y, PANEL_W, PANEL_H, BORDER);
        context.fill(x, y, x + PANEL_W, y + 2, ACCENT);

        context.getMatrices().push();
        context.getMatrices().translate((float) (x + 14), (float) (y + 9), 0.0f);
        context.getMatrices().scale(1.4f, 1.4f, 1.0f);
        context.drawTextWithShadow(this.textRenderer, Text.literal("SHARED EYE"), 0, 0, TEXT);
        context.getMatrices().pop();

        context.drawTextWithShadow(this.textRenderer, Text.literal("co-op overlay"), x + 112, y + 11, MUTED);
        context.drawTextWithShadow(this.textRenderer, Text.literal("1.21.1"), x + PANEL_W - 52, y + 11, MUTED);

        int rowY = y + TOP;
        rowY = toggle(context, x + LEFT_X, rowY, LEFT_W, "Container ESP", cfg.espContainers, mouseX, mouseY);
        rowY = toggle(context, x + LEFT_X, rowY, LEFT_W, "Light ESP", cfg.espLights, mouseX, mouseY);
        rowY = toggle(context, x + LEFT_X, rowY, LEFT_W, "Artificial lights", cfg.espArtificialLights, mouseX, mouseY);
        rowY = toggle(context, x + LEFT_X, rowY, LEFT_W, "Amethyst geodes", cfg.espAmethyst, mouseX, mouseY);
        rowY = toggle(context, x + LEFT_X, rowY, LEFT_W, "Base hints", cfg.baseFinder, mouseX, mouseY);
        toggle(context, x + LEFT_X, rowY, LEFT_W, "HUD", cfg.showHud, mouseX, mouseY);

        int sliderX = x + RIGHT_X;
        int sliderY = y + TOP;
        slider(context, sliderX, sliderY, RIGHT_W, "Scan radius", cfg.radiusChunks + " ch", fraction(cfg.radiusChunks, 1, 12));
        slider(context, sliderX, sliderY + SLIDER_STEP, RIGHT_W, "Scan interval", cfg.scanIntervalTicks + " t", fraction(cfg.scanIntervalTicks, 20, 200));
        slider(context, sliderX, sliderY + SLIDER_STEP * 2, RIGHT_W, "Base threshold", Integer.toString(cfg.baseMinWeight), fraction(cfg.baseMinWeight, 5, 60));
        slider(context, sliderX, sliderY + SLIDER_STEP * 3, RIGHT_W, "Freecam speed", String.format(Locale.ROOT, "%.1f", cfg.freecamSpeed), fraction(cfg.freecamSpeed, 0.2, 3.0));

        button(context, sliderX, y + BUTTON_Y, RIGHT_W, "Rescan now", false, mouseX, mouseY);
        button(context, sliderX, y + BUTTON_Y + 24, RIGHT_W, Freecam.isActive() ? "Freecam: ON" : "Freecam: OFF", Freecam.isActive(), mouseX, mouseY);

        context.drawTextWithShadow(this.textRenderer, Text.literal(
                "Right Shift / Esc closes   -   settings save instantly"), x + LEFT_X, y + PANEL_H - 15, MUTED);
    }

    private int toggle(DrawContext context, int x, int y, int width, String label, boolean on, int mouseX, int mouseY) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, ROW_H);
        context.fill(x, y, x + width, y + ROW_H, hovered ? ROW_HOVER : ROW);
        context.fill(x, y, x + 2, y + ROW_H, on ? ON : OFF);
        context.drawTextWithShadow(this.textRenderer, Text.literal(label), x + 10, y + 7, TEXT);

        int pillW = 30;
        int pillH = 12;
        int pillX = x + width - pillW - 8;
        int pillY = y + (ROW_H - pillH) / 2;
        context.fill(pillX, pillY, pillX + pillW, pillY + pillH, on ? ON : OFF);
        if (on) {
            context.fill(pillX + pillW - 11, pillY + 1, pillX + pillW - 1, pillY + pillH - 1, 0xFFF2F5F8);
        } else {
            context.fill(pillX + 1, pillY + 1, pillX + 11, pillY + pillH - 1, 0xFFF2F5F8);
        }
        return y + ROW_STEP;
    }

    private void slider(DrawContext context, int x, int y, int width, String label, String value, float fraction) {
        context.drawTextWithShadow(this.textRenderer, Text.literal(label), x, y, MUTED);
        context.drawTextWithShadow(this.textRenderer, Text.literal(value),
                x + width - this.textRenderer.getWidth(value), y, ACCENT);

        int trackY = y + 16;
        context.fill(x, trackY, x + width, trackY + 4, 0xFF2A3340);
        int filled = Math.round(width * fraction);
        context.fill(x, trackY, x + filled, trackY + 4, ACCENT);
        int knob = x + filled;
        context.fill(knob - 3, trackY - 3, knob + 3, trackY + 7, 0xFFE8EEF5);
    }

    private void button(DrawContext context, int x, int y, int width, String label, boolean accent, int mouseX, int mouseY) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, 20);
        context.fill(x, y, x + width, y + 20, hovered ? 0xFF2B3542 : 0xFF1D2530);
        border(context, x, y, width, 20, accent ? ACCENT : BORDER);
        int textWidth = this.textRenderer.getWidth(label);
        context.drawTextWithShadow(this.textRenderer, Text.literal(label),
                x + (width - textWidth) / 2, y + 6, accent ? ACCENT : TEXT);
    }

    private static void border(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private static float fraction(double value, double min, double max) {
        if (max <= min) {
            return 0.0f;
        }
        return MathHelper.clamp((float) ((value - min) / (max - min)), 0.0f, 1.0f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        SharedEyeConfig cfg = SharedEye.config;
        if (cfg == null) {
            return false;
        }
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        int x = this.originX;
        int y = this.originY;

        for (int i = 0; i < 6; i++) {
            int rowY = y + TOP + i * ROW_STEP;
            if (!inside(mouseX, mouseY, x + LEFT_X, rowY, LEFT_W, ROW_H)) {
                continue;
            }
            switch (i) {
                case 0 -> cfg.espContainers = !cfg.espContainers;
                case 1 -> cfg.espLights = !cfg.espLights;
                case 2 -> cfg.espArtificialLights = !cfg.espArtificialLights;
                case 3 -> cfg.espAmethyst = !cfg.espAmethyst;
                case 4 -> {
                    cfg.baseFinder = !cfg.baseFinder;
                    cfg.espBaseSignals = cfg.baseFinder;
                }
                case 5 -> cfg.showHud = !cfg.showHud;
                default -> {
                }
            }
            SharedEye.scanner.requestRescan();
            SharedEye.saveConfig();
            return true;
        }

        for (int i = 0; i < 4; i++) {
            int sliderY = y + TOP + i * SLIDER_STEP;
            if (inside(mouseX, mouseY, x + RIGHT_X, sliderY + 8, RIGHT_W, 18)) {
                this.dragSlider = i;
                applySlider(i, mouseX);
                return true;
            }
        }

        if (inside(mouseX, mouseY, x + RIGHT_X, y + BUTTON_Y, RIGHT_W, 20)) {
            SharedEye.scanner.requestRescan();
            return true;
        }
        if (inside(mouseX, mouseY, x + RIGHT_X, y + BUTTON_Y + 24, RIGHT_W, 20)) {
            Freecam.toggle(this.client);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.dragSlider >= 0) {
            applySlider(this.dragSlider, mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && this.dragSlider >= 0) {
            this.dragSlider = -1;
            SharedEye.saveConfig();
            SharedEye.scanner.requestRescan();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void applySlider(int index, double mouseX) {
        SharedEyeConfig cfg = SharedEye.config;
        if (cfg == null) {
            return;
        }
        float f = MathHelper.clamp((float) ((mouseX - (this.originX + RIGHT_X)) / (double) RIGHT_W), 0.0f, 1.0f);
        switch (index) {
            case 0 -> cfg.radiusChunks = Math.round(1 + f * 11);
            case 1 -> cfg.scanIntervalTicks = Math.round(20 + f * 180);
            case 2 -> cfg.baseMinWeight = Math.round(5 + f * 55);
            case 3 -> cfg.freecamSpeed = Math.round((0.2 + f * 2.8) * 10) / 10.0;
            default -> {
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
