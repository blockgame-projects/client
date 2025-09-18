package com.james090500.blocks.model;

import com.james090500.blocks.Blocks;
import com.james090500.renderer.ModelBuilder;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.joml.Vector3i;

public class CactusModel implements IBlockModel {

    ModelBuilder cactusModel;

    public void create() {
        // inputs
        float[] uv = Blocks.cactusBlock.getUv();
        cactusModel = new ModelBuilder().addCube(0f, 0f, 0f, 1f, 1f, 0.9375f).setTexture(uv).build();
    }

    /**
     * Render
     * @param positions
     */
    public void render(ObjectList<Vector3i> positions) {
        this.cactusModel.render(positions);
    }
}
