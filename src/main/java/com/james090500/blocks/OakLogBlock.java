package com.james090500.blocks;

public class OakLogBlock extends Block {
    public OakLogBlock(byte id) {
        super(id);
        this.name = "Oak Log";
        this.sound = "wood";
        this.texture = 8;
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
