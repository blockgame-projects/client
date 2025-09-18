package com.james090500.blocks;

import com.james090500.utils.TextureLocation;

public class GrassBlock extends Block {
    public GrassBlock(byte id) {
        super(id);
        this.name = "Grass";
        this.sound = "grass";
        this.texture = TextureLocation.get("assets/blocks/grass_block_side");
    }

    @Override
    public int getTexture(String face) {
        if (face.equalsIgnoreCase("top")) {
            return TextureLocation.get("assets/blocks/grass_block_top");
        } else if (face.equalsIgnoreCase("bottom")) {
            return TextureLocation.get("assets/blocks/dirt");
        } else {
            return this.texture;
        }
    }
}
