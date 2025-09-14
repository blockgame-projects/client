package com.james090500.world;

import com.james090500.BlockGame;
import com.james090500.blocks.Block;
import com.james090500.blocks.Blocks;
import com.james090500.blocks.GrassBlock;
import com.james090500.blocks.WaterBlock;
import com.james090500.renderer.world.ChunkRenderer;
import com.james090500.structure.Tree;
import com.james090500.utils.NoiseManager;
import com.james090500.utils.OpenSimplexNoise;
import com.james090500.utils.ThreadUtil;
import lombok.Getter;

public class Chunk {

    public final byte[] chunkData;

    @Getter
    private final ChunkRenderer chunkRenderer = new ChunkRenderer(this);

    public final int chunkSize = 16;
    public final int chunkHeight = 300;
    public final int chunkX;
    public final int chunkZ;

    private boolean queued = false;

    public boolean needsMeshing = false;
    public boolean needsSaving = false;

    public ChunkStatus chunkStatus = ChunkStatus.EMPTY;

    public Chunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;

        this.chunkData = new byte[chunkSize * chunkSize * chunkHeight];
    }

    public Chunk(int chunkX, int chunkZ, byte[] chunkData) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.chunkData = chunkData;

        this.chunkStatus = ChunkStatus.FINISHED;
        this.needsMeshing = true;
    }

    /**
     * Gets the index from the byte array
     *
     * @param x The x in the chunk
     * @param y The y in the chunk
     * @param z The z in the chunk
     * @return The index returned (null/-1 if invalid)
     */
    private int getIndex(int x, int y, int z) {
        if (
                x >= this.chunkSize || x < 0 ||
                        y >= this.chunkHeight || y < 0 ||
                        z >= this.chunkSize || z < 0
        ) {
            return -1;
        }

        return x + this.chunkSize * (y + this.chunkHeight * z);
    }

    /**
     * Gets a block in chunk coordinates
     *
     * @param x The x in the chunk
     * @param y The y in the chunk
     * @param z The z in the chunk
     */
    public Block getBlock(int x, int y, int z) {
        if (
                x >= this.chunkSize || x < 0 ||
                        y >= this.chunkHeight || y < 0 ||
                        z >= this.chunkSize || z < 0
        ) {
            return null;
        }

        int index = this.getIndex(x, y, z);
        int blockID = this.chunkData[index];
        return Blocks.ids[blockID];
    }

    /**
     * Sets a block in chunk coordinates
     *
     * @param x The x in the chunk
     * @param y The y in the chunk
     * @param z The z in the chunk
     */
    public void setBlock(int x, int y, int z, byte block) {
        if (
                x >= this.chunkSize || x < 0 ||
                        y >= this.chunkHeight || y < 0 ||
                        z >= this.chunkSize || z < 0
        ) {
            BlockGame.getInstance().getWorld().setChunkBlock(chunkX, chunkZ, x, y, z, block);
        } else {
            this.chunkData[this.getIndex(x, y, z)] = block;
        }
    }

    /**
     * Are our neighbours a minimum specific status?
     * @return
     */
    public boolean isNeighbors(ChunkStatus chunkStatus) {
        return BlockGame.getInstance().getWorld().isChunkStatus(chunkX + 1, chunkZ, chunkStatus) &&
                BlockGame.getInstance().getWorld().isChunkStatus(chunkX - 1,chunkZ, chunkStatus) &&
                BlockGame.getInstance().getWorld().isChunkStatus(chunkX, chunkZ + 1, chunkStatus) &&
                BlockGame.getInstance().getWorld().isChunkStatus(chunkX, chunkZ - 1, chunkStatus);
    }

    /**
     * Handle chunk generation
     */
    public void generate() {
        if (this.queued || this.chunkStatus == ChunkStatus.FINISHED) return;

        this.queued = true;
        ThreadUtil.getQueue("worldGen").submit(() -> {
            if (this.chunkStatus == ChunkStatus.EMPTY) {
                this.chunkStatus = ChunkStatus.TERRAIN;
                this.generateTerrain();
            } else if (this.chunkStatus == ChunkStatus.TERRAIN) {
                if (isNeighbors(ChunkStatus.EMPTY)) {
                    this.chunkStatus = ChunkStatus.DECORATIONS;
                    this.generateTrees();
                }
            } else if (this.chunkStatus == ChunkStatus.DECORATIONS) {
                // We have finished generation time to mesh
                this.chunkStatus = ChunkStatus.FINISHED;
                this.needsMeshing = true;
            }

            this.queued = false;
        });
    }
    /**
     * Generates the actual terrain
     */
    private void generateTerrain() {
        int landLevel = 70;
        int waterLevel = 64;

        for (int x = 0; x < chunkSize; x++) {
            for (int z = 0; z < chunkSize; z++) {
                int nx = x + this.chunkX * this.chunkSize;
                int nz = z + this.chunkZ * this.chunkSize;

                Biomes biome = BiomeGenerator.getBiome(nx, nz);

                // column elevation noise is 0..1
                double colElev = (NoiseManager.elevationNoise(nx, nz) + 1.0) / 2.0; // already 0..1 per you
                int colTargetY = mapElevToSurfaceY(colElev, waterLevel, this.chunkHeight - 1);

                boolean enforceBelowWater = (colElev < 0.5); // only force down when elevation ≤ 0.5

                int topSoilDepth = -1;

                for (int y = this.chunkHeight - 1; y >= 0; y--) {
                    double density = NoiseManager.chunkNoise(biome, nx, y, nz);

                    // your original heightFactor adjustments (keeps caves/overhang feel)
                    double heightFactor = (landLevel - y) / (double) landLevel;

                    // *** NEW: if this column must be below water, smoothly push density negative above colTargetY ***
                    if (enforceBelowWater) {
                        // smoothstep goes 0 at colTargetY and 1 at (colTargetY + FORCE_TRANSITION)
                        density += heightFactor * 2.0;
                        double t = smoothstep(colTargetY, colTargetY + 6, y);
                        density -= t * 2.0;
                        // result: density unaffected below target, gradually reduced above target,
                        // strongly reduced above target + FORCE_TRANSITION.
                    } else if (y > waterLevel) {
                        if (density > 0.35) {
                            density += heightFactor;
                        } else {
                            density += heightFactor * 2.0;
                        }
                    } else {
                        density += heightFactor * 6.0;
                        density += (smoothstep(colTargetY, colTargetY + 6, y) * 2.0);
                    }

                    byte nextBlock = 0;

                    if (density >= 0) {
                        if (topSoilDepth == -1) {
                            if(y < waterLevel || biome.equals(Biomes.DESERT) || biome.equals(Biomes.OCEAN)) {
                                nextBlock = Blocks.sandBlock.getId();
                            } else if(biome.equals(Biomes.TAIGA)) {
                                nextBlock = Blocks.snowyGrassBlock.getId();
                            } else {
                                nextBlock = Blocks.grassBlock.getId();
                            }
                            topSoilDepth++;
                        } else if (topSoilDepth < 3) {
                            if(!biome.equals(Biomes.DESERT)) {
                                nextBlock = Blocks.dirtBlock.getId();
                            } else {
                                nextBlock = Blocks.sandBlock.getId();
                            }
                            topSoilDepth++;
                        } else {
                            nextBlock = Blocks.stoneBlock.getId();
                        }
                    } else {
                        if (y <= waterLevel) {
                            nextBlock = Blocks.waterBlock.getId();
                        }
                        topSoilDepth = -1;
                    }

                    if (nextBlock != 0) {
                        this.setBlock(x, y, z, nextBlock);
                    }
                }
            }
        }
    }

    /**
     * Generate trees
     */
    private void generateTrees() {
        int treeSeed = BlockGame.getInstance().getWorld().getWorldSeed() + 2390; // Don't follow terrain otherwise it looks odd

        for (int x = 0; x < this.chunkSize; x++) {
            for (int z = 0; z < this.chunkSize; z++) {
                int nx = x + this.chunkX * this.chunkSize;
                int nz = z + this.chunkZ * this.chunkSize;

                Biomes biome = BiomeGenerator.getBiome(nx, nz);
                double treeCap;

                if (biome.equals(Biomes.FOREST)) {
                    treeCap = 0.75;
                } else if(biome.equals(Biomes.PLAINS)) {
                    treeCap = 0.90;
                } else {
                    return;
                }

                double noise = OpenSimplexNoise.noise2(treeSeed, nx, nz);
                if (noise > treeCap) {
                    for (int y = this.chunkHeight - 1; y >= 0; y--) {
                        Block block = this.getBlock(x, y, z);
                        if (block instanceof GrassBlock) {
                            Tree tree = new Tree(noise, this);
                            tree.build(x, y, z);
                        }
                    }
                }
            }
        }
    }

    private int mapElevToSurfaceY(double elev01, int waterLevel, int maxHeight) {
        if (elev01 <= 0.5) {
            return (int)Math.round((elev01 / 0.5) * waterLevel);
        } else {
            return waterLevel + (int)Math.round(((elev01 - 0.5) / 0.5) * (maxHeight - waterLevel));
        }
    }

    private static double smoothstep(double edge0, double edge1, double y) {
        if (edge0 == edge1) return y < edge0 ? 0.0 : 1.0;
        double t = (y - edge0) / (edge1 - edge0);
        t = Math.clamp(t, 0.0, 1.0);
        return t * t * (3.0 - 2.0 * t);
    }

    public void saveChunk() {
        if(this.chunkData != null && this.needsSaving && !BlockGame.getInstance().getWorld().isRemote()) {
            this.needsSaving = false;
            ThreadUtil.getQueue("worldDisk").submit(() -> BlockGame.getInstance().getWorld().saveChunk(this));
        }
    }
}