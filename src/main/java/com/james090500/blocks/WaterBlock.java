package com.james090500.blocks;

import com.james090500.utils.TextureLocation;

public class WaterBlock extends Block {
    public WaterBlock(byte id) {
        super(id);
        this.name = "Water";
        this.texture = TextureLocation.get("assets/blocks/water");
        this.transparent = true;
        this.solid = false;
        this.liquid = true;
    }
}
