package com.james090500.world;

import com.james090500.BlockGame;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Region {

    private String fileName;
    private int x;
    private int z;

    private final int size = 32;

    private void setRegionSize(int chunkX, int chunkZ) {
        int offsetChunkX = Math.floorDiv(x, size);
        x += offsetChunkX;

        int offsetChunkZ = Math.floorDiv(z, size);
        z += offsetChunkZ;
    }

    public void saveFile(int chunkX, int chunkZ, byte[] chunkData) {
        // Fist set the region size
        this.setRegionSize(chunkX, chunkZ);

        // File Name
        this.fileName = "r." + x + "," + z + ".bgr";

        // World Name
        String worldName = BlockGame.getInstance().getWorld().getName();

//        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream("region/")) {
//            dos.writeInt(chunkX);
//            dos.writeInt(chunkZ);
//            dos.writeInt(chunkData.length);
//            dos.write(chunkData);
//        } catch (IOException e) {
//            System.out.println("Error Reading Region!");
//        }
    }

    public void loadFile() {

    }
}
