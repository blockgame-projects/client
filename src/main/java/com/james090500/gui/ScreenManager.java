package com.james090500.gui;

import com.james090500.BlockGame;
import com.james090500.utils.TextureManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.nanovg.NVGPaint;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVG.nvgBeginPath;
import static org.lwjgl.nanovg.NanoVG.nvgFill;
import static org.lwjgl.nanovg.NanoVG.nvgFillPaint;
import static org.lwjgl.nanovg.NanoVG.nvgImagePattern;
import static org.lwjgl.nanovg.NanoVG.nvgRect;

public class ScreenManager {

    private static final List<Screen> activeScreens = new ArrayList<>();

    public static void closeAll() {
        activeScreens.clear();
    }

    public static List<Screen> active() {
        return activeScreens;
    }

    public static void add(Screen screen) {
        activeScreens.add(screen);
    }

    public static void remove(Screen screen) {
        activeScreens.remove(screen);
    }

    public static void render() {
        int width = BlockGame.getInstance().getClientWindow().getWidth();
        int height = BlockGame.getInstance().getClientWindow().getHeight();

        long vg = BlockGame.getInstance().getClientWindow().getVg();

        for(Screen screen : activeScreens) {
            // Start NanoVG
            nvgBeginFrame(vg, width, height, BlockGame.getInstance().getClientWindow().getDevicePixelRatio());

            screen.render();

            // End NanoVG
            nvgEndFrame(vg);
        }
    }
}
