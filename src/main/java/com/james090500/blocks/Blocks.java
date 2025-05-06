package com.james090500.blocks;

public class Blocks {

    public static Block[] ids;

    static {
        ids = new Block[] {
                null,
                new GrassBlock(1),
                new DirtBlock(2),
                new StoneBlock(3),
                new SandBlock(4),
                new WaterBlock(5),
                new LogBlock(6),
                new LeafBlock(7)
        };
    }

}
