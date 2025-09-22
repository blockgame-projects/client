package com.james090500.blocks.model;

import com.james090500.textures.TextureLocation;
import com.james090500.world.World;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.joml.Vector3i;

public interface IBlockModel {

    int[] getIndices();

    float[] getVertices();

    int getFaces();

    float[] getUv();

    TextureLocation getTexture();
}
