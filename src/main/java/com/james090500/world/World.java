package com.james090500.world;

import com.james090500.blocks.Block;
import com.james090500.renderer.RenderManager;
import com.james090500.renderer.world.ChunkRenderer;
import lombok.Getter;

import java.util.HashMap;
import java.util.Objects;

public class World {

    private final HashMap<ChunkPos, Chunk> chunks = new HashMap<>();

    @Getter
    private final int worldSeed = (int) Math.floor(Math.random() * 1000000);
    private final int worldSize = 12;

    public void createWorld() {
        for(int x = -this.worldSize; x < this.worldSize; x++) {
            for(int z = -this.worldSize; z < this.worldSize; z++) {
                Chunk chunk = new Chunk(x, z);
                chunks.put(new ChunkPos(x, z), chunk);
            }
        }
    }

    public Block getChunkBlock(int chunkX, int chunkZ, int x, int y, int z) {
        int offsetChunkX = Math.floorDiv(x, 16);
        chunkX += offsetChunkX;
        x = Math.floorMod(x, 16);

        int offsetChunkZ = Math.floorDiv(z, 16);
        chunkZ += offsetChunkZ;
        z = Math.floorMod(z, 16);

        Chunk target = this.chunks.get(new ChunkPos(chunkX, chunkZ));
        if (target == null) {
            return null;
        }

        return target.getBlock(x, y, z);
    }

    public void render() {
        this.chunks.forEach((pos, chunk) -> {
            if(chunk.generated && !chunk.rendered) {
                ChunkRenderer chunkRenderer = new ChunkRenderer();
                chunkRenderer.mesh(chunk);
                chunkRenderer.meshTransparent(chunk);

                chunk.rendered = true;

                RenderManager.add(chunkRenderer);
            }
        });
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
