package com.james090500.blocks;

import com.james090500.utils.TextureLocation;

public class SprucePlanksBlock extends Block {
    public SprucePlanksBlock(byte id) {
        super(id);
        this.name = "Spruce Planks";
        this.sound = "wood";
        this.texture = TextureLocation.get("assets/blocks/spruce_planks");
    }
}
