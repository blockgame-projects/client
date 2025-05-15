package com.james090500.blocks;

public class LogBlock extends Block {
    public LogBlock(int id) {
        super(id);
        this.texture = 20;
    }

    @Override
    public float[] getTexture(String face) {
        if (face.equalsIgnoreCase("top") || face.equalsIgnoreCase("bottom")) {
            return this.textureOffset(21);
        } else {
            return this.textureOffset(this.texture);
        }
    }
}
