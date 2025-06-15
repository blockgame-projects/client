package com.james090500.world;

import java.io.*;

public class Region {

    private final File regionFile;

    private final int size = 32;

    public Region(int regionX, int regionZ) {
        this.regionFile = new File("region/r." + regionX + "." + regionZ + ".bgr");

        // Ensure the "region" folder exists
        new File("region").mkdirs();
    }

    void saveChunk(int chunkX, int chunkZ, byte[] chunkData) throws IOException {
        synchronized (regionFile) {
            try (RandomAccessFile raf = new RandomAccessFile(regionFile, "rw")) {

                // Ensure file is at least 8192 bytes
                if (raf.length() < 8192) {
                    raf.setLength(8192);
                }

                int localX = Math.floorMod(chunkX, size);
                int localZ = Math.floorMod(chunkZ, size);
                int index = localZ * size + localX;

                // Write the chunk at the end of the file
                long chunkOffset = raf.length();
                raf.seek(chunkOffset);
                raf.writeInt(chunkData.length); // optional
                raf.write(chunkData);

                // Update header with offset and length
                raf.seek(index * 8);
                raf.writeInt((int) chunkOffset);
                raf.writeInt(chunkData.length);
            }
        }
    }

    byte[] loadChunk(int chunkX, int chunkZ) throws IOException {
        synchronized (regionFile) {
            try (RandomAccessFile raf = new RandomAccessFile(regionFile, "r")) {

                int localX = Math.floorMod(chunkX, size);
                int localZ = Math.floorMod(chunkZ, size);
                int index = localZ * size + localX;

                // Read offset and length
                raf.seek(index * 8);
                int offset = raf.readInt();
                int length = raf.readInt();

                if (offset == 0 || length == 0) {
                    return null; // Chunk not saved yet
                }

                // Read chunk
                raf.seek(offset);
                int storedLength = raf.readInt(); // read the same length you wrote
                byte[] data = new byte[storedLength];
                raf.readFully(data);
                return data;
            }
        }
    }
}
