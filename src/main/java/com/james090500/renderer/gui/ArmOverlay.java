package com.james090500.renderer.gui;

import com.james090500.BlockGame;
import com.james090500.blocks.Block;
import com.james090500.blocks.Blocks;
import com.james090500.renderer.Renderer;
import com.james090500.renderer.ShaderManager;
import com.james090500.textures.TextureLocation;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class ArmOverlay implements Renderer {
    private int vao;

    float[] vertices = {
            // Front face
            -0.5f, -0.5f,  0.5f,
            0.5f, -0.5f,  0.5f,
            0.5f,  0.5f,  0.5f,
            -0.5f,  0.5f,  0.5f,

            // Top face
            -0.5f,  0.5f,  0.5f,
            0.5f,  0.5f,  0.5f,
            0.5f,  0.5f, -0.5f,
            -0.5f,  0.5f, -0.5f,
    };

    int[] indices = {
            0, 1, 2,   2, 3, 0,       // Front
            4, 5, 6,   6, 7, 4,       // Top
    };

    Block currentBlock;

    public void create() {
        this.currentBlock = Blocks.get(BlockGame.getInstance().getLocalPlayer().getCurrentBlock());
        float[] uv = this.setUV(currentBlock.getUv());
        int[] textures = this.setTexture(new TextureLocation[] {
                currentBlock.getTexture("side"),
                currentBlock.getTexture("top")
        });

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // Vertex Position VBO
        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // UV VBO
        int tbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, tbo);
        glBufferData(GL_ARRAY_BUFFER, uv, GL_STATIC_DRAW);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(1);

        // Texture
        int texOffsetVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, texOffsetVBO);
        glBufferData(GL_ARRAY_BUFFER, textures, GL_STATIC_DRAW);

        glVertexAttribIPointer(2, 1, GL_UNSIGNED_INT, 0, 0);
        glEnableVertexAttribArray(2);

        // Index Buffer (EBO)
        int ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
    }

    private float[] setUV(float[] uvBases) {
        if (uvBases == null || uvBases.length != 8) {
            throw new IllegalArgumentException("UV must have 8 floats (4 verts × 2 coords).");
        }
        float[] uv = new float[8 * 2];
        for (int face = 0; face < 2; face++) {
            System.arraycopy(uvBases, 0, uv, face * 8, 8);
        }
        return uv;
    }

    @Override
    public Vector3f getPosition() {
        return null;
    }

    @Override
    public Vector3f getBoundingBox() {
        return null;
    }

    public Matrix4f getModel() {
        return new Matrix4f()
                .translate(0.93f, -0.7f, -1.5f) // Right, down, forward from camera
                .rotateX((float) Math.toRadians(10)) // Optional tilt
                .rotateY((float) Math.toRadians(-35))
                .scale(0.5f); // Scale down to fit in view
    }

    /**
     * Convert texture array into an int of IDs
     * @param faceLayers The texture to use
     * @return
     */
    public int[] setTexture(TextureLocation[] faceLayers) {
        int numVerts = 4 * 2;
        int[] texture = new int[numVerts];
        for (int face = 0; face < 2; face++) {
            int id = faceLayers[face].getId();
            int base = face * 4;
            texture[base] = id;
            texture[base + 1] = id;
            texture[base + 2] = id;
            texture[base + 3] = id;
        }

        return texture;
    }

    @Override
    public void render() {
        boolean depth = glIsEnabled(GL_DEPTH_TEST);
        glDisable(GL_DEPTH_TEST);

        // Create identity view matrix (static in camera space)
        Matrix4f view = new Matrix4f(); // No camera transform

        // Perspective projection matrix (same as world rendering)
        Matrix4f projection = new Matrix4f().perspective(
                BlockGame.getInstance().getCamera().getFov(),
                BlockGame.getInstance().getCamera().getAspect(),
                0.01f,
                100f
        );

        ShaderManager.basicBlockShader.use();

        ShaderManager.basicBlockShader.setMat4("model", getModel());
        ShaderManager.basicBlockShader.setMat4("view", view);
        ShaderManager.basicBlockShader.setMat4("projection", projection);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D_ARRAY, BlockGame.getInstance().getTextureManager().getChunkTexture());

        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);

        glBindVertexArray(0);
        ShaderManager.basicBlockShader.stop();

        if(depth) glEnable(GL_DEPTH_TEST);
    }
}
