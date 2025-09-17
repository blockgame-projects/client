package com.james090500.blocks;

import com.james090500.blocks.model.IBlockModel;
import lombok.Getter;

public class Block {

    @Getter
    byte id;
    int texture = 255;
    @Getter
    boolean transparent = false;
    @Getter
    boolean solid = true;
    @Getter
    String sound = null;
    @Getter
    String name = "UNKNOWN";
    @Getter
    IBlockModel model = null;

    public Block(byte id) {
        this.id = id;
    }

    public float[] getTexture() {
        return this.textureOffset(this.texture);
    }

    public float[] getTexture(String face) {
        return this.textureOffset(this.texture);
    }

    public float[] textureOffset(int texture) {
        float tileScale = 1.0f / 16.0f;

        int x = texture % 16;
        int y = texture / 16; // Use integer division to floor

        float u = x * tileScale;
        float v = 1.0f - (y + 1) * tileScale; // Flip vertically: top-left = [0, 0.9375]

        return new float[] { u, v };
    }
}
