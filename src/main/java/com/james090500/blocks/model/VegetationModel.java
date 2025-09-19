package com.james090500.blocks.model;

import com.james090500.renderer.InstancedBlockRenderer;
import com.james090500.textures.TextureLocation;

public class VegetationModel extends InstancedBlockRenderer implements IBlockModel {

    float[] vertices = {
        0f, 0f, 0f, 1f, 0f, 1f, 1f, 1f, 1f, 0f, 1f, 0f,
        1f, 0f, 0f, 0f, 0f, 1f, 0f, 1f, 1f, 1f, 1f, 0f,
    };

    int[] indices = {
        0, 1, 2, 2, 3, 0,       // Front
        4, 5, 6, 6, 7, 4,       // Back
    };

    public VegetationModel(float[] uvBases, TextureLocation textureLocation) {
        this.setIndices(indices);
        this.setVertices(vertices);
        this.setUV(2, uvBases);
        this.setTexture(new int[]{
                textureLocation.getId(),
                textureLocation.getId(),
                textureLocation.getId(),
                textureLocation.getId(),
                textureLocation.getId(),
                textureLocation.getId(),
                textureLocation.getId(),
                textureLocation.getId(),
        });
    }
}
