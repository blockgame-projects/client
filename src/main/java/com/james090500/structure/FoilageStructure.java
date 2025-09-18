package com.james090500.structure;

import com.james090500.blocks.Block;
import com.james090500.blocks.Blocks;
import com.james090500.blocks.GrassBlock;
import com.james090500.world.Chunk;

public class FoilageStructure implements Structure {

    private final double noise;
    private final Chunk chunk;

    public FoilageStructure(double noise, Chunk chunk) {
        this.noise = noise;
        this.chunk = chunk;
    }

    @Override
    public boolean build(int x, int y, int z) {
        Block block = chunk.getBlock(x, y, z);
        Block destination = chunk.getBlock(x, y + 1, z);
        if (!(block instanceof GrassBlock) || destination != null) {
            return false;
        }
        if(noise >= 0.5 && noise < 0.8) {
            chunk.setBlock(x, y + 1, z, Blocks.shortGrassBlock.getId());
            return true;
        } else if(noise >= 0.8 && noise < 0.9) {
            chunk.setBlock(x, y + 1, z, Blocks.yellowFlowerBlock.getId());
            return true;
        } else if(noise >= 0.9 && noise < 1.0) {
            chunk.setBlock(x, y + 1, z, Blocks.redFlowerBlock.getId());
            return true;
        }
        return true;
    }
}
