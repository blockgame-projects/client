package com.james090500.blocks;

import com.james090500.textures.TextureLocation;

public class OakLeafBlock extends Block {
    public OakLeafBlock(byte id) {
        super(id);
        this.name = "Leaf";
        this.sound = "grass";
        this.texture = TextureLocation.get("assets/blocks/oak_leaves");
        this.transparent = true;
    }
}
