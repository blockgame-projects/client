package com.james090500.entity.model;

import com.james090500.renderer.Renderer;
import com.james090500.renderer.ShaderManager;
import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class PlayerModel implements Renderer {

    private final int vao;

    // Cube centered at origin, side length = 1.0 (±0.5)
    float[] vertices = {
        // Back face (z = -0.5)
        -0.5f, -1.0f, -0.5f, // 0
        0.5f, -1.0f, -0.5f, // 1
        0.5f,  1.0f, -0.5f, // 2
        -0.5f,  1.0f, -0.5f, // 3

        // Front face (z = +0.5)
        -0.5f, -1.0f,  0.5f, // 4
        0.5f, -1.0f,  0.5f, // 5
        0.5f,  1.0f,  0.5f, // 6
        -0.5f,  1.0f,  0.5f  // 7
    };

    int[] indices = {
        // Front
        4, 5, 6,   6, 7, 4,
        // Back
        1, 0, 3,   3, 2, 1,
        // Left
        0, 4, 7,   7, 3, 0,
        // Right
        5, 1, 2,   2, 6, 5,
        // Top
        3, 7, 6,   6, 2, 3,
        // Bottom
        0, 1, 5,   5, 4, 0
    };


    @Getter @Setter private Vector3f position;

    public PlayerModel() {
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
    }

    @Override
    public Vector3f getBoundingBox() {
        return null;
    }

    public void render() {
        Matrix4f model = new Matrix4f().translate(this.getPosition());

        ShaderManager.basicShader.use();
        ShaderManager.basicShader.setMat4("model", model);
        ShaderManager.basicShader.setVec3("color", new Vector3f(0, 0, 1));

        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);

        glBindVertexArray(0);
        ShaderManager.basicShader.stop();
    }


}
