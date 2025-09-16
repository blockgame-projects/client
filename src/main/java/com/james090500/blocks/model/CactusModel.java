package com.james090500.blocks.model;

import com.james090500.blocks.Blocks;
import com.james090500.renderer.ModelBuilder;
import com.james090500.renderer.ShaderManager;
import com.james090500.utils.TextureManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class CactusModel implements IBlockModel {

    ModelBuilder.Model cactusModel;

    public void create() {

        // inputs
        float[] sideUV   = Blocks.cactusBlock.getTexture();
        float[] topUV    = Blocks.cactusBlock.getTexture("top");
        float[] bottomUV = Blocks.cactusBlock.getTexture("bottom");
        float tileSize = 1.0f / 16.0f;

        // Build texcoords: each face samples the full tile (0..tileSize) so spike pixels in the margin will be visible.
        float[] texCoords = new float[24 * 2];
        float[][] uvBases = new float[][] { sideUV, sideUV, sideUV, sideUV, topUV, bottomUV };

        for (int face = 0; face < 6; face++) {
            float u0 = uvBases[face][0];
            float v0 = uvBases[face][1];
            int dest = face * 8; // 4 verts * 2 coords

            // bottom-left  (0,0)
            texCoords[dest + 0] = u0 + 0f * tileSize;
            texCoords[dest + 1] = v0 + 0f * tileSize;

            // bottom-right (1,0)
            texCoords[dest + 2] = u0 + tileSize;
            texCoords[dest + 3] = v0 + 0f * tileSize;

            // top-right    (1,1)
            texCoords[dest + 4] = u0 + tileSize;
            texCoords[dest + 5] = v0 + tileSize;

            // top-left     (0,1)
            texCoords[dest + 6] = u0 + 0f * tileSize;
            texCoords[dest + 7] = v0 + tileSize;
        }

        cactusModel = ModelBuilder.create().addCube(0.0625f, 0f, 0.0625f, 0.875f, 1, 0.875f).build(texCoords);
    }

    /**
     * Render
     * @param position
     */
    public void render(Vector3i position) {
        Matrix4f model = new Matrix4f().translate(new Vector3f(position));

        ShaderManager.basicBlockShader.use();
        ShaderManager.basicBlockShader.setMat4("model", model);

        glBindVertexArray(cactusModel.vao());
        glDrawElements(GL_TRIANGLES, cactusModel.indicies(), GL_UNSIGNED_INT, 0);

        glBindTexture(GL_TEXTURE_2D, TextureManager.terrainTexture);

        glBindVertexArray(0);
        ShaderManager.basicBlockShader.stop();
    }
}
