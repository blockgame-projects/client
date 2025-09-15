package com.james090500.structure;

import com.james090500.blocks.Block;
import com.james090500.blocks.Blocks;
import com.james090500.blocks.GrassBlock;
import com.james090500.blocks.SandBlock;
import com.james090500.world.Chunk;

public class Cactus implements Structure {

    private final double noise;
    private final Chunk chunk;

    public Cactus(double noise, Chunk chunk) {
        this.noise = noise;
        this.chunk = chunk;
    }

    @Override
    public void build(int x, int y, int z) {
        Block block = chunk.getBlock(x, y, z);
        if (!(block instanceof SandBlock)) {
            return;
        }

        int height = (int) (3 + Math.floor((noise - 0.9) * 10)); // 3-5 block tall trunk

        // Build trunk
        for (int t = 0; t < height; t++) {
            chunk.setBlock(x, 1 + y + t, z, Blocks.cactusBlock.getId());
        }
    }
}
