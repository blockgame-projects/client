package com.james090500.blocks;

import lombok.Getter;

public class Block {

    @Getter
    int id = 999;
    int texture = 14;
    @Getter
    boolean transparent = false;
    @Getter
    boolean solid = true;

    public Block(int id) {
        this.id = id;
    }

    public float[] getTexture() {
        return this.textureOffset(this.texture);
    }

    public float[] getTexture(String face) {
        return this.textureOffset(this.texture);
    }

    public float[] textureOffset(int texture) {
        int tileScale = 1 / 16;

        double x = texture % 16;
        double y = (double) texture / 16;

        int u = (int) (x * tileScale);
        int v = (int) (y * tileScale - tileScale);

        return new float[] { u, v };
    }
}
