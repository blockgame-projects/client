package com.james090500.client;

import com.james090500.BlockGame;
import lombok.Getter;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class ClientWindow {

    private final String title = "BlockGame";
    @Getter
    private final int width = 962;
    @Getter
    private final int height = 768;

    @Getter
    private long window;

    private float speed = 0.05f;
    private double lastMouseX, lastMouseY;
    private boolean firstMouse = true;

    public void create() {
        GLFWErrorCallback errorCallback;
        glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints(); // Loads GLFW's default window settings
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE); // Sets window to be visible
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // Sets windows to be non-resizable

        // Set OpenGL version
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        this.window = glfwCreateWindow(width, height, title, NULL, NULL); // Does the actual window creation
        if ( this.window == NULL ) throw new RuntimeException("Failed to create window");

        glfwMakeContextCurrent(this.window); // glfwSwapInterval needs a context on the calling thread, otherwise will cause NO_CURRENT_CONTEXT error
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED); // take the mouse
        GL.createCapabilities(); // Will let lwjgl know we want to use this context as the context to draw with

        // Viewport
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer framebufferWidth = stack.mallocInt(1);
            IntBuffer framebufferHeight = stack.mallocInt(1);
            glfwGetFramebufferSize(window, framebufferWidth, framebufferHeight);
            glViewport(0, 0, framebufferWidth.get(0), framebufferHeight.get(0));
        }

        // Blending
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // 3D Depth
        glEnable(GL_DEPTH_TEST);

        // Disable back faces
        glEnable(GL_CULL_FACE); // Enable face culling
        glCullFace(GL_BACK); // Cull back faces (i.e. only render front faces)

        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        // Bring to front
        glfwFocusWindow(window);

        // Cursor
        glfwSetCursorPosCallback(window, (w, xpos, ypos) -> {
            if (firstMouse) {
                lastMouseX = xpos;
                lastMouseY = ypos;
                firstMouse = false;
            }
            float sensitivity = 0.1f;
            double dx = xpos - lastMouseX;
            double dy = lastMouseY - ypos;

            lastMouseX = xpos;
            lastMouseY = ypos;

            Camera camera = BlockGame.getInstance().getCamera();
            camera.yaw += (float) (dx * sensitivity);
            camera.pitch += (float) (dy * sensitivity);
            camera.pitch = Math.max(-89f, Math.min(89f, camera.pitch));
        });
    }

    public void loop() {
        glfwPollEvents();

        Camera camera = BlockGame.getInstance().getCamera();
        float[] dir = camera.getDirection();
        float[] right = new float[]{-dir[2], 0, dir[0]}; // cross product with Y up

        if (isKeyDown(GLFW_KEY_W))
            camera.move(dir[0] * speed, dir[1] * speed, dir[2] * speed);

        if (isKeyDown(GLFW_KEY_S))
            camera.move(-dir[0] * speed, -dir[1] * speed, -dir[2] * speed);

        if (isKeyDown(GLFW_KEY_A))
            camera.move(right[0] * speed, 0, right[2] * speed);

        if (isKeyDown(GLFW_KEY_D))
            camera.move(-right[0] * speed, 0, -right[2] * speed);

        if (isKeyDown(GLFW_KEY_SPACE))
            camera.move(0, speed, 0);

        if (isKeyDown(GLFW_KEY_LEFT_SHIFT))
            camera.move(0, -speed, 0);

        if (isKeyDown(GLFW_KEY_ESCAPE))
            glfwSetWindowShouldClose(window, true);
    }

    private boolean isKeyDown(int key) {
        return glfwGetKey(window, key) == GLFW_PRESS;
    }
}
