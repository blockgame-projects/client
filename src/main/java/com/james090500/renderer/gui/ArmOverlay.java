package com.james090500.renderer.gui;

import com.james090500.BlockGame;
import com.james090500.blocks.Blocks;
import com.james090500.client.Camera;
import com.james090500.renderer.Renderer;
import com.james090500.renderer.ShaderManager;
import com.james090500.utils.TextureManager;
import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

            // Back face
            0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f,  0.5f, -0.5f,
            0.5f,  0.5f, -0.5f,

            // Left face
            -0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f,  0.5f,
            -0.5f,  0.5f,  0.5f,
            -0.5f,  0.5f, -0.5f,

            // Right face
            0.5f, -0.5f,  0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f,  0.5f, -0.5f,
            0.5f,  0.5f,  0.5f,

            // Top face
            -0.5f,  0.5f,  0.5f,
            0.5f,  0.5f,  0.5f,
            0.5f,  0.5f, -0.5f,
            -0.5f,  0.5f, -0.5f,

            // Bottom face
            -0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f, -0.5f,  0.5f,
            -0.5f, -0.5f,  0.5f
    };

    int[] indices = {
            0, 1, 2,   2, 3, 0,       // Front
            4, 5, 6,   6, 7, 4,       // Back
            8, 9,10,  10,11, 8,       // Left
            12,13,14,  14,15,12,       // Right
            16,17,18,  18,19,16,       // Top
            20,21,22,  22,23,20        // Bottom
    };

    private float[] texCoords;

    public ArmOverlay() {
        this.create();
    }

    public void create() {
//        float[] uv = Blocks.ids[BlockGame.getInstance().getLocalPlayer().getCurrentBlock()].getTexture();
        float[] uv = Blocks.dirtBlock.getTexture();
        float tileSize = 1.0f / 16.0f;

        float[] texCoords = new float[6 * 4 * 2]; // 6 faces × 4 vertices × 2 UVs

        float[] faceUV = {
                uv[0], uv[1] + tileSize,             // Bottom-left
                uv[0] + tileSize, uv[1] + tileSize,  // Bottom-right
                uv[0] + tileSize, uv[1],             // Top-right
                uv[0], uv[1]                         // Top-left
        };

        // Fill texCoords with the same faceUV 6 times
        for (int i = 0; i < 6; i++) {
            System.arraycopy(faceUV, 0, texCoords, i * 8, 8);
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
                .translate(0.6f, -0.6f, -1.5f) // Right, down, forward from camera
                .rotateX((float) Math.toRadians(-20)) // Optional tilt
                .rotateY((float) Math.toRadians(30))
                .scale(0.4f); // Scale down to fit in view
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
