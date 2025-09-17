package com.james090500.renderer.shaders;

import com.james090500.BlockGame;
import com.james090500.world.World;
import org.joml.Matrix4f;
import org.joml.Vector3f;
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

        String vsLog = glGetShaderInfoLog(vertexShader);
        if (!vsLog.isEmpty()) {
            BlockGame.getLogger().severe("Vertex Shader log:\n" + vsLog);
        }

        String fsLog = glGetShaderInfoLog(fragmentShader);
        if (!fsLog.isEmpty()) {
            BlockGame.getLogger().severe("Fragment Shader log:\n" + fsLog);
        }

        String progLog = glGetProgramInfoLog(programId);
        if (!progLog.isEmpty()) {
            BlockGame.getLogger().severe("Program link log:\n" + progLog);
        }


        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    private int getUniformLocation(String name) {
        int location = glGetUniformLocation(programId, name);
        if (location == -1) {
            System.err.println("Uniform '" + name + "' not found in shader!");
        }
        return location;
    }

    public void setFloat(String name, float value) {
        int location = getUniformLocation(name);
        if (location != -1) {
            glUniform1f(location, value);
        }
    }

    public void setVec3(String name, Vector3f vector3f) {
        int location = getUniformLocation(name);
        if (location != -1) {
            glUniform3f(location, vector3f.x, vector3f.y, vector3f.z);
        }
    }

    public void setMat4(String name, Matrix4f matrix) {
        int location = getUniformLocation(name);
        if (location != -1) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                FloatBuffer buffer = stack.mallocFloat(16);
                matrix.get(buffer);
                glUniformMatrix4fv(location, false, buffer);
            }
        }
    }

    public void use() {
        glUseProgram(programId);
        setMat4("view", BlockGame.getInstance().getCamera().getViewMatrix());
        setMat4("projection", BlockGame.getInstance().getCamera().getProjectionMatrix());
    }

    public void useFog() {
        World.Fog fog = BlockGame.getInstance().getWorld().getFog();

        setVec3("fog.color", fog.color());
        setFloat("fog.start", fog.start());
        setFloat("fog.density", fog.density());
    }

    public void stop() {
        glUseProgram(0);
    }

    public void delete() {
        glDeleteProgram(programId);
    }
}
