package com.james090500.blocks;

import com.james090500.textures.TextureLocation;

public class SnowyGrassBlock extends Block {
    public SnowyGrassBlock(byte id) {
        super(id);
        this.name = "Snowy Grass";
        this.sound = "cloth";
        this.texture = TextureLocation.get("assets/blocks/snowy_grass_block_side");
    }

    @Override
    public TextureLocation getTexture(String face) {
        if (face.equalsIgnoreCase("top")) {
            return TextureLocation.get("assets/blocks/snowy_grass_block_top");
        } else if (face.equalsIgnoreCase("bottom")) {
            return TextureLocation.get("assets/blocks/dirt");
        } else {
            return this.texture;
        }
    }
}
