package com.james090500.renderer;

import com.james090500.BlockGame;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.Setter;
import org.joml.Vector3i;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class InstancedBlockRenderer {

    private int vao;
    private int instanceVbo;

    @Setter
    float[] vertices;

    @Setter
    int[] indices;

    @Setter
    float[] uv;

    @Setter
    int[] texture;

    public void create() {
        System.out.println(1);
        System.out.println(Arrays.toString(texture));
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // Vertex Position VBO
        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Index Buffer
        int ibo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        // Instanced VBO
        instanceVbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, instanceVbo);
        glBufferData(GL_ARRAY_BUFFER, 0, GL_DYNAMIC_DRAW);

        glVertexAttribPointer(1, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(1);
        glVertexAttribDivisor(1, 1);

        // UV VBO
        int tbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, tbo);
        glBufferData(GL_ARRAY_BUFFER, this.uv, GL_STATIC_DRAW);

        glVertexAttribPointer(2, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(2);

        // Texture
        int texOffsetVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, texOffsetVBO);
        glBufferData(GL_ARRAY_BUFFER, texture, GL_STATIC_DRAW);

        glVertexAttribIPointer(3, 1, GL_UNSIGNED_INT, 0, 0);
        glEnableVertexAttribArray(3);

        glBindVertexArray(0);
    }

    public void setUV(int faceCount, float[] uvBases) {
        float[][] newUvBases = new float[faceCount][];
        Arrays.fill(newUvBases, uvBases);
        this.setUV(faceCount, newUvBases);
    }

    public void setUV(int faceCount, float[][] uvBases) {
        this.uv = new float[8 * faceCount];
        for (int face = 0; face < faceCount; face++) {
            System.arraycopy(uvBases[face], 0, this.uv, face * 8, 8);
        }
    }

    /**
     * Render
     */
    public void render(ObjectList<Vector3i> positions) {
        // TODO Remove - Ideally need a Instance for each chunk.
        int totalElements = positions.size();
        int needed = positions.size() * 3 * Float.BYTES;

        FloatBuffer inst = MemoryUtil.memAllocFloat(totalElements * 3);
        for (Vector3i p : positions) {
            inst.put(p.x).put(p.y).put(p.z);
        }
        inst.flip();

        glBindBuffer(GL_ARRAY_BUFFER, instanceVbo);
        glBufferData(GL_ARRAY_BUFFER, needed, GL_STREAM_DRAW);
        ByteBuffer dst = glMapBufferRange(GL_ARRAY_BUFFER, 0, needed, GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
        dst.asFloatBuffer().put(inst);
        glUnmapBuffer(GL_ARRAY_BUFFER);
        // TODO End

        ShaderManager.instancedBlockShader.use();
        ShaderManager.instancedBlockShader.useFog();

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D_ARRAY, BlockGame.getInstance().getTextureManager().getChunkTexture());

        glBindVertexArray(vao);
        glDrawElementsInstanced(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0, totalElements);

        glBindVertexArray(0);
        ShaderManager.instancedBlockShader.stop();
    }

}
