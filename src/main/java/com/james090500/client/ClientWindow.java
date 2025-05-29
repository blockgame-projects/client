package com.james090500.client;

import com.james090500.BlockGame;
import lombok.Getter;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nanovg.NanoVGGL3.*;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

public class ClientWindow {

    private final String title = "BlockGame";

    @Getter
    private int baseWidth = 854;
    @Getter
    private int baseHeight = 480;
    @Getter
    private int windowWidth = baseWidth;
    @Getter
    private int windowHeight = baseHeight;
    @Getter
    private int framebufferWidth = baseWidth;
    @Getter
    private int framebufferHeight = baseHeight;

    @Getter
    private long window;
    @Getter
    private long vg;

    @Getter
    private ClientInput clientInput = new ClientInput();
    double[] mouseX = new double[1];
    double[] mouseY = new double[1];

    public void create() {
        glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints(); // Loads GLFW's default window settings
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE); // Sets window to be visible
        GLFW.glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); //Allows resizing the window

        // Set OpenGL version
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        this.window = glfwCreateWindow(windowWidth, windowHeight, title, NULL, NULL); // Does the actual window creation
        if ( this.window == NULL ) throw new RuntimeException("Failed to create window");

        glfwMakeContextCurrent(this.window); // glfwSwapInterval needs a context on the calling thread, otherwise will cause NO_CURRENT_CONTEXT error
        GL.createCapabilities(); // Will let lwjgl know we want to use this context as the context to draw with

        glfwSetWindowSizeCallback(window, (win, w, h) -> {
            this.windowWidth = w;
            this.windowHeight = h;
        });

        glfwSetFramebufferSizeCallback(window, (win, fbw, fbh) -> {
            this.framebufferWidth = fbw;
            this.framebufferHeight = fbh;

            glViewport(0, 0, fbw, fbh);

            if (BlockGame.getInstance().getCamera() != null) {
                BlockGame.getInstance().getCamera().setAspectRatio((float) fbw / fbh);
                BlockGame.getInstance().getCamera().updateProjectionMatrix();
            }
        });

        // Initial size setup
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer winWidth = stack.mallocInt(1);
            IntBuffer winHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, winWidth, winHeight);
            this.windowWidth = winWidth.get(0);
            this.windowHeight = winHeight.get(0);

            IntBuffer fbWidth = stack.mallocInt(1);
            IntBuffer fbHeight = stack.mallocInt(1);
            glfwGetFramebufferSize(window, fbWidth, fbHeight);
            this.framebufferWidth = fbWidth.get(0);
            this.framebufferHeight = fbHeight.get(0);

            glViewport(0, 0, fbWidth.get(0), fbHeight.get(0));
        }

        // Enable v-sync
        glfwSwapInterval(0);

        // Make the window visible
        glfwShowWindow(window);

        //Center the window
        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        GLFW.glfwSetWindowPos(window, (vidMode.width() - windowWidth) / 2, (vidMode.height() - windowHeight) / 2);

        // Bring to front
        glfwFocusWindow(window);

        // Cursor Movement and Clicks
        glfwSetCursorPosCallback(window, clientInput::mouseMovement);
        glfwSetMouseButtonCallback(window, clientInput::mouseClicked);
        glfwSetKeyCallback(window, clientInput::keyPressed);

        // Generate NanoVG Instance
        vg = nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES);
        if (vg == 0) {
            throw new RuntimeException("Could not init NanoVG");
        }
    }

    public void poll() {
        glfwPollEvents();
        glfwGetCursorPos(window, mouseX, mouseY);
    }

    public double getMouseX() {
        return mouseX[0];
    }

    public double getMouseY() {
        return mouseY[0];
    }


}
