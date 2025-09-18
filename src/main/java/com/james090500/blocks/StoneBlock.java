package com.james090500.blocks;

import com.james090500.utils.TextureLocation;

public class StoneBlock extends Block {

    public StoneBlock(byte id) {
        super(id);
        this.name = "Stone";
        this.sound = "stone";
        this.texture = TextureLocation.get("assets/blocks/stone");
    }
}
