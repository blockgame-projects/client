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

    @Deprecated
    public int[] uv() {
        int tileScale = 16 / 256;

        double x = this.texture % 16;
        double y = (double) this.texture / 16;

        int u = (int) (x * tileScale);
        int v = (int) (1 - y * tileScale);

        return new int[] { u, v, tileScale, tileScale };
    }

    public int[] getTexture() {
        return this.textureOffset(this.texture);
    }

    public int[] getTexture(String face) {
        return this.textureOffset(this.texture);
    }

    public int[] textureOffset(int texture) {
        int tileScale = 1 / 16;

        double x = texture % 16;
        double y = (double) texture / 16;

        int u = (int) (x * tileScale);
        int v = (int) (1 - y * tileScale - tileScale);

        return new int[] { u, v };
    }
}
