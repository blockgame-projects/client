package com.james090500.blocks;

import com.james090500.utils.TextureLocation;

public class BirchLogBlock extends Block {
    public BirchLogBlock(byte id) {
        super(id);
        this.name = "Birch Log";
        this.sound = "wood";
        this.texture = TextureLocation.get("assets/blocks/birch_log");
    }

    @Override
    public int getTexture(String face) {
        if (face.equalsIgnoreCase("top") || face.equalsIgnoreCase("bottom")) {
            return TextureLocation.get("assets/blocks/birch_log_top");
        } else {
            return this.texture;
        }
    }
}
