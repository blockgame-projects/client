package com.james090500.gui;

import java.util.ArrayList;
import java.util.List;

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
        for(Screen screen : activeScreens) {
            screen.render();
        }
    }
}
