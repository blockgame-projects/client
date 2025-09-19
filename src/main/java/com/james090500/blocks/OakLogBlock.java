package com.james090500.blocks;

import com.james090500.textures.TextureLocation;

public class OakLogBlock extends Block {
    public OakLogBlock(byte id) {
        super(id);
        this.name = "Oak Log";
        this.sound = "wood";
        this.texture = TextureLocation.get("assets/blocks/oak_log");
    }

    @Override
    public TextureLocation getTexture(String face) {
        if (face.equalsIgnoreCase("top") || face.equalsIgnoreCase("bottom")) {
            return TextureLocation.get("assets/blocks/oak_log_top");
        } else {
            return this.texture;
        }
    }
}
