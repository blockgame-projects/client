package com.james090500.world;

import com.james090500.BlockGame;
import com.james090500.blocks.Block;
import com.james090500.blocks.Blocks;
import com.james090500.renderer.world.ChunkRenderer;
import com.james090500.structure.*;
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

    private boolean visible = true;
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
    public int getIndex(int x, int y, int z) {
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
        return Blocks.get(blockID);
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
        if (this.queued || !this.visible || this.chunkStatus == ChunkStatus.FINISHED) return;

        this.queued = true;
        ThreadUtil.getQueue("worldGen").submit(() -> {
            if (this.chunkStatus == ChunkStatus.EMPTY) {
                this.chunkStatus = ChunkStatus.TERRAIN;
                this.generateTerrain();
            } else if (this.chunkStatus == ChunkStatus.TERRAIN) {
                if (isNeighbors(ChunkStatus.EMPTY)) {
                    this.chunkStatus = ChunkStatus.DECORATIONS;
                    this.generateDecorations();
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
        int lowestLevel = 45;
        int waterLevel = 64;

        for (int x = 0; x < chunkSize; x++) {
            for (int z = 0; z < chunkSize; z++) {
                int nx = x + this.chunkX * this.chunkSize;
                int nz = z + this.chunkZ * this.chunkSize;

                Biomes biome = BiomeGenerator.getBiome(nx, nz);

                int topSoilDepth = -1;

                for (int y = this.chunkHeight - 1; y >= 0; y--) {
                    double density = NoiseManager.chunkNoise(nx, y, nz);

                    // your original heightFactor adjustments (keeps caves/overhang feel)
                    double heightFactor = (waterLevel - y) / (double) waterLevel;

                    if (y > lowestLevel) {
                        if (y > waterLevel) {
                            if (density > 0.35 && (biome.equals(Biomes.FOREST) || biome.equals(Biomes.PLAINS))) {
                                density += heightFactor / 2;
                            } else {
                                density += heightFactor * 1.5;
                            }
                        } else {
                            density += heightFactor;
                        }
                    } else {
                        density += heightFactor * 6.0;
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
    private void generateDecorations() {
        int treeSeed = BlockGame.getInstance().getWorld().getWorldSeed() + 2390; // Don't follow terrain otherwise it looks odd
        int foilageSeed = BlockGame.getInstance().getWorld().getWorldSeed() + 1965; // Don't follow terrain otherwise it looks odd

        for (int x = 0; x < this.chunkSize; x++) {
            for (int z = 0; z < this.chunkSize; z++) {
                int nx = x + this.chunkX * this.chunkSize;
                int nz = z + this.chunkZ * this.chunkSize;

                Biomes biome = BiomeGenerator.getBiome(nx, nz);
                double treeNoise = OpenSimplexNoise.noise2_ImproveX(treeSeed, nx, nz);
                double foilageNoise = OpenSimplexNoise.noise2_ImproveX(foilageSeed, nx, nz);

                double treeNoiseCap;
                double foilageNoiseCap;

                Structure treeStructure;
                Structure foilageStructure = new FoilageStructure(foilageNoise, this);

                if (biome.equals(Biomes.FOREST) || biome.equals(Biomes.PLAINS)) {
                    treeNoiseCap = biome.equals(Biomes.FOREST) ? 0.75 : 0.90;
                    foilageNoiseCap = 0;

                    if(treeNoise > 0.95) {
                        treeStructure = new BirchTree(treeNoise, this);
                    } else {
                        treeStructure = new OakTree(treeNoise, this);
                    }
                } else if(biome.equals(Biomes.DESERT)) {
                    treeNoiseCap = 0.90;
                    foilageNoiseCap = 10; //Impossible

                    treeStructure = new Cactus(treeNoise, this);
                } else if(biome.equals(Biomes.TAIGA)) {
                    treeNoiseCap = 0.90;
                    foilageNoiseCap = 10; //Impossible

                    treeStructure = new SpruceTree(treeNoise, this);
                } else {
                    return;
                }


                if (treeNoise > treeNoiseCap) {
                    for (int y = this.chunkHeight - 1; y >= 0; y--) {
                        if(treeStructure.build(x, y, z)) break;
                    }
                }

                if (foilageNoise > foilageNoiseCap) {
                    for (int y = this.chunkHeight - 1; y >= 0; y--) {
                        if(foilageStructure.build(x, y, z)) break;
                    }
                }
            }
        }
    }

    public void saveChunk() {
        if(this.chunkData != null && this.needsSaving && !BlockGame.getInstance().getWorld().isRemote()) {
            this.needsSaving = false;
            ThreadUtil.getQueue("worldDisk").submit(() -> BlockGame.getInstance().getWorld().saveChunk(this));
        }
    }
}