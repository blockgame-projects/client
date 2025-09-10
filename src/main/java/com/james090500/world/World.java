package com.james090500.world;

import com.james090500.BlockGame;
import com.james090500.blocks.Block;
import com.james090500.blocks.Blocks;
import com.james090500.entity.Entity;
import com.james090500.entity.PlayerEntity;
import com.james090500.network.packets.BlockUpdatePacket;
import com.james090500.network.packets.DisconnectPacket;
import com.james090500.renderer.RenderManager;
import com.james090500.utils.SoundManager;
import com.james090500.utils.ThreadUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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

    private final Object2ObjectMap<ChunkPos, Chunk> chunks = new Object2ObjectOpenHashMap<>();

    public record ChunkPos(int x, int y) { }
    public record ChunkOffset(int dx, int dz, int distSq) {}

    public final Int2ObjectOpenHashMap<Entity> entities = new Int2ObjectOpenHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private int lastPlayerX;
    private int lastPlayerZ;

    @Getter
    private int worldSeed;
    private final int worldSize = 16;

    @Getter
    private String worldName;

    @Getter @Setter
    private boolean remote = false;

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
     * Checks whether the player chunk exists and is a specific status
     * @return
     */
    public boolean isChunkStatus(int x, int z, ChunkStatus status) {
        ChunkPos chunkPos = new ChunkPos(x, z);
        if(this.chunks.containsKey(chunkPos)) {
            Chunk chunk = this.chunks.get(chunkPos);
            return chunk.chunkStatus.ordinal() >= status.ordinal();
        }
        return this.chunks.containsKey(chunkPos);
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
        Block currentBlock = block == 0 ? BlockGame.getInstance().getWorld().getBlock(x, y, z) : Blocks.ids[block];
        SoundManager.play("assets/sound/block/" + currentBlock.getSound(), 4);

        if(this.remote) {
            BlockUpdatePacket blockUpdatePacket = new BlockUpdatePacket(x, y, z, block);
            blockUpdatePacket.write(BlockGame.getInstance().getChannel());
        }

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
            BlockGame.getLogger().severe("Block tried to place outside area");
            return;
        }

        // Update block and flag for meshing
        target.setBlock(x, y, z, block);
        target.needsMeshing = true;
        target.needsSaving = true;

        // Check if the block is on the chunk border, and update neighbors
        if (x == 0) {
            Chunk left = this.chunks.get(new ChunkPos(chunkX - 1, chunkZ));
            if (left != null) left.needsMeshing = true;
        }
        if (x == 15) {
            Chunk right = this.chunks.get(new ChunkPos(chunkX + 1, chunkZ));
            if (right != null) right.needsMeshing = true;
        }
        if (z == 0) {
            Chunk back = this.chunks.get(new ChunkPos(chunkX, chunkZ - 1));
            if (back != null) back.needsMeshing = true;
        }
        if (z == 15) {
            Chunk front = this.chunks.get(new ChunkPos(chunkX, chunkZ + 1));
            if (front != null) front.needsMeshing = true;
        }
    }

    public void loadRemoteChunk(int chunkX, int chunkZ, byte[] chunkData) {
        ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        Chunk newChunk = new Chunk(pos.x(), pos.y(), chunkData);
        chunks.put(pos, newChunk);
    }

    /**
     * update the world. This also loads and remove chunks as needed
     */
    public void update() {
        Vector3f playerPos = BlockGame.getInstance().getCamera().getPosition();
        int playerChunkX = (int) Math.floor(playerPos.x / 16);
        int playerChunkZ = (int) Math.floor(playerPos.z / 16);

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

        for (ChunkOffset offset : offsets) {
            ChunkPos pos = new ChunkPos(playerChunkX + offset.dx(), playerChunkZ + offset.dz());
            Chunk chunk = chunks.get(pos);
            if (chunk != null) {
                chunk.generate();
                this.queueChunkUpdate(chunk);
            }
        }

        // No point looping if we aren't moving
        if(playerChunkX == lastPlayerX && playerChunkZ == lastPlayerZ && !chunks.isEmpty()) return;

        // Render chunks from players pos.
        Set<ChunkPos> requiredChunks = new HashSet<>();
        for (ChunkOffset offset : offsets) {
            ChunkPos pos = new ChunkPos(playerChunkX + offset.dx(), playerChunkZ + offset.dz());
            requiredChunks.add(pos);

            if (!chunks.containsKey(pos)) {
                if(remote) {
                    //BlockGame.getLogger().info("Asking server for " + pos);
                } else {
                    // Try and load data from disk
                    //TODO remove this from main thread as its slow
                    byte[] chunkData = loadChunk(pos.x(), pos.y());

                    // Generate chunk from data or new terrain
                    Chunk newChunk;
                    if (chunkData == null) {
                        newChunk = new Chunk(pos.x(), pos.y());
                    } else {
                        newChunk = new Chunk(pos.x(), pos.y(), chunkData);
                    }

                    chunks.put(pos, newChunk);
                }
            }
        }

        // Loop through chunks generating and removing invalid ones
        chunks.entrySet().removeIf(entry -> {
            ChunkPos chunkPos = entry.getKey();
            Chunk chunk = entry.getValue();

            if (!requiredChunks.contains(chunkPos)) {
                if (chunk != null) {
                    RenderManager.remove(chunk.getChunkRenderer());
                    //TODO remove this from main thread as its slow
                    chunk.saveChunk();
                    return true;
                }
            }

            return false;
        });

        this.lastPlayerX = playerChunkX;
        this.lastPlayerZ = playerChunkZ;
    }

    /**
     * Queue a chunk update
     * @param chunk
     */
    private void queueChunkUpdate(Chunk chunk) {
        if(chunk.needsMeshing) {
            chunk.needsMeshing = false;
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
        if(this.remote) {
            DisconnectPacket disconnectPacket = new DisconnectPacket();
            disconnectPacket.write(BlockGame.getInstance().getChannel());
        } else {
            this.scheduler.close();
            this.saveWorld();
        }
    }
}
