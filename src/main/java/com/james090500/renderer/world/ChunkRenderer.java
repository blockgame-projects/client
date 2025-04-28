package com.james090500.renderer.world;

import com.james090500.renderer.Renderer;
import com.james090500.renderer.ShaderManager;
import com.james090500.world.Chunk;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class ChunkRenderer implements Renderer {

    private int vao;
    private int vertexCount;

    public void mesh(Chunk chunk) {
        float[] vertices = {
            // Front face
            1.0f,  1.0f,  1.0f, // 0 Top Right Front
            1.0f, 0f,  1.0f, // 1 Bottom Right Front
            0f, 0f,  1.0f, // 2 Bottom Left Front
            0f,  1.0f,  1.0f, // 3 Top Left Front

            // Back face
            1.0f,  1.0f, 0f, // 4 Top Right Back
            1.0f, 0f, 0f, // 5 Bottom Right Back
            0f, 0f, 0f, // 6 Bottom Left Back
            0f,  1.0f, 0f  // 7 Top Left Back
        };

        int[] indices = {
                // Front face
                0, 1, 3,
                1, 2, 3,

                // Right face
                4, 5, 0,
                5, 1, 0,

                // Back face
                7, 6, 4,
                6, 5, 4,

                // Left face
                3, 2, 7,
                2, 6, 7,

                // Top face
                4, 0, 7,
                0, 3, 7,

                // Bottom face
                1, 5, 2,
                5, 6, 2
        };

        vertexCount = indices.length;

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, BufferUtils.createFloatBuffer(vertices.length).put(vertices).flip(), GL_STATIC_DRAW);

        int ibo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, BufferUtils.createIntBuffer(indices.length).put(indices).flip(), GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);

        glBindVertexArray(0); // unbind for safety
    }

    @Override
    public void render() {
        float time = (float) glfwGetTime();
        Matrix4f model = new Matrix4f()
                .rotate(time * (float) Math.toRadians(50.0f), new Vector3f(0.5f, 1.0f, 0.0f));

        ShaderManager.chunk.use();
        ShaderManager.chunk.setUniformMat4("model", model);
        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0L);
        glBindVertexArray(0);
        ShaderManager.chunk.stop();
    }
}