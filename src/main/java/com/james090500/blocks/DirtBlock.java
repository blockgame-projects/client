package com.james090500.blocks;

import com.james090500.utils.TextureLocation;

public class DirtBlock extends Block {
    public DirtBlock(byte id) {
        super(id);
        this.name = "Dirt";
        this.sound = "gravel";
        this.texture = TextureLocation.get("assets/blocks/dirt");;
    }
}
