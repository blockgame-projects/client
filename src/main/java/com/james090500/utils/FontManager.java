package com.james090500.utils;

import com.james090500.BlockGame;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGTextRow;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.system.MemoryUtil.memAddress;

public class FontManager {

    private static long vg = BlockGame.getInstance().getClientWindow().getVg();

    static {
        long font = nvgCreateFont(vg, "default", "assets/fonts/Minecraftia-Regular.ttf");
        if (font == -1) {
            throw new RuntimeException("Could not load font");
        }
    }

    private boolean center;

    /**
     * Create a new FontManager instance
     * @return The new instance
     */
    public static FontManager create() {
        return new FontManager();
    }

    /**
     * Center the text
     * @return The instance
     */
    public FontManager center() {
        this.center = true;
        return this;
    }

    /**
     * Color the text
     * @param r Red
     * @param g Green
     * @param b Blue
     * @param a Alpha
     * @return The instance
     */
    public FontManager color(float r, float g, float b, float a) {
        try (NVGColor color = NVGColor.calloc()) {
            color.r(r).g(g).b(b).a(a);
            nvgFillColor(vg, color);
        }
        return this;
    }

    /**
     * A normal text instance
     * @param text The text
     * @param size The size
     * @param x The start x
     * @param y The start y
     * @return The instance
     */
    public FontManager text(String text, float size, float x, float y) {
        return this.textFixed(text, size, x, y, 0);
    }

    /**
     * A fixed text instance which will wrap to a width
     * @param text The text
     * @param size The size
     * @param x The start x
     * @param y The start y
     * @param w The max width
     * @return The instance
     */
    public FontManager textFixed(String text, float size, float x, float y, float w) {
        if(text != null) {
            NVGColor textBackground = NVGColor.calloc();
            NVGColor textColor = NVGColor.calloc();

            nvgRGBA((byte) 92, (byte) 88, (byte) 95, (byte) 255, textBackground);
            nvgRGBA((byte) 255, (byte) 255, (byte) 255, (byte) 255, textColor);

            if(center) {
                nvgTextAlign(vg, NVG_ALIGN_CENTER);
            } else {
                nvgTextAlign(vg, NVG_ALIGN_LEFT);
            }

            // Text Background
            nvgFontSize(vg, size);
            nvgFontFace(vg, "default");
            nvgFillColor(vg, textBackground);
            if(w == 0) {
                nvgText(vg, x + 2, y + 2, text);
            } else {
                drawLastWrappedLine(x + 2, y + 2, w, text);
            }

            // Text
            nvgFontSize(vg, size);
            nvgFontFace(vg, "default");
            nvgFillColor(vg, textColor);
            if(w == 0) {
                nvgText(vg, x, y, text);
            } else {
                drawLastWrappedLine(x, y, w, text);
            }

            textBackground.free();
            textColor.free();
        }
        return this;
    }

    /**
     * Draw the last line from a string state
     * @param x Start X
     * @param y Start Y
     * @param maxWidth
     * @param text
     */
    private void drawLastWrappedLine(float x, float y, float maxWidth, String text) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            NVGTextRow.Buffer rows = NVGTextRow.calloc(1, stack);
            ByteBuffer utf8 = stack.UTF8(text, true);

            long start = memAddress(utf8);
            long end   = 0L;
            long lastStart = 0L, lastEnd = 0L;

            while (true) {
                int nrows = nnvgTextBreakLines(vg, start, end, maxWidth, rows.address(), 1);
                if (nrows == 0) break;
                NVGTextRow row = rows.get(0);
                lastStart = row.start();
                lastEnd   = row.end();
                start     = row.next();
            }

            if (lastStart != 0L)
                nnvgText(vg, x, y, lastStart, lastEnd);
        }
    }

}
