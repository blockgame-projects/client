package com.james090500.blocks;

public class CactusBlock extends Block {
    public CactusBlock(byte id) {
        super(id);
        this.name = "Cactus";
        this.sound = "cloth";
        this.texture = 19;
    }

    @Override
    public float[] getTexture(String face) {
        if (face.equalsIgnoreCase("top")) {
            return this.textureOffset(18);
        } else if (face.equalsIgnoreCase("bottom")) {
            return this.textureOffset(20);
        } else {
            return this.textureOffset(this.texture);
        }
    }
}
