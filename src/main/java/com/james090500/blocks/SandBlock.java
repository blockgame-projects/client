package com.james090500.blocks;

import com.james090500.textures.TextureLocation;

public class SandBlock extends Block {
    public SandBlock(byte id) {
        super(id);
        this.name = "Sand";
        this.sound = "sand";
        this.texture = TextureLocation.get("assets/blocks/sand");
    }
}
