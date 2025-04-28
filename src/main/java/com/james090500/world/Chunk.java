package com.james090500.world;

public class Chunk {

    private byte[] chunkData;

    private final int chunkSize = 16;
    private final int chunkHeight = 300;
    private final int chunkX;
    private final int chunkZ;

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
                this.setBlock(x, 1, z, (byte) 1);
            }
        }
    }
}
