package com.james090500.blocks.model;

import com.james090500.renderer.InstancedBlockRenderer;
import com.james090500.textures.TextureLocation;
import lombok.Getter;

public class VegetationModel implements IBlockModel {

    @Getter
    int[] indices = {
            0, 1, 2, 2, 3, 0,       // Front
            4, 5, 6, 6, 7, 4,       // Back
    };

    @Getter
    float[] vertices = {
        0f, 0f, 0f, 1f,
        0f, 1f, 1f, 1f,
        1f, 0f, 1f, 0f,
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 1f,
        1f, 1f, 1f, 0f,
    };

    @Getter
    int faces = 2;

    @Getter
    float[] uv;

    @Getter
    TextureLocation texture;

    public VegetationModel(float[] uvBases, TextureLocation textureLocation) {
        this.uv = uvBases;
        this.texture = textureLocation;
    }
}
