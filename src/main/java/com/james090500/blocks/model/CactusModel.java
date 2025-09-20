package com.james090500.blocks.model;

import com.james090500.blocks.Blocks;
import com.james090500.renderer.ModelBuilder;
import com.james090500.textures.TextureLocation;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.joml.Vector3i;

public class CactusModel implements IBlockModel {

    ModelBuilder cactusModel;

    public void create() {
        // inputs
        float[] uv = Blocks.cactusBlock.getUv();

        TextureLocation sideTexture = Blocks.cactusBlock.getTexture();
        TextureLocation topTexture = Blocks.cactusBlock.getTexture("top");
        TextureLocation bottomTexture = Blocks.cactusBlock.getTexture("bottom");
        TextureLocation[] textures = new TextureLocation[] { sideTexture, sideTexture, sideTexture, sideTexture, topTexture, bottomTexture };

        cactusModel = new ModelBuilder();
        cactusModel.addCube(0f, 0f, 0f, 1f, 1f, 0.9375f);
        cactusModel.setFaces(6);
        cactusModel.setUV(uv);
        cactusModel.setTexture(textures);
        cactusModel.build();
    }

    /**
     * Render
     */
    public void render(ObjectList<Vector3i> positions) {
        this.cactusModel.render(positions);
    }
}
