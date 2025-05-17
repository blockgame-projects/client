package com.james090500.world;

import com.james090500.blocks.Block;
import com.james090500.blocks.Blocks;

public class Chunk {

    private byte[] chunkData;

    public final int chunkSize = 16;
    public final int chunkHeight = 300;
    public final int chunkX;
    public final int chunkZ;

    public Chunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;

        this.chunkData = new byte[chunkSize * chunkSize * chunkHeight];

        this.generateTerrain();
    }

    /**
     * Gets the index from the byte array
     * @param x The x in the chunk
     * @param y The y in the chunk
     * @param z The z in the chunk
     * @return The index returned (null/-1 if invalid)
     */
    private int getIndex(int x, int y, int z) {
        if(
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
     * @param x The x in the chunk
     * @param y The y in the chunk
     * @param z The z in the chunk
     */
    public Block getBlock(int x, int y, int z) {
        if(
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
     * @param x The x in the chunk
     * @param y The y in the chunk
     * @param z The z in the chunk
     */
    public void setBlock(int x, int y, int z, byte block) {
        if(
                x >= this.chunkSize || x < 0 ||
                y >= this.chunkHeight || y < 0 ||
                z >= this.chunkSize || z < 0
        ) {
            return;
        }

        this.chunkData[this.getIndex(x, y, z)] = block;
    }

    private void generateTerrain() {
        for(int x = 0; x < this.chunkSize; x++) {
            for(int z = 0; z < this.chunkSize; z++) {
                this.setBlock(x, 3, z, (byte) 1);
                this.setBlock(x, 2, z, (byte) 2);
                this.setBlock(x, 1, z, (byte) 3);
                this.setBlock(x, 0, z, (byte) 3);
            }
        }
    }
}
