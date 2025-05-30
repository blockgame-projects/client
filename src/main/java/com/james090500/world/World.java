package com.james090500.world;

import com.james090500.BlockGame;
import com.james090500.blocks.Block;
import com.james090500.renderer.RenderManager;
import com.james090500.utils.ThreadUtil;
import lombok.Getter;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ExecutorService;

public class World {

    private final HashMap<ChunkPos, Chunk> chunks = new HashMap<>();
    private final Map<ChunkPos, List<BlockPlacement>> deferredBlocks = new HashMap<>();

    record ChunkPos(int x, int y) { }
    record BlockPlacement(int x, int y, int z, byte blockId) {}
    record ChunkOffset(int dx, int dz, int distSq) {}

    @Getter
    private final int worldSeed = (int) Math.floor(Math.random() * 1000000);
    private final int worldSize = 4;

    private int lastPlayerX = 0;
    private int lastPlayerZ = 0;

    /**
     * Checks whether the player chunk is loaded
     * @return
     */
    public boolean isChunkLoaded() {
        Vector3f playerPos = BlockGame.getInstance().getCamera().getPosition();
        int playerPosX = (int) Math.floor(playerPos.x / 16);
        int playerPosZ = (int) Math.floor(playerPos.z / 16);

        ChunkPos chunkPos = new ChunkPos(playerPosX, playerPosZ);

        if(this.chunks.containsKey(chunkPos)) {
            Chunk chunk = this.chunks.get(chunkPos);
            return chunk.generated && !chunk.needsUpdate;
        }

        return false;
    }

    /**
     * Gets a block in the world
     * @param x The world x coord
     * @param y The world y coord
     * @param z The world z coord
     * @return The block or null if no block
     */
    public Block getBlock(int x, int y, int z) {
        return this.getChunkBlock(0, 0, x, y, z);
    }

    /**
     * Sets a block in the world
     * @param x The world x coord
     * @param y The world y coord
     * @param z The world z coord
     * @param block The block or null if no block
     */
    public void setBlock(int x, int y, int z, byte block) {
        this.setChunkBlock(0, 0, x, y, z, block);
    }

    /**
     * Get a block from a specific chunk
     * @param chunkX The chunk X coordinate
     * @param chunkZ The chunk Z coordinate
     * @param x The localised x coordinate
     * @param y The localised y coordinate
     * @param z The localised z coordinate
     * @return The block or null if no block
     */
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

    /**
     * Sets a block at a specific chunk
     * @param chunkX The chunk X coordinate
     * @param chunkZ The chunk Z coordinate
     * @param x The localised x coordinate
     * @param y The localised y coordinate
     * @param z The localised z coordinate
     * @param block The block
     */
    public void setChunkBlock(int chunkX, int chunkZ, int x, int y, int z, byte block) {
        int offsetChunkX = Math.floorDiv(x, 16);
        chunkX += offsetChunkX;
        x = Math.floorMod(x, 16);

        int offsetChunkZ = Math.floorDiv(z, 16);
        chunkZ += offsetChunkZ;
        z = Math.floorMod(z, 16);

        Chunk target = this.chunks.get(new ChunkPos(chunkX, chunkZ));
        if (target == null) {
            ChunkPos deferredPos = new ChunkPos(chunkX, chunkZ);
            synchronized (deferredBlocks) {
                deferredBlocks.computeIfAbsent(deferredPos, k -> new ArrayList<>()).add(new BlockPlacement(x, y, z, block));
            }
        } else {
            target.needsUpdate = true;
            target.setBlock(x, y, z, block);
        }
    }

    /**
     * update the world. This also loads and remove chunks as needed
     */
    public void update() {
        Vector3f playerPos = BlockGame.getInstance().getCamera().getPosition();
        int playerChunkX = (int) Math.floor(playerPos.x / 16);
        int playerChunkZ = (int) Math.floor(playerPos.z / 16);

        if (playerChunkX == lastPlayerX && playerChunkZ == lastPlayerZ && !chunks.isEmpty()) {
            // Only update mesh if chunk data has changed
            for (Map.Entry<ChunkPos, Chunk> entry : chunks.entrySet()) {
                Chunk chunk = entry.getValue();
                if (chunk.needsUpdate && !chunk.queued) {
                    queueChunkUpdate(chunk);
                }
            }
            return;
        }

        lastPlayerX = playerChunkX;
        lastPlayerZ = playerChunkZ;

        // Load/generate nearby chunks in render distance
        List<ChunkOffset> offsets = new ArrayList<>();
        for (int dx = -worldSize; dx <= worldSize; dx++) {
            for (int dz = -worldSize; dz <= worldSize; dz++) {
                int distSq = dx * dx + dz * dz;
                if (distSq > worldSize * worldSize) continue;
                offsets.add(new ChunkOffset(dx, dz, distSq));
            }
        }

        offsets.sort(Comparator.comparingInt(ChunkOffset::distSq)); // Closest first

        // Render chunks from players pos.
        Set<ChunkPos> requiredChunks = new HashSet<>();
        for (ChunkOffset offset : offsets) {
            ChunkPos pos = new ChunkPos(playerChunkX + offset.dx(), playerChunkZ + offset.dz());
            requiredChunks.add(pos);

            if (!chunks.containsKey(pos)) {
                List<BlockPlacement> blockPlacements = deferredBlocks.remove(pos);
                Chunk newChunk = new Chunk(pos.x(), pos.y(), blockPlacements);
                chunks.put(pos, newChunk);
                queueChunkUpdate(newChunk);
            }
        }

        // Remove chunks no longer needed
        chunks.keySet().removeIf(pos -> {
            if (!requiredChunks.contains(pos)) {
                Chunk toRemove = chunks.get(pos);
                ThreadUtil.getMainQueue().add(() -> RenderManager.remove(toRemove.getChunkRenderer()));
                return true;
            }
            return false;
        });

        // Queue any modified chunks
        for (Chunk chunk : chunks.values()) {
            if (chunk.needsUpdate && !chunk.queued) {
                queueChunkUpdate(chunk);
            }
        }
    }

    private void queueChunkUpdate(Chunk chunk) {
        chunk.queued = true;
        ThreadUtil.getQueue("worldGen").submit(() -> {
            chunk.getChunkRenderer().mesh();
            chunk.needsUpdate = false;
            chunk.queued = false;

            ThreadUtil.getMainQueue().add(() -> {
                RenderManager.remove(chunk.getChunkRenderer());
                RenderManager.add(chunk.getChunkRenderer());
            });
        });
    }
}
