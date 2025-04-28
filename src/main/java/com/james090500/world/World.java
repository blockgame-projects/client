package com.james090500.world;

import com.james090500.renderer.RenderManager;
import com.james090500.renderer.world.ChunkRenderer;

import java.util.HashMap;

public class World {

    private final HashMap<String, Chunk> chunks = new HashMap<>();

    public void createWorld() {
        Chunk chunk = new Chunk(0, 0);

        ChunkRenderer chunkRenderer = new ChunkRenderer();
        chunkRenderer.mesh(chunk);
        RenderManager.add(chunkRenderer);
    }
}
