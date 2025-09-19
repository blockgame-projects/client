package com.james090500.blocks;

import com.james090500.textures.TextureLocation;

public class SpruceLeafBlock extends Block {
    public SpruceLeafBlock(byte id) {
        super(id);
        this.name = "Leaf";
        this.sound = "grass";
        this.texture = TextureLocation.get("assets/blocks/spruce_leaves");
        this.transparent = true;
    }
}
