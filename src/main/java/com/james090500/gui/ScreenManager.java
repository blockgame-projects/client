package com.james090500.gui;

import com.james090500.BlockGame;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.nanovg.NanoVG.*;

public class ScreenManager {

    private static final List<Screen> activeScreens = new ArrayList<>();

    public static void clear() {
        activeScreens.clear();
        activeScreens.add(new DebugScreen());
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
        long vg = BlockGame.getInstance().getClientWindow().getVg();

        float baseWidth = BlockGame.getInstance().getClientWindow().getBaseWidth();
        float baseHeight = BlockGame.getInstance().getClientWindow().getBaseHeight();

        int fbWidth = BlockGame.getInstance().getClientWindow().getFramebufferWidth();
        int fbHeight = BlockGame.getInstance().getClientWindow().getFramebufferHeight();

        float scaleX = (float) fbWidth / baseWidth;
        float scaleY = (float) fbHeight / baseHeight;
        float scale = Math.min(scaleX, scaleY);

        float offsetX = (fbWidth - baseWidth * scale) / 2f;
        float offsetY = (fbHeight - baseHeight * scale) / 2f;

        nvgBeginFrame(vg, fbWidth, fbHeight, 1f);

        nvgSave(vg);
        {
            for (Screen screen : activeScreens) {
                if(screen.isOverlay()) {
                    screen.renderOverlay();
                } else if(screen.isBackground()){
                    screen.renderBackground();
                }

                if(screen.isInGame()) {
                    screen.render();
                }
            }
        }
        nvgRestore(vg);

        nvgSave(vg);
        nvgTranslate(vg, offsetX, offsetY);
        nvgScale(vg, scale, scale);

        for (Screen screen : activeScreens) {
            if(!screen.isInGame()) {
                screen.render();
            }
        }

        nvgRestore(vg);
        nvgEndFrame(vg);
    }
}
