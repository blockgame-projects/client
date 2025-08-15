package com.james090500.renderer.gui;

import com.james090500.BlockGame;
import com.james090500.blocks.Blocks;
import com.james090500.renderer.Renderer;
import com.james090500.renderer.ShaderManager;
import com.james090500.utils.TextureManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

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

    private float[] texCoords;

    public void create() {
        float[] sideUV = Blocks.ids[BlockGame.getInstance().getLocalPlayer().getCurrentBlock()].getTexture();
        float[] topUV = Blocks.ids[BlockGame.getInstance().getLocalPlayer().getCurrentBlock()].getTexture("top");

        float tileSize = 1.0f / 16.0f;

        float[] texCoords = new float[8 * 2]; // 2 faces × 4 vertices × 2 coords

        // Front (side) face
        texCoords[0] = sideUV[0];                texCoords[1] = sideUV[1];            // bottom-left
        texCoords[2] = sideUV[0] + tileSize;     texCoords[3] = sideUV[1];            // bottom-right
        texCoords[4] = sideUV[0] + tileSize;     texCoords[5] = sideUV[1] + tileSize; // top-right
        texCoords[6] = sideUV[0];                texCoords[7] = sideUV[1] + tileSize; // top-left

        // Top face
        texCoords[8] = topUV[0];                 texCoords[9] = topUV[1] + tileSize;
        texCoords[10] = topUV[0] + tileSize;     texCoords[11] = topUV[1] + tileSize;
        texCoords[12] = topUV[0] + tileSize;     texCoords[13] = topUV[1];
        texCoords[14] = topUV[0];                texCoords[15] = topUV[1];

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

    @Override
    public void render() {
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

        glBindTexture(GL_TEXTURE_2D, TextureManager.terrainTexture);

        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);

        glBindVertexArray(0);
        ShaderManager.basicBlockShader.stop();

        glEnable(GL_DEPTH_TEST);
    }
}
