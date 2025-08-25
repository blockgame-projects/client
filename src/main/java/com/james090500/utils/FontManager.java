package com.james090500.utils;

import com.james090500.BlockGame;
import org.lwjgl.nanovg.NVGColor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.lwjgl.nanovg.NanoVG.*;
public class FontManager {

    private static long vg = BlockGame.getInstance().getClientWindow().getVg();

    static {
        long font = nvgCreateFont(vg, "default", "assets/fonts/Minecraftia-Regular.ttf");
        if (font == -1) {
            throw new RuntimeException("Could not load font");
        }
    }

    private boolean center;

    public static FontManager create() {
        return new FontManager();
    }

    public FontManager center() {
        this.center = true;
        return this;
    }

    public FontManager color(float r, float g, float b, float a) {
        try (NVGColor color = NVGColor.calloc()) {
            color.r(r).g(g).b(b).a(a);
            nvgFillColor(vg, color);
        }
        return this;
    }

    public FontManager uiText(String text, float size, float x, float y) {
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
            nvgText(vg, x + 2, y + 2, text);

            // Text
            nvgFontSize(vg, size);
            nvgFontFace(vg, "default");
            nvgFillColor(vg, textColor);
            nvgText(vg, x, y, text);

            textBackground.free();
            textColor.free();
        }
        return this;
    }

    public FontManager text(String text, float size, float x, float y) {
        int scale = BlockGame.getInstance().getClientWindow().getFramebufferWidth() / BlockGame.getInstance().getClientWindow().getWindowWidth();
        return uiText(text, size * scale, x * scale, y * scale);
    }
}
