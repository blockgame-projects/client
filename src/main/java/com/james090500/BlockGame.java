package com.james090500;

import com.james090500.client.Camera;
import com.james090500.client.ClientWindow;
import com.james090500.renderer.RenderManager;
import com.james090500.utils.ThreadUtil;
import com.james090500.world.World;
import lombok.Getter;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

@Getter
public class BlockGame {

    @Getter
    private static BlockGame instance;
    private final ClientWindow clientWindow;
    private final Camera camera;
    private final World world;

    public BlockGame() {
        instance = this;

        clientWindow = new ClientWindow();
        clientWindow.create();

        camera = new Camera(0, 100, 0);
        world = new World();

        this.loop(clientWindow);

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(clientWindow.getWindow());
        glfwDestroyWindow(clientWindow.getWindow());

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void loop(ClientWindow clientWindow) {
        int fps = 0;
        long start = System.currentTimeMillis();

        // Set the clear color
        glClearColor(0.529f, 0.808f, 0.922f, 1.0f);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(clientWindow.getWindow()) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            // Inputs etc
            clientWindow.loop();

            // Render all pending objects
            RenderManager.render();

            //Run a single main thread queue
            ThreadUtil.runMainQueue();

            // Swap the buffers
            glfwSwapBuffers(clientWindow.getWindow());

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();

            long now = System.currentTimeMillis();
            if (now - start >= 1000) {
                System.out.println(fps + " FPS");

                start = now;
                fps = 0;
            } else {
                fps++;
            }
        }
    }

}
