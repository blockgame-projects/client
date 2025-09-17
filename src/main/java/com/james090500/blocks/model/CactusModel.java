package com.james090500.blocks.model;

import com.james090500.blocks.Blocks;
import com.james090500.renderer.ModelBuilder;
import com.james090500.renderer.ShaderManager;
import com.james090500.utils.TextureManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class CactusModel implements IBlockModel {

    ModelBuilder.Model cactusModel;

    public void create() {
        // inputs
        float[] sideUV = Blocks.cactusBlock.getTexture();
        float[] topUV = Blocks.cactusBlock.getTexture("top");
        float[] bottomUV = Blocks.cactusBlock.getTexture("bottom");
        float[][] uvBases = new float[][] { sideUV, sideUV, sideUV, sideUV, topUV, bottomUV };

        cactusModel = ModelBuilder.create().addCube(0f, 0f, 0f, 1f, 1f, 0.9375f).setTexture(uvBases).build();
    }

    /**
     * Render
     * @param position
     */
    public void render(Vector3i position) {
        Matrix4f model = new Matrix4f().translate(new Vector3f(position));

        ShaderManager.basicBlockShader.use();
        ShaderManager.basicBlockShader.setMat4("model", model);
        ShaderManager.basicBlockShader.useFog();

        glBindVertexArray(cactusModel.vao());
        glDrawElements(GL_TRIANGLES, cactusModel.indicies(), GL_UNSIGNED_INT, 0);

        glBindTexture(GL_TEXTURE_2D, TextureManager.terrainTexture);

        glBindVertexArray(0);
        ShaderManager.basicBlockShader.stop();
    }
}
