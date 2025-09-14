package com.james090500.blocks;

public class Blocks {

    public static Block[] ids;

    public static final GrassBlock grassBlock = new GrassBlock((byte) 1);
    public static final DirtBlock dirtBlock = new DirtBlock((byte) 2);
    public static final StoneBlock stoneBlock = new StoneBlock((byte) 3);
    public static final SandBlock sandBlock = new SandBlock((byte) 4);
    public static final WaterBlock waterBlock = new WaterBlock((byte) 5);
    public static final OakLeafBlock leafBlock = new OakLeafBlock((byte) 6);
    public static final SpruceLeafBlock spruceLeafBlock = new SpruceLeafBlock((byte) 7);
    public static final OakLogBlock logBlock = new OakLogBlock((byte) 8);
    public static final SpruceLogBlock spruceLogBlock = new SpruceLogBlock((byte) 9);
    public static final BirchLogBlock birchLogBlock = new BirchLogBlock((byte) 10);
    public static final OakPlanksBlock oakPlanksBlock = new OakPlanksBlock((byte) 11);
    public static final SprucePlanksBlock sprucePlanksBlock = new SprucePlanksBlock((byte) 12);
    public static final BirchPlanksBlock birchPlanksBlock = new BirchPlanksBlock((byte) 13);
    public static final GlassBlock glassBlock = new GlassBlock((byte) 14);
    public static final SnowyGrassBlock snowyGrassBlock = new SnowyGrassBlock((byte) 15);
    public static final CactusBlock cactusBlock = new CactusBlock((byte) 16);

    static {
        ids = new Block[] {
                null,
                grassBlock,
                dirtBlock,
                stoneBlock,
                sandBlock,
                waterBlock,
                leafBlock,
                spruceLeafBlock,
                logBlock,
                spruceLogBlock,
                birchLogBlock,
                oakPlanksBlock,
                sprucePlanksBlock,
                birchPlanksBlock,
                glassBlock,
                snowyGrassBlock,
                cactusBlock,
        };
    }

}
