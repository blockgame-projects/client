package com.james090500.blocks.model;

import com.james090500.blocks.Blocks;
import com.james090500.renderer.ModelBuilder;
import com.james090500.textures.TextureLocation;
import com.james090500.world.World;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.joml.Vector3i;

public class CactusModel implements IBlockModel {

    ModelBuilder cactusModel;

    public void create() {

        TextureLocation sideTexture = Blocks.cactusBlock.getTexture();
        TextureLocation topTexture = Blocks.cactusBlock.getTexture("top");
        TextureLocation bottomTexture = Blocks.cactusBlock.getTexture("bottom");
        TextureLocation[] textures = new TextureLocation[] { sideTexture, sideTexture, sideTexture, sideTexture, topTexture, bottomTexture };

        cactusModel = new ModelBuilder();
        cactusModel.addCube(0f, 0f, 0f, 1f, 1f, 0.9375f);
        cactusModel.build();
    }

    @Override
    public int[] getIndices() {
        return cactusModel.getIndices();
    }

    @Override
    public float[] getVertices() {
        return cactusModel.getVertices();
    }

    @Override
    public int getFaces() {
        return 6;
    }

    @Override
    public float[] getUv() {
        return Blocks.cactusBlock.getUv();
    }

    @Override
    public TextureLocation getTexture() {
        return null;
    }
}
