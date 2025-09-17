package com.james090500.blocks.model;

import com.james090500.renderer.ModelBuilder;
import com.james090500.renderer.ShaderManager;
import com.james090500.utils.TextureManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class VegetationModel implements IBlockModel {

    int vao;
    float[] vertices = {
        0f, 0f, 0f, 1f, 0f, 1f, 1f, 1f, 1f, 0f, 1f, 0f,
        1f, 0f, 0f, 0f, 0f, 1f, 0f, 1f, 1f, 1f, 1f, 0f,
    };

    int[] indicies = {
        0, 1, 2, 2, 3, 0,       // Front
        4, 5, 6, 6, 7, 4,       // Back
    };

    private final float[] texCoords;

    public VegetationModel(float[] uvBases) {
        float tileSize = 1.0f / 16.0f;
        this.texCoords = new float[24 * 2];
        for (int face = 0; face < 2; face++) {
            float u0 = uvBases[0];
            float v0 = uvBases[1];
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
    }

    public void create() {
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
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, MemoryUtil.memAllocInt(indicies.length).put(indicies).flip(), GL_STATIC_DRAW);
    }

    /**
     * Render
     * @param position
     */
    public void render(Vector3i position) {
        boolean cullFace = glIsEnabled(GL_CULL_FACE);
        glDisable(GL_CULL_FACE);

        Matrix4f model = new Matrix4f().translate(new Vector3f(position));

        ShaderManager.basicBlockShader.use();
        ShaderManager.basicBlockShader.setMat4("model", model);
        ShaderManager.basicBlockShader.useFog();

        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, indicies.length, GL_UNSIGNED_INT, 0);

        glBindTexture(GL_TEXTURE_2D, TextureManager.terrainTexture);

        glBindVertexArray(0);
        ShaderManager.basicBlockShader.stop();

        if(cullFace) glEnable(GL_CULL_FACE);
    }
}
