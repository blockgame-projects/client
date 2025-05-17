package com.james090500.world;

import com.james090500.BlockGame;
import com.james090500.blocks.Block;
import com.james090500.blocks.Blocks;
import com.james090500.utils.OpenSimplexNoise;
import lombok.Getter;

public class Chunk {

    private byte[] chunkData;

    public final int chunkSize = 16;
    public final int chunkHeight = 300;
    public final int chunkX;
    public final int chunkZ;

    public boolean rendered;
    public boolean generated;

    public Chunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;

        this.chunkData = new byte[chunkSize * chunkSize * chunkHeight];

        this.generateTerrain();

        this.generated = true;
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

        int blockID = this.chunkData[this.getIndex(x, y, z)];
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
            return;
        }

        this.chunkData[this.getIndex(x, y, z)] = block;
    }

    private void generateTerrain() {
        int waterLevel = 64;

        for (int x = 0; x < chunkSize; x++) {
            for (int z = 0; z < chunkSize; z++) {
                int nx = x + this.chunkX * this.chunkSize;
                int nz = z + this.chunkZ * this.chunkSize;

                boolean beach = false;
                int topSoilDepth = -1;

                for (int y = this.chunkHeight - 1; y >= 0; y--) {
                    // terrain shaping
                    double density = this.octaveNoise3D(nx, y, nz);

                    double heightFactor = (waterLevel - y) / (double) waterLevel;
                    if (y > waterLevel) {
                        if (density > 0.35) {
                            density += heightFactor;
                        } else {
                            density += heightFactor * 2;
                        }
                    } else {
                        density += heightFactor * 2;
                    }

                    Byte nextBlock = null;
                    if (density >= 0) {
                        if (topSoilDepth == -1) {
                            if (y < waterLevel + 2) {
                                nextBlock = (byte) Blocks.sandBlock.getId();
                                beach = true;
                            } else {
                                nextBlock = (byte) Blocks.grassBlock.getId();
                                beach = false;
                            }
                            topSoilDepth++;
                        } else if (topSoilDepth < 3) {
                            if (beach) {
                                nextBlock = (byte) Blocks.sandBlock.getId();
                            } else {
                                nextBlock = (byte) Blocks.dirtBlock.getId();
                            }
                            topSoilDepth++;
                        } else {
                            nextBlock = (byte) Blocks.stoneBlock.getId();
                        }
                    } else {
                        if (y <= waterLevel) {
                            nextBlock = (byte) Blocks.waterBlock.getId();
                        }
                        topSoilDepth = -1;
                    }

                    if (nextBlock != null) {
                        this.setBlock(x, y, z, nextBlock);
                    }
                }
            }
        }
    }

    /**
     * Generates 3D octave noise using OpenSimplexNoise (similar to JS octaveNoise3D).
     */
    private double octaveNoise3D(double x, double y, double z) {
        int octaves = 4;
        double persistence = 0.5;
        double lacunarity = 2.0;
        double total = 0;
        double frequency = 0.005;
        double amplitude = 5;
        double maxValue = 0;

        for (int i = 0; i < octaves; i++) {
            total += OpenSimplexNoise.noise3_ImproveXY(
                    BlockGame.getInstance().getWorld().getWorldSeed(),
                    x * frequency,
                    y * frequency,
                    z * frequency
            ) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }

        return total / maxValue;
    }
}