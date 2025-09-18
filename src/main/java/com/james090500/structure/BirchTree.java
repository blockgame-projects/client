package com.james090500.structure;

import com.james090500.blocks.Block;
import com.james090500.blocks.Blocks;
import com.james090500.blocks.GrassBlock;
import com.james090500.world.Chunk;

public class BirchTree implements Structure {

    private final double noise;
    private final Chunk chunk;

    public BirchTree(double noise, Chunk chunk) {
        this.noise = noise;
        this.chunk = chunk;
    }

    @Override
    public boolean build(int x, int y, int z) {
        Block block = chunk.getBlock(x, y, z);
        if (!(block instanceof GrassBlock)) {
            return false;
        }

        int trunkHeight = (int) (4 + Math.floor((noise - 0.9) * 10)); // 3-5 block tall trunk

        // Build trunk
        for (int t = 0; t < trunkHeight; t++) {
            chunk.setBlock(x, 1 + y + t, z, Blocks.birchLogBlock.getId());
        }

        // Build leaves
        for (int lx = -2; lx <= 2; lx++) {
            for (int lz = -2; lz <= 2; lz++) {
                for (int ly = 0; ly <= 3; ly++) {
                    // a little taller
                    int horizontalDist = Math.abs(lx) + Math.abs(lz);

                    // Simple rules:
                    if (horizontalDist <= 3 - ly) {
                        // narrower as ly goes up
                        byte blockToSet =
                                ly < 3 && lx == 0 && lz == 0
                                        ? Blocks.birchLogBlock.getId()
                                        : Blocks.leafBlock.getId();

                        chunk.setBlock(
                                x + lx,
                                1 + y + trunkHeight + ly,
                                z + lz,
                                blockToSet
                        );
                    }
                }
            }
        }
        return true;
    }
}
