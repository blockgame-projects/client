package com.james090500.blocks.model;

import com.james090500.blocks.Blocks;
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

    private final int[] indicies= {
            // Front
            4, 5, 6, 6, 7, 4,
            // Back
            1, 0, 3, 3, 2, 1,
            // Left
            0, 4, 7, 7, 3, 0,
            // Right
            5, 1, 2, 2, 6, 5,
            // Top
            3, 7, 6, 6, 2, 3,
            // Bottom
            0, 1, 5, 5, 4, 0
    };

    private int vao;

    public void create() {
        // inputs
        float[] sideUV   = Blocks.cactusBlock.getTexture();
        float[] topUV    = Blocks.cactusBlock.getTexture("top");
        float[] bottomUV = Blocks.cactusBlock.getTexture("bottom");
        float tileSize = 1.0f / 16.0f;

        // inset amount
        final float inset = 0.0625f;

        // Build 24 vertices (4 verts per face). Order per face = bottom-left, bottom-right, top-right, top-left
        float[] vertices = new float[] {
                // FRONT  (plane pushed back on Z to 1 - inset). X and Y go full 0..1 so UVs map full tile.
                0f, 0f, 1f - inset,   1f, 0f, 1f - inset,   1f, 1f, 1f - inset,   0f, 1f, 1f - inset,

                // BACK   (plane pushed forward on Z to inset)
                1f, 0f, inset,        0f, 0f, inset,        0f, 1f, inset,        1f, 1f, inset,

                // LEFT   (plane pushed right on X to inset). Z and Y full 0..1.
                inset, 0f, 0f,        inset, 0f, 1f,        inset, 1f, 1f,        inset, 1f, 0f,

                // RIGHT  (plane pushed left on X to 1 - inset)
                1f - inset, 0f, 1f,   1f - inset, 0f, 0f,   1f - inset, 1f, 0f,   1f - inset, 1f, 1f,

                // TOP    (full block X/Z, y = 1)
                0f, 1f, 1f,           1f, 1f, 1f,           1f, 1f, 0f,           0f, 1f, 0f,

                // BOTTOM (full block X/Z, y = 0)
                0f, 0f, 0f,           1f, 0f, 0f,           1f, 0f, 1f,           0f, 0f, 1f
        };

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

        // indices unchanged (6 faces × 2 tris × 3 indices)
        int[] indices = new int[36];
        for (int f = 0; f < 6; f++) {
            int vo = f * 4;
            int ii = f * 6;
            indices[ii + 0] = vo + 0;
            indices[ii + 1] = vo + 1;
            indices[ii + 2] = vo + 2;
            indices[ii + 3] = vo + 2;
            indices[ii + 4] = vo + 3;
            indices[ii + 5] = vo + 0;
        }


        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // Vertex Position VBO
        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, MemoryUtil.memAllocFloat(vertices.length).put(vertices).flip(), GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // UV VBO
        int tbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, tbo);
        glBufferData(GL_ARRAY_BUFFER, MemoryUtil.memAllocFloat(texCoords.length).put(texCoords).flip(), GL_STATIC_DRAW);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(1);

        // Index Buffer (EBO)
        int ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, MemoryUtil.memAllocInt(indices.length).put(indices).flip(), GL_STATIC_DRAW);
    }

    /**
     * Render
     * @param position
     */
    public void render(Vector3i position) {
        Matrix4f model = new Matrix4f().translate(new Vector3f(position));

        ShaderManager.basicBlockShader.use();
        ShaderManager.basicBlockShader.setMat4("model", model);

        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, indicies.length, GL_UNSIGNED_INT, 0);

        glBindTexture(GL_TEXTURE_2D, TextureManager.terrainTexture);

        glBindVertexArray(0);
        ShaderManager.basicBlockShader.stop();
    }
}
