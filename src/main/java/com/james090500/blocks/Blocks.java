package com.james090500.blocks;

public class Blocks {

    public static Block[] ids;

    public static final GrassBlock grassBlock = new GrassBlock((byte) 1);
    public static final DirtBlock dirtBlock = new DirtBlock((byte) 2);
    public static final StoneBlock stoneBlock = new StoneBlock((byte) 3);
    public static final SandBlock sandBlock = new SandBlock((byte) 4);
    public static final WaterBlock waterBlock = new WaterBlock((byte) 5);
    public static final LogBlock logBlock = new LogBlock((byte) 6);
    public static final LeafBlock leafBlock = new LeafBlock((byte) 7);

    static {
        ids = new Block[] {
                null,
                grassBlock,
                dirtBlock,
                stoneBlock,
                sandBlock,
                waterBlock,
                logBlock,
                leafBlock
        };
    }

}
