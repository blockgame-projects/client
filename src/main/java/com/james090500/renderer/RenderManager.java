package com.james090500.renderer;

import com.james090500.BlockGame;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.joml.Vector3f;

import java.util.Comparator;

public class RenderManager {
    private static final ObjectArrayList<Renderer> renderQueue = new ObjectArrayList<>();
    private static final ObjectArrayList<LayeredRenderer> transparentQueue = new ObjectArrayList<>();

    public static void add(Renderer renderer) {
        renderQueue.add(renderer);

        if(renderer instanceof LayeredRenderer) {
            transparentQueue.add((LayeredRenderer) renderer);
        }

        sort();
    }

    public static void remove(Renderer renderer) {
        renderQueue.remove(renderer);

        if(renderer instanceof LayeredRenderer) {
            transparentQueue.remove((LayeredRenderer) renderer);
        }

        sort();
    }

    private static void sort() {
        Vector3f playerPos = BlockGame.getInstance().getLocalPlayer().getPosition();

        renderQueue.sort(Comparator.comparingDouble((Renderer r) -> r.getPosition().distanceSquared(playerPos)).reversed());
        transparentQueue.sort(Comparator.comparingDouble((LayeredRenderer r) -> r.getPosition().distanceSquared(playerPos)).reversed());
    }

    public static void clear() {
        renderQueue.clear();
        transparentQueue.clear();
    }

    public static void render() {
        // Lets not render if not in game!
        if(BlockGame.getInstance().getWorld() == null) {
            renderQueue.clear();
            transparentQueue.clear();
            return;
        }

        // Go through the render queue
        for (Renderer renderer : renderQueue) {
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

        // Render any entitiess
        BlockGame.getInstance().getWorld().entities.forEach((integer, entity) -> {
            entity.getModel().render();
        });
    }
}
