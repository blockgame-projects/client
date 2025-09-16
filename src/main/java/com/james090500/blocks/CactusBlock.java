package com.james090500.blocks;

import com.james090500.blocks.model.CactusModel;
import org.joml.Vector3i;

public class CactusBlock extends Block implements IBlockRender {

    public CactusBlock(byte id) {
        super(id);
        this.name = "Cactus";
        this.sound = "cloth";
        this.texture = 19;
        this.model = new CactusModel();
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

    @Override
    public void render(Vector3i position) {
        this.model.render(position);
    }
}
