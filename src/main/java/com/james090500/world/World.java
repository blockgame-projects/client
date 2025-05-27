package com.james090500.world;

import com.james090500.BlockGame;
import com.james090500.blocks.Block;
import com.james090500.renderer.RenderManager;
import lombok.Getter;
import org.joml.Vector3f;

import java.util.*;

public class World {

    private final HashMap<ChunkPos, Chunk> chunks = new HashMap<>();
    private final Map<ChunkPos, List<BlockPlacement>> deferredBlocks = new HashMap<>();

    record ChunkPos(int x, int y) { }
    record BlockPlacement(int x, int y, int z, byte blockId) {}
    record ChunkOffset(int dx, int dz, int distSq) {
        public ChunkOffset(int dx, int dz) {
            this(dx, dz, dx * dx + dz * dz);
        }
    }

    @Getter
    private final int worldSeed = (int) Math.floor(Math.random() * 1000000);
    private final int worldSize = 16;
    private boolean allChunksGenerated = false;

    private int lastPlayerX = 0;
    private int lastPlayerZ = 0;

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
            target.setBlock(x, y, z, block);
        }
    }

    /**
     * Render the world. This also loads and remove chunks as needed
     */
    public void render() {
        Vector3f playerPos = BlockGame.getInstance().getCamera().getPosition();
        int playerPosX = (int) Math.floor(playerPos.x / 16);
        int playerPosZ = (int) Math.floor(playerPos.z / 16);

        // Dont loop if we haven't moved and we have chunks loaded
        if(playerPosX == lastPlayerX && playerPosZ == lastPlayerZ && allChunksGenerated) {
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
                List<BlockPlacement> blockPlacements = deferredBlocks.remove(chunkPos);
                Chunk chunk = new Chunk(chunkX, chunkZ, blockPlacements);
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
        allChunksGenerated = true;
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
            } else {
                allChunksGenerated = false;
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
}
