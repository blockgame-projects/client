package com.james090500.blocks;

import com.james090500.utils.TextureLocation;

public class BirchPlanksBlock extends Block {
    public BirchPlanksBlock(byte id) {
        super(id);
        this.name = "Birch Planks";
        this.sound = "wood";
        this.texture = TextureLocation.get("assets/blocks/birch_planks");;
    }
}
