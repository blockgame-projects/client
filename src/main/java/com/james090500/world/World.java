package com.james090500.world;

import com.james090500.BlockGame;
import com.james090500.blocks.Block;
import com.james090500.renderer.RenderManager;
import com.james090500.utils.ThreadUtil;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class World {

    private final Map<String, Region> regions = new HashMap<>();

    private final HashMap<ChunkPos, Chunk> chunks = new HashMap<>();
    private final Map<ChunkPos, List<BlockPlacement>> deferredBlocks = new HashMap<>();

    public record ChunkPos(int x, int y) { }
    public record BlockPlacement(int x, int y, int z, byte blockId) {}
    public record ChunkOffset(int dx, int dz, int distSq) {}

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Getter
    private int worldSeed;
    private final int worldSize = 16;

    @Getter
    private String worldName;

    @Getter @Setter
    private boolean remote = false;

    private int lastPlayerX = 0;
    private int lastPlayerZ = 0;

    /**
     * Starts a remote world
     */
    public World() {
        this.remote = true;
    }

    /**
     * Start a world instance
     * @param name The name, if exists it will load a world otherwise load a new one
     * @param seed The seed if specified it will use the seed provided or a random.
     */
    public World(String name, String seed) {
        this.worldName = name;
        File worldPath = new File("worlds/" + worldName);
        File worldData = new File(worldPath + "/world.bg");
        if(!worldPath.exists()) {
            // Make world path
            worldPath.mkdirs();

            // Generate seed
            int finalWorldSeed;
            if(!seed.isEmpty()) {
                try {
                    finalWorldSeed = Integer.parseInt(seed);
                } catch (NumberFormatException e) {
                    finalWorldSeed = seed.hashCode();
                }
            } else {
                finalWorldSeed = (int) Math.floor(Math.random() * Integer.MAX_VALUE);
            }
            this.worldSeed = finalWorldSeed;

            // Write to file
            try (RandomAccessFile raf = new RandomAccessFile(worldData, "rw")) {
                raf.writeInt(worldSeed);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try (RandomAccessFile raf = new RandomAccessFile(worldData, "rw")) {
                this.worldSeed = raf.readInt();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        scheduler.scheduleAtFixedRate(this::saveWorld, 5, 5, TimeUnit.MINUTES);
    }

    /**
     * Checks whether the player chunk is loaded
     * @return
     */
    public boolean isChunkLoaded(int x, int z) {
        ChunkPos chunkPos = new ChunkPos(x, z);

        if(this.chunks.containsKey(chunkPos)) {
            Chunk chunk = this.chunks.get(chunkPos);
            return chunk.generated;
        }

        return false;
    }

    /**
     * Get a chunk
     * @param x chunkX
     * @param z chunkZ
     * @return The chunk
     */
    public Chunk getChunk(int x, int z) {
        return this.chunks.get(new ChunkPos(x, z));
    }

    /**
     * Get a block via a Vector3f
     * @param pos Position to check
     * @return
     */
    public Block getBlock(Vector3f pos) {
        return getBlock(
                (int) Math.floor(pos.x),
                (int) Math.floor(pos.y),
                (int) Math.floor(pos.z)
        );
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
        // Adjust for cross-chunk placement
        int offsetChunkX = Math.floorDiv(x, 16);
        chunkX += offsetChunkX;
        x = Math.floorMod(x, 16);

        int offsetChunkZ = Math.floorDiv(z, 16);
        chunkZ += offsetChunkZ;
        z = Math.floorMod(z, 16);

        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
        Chunk target = this.chunks.get(chunkPos);

        if (target == null) {
            synchronized (deferredBlocks) {
                deferredBlocks.computeIfAbsent(chunkPos, k -> new ArrayList<>())
                        .add(new BlockPlacement(x, y, z, block));
            }
            return;
        }

        // Update block and flag for meshing
        target.setBlock(x, y, z, block);
        target.needsUpdate = true;
        target.needsSaving = true;

        // Check if the block is on the chunk border, and update neighbors
        if (x == 0) {
            Chunk left = this.chunks.get(new ChunkPos(chunkX - 1, chunkZ));
            if (left != null) left.needsUpdate = true;
        }
        if (x == 15) {
            Chunk right = this.chunks.get(new ChunkPos(chunkX + 1, chunkZ));
            if (right != null) right.needsUpdate = true;
        }
        if (z == 0) {
            Chunk back = this.chunks.get(new ChunkPos(chunkX, chunkZ - 1));
            if (back != null) back.needsUpdate = true;
        }
        if (z == 15) {
            Chunk front = this.chunks.get(new ChunkPos(chunkX, chunkZ + 1));
            if (front != null) front.needsUpdate = true;
        }
    }

    public void loadRemoteChunk(int chunkX, int chunkZ, byte[] chunkData) {
        ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        Chunk newChunk = new Chunk(pos.x(), pos.y(), null, chunkData);
        chunks.put(pos, newChunk);
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
                queueChunkUpdate(chunk);
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
                if(remote) {
                    //BlockGame.getLogger().info("Asking server for " + pos);
                } else {
                    List<BlockPlacement> blockPlacements = deferredBlocks.remove(pos);

                    // Try and load data from disk
                    //TODO remove this from main thread as its slow
                    byte[] chunkData = loadChunk(pos.x(), pos.y());

                    // Generate chunk from data or new terrain
                    Chunk newChunk;
                    if (chunkData == null) {
                        newChunk = new Chunk(pos.x(), pos.y(), blockPlacements);
                    } else {
                        newChunk = new Chunk(pos.x(), pos.y(), blockPlacements, chunkData);
                    }

                    chunks.put(pos, newChunk);
                }
            }
        }

        // Update required chunks
        chunks.forEach((chunkPos, chunk) -> {
            // Remove chunks no longer needed
            if (!requiredChunks.contains(chunkPos)) {
                if (chunk != null) {
                    RenderManager.remove(chunk.getChunkRenderer());
                    chunk.loaded = false;
                }
            } else {
                queueChunkUpdate(chunk);
            }
        });

        // Remove broken chunks from array
        chunks.keySet().removeIf(pos -> {
            Chunk chunk = chunks.get(pos);
            if(!chunk.loaded) {
                //TODO remove this from main thread as its slow
                chunk.saveChunk();
                return true;
            }
            return false;
        });
    }

    /**
     * Queue a chunk update
     * @param chunk
     */
    private void queueChunkUpdate(Chunk chunk) {
        if(chunk.needsUpdate && !chunk.queued && chunk.loaded && chunk.generated) {
            chunk.queued = true;
            ThreadUtil.getQueue("worldGen").submit(() -> chunk.getChunkRenderer().mesh());
        }
    }

    /**
     *
     */
    public int getChunkCount() {
       return chunks.size();
    }

    public Region getRegion(int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, 32);
        int regionZ = Math.floorDiv(chunkZ, 32);
        String key = regionX + "," + regionZ;

        return regions.computeIfAbsent(key, k -> {
            try {
                return new Region(worldName, regionX, regionZ);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void saveWorld() {
        // Dont save remote worlds
        if(remote) return;

        BlockGame.getLogger().info("Saving World...");
        for(Chunk chunk : this.chunks.values()) {
            if(chunk.needsSaving) {
                BlockGame.getInstance().getWorld().saveChunk(chunk.chunkX, chunk.chunkZ, chunk.chunkData);
                chunk.needsSaving = false;
            }
        }
        BlockGame.getLogger().info("Save Complete!");
    }

    public void saveChunk(int chunkX, int chunkZ, byte[] data) {
        // Dont save remote chunks
        if(remote) return;

        try {
            getRegion(chunkX, chunkZ).saveChunk(chunkX, chunkZ, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] loadChunk(int chunkX, int chunkZ) {
        // Dont load remote chunks
        if(remote) return null;

        try {
            return getRegion(chunkX, chunkZ).loadChunk(chunkX, chunkZ);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Exit the world
     */
    public void exitWorld() {
        this.scheduler.close();
        this.saveWorld();
    }
}
