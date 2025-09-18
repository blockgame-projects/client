package com.james090500.blocks;

import com.james090500.utils.TextureLocation;

public class GlassBlock extends Block {
    public GlassBlock(byte id) {
        super(id);
        this.name = "Glass";
        this.sound = "stone";
        this.texture = TextureLocation.get("assets/blocks/glass");;
        this.transparent = true;
    }
}
