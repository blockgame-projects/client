package com.james090500.blocks;

public class SpruceLogBlock extends Block {
    public SpruceLogBlock(byte id) {
        super(id);
        this.name = "Spruce Log";
        this.sound = "wood";
        this.texture = 10;
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
