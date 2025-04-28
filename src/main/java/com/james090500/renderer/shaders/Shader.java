package com.james090500.renderer.shaders;

import com.james090500.BlockGame;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.*;

public class Shader {

    private int programId;

    public void create(String vertexSrc, String fragmentSrc) {
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexSrc);
        glCompileShader(vertexShader);
        if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Vertex shader compilation failed:\n" + glGetShaderInfoLog(vertexShader));
        }

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentSrc);
        glCompileShader(fragmentShader);
        if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Fragment shader compilation failed:\n" + glGetShaderInfoLog(fragmentShader));
        }

        programId = glCreateProgram();
        glAttachShader(programId, vertexShader);
        glAttachShader(programId, fragmentShader);
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader linking failed:\n" + glGetProgramInfoLog(programId));
        }

        System.out.println(glGetShaderInfoLog(vertexShader));
        System.out.println(glGetShaderInfoLog(fragmentShader));
        System.out.println(glGetProgramInfoLog(programId));

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    public void setUniformMat4(String name, Matrix4f matrix) {
        int location = glGetUniformLocation(programId, name);
        if (location != -1) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                FloatBuffer buffer = stack.mallocFloat(16);
                matrix.get(buffer);
                glUniformMatrix4fv(location, false, buffer);
            }
        } else {
            System.err.println("Uniform '" + name + "' not found in shader!");
        }
    }

    public void use() {
        glUseProgram(programId);
        setUniformMat4("view", BlockGame.getInstance().getCamera().getViewMatrix());
        setUniformMat4("projection", BlockGame.getInstance().getCamera().getProjectionMatrix());
    }

    public void stop() {
        glUseProgram(0);
    }

    public void delete() {
        glDeleteProgram(programId);
    }
}
