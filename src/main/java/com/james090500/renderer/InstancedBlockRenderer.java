package com.james090500.renderer;

import com.james090500.BlockGame;
import com.james090500.blocks.model.IBlockModel;
import com.james090500.textures.TextureLocation;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.joml.Vector3i;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class InstancedBlockRenderer {

    private final int vao;
    private final int instanceVbo;
    private int totalElements;

    private final int faces;
    private final int[] indices;

    /**
     * Create initial mesh
     * @param model
     */
    public InstancedBlockRenderer(IBlockModel model) {
        this.faces = model.getFaces();
        this.indices = model.getIndices();
        float[] uv = this.setUV(model.getUv());
        int[] textures = this.setTexture(model.getTexture());

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // Vertex Position VBO
        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, model.getVertices(), GL_STATIC_DRAW);

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
        glBufferData(GL_ARRAY_BUFFER, uv, GL_STATIC_DRAW);

        glVertexAttribPointer(2, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(2);

        // Texture
        int texOffsetVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, texOffsetVBO);
        glBufferData(GL_ARRAY_BUFFER, textures, GL_STATIC_DRAW);

        glVertexAttribIPointer(3, 1, GL_UNSIGNED_INT, 0, 0);
        glEnableVertexAttribArray(3);

        glBindVertexArray(0);
    }

    /**
     * Update instance positions
     * @param positions
     */
    public void updatePositions(ObjectList<Vector3i> positions) {
        glBindBuffer(GL_ARRAY_BUFFER, instanceVbo);

        final int count = positions.size();
        this.totalElements = count;

        if (count == 0) {
            // Orphan to zero so future maps/draws are harmless
            glBufferData(GL_ARRAY_BUFFER, 0L, GL_STREAM_DRAW);
            return;
        }

        final long bytes = (long) count * 3L * Float.BYTES;

        // As size changes frequently this orphans and avoids stalls.
        glBufferData(GL_ARRAY_BUFFER, bytes, GL_STREAM_DRAW);

        // UNSYNCHRONIZED avoids waiting on GPU when we've just orphaned.
        int flags = GL_MAP_WRITE_BIT | GL_MAP_UNSYNCHRONIZED_BIT;

        ByteBuffer bb = glMapBufferRange(GL_ARRAY_BUFFER, 0L, bytes, flags);
        if (bb == null) {
            throw new IllegalStateException("glMapBufferRange returned null");
        }

        FloatBuffer fb = bb.asFloatBuffer();
        for (Vector3i p : positions) {
            fb.put((float) p.x).put((float) p.y).put((float) p.z);
        }
        // No flip needed for a mapped view
        glUnmapBuffer(GL_ARRAY_BUFFER);
    }

    /**
     * Render
     */
    public void render() {
        if(totalElements > 0) {
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

    /**
     * Convert UVs for a single face into the correct size
     * @param uvBases
     * @return
     */
    public float[] setUV(float[] uvBases) {
        if (uvBases == null || uvBases.length != 8) {
            throw new IllegalArgumentException("UV must have 8 floats (4 verts × 2 coords).");
        }
        float[] uv = new float[8 * this.faces];
        for (int face = 0; face < this.faces; face++) {
            System.arraycopy(uvBases, 0, uv, face * 8, 8);
        }
        return uv;
    }

    /**
     * Convert texture array into an int of IDs
     * @param faceLayers The texture to use
     * @return
     */
    public int[] setTexture(TextureLocation[] faceLayers) {
        if (faceLayers == null) throw new IllegalArgumentException("faceLayers is null");
        if (faceLayers.length < this.faces) {
            throw new IllegalArgumentException("faceLayers.length (" + faceLayers.length + ") < faces (" + this.faces + ")");
        }

        int numVerts = this.faces * 4;
        int[] texture = new int[numVerts];
        for (int face = 0; face < this.faces; face++) {
            int id = faceLayers[face].getId();
            int base = face * 4;
            texture[base] = id;
            texture[base + 1] = id;
            texture[base + 2] = id;
            texture[base + 3] = id;
        }

        return texture;
    }

}
