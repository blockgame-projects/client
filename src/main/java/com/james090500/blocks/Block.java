package com.james090500.blocks;

import com.james090500.blocks.model.IBlockModel;
import com.james090500.utils.TextureLocation;
import lombok.Getter;

public class Block {

    @Getter
    byte id;
    @Getter
    public int texture = TextureLocation.get("assets/error");
    @Getter
    boolean transparent = false;
    @Getter
    boolean solid = true;
    @Getter
    boolean liquid = false;
    @Getter
    String sound = null;
    @Getter
    String name = "UNKNOWN";
    @Getter
    IBlockModel model = null;
    @Getter
    float[] uv = new float[] {
        0.0f, 0.0f,   // bottom-left
        1.0f, 0.0f,   // bottom-right
        1.0f, 1.0f,   // top-right
        0.0f, 1.0f    // top-left
    };

    public Block(byte id) {
        this.id = id;
    }

    public int getTexture(String face) {
        return this.texture;
    }
}
