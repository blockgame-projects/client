package com.james090500.renderer;

import com.james090500.BlockGame;
import com.james090500.textures.TextureLocation;
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
    private int totalElements;

    @Setter
    float[] vertices;

    @Setter
    int[] indices;

    float[] uv;

    @Setter
    int faces;

    int[] texture;

    public InstancedBlockRenderer() {
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

    public void setTexture(TextureLocation layer) {
        int numVerts = this.faces * 4;
        this.texture = new int[numVerts];
        Arrays.fill(this.texture, layer.getId());
    }

    public void setTexture(TextureLocation[] faceLayers) {
        if (faceLayers == null) throw new IllegalArgumentException("faceLayers is null");
        if (faceLayers.length < this.faces) {
            throw new IllegalArgumentException("faceLayers.length (" + faceLayers.length + ") < faces (" + this.faces + ")");
        }

        int numVerts = this.faces * 4;
        this.texture = new int[numVerts];
        for (int face = 0; face < this.faces; face++) {
            int id = faceLayers[face].getId();
            int base = face * 4;
            this.texture[base] = id;
            this.texture[base + 1] = id;
            this.texture[base + 2] = id;
            this.texture[base + 3] = id;
        }
    }

    public void setUV(float[] uvBases) {
        if (uvBases == null || uvBases.length != 8) {
            throw new IllegalArgumentException("UV must have 8 floats (4 verts × 2 coords).");
        }
        this.uv = new float[8 * this.faces];
        for (int face = 0; face < this.faces; face++) {
            System.arraycopy(uvBases, 0, this.uv, face * 8, 8);
        }
    }

    public void updatePositions(ObjectList<Vector3i> positions) {
        totalElements = positions.size();
        int needed = totalElements * 3 * Float.BYTES;
        FloatBuffer inst = MemoryUtil.memAllocFloat(needed);
        for(Vector3i p : positions) {
            inst.put(p.x).put(p.y).put(p.z);
        }
        inst.flip();

        glBindBuffer(GL_ARRAY_BUFFER, instanceVbo);
        glBufferData(GL_ARRAY_BUFFER, needed, GL_STREAM_DRAW);
        ByteBuffer dst = glMapBufferRange(GL_ARRAY_BUFFER, 0, needed, GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
        dst.asFloatBuffer().put(inst);
        glUnmapBuffer(GL_ARRAY_BUFFER);
    }

    /**
     * Render
     */
    public void render() {
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
