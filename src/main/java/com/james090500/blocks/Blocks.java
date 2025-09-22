package com.james090500.blocks;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;

import java.util.function.Function;

public class Blocks {

    // 0 reserved for "air"/null
    private static final int MAX_BLOCKS = 256;
    private static final Int2ObjectArrayMap<Block> REGISTRY = new Int2ObjectArrayMap<>();
    private static int nextId = 1;

    /**
     * Stores block in registry by instance
     * @param ctor The block instance
     * @return The block instance
     * @param <T> A block ::new
     */
    private static <T extends Block> T register(Function<Byte, T> ctor) {
        if (nextId >= MAX_BLOCKS) throw new IllegalStateException("Block registry full");
        byte id = (byte) nextId;
        T block = ctor.apply(id);
        REGISTRY.put(id & 0xFF, block);
        nextId++;
        return block;
    }

    /**
     * Get the block by ID (0 is null)
     * @param id Block ID
     * @return The block instance
     */
    public static Block get(int id) {
        return REGISTRY.get(id & 0xFF);
    }

    /**
     * Get the total blocks added
     * @return
     */
    public static int getTotalBlocks() {
        return REGISTRY.size();
    }

    // ---- Declarations (order == IDs). Append new ones at the end. ----
    public static final GrassBlock grassBlock = register(GrassBlock::new);
    public static final DirtBlock dirtBlock = register(DirtBlock::new);
    public static final StoneBlock stoneBlock = register(StoneBlock::new);
    public static final SandBlock sandBlock = register(SandBlock::new);
    public static final WaterBlock waterBlock = register(WaterBlock::new);
    public static final OakLeafBlock leafBlock = register(OakLeafBlock::new);
    public static final SpruceLeafBlock spruceLeafBlock = register(SpruceLeafBlock::new);
    public static final OakLogBlock logBlock = register(OakLogBlock::new);
    public static final SpruceLogBlock spruceLogBlock = register(SpruceLogBlock::new);
    public static final BirchLogBlock birchLogBlock = register(BirchLogBlock::new);
    public static final OakPlanksBlock oakPlanksBlock = register(OakPlanksBlock::new);
    public static final SprucePlanksBlock sprucePlanksBlock = register(SprucePlanksBlock::new);
    public static final BirchPlanksBlock birchPlanksBlock = register(BirchPlanksBlock::new);
    public static final GlassBlock glassBlock = register(GlassBlock::new);
    public static final SnowyGrassBlock snowyGrassBlock  = register(SnowyGrassBlock::new);
    public static final CactusBlock cactusBlock = register(CactusBlock::new);
    public static final ShortGrassBlock shortGrassBlock = register(ShortGrassBlock::new);
    public static final RedFlowerBlock redFlowerBlock = register(RedFlowerBlock::new);
    public static final YellowFlowerBlock yellowFlowerBlock = register(YellowFlowerBlock::new);
}