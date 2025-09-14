package com.james090500.blocks;

public class BirchLogBlock extends Block {
    public BirchLogBlock(byte id) {
        super(id);
        this.name = "Birch Log";
        this.sound = "wood";
        this.texture = 11;
    }

    @Override
    public float[] getTexture(String face) {
        if (face.equalsIgnoreCase("top") || face.equalsIgnoreCase("bottom")) {
            return this.textureOffset(9);
        } else {
            return this.textureOffset(this.texture);
        }
    }
}
