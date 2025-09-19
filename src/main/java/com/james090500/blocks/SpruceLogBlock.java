package com.james090500.blocks;

import com.james090500.textures.TextureLocation;

public class SpruceLogBlock extends Block {
    public SpruceLogBlock(byte id) {
        super(id);
        this.name = "Spruce Log";
        this.sound = "wood";
        this.texture = TextureLocation.get("assets/blocks/spruce_log");
    }

    @Override
    public TextureLocation getTexture(String face) {
        if (face.equalsIgnoreCase("top") || face.equalsIgnoreCase("bottom")) {
            return TextureLocation.get("assets/blocks/spruce_log_top");
        } else {
            return this.texture;
        }
    }
}
