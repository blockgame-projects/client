package com.james090500.renderer;

import java.util.ArrayList;
import java.util.List;

public class RenderManager {
    private static final List<Renderer> renderQueue = new ArrayList<>();

    public static void add(Renderer renderer) {
        renderQueue.add(renderer);
    }

    public static void remove(Renderer renderer) {
        renderQueue.remove(renderer);
    }

    public static void render() {
        for (Renderer renderer : renderQueue) {
            renderer.render();
        }
    }
}
