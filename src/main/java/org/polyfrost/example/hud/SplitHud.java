package org.polyfrost.example.hud;

import cc.polyfrost.oneconfig.config.annotations.Exclude;
import cc.polyfrost.oneconfig.config.annotations.Slider;
import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import org.polyfrost.example.utils.TimeUtils;

public class SplitHud extends Hud {

    @Exclude private static final int COLOR_WHITE = 0xFFFFFFFF;
    @Exclude private static final int COLOR_BLUE  = 0x600000FF;
    @Exclude private static final int COLOR_RED   = 0x50FF0000;
    @Exclude private static final int COLOR_GREY  = 0x60808080;
    @Exclude private static final int COLOR_BLACK = 0x80000000;

    @Exclude private static final int PADDING = 2;
    @Exclude private static final long SHOW_DURATION_MILLIS = 1000;

    @Slider(
            name = "Delta Scale",
            min = 0.70f,
            max = 1.0f
    )
    public float deltaScale = 0.90f;

    @Exclude private long elapsedMillis;
    @Exclude private long deltaMillis;
    @Exclude private boolean hasDelta;
    @Exclude private long triggeredTimeMillis;

    public SplitHud() {
        super(true);
    }

    @Override
    protected void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {
        if (!example && !isWithinShowWindow()) return;

        Minecraft mc = Minecraft.getMinecraft();
        int fontHeight = mc.fontRendererObj.FONT_HEIGHT;
        int boxHeight = fontHeight + 2 * PADDING;

        String elapsedText = example ? "0:00.000" : TimeUtils.formatMillis(elapsedMillis);
        int elapsedBoxWidth = mc.fontRendererObj.getStringWidth(elapsedText) + 2 * PADDING;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(scale, scale, 1f);

        // Elapsed time box
        Gui.drawRect(0, 0, elapsedBoxWidth, boxHeight, COLOR_BLACK);
        mc.fontRendererObj.drawString(elapsedText, PADDING, PADDING + 1, COLOR_WHITE);

        // Delta box — nested scale transform so it can be sized independently
        if (example || hasDelta) {
            String deltaText = example ? "+0.000" : TimeUtils.formatDelta(deltaMillis);
            int deltaBoxWidth = mc.fontRendererObj.getStringWidth(deltaText) + 2 * PADDING;

            int deltaColor;
            if (example || deltaMillis == 0) {
                deltaColor = COLOR_GREY;
            } else if (deltaMillis < 0) {
                deltaColor = COLOR_BLUE;
            } else {
                deltaColor = COLOR_RED;
            }

            // Translate to the delta's position in parent space first (float precision) then apply the scale
            float deltaOffsetX = elapsedBoxWidth - deltaBoxWidth * deltaScale;

            GlStateManager.pushMatrix();
            GlStateManager.translate(deltaOffsetX, boxHeight, 0);
            GlStateManager.scale(deltaScale, deltaScale, 1f);

            Gui.drawRect(0, 0, deltaBoxWidth, boxHeight, deltaColor);
            mc.fontRendererObj.drawString(deltaText, PADDING, PADDING + 1, COLOR_WHITE);

            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
    }

    @Override
    protected float getWidth(float scale, boolean example) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.fontRendererObj == null) return 50 * scale;
        String text = example ? "0:00.000" : TimeUtils.formatMillis(elapsedMillis);
        return (mc.fontRendererObj.getStringWidth(text) + 2 * PADDING) * scale;
    }

    @Override
    protected float getHeight(float scale, boolean example) {
        Minecraft mc = Minecraft.getMinecraft();
        int fontHeight = (mc == null || mc.fontRendererObj == null) ? 9 : mc.fontRendererObj.FONT_HEIGHT;
        int singleBox = fontHeight + 2 * PADDING;
        return singleBox * ((example || hasDelta) ? 1f + deltaScale : 1f) * scale;
    }

    public void showSplit(long elapsedMillis) {
        this.elapsedMillis = elapsedMillis;
        this.hasDelta = false;
        this.triggeredTimeMillis = System.currentTimeMillis();
    }

    public void showSplit(long elapsedMillis, long deltaMillis) {
        this.elapsedMillis = elapsedMillis;
        this.deltaMillis = deltaMillis;
        this.hasDelta = true;
        this.triggeredTimeMillis = System.currentTimeMillis();
    }

    private boolean isWithinShowWindow() {
        return System.currentTimeMillis() - triggeredTimeMillis < SHOW_DURATION_MILLIS;
    }
}
