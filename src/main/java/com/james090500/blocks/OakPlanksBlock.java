package com.james090500.blocks;

import com.james090500.textures.TextureLocation;

public class OakPlanksBlock extends Block {
    public OakPlanksBlock(byte id) {
        super(id);
        this.name = "Oak Planks";
        this.sound = "wood";
        this.texture = TextureLocation.get("assets/blocks/oak_planks");
    }
}
