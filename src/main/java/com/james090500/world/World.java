package com.james090500.world;

import com.james090500.blocks.Block;
import com.james090500.renderer.RenderManager;
import com.james090500.renderer.world.ChunkRenderer;

import java.util.HashMap;
import java.util.Objects;

public class World {

    private final HashMap<ChunkPos, Chunk> chunks = new HashMap<>();

    public void createWorld() {
        Chunk chunk = new Chunk(0, 0);
        chunks.put(new ChunkPos(0, 0), chunk);

        ChunkRenderer chunkRenderer = new ChunkRenderer();
        chunkRenderer.mesh(chunk);
        RenderManager.add(chunkRenderer);
    }

    public Block getChunkBlock(int chunkX, int chunkZ, int x, int y, int z) {
        // Adjust X coordinate and chunk
        int offsetChunkX = (int) (double) (x / 16);
        chunkX += offsetChunkX;
        x = ((x % 16) + 16) % 16;

        // Adjust Z coordinate and chunk
        int offsetChunkZ = (int) (double) (z / 16);
        chunkZ += offsetChunkZ;
        z = ((z % 16) + 16) % 16;

        Chunk target = this.chunks.get(new ChunkPos(chunkX, chunkZ));
        if (target == null) {
            return null;
        }

        return target.getBlock(x, y, z);
    }

    /**
     * A special hash mapping class
     */
    static class ChunkPos {
        int x, y;

        ChunkPos(int x, int y) {
            this.x = x;
            this.y = y;
        }

        // Ensure proper hash-based lookup
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ChunkPos other)) return false;
            return x == other.x && y == other.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }
}
