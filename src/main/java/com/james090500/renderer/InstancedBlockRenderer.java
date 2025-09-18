package com.james090500.renderer;

import com.james090500.BlockGame;
import com.james090500.blocks.IBlockRender;
import com.james090500.utils.TextureManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.Setter;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class InstancedBlockRenderer {

    private int currentCapacity = 4096;
    private int vao;
    private int instanceVbo;

    @Setter
    float[] vertices;

    @Setter
    int[] indices;

    @Setter
    float[] texCoords;

    public void create() {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // Vertex Position VBO
        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Index Buffer (EBO)
        int ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        // Instanced VBO
        instanceVbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, instanceVbo);
        // Allocate some capacity; you can grow/shrink later.
        glBufferData(GL_ARRAY_BUFFER, 3L * Float.BYTES * currentCapacity, GL_DYNAMIC_DRAW);

        // Position
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(1);
        glVertexAttribDivisor(1, 1);

        // UV VBO
        int tbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, tbo);
        glBufferData(GL_ARRAY_BUFFER, texCoords, GL_STATIC_DRAW);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(2);

        glBindVertexArray(0);
    }

    public void setUV(int faceCount, float[] uvBases) {
        float[][] newUvBases = new float[faceCount][];
        for(int i = 0; i < faceCount; i++) {
            newUvBases[i] = new float[] { uvBases[0], uvBases[1] };
        }

        this.setUV(faceCount, newUvBases);
    }

    public void setUV(int faceCount, float[][] uvBases) {
        float tileSize = 1.0f / 16.0f;
        texCoords = new float[faceCount * 8];
        for (int face = 0; face < faceCount; face++) {
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
    }

    /**
     * Render
     * @param positions
     */
    public void render(ObjectList<Vector3i> positions) {
        // Build instance data: [x,y,z,rot] per instance
        int n = positions.size();
        if (n == 0) return;

        FloatBuffer inst = MemoryUtil.memAllocFloat(n * 3);
        for (Vector3i p : positions) {
            inst.put(p.x).put(p.y).put(p.z);
        }
        inst.flip();

        glBindBuffer(GL_ARRAY_BUFFER, instanceVbo);
        int strideBytes = 3 * Float.BYTES; // e.g., x,y,z
        int needed = positions.size() * strideBytes;
        if (needed > currentCapacity) {
            glBindBuffer(GL_ARRAY_BUFFER, instanceVbo);
            glBufferData(GL_ARRAY_BUFFER, needed, GL_DYNAMIC_DRAW); // reallocate
            currentCapacity = needed;
        }
        glBufferSubData(GL_ARRAY_BUFFER, 0, inst);
        MemoryUtil.memFree(inst);

        // State once for the whole vegetation pass ideally (outside this method):
        glBindTexture(GL_TEXTURE_2D, TextureManager.terrainTexture);

        ShaderManager.instancedBlockShader.use();
        ShaderManager.instancedBlockShader.useFog();

        glBindVertexArray(vao);
        glDrawElementsInstanced(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0, n);
        glBindVertexArray(0);
    }

}
