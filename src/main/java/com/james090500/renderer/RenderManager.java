package com.james090500.renderer;

import com.james090500.BlockGame;

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

    public static void clear() {
        renderQueue.clear();
    }

    public static void render() {
        // Lets not render if not in game!
        if(BlockGame.getInstance().getWorld() == null) {
            renderQueue.clear();
            return;
        }

        List<LayeredRenderer> transparentQueue = new ArrayList<>();

        // Go through the render queue
        for (Renderer renderer : renderQueue) {
            if (renderer instanceof LayeredRenderer) {
                transparentQueue.add((LayeredRenderer) renderer);
            }

            if (BlockGame.getInstance().getCamera().insideFrustum(renderer.getPosition(), renderer.getBoundingBox())) {
                renderer.render();
            }
        }

        // Render any transparent items
        for(LayeredRenderer renderer : transparentQueue) {
            if (BlockGame.getInstance().getCamera().insideFrustum(renderer.getPosition(), renderer.getBoundingBox())) {
                renderer.renderTransparent();
            }
        }
    }
}
