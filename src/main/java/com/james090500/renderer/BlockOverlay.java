package com.james090500.renderer;

import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class BlockOverlay implements Renderer {
    private int vao;
    private final float[] cubeEdges = {
            // Bottom square
            0.0f, 0.0f, 0.0f,   1.01f, 0.0f, 0.0f,
            1.01f, 0.0f, 0.0f,   1.01f, 0.0f,  1.01f,
            1.01f, 0.0f,  1.01f,  0.0f, 0.0f,  1.01f,
            0.0f, 0.0f,  1.01f,  0.0f, 0.0f, 0.0f,

            // Top square
            0.0f,  1.01f, 0.0f,   1.01f,  1.01f, 0.0f,
            1.01f,  1.01f, 0.0f,   1.01f,  1.01f,  1.01f,
            1.01f,  1.01f,  1.01f,  0.0f,  1.01f,  1.01f,
            0.0f,  1.01f,  1.01f,  0.0f,  1.01f, 0.0f,

            // Vertical lines
            0.0f, 0.0f, 0.0f,  0.0f,  1.01f, 0.0f,
            1.01f, 0.0f, 0.0f,   1.01f,  1.01f, 0.0f,
            1.01f, 0.0f,  1.01f,   1.01f,  1.01f,  1.01f,
            0.0f, 0.0f,  1.01f,  0.0f,  1.01f,  1.01f,
    };

    @Getter @Setter private Vector3f position;

    public BlockOverlay() {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, cubeEdges, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
    }

    @Override
    public Vector3f getBoundingBox() {
        return null;
    }

    @Override
    public void render() {
        Matrix4f model = new Matrix4f().translate(this.getPosition());

        ShaderManager.basicShader.use();
        ShaderManager.basicShader.setMat4("model", model);
        ShaderManager.basicShader.setVec3("color", new Vector3f(0, 0, 0));

        glBindVertexArray(vao);
        glDrawArrays(GL_LINES, 0, cubeEdges.length / 3);

        glBindVertexArray(0);
        ShaderManager.basicShader.stop();
    }
}
