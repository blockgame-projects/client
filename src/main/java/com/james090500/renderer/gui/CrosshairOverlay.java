package com.james090500.renderer.gui;

import com.james090500.BlockGame;
import com.james090500.renderer.Renderer;
import com.james090500.renderer.ShaderManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class CrosshairOverlay implements Renderer {
    private final int vao;
    private final float size = 0.02f;

    // 4 vertices: 2 for horizontal, 2 for vertical
    float[] vertices = {
            -size, 0f,   // left
            size, 0f,   // right
            0f, -size,  // down
            0f,  size   // up
    };

    public CrosshairOverlay() {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
    }

    @Override
    public Vector3f getPosition() {
        return null;
    }

    @Override
    public Vector3f getBoundingBox() {
        return null;
    }

    @Override
    public void render() {
        boolean depth = glIsEnabled(GL_DEPTH_TEST);
        glDisable(GL_DEPTH_TEST);
        float width = 2.0f;
        glLineWidth(width);

        Matrix4f model = new Matrix4f().translate(0f, 0f, -1.5f); // Right, down, forward from camera

        // Create identity view matrix (static in camera space)
        Matrix4f view = new Matrix4f(); // No camera transform

        // Perspective projection matrix (same as world rendering)
        Matrix4f projection = new Matrix4f().perspective(
                BlockGame.getInstance().getCamera().getFov(),
                BlockGame.getInstance().getCamera().getAspect(),
                0.01f,
                100f
        );

        ShaderManager.basicShader.use();
        ShaderManager.basicShader.setMat4("model", model);
        ShaderManager.basicShader.setMat4("view", view);
        ShaderManager.basicShader.setMat4("projection", projection);
        ShaderManager.basicShader.setVec3("color", new Vector3f(0, 0, 0));

        glBindVertexArray(vao);
        glDrawArrays(GL_LINES, 0, 4);

        glBindVertexArray(0);
        ShaderManager.basicShader.stop();

        if (depth) glEnable(GL_DEPTH_TEST);
        glLineWidth(1f);
    }
}
