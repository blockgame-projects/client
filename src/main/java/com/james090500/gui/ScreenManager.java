package com.james090500.gui;

import com.james090500.BlockGame;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.nanovg.NanoVG.*;

public class ScreenManager {

    private static final List<Screen> activeScreens = new ArrayList<>();

    public static void clear() {
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
