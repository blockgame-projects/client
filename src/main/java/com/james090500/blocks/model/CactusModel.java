package com.james090500.blocks.model;

import com.james090500.blocks.Blocks;
import com.james090500.renderer.ModelBuilder;
import com.james090500.renderer.ShaderManager;
import com.james090500.utils.TextureManager;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class CactusModel implements IBlockModel {

    ModelBuilder cactusModel;

    public void create() {
        // inputs
        float[] sideUV = Blocks.cactusBlock.getTexture();
        float[] topUV = Blocks.cactusBlock.getTexture("top");
        float[] bottomUV = Blocks.cactusBlock.getTexture("bottom");
        float[][] uvBases = new float[][] { sideUV, sideUV, sideUV, sideUV, topUV, bottomUV };

        cactusModel = new ModelBuilder().addCube(0f, 0f, 0f, 1f, 1f, 0.9375f).setTexture(uvBases).build();
    }

    /**
     * Render
     * @param positions
     */
    public void render(ObjectList<Vector3i> positions) {
        this.cactusModel.render(positions);
    }
}
