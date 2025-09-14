package com.james090500.blocks;

public class SnowyGrassBlock extends Block {
    public SnowyGrassBlock(byte id) {
        super(id);
        this.name = "Snowy Grass";
        this.sound = "cloth";
        this.texture = 17;
    }

    @Override
    public float[] getTexture(String face) {
        if (face.equalsIgnoreCase("top")) {
            return this.textureOffset(16);
        } else if (face.equalsIgnoreCase("bottom")) {
            return this.textureOffset(2);
        } else {
            return this.textureOffset(this.texture);
        }
    }
}
