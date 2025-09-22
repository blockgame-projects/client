package com.james090500.blocks;

import com.james090500.blocks.model.CactusModel;
import com.james090500.textures.TextureLocation;

public class CactusBlock extends Block {

    public CactusBlock(byte id) {
        super(id);
        this.name = "Cactus";
        this.sound = "cloth";
        this.texture = TextureLocation.get("assets/blocks/cactus_side");
        this.model = new CactusModel();
    }

    @Override
    public TextureLocation getTexture(String face) {
        if (face.equalsIgnoreCase("top")) {
            return TextureLocation.get("assets/blocks/cactus_top");
        } else if (face.equalsIgnoreCase("bottom")) {
            return TextureLocation.get("assets/blocks/cactus_bottom");
        } else {
            return this.texture;
        }
    }
}
