package com.james090500.client;

import com.james090500.BlockGame;
import com.james090500.Config;
import lombok.Getter;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL3.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class ClientWindow {

    private final String title = "BlockGame";
    @Getter
    private final int width = 854;
    @Getter
    private final int height = 480;
    @Getter
    private float devicePixelRatio = 1f;

    @Getter
    private long window;
    @Getter
    private long vg;

    private ClientInput clientInput = new ClientInput();
    double[] mouseX = new double[1];
    double[] mouseY = new double[1];

    private float speed = 0.05f;

    public void create() {
        glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints(); // Loads GLFW's default window settings
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE); // Sets window to be visible

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
            devicePixelRatio = framebufferWidth.get(0) / width;
        }

        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        // Bring to front
        glfwFocusWindow(window);

        // Cursor Movement and Clicks
        glfwSetCursorPosCallback(window, clientInput::mouseMovement);
        glfwSetMouseButtonCallback(window, clientInput::mouseClicked);

        // Generate NanoVG Instance
        vg = nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES);
        if (vg == 0) {
            throw new RuntimeException("Could not init NanoVG");
        }
    }

    public void loop() {
        glfwPollEvents();

        glfwGetCursorPos(window, mouseX, mouseY);

        //Skip if paused
        if(BlockGame.getInstance().getConfig().isPaused())
            return;

        Camera camera = BlockGame.getInstance().getCamera();
        float[] dir = camera.getDirection();

        // Flatten direction to XZ plane
        float[] flatDir = new float[]{dir[0], 0, dir[2]};
        float length = (float) Math.sqrt(flatDir[0] * flatDir[0] + flatDir[2] * flatDir[2]);
        if (length != 0) {
            flatDir[0] /= length;
            flatDir[2] /= length;
        }

        // Left vector (90 degrees rotated from flatDir)
        float[] left = new float[]{flatDir[2], 0, flatDir[0]};

        if (isKeyDown(GLFW_KEY_W))
            camera.move(flatDir[0] * speed, 0, flatDir[2] * speed);

        if (isKeyDown(GLFW_KEY_S))
            camera.move(-flatDir[0] * speed, 0, -flatDir[2] * speed);

        if (isKeyDown(GLFW_KEY_A))
            camera.move(left[0] * speed, 0, left[2] * speed);

        if (isKeyDown(GLFW_KEY_D))
            camera.move(-left[0] * speed, 0, -left[2] * speed);

        if (isKeyDown(GLFW_KEY_SPACE))
            camera.move(0, speed, 0);

        if (isKeyDown(GLFW_KEY_LEFT_SHIFT))
            camera.move(0, -speed, 0);

        if (isKeyDown(GLFW_KEY_ESCAPE))
            BlockGame.getInstance().pause();
    }

    public double getMouseX() {
        return mouseX[0];
    }

    public double getMouseY() {
        return mouseY[0];
    }

    private boolean isKeyDown(int key) {
        return glfwGetKey(window, key) == GLFW_PRESS;
    }
}
