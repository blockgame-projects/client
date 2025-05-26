package com.james090500.world;

import com.james090500.BlockGame;
import com.james090500.blocks.Block;
import com.james090500.renderer.RenderManager;
import com.james090500.renderer.world.ChunkRenderer;
import lombok.Getter;
import org.joml.Vector3f;

import java.lang.reflect.Array;
import java.util.*;

public class World {

    private final HashMap<ChunkPos, Chunk> chunks = new HashMap<>();

    @Getter
    private final int worldSeed = (int) Math.floor(Math.random() * 1000000);
    private final int worldSize = 16;

    private int lastPlayerX = -9999999;
    private int lastPlayerZ = -9999999;

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
        Vector3f playerPos = BlockGame.getInstance().getCamera().getPosition();
        int playerPosX = (int) Math.floor(playerPos.x / 16);
        int playerPosZ = (int) Math.floor(playerPos.z / 16);

        // Dont loop if we haven't moved
        if(playerPosX == lastPlayerX && playerPosZ == lastPlayerZ) {
            return;
        }

        List<ChunkOffset> offsets = new ArrayList<>();
        int worldSizeSq = worldSize * worldSize;

        // Generate all offsets in a circle around the player
        for (int dx = -worldSize; dx <= worldSize; dx++) {
            for (int dz = -worldSize; dz <= worldSize; dz++) {
                int distSq = dx * dx + dz * dz;
                if (distSq <= worldSizeSq) {
                    offsets.add(new ChunkOffset(dx, dz));
                }
            }
        }

        // Sort offsets to radiate outward
        offsets.sort(Comparator.comparingInt(o -> o.distSq));

        // Create missing chunks in order of distance
        for (ChunkOffset offset : offsets) {
            int chunkX = playerPosX + offset.dx;
            int chunkZ = playerPosZ + offset.dz;

            ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
            if (!this.chunks.containsKey(chunkPos)) {
                Chunk chunk = new Chunk(chunkX, chunkZ);
                this.chunks.put(chunkPos, chunk);

                // Invalidate neighboring chunks for re-rendering
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dz == 0) continue;

                        ChunkPos neighborPos = new ChunkPos(chunkX + dx, chunkZ + dz);
                        Chunk neighbor = this.chunks.get(neighborPos);
                        if (neighbor != null) {
                            neighbor.rendered = false; // Force it to re-mesh next frame
                        }
                    }
                }
            }
        }

        // Mesh and render unrendered chunks in same distance order
        for (ChunkOffset offset : offsets) {
            int chunkX = playerPosX + offset.dx;
            int chunkZ = playerPosZ + offset.dz;

            ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
            Chunk chunk = this.chunks.get(chunkPos);

            if (chunk != null && chunk.generated && !chunk.rendered) {
                RenderManager.remove(chunk.getChunkRenderer());
                chunk.getChunkRenderer().mesh();              // opaque first
                chunk.getChunkRenderer().meshTransparent();   // then transparent
                chunk.rendered = true;
                RenderManager.add(chunk.getChunkRenderer());
            }
        }

        // Unload distant chunks
        Iterator<Map.Entry<ChunkPos, Chunk>> iterator = chunks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ChunkPos, Chunk> entry = iterator.next();
            ChunkPos chunkPos = entry.getKey();

            int dx = chunkPos.x - playerPosX;
            int dz = chunkPos.y - playerPosZ;
            int distSq = dx * dx + dz * dz;

            if (distSq > worldSizeSq) {
                RenderManager.remove(entry.getValue().getChunkRenderer());
                iterator.remove();
            }
        }

        // Update last player chunk position after work is done
        this.lastPlayerX = playerPosX;
        this.lastPlayerZ = playerPosZ;
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

    static class ChunkOffset {
        public int dx, dz;
        public int distSq;

        public ChunkOffset(int dx, int dz) {
            this.dx = dx;
            this.dz = dz;
            this.distSq = dx * dx + dz * dz;
        }
    }
}
