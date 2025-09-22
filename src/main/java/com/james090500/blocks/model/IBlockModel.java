package com.james090500.blocks.model;

import com.james090500.textures.TextureLocation;

public interface IBlockModel {

    int[] getIndices();

    float[] getVertices();

    int getFaces();

    float[] getUv();

    TextureLocation[] getTexture();
}
