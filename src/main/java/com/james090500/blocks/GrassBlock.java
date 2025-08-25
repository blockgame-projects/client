package com.james090500.blocks;

public class GrassBlock extends Block {
    public GrassBlock(byte id) {
        super(id);
        this.name = "Grass";
        this.sound = "grass";
        this.texture = 3;
    }

    @Override
    public float[] getTexture(String face) {
        if (face.equalsIgnoreCase("top")) {
            return this.textureOffset(0);
        } else if (face.equalsIgnoreCase("bottom")) {
            return this.textureOffset(2);
        } else {
            return this.textureOffset(this.texture);
        }
    }
}
