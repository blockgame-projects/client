package com.james090500;

import com.james090500.client.Camera;
import com.james090500.client.ClientWindow;
import com.james090500.renderer.RenderManager;
import com.james090500.world.World;
import lombok.Getter;
import org.lwjgl.opengl.GL;

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

        camera = new Camera();
        world = new World();

        // Create the world
        world.createWorld();

        this.loop(clientWindow);
    }

    public void loop(ClientWindow clientWindow) {
        // Set the clear color
        glClearColor(0.529f, 0.808f, 0.922f, 1.0f);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(clientWindow.getWindow()) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            // Render all pending objects
            RenderManager.render();

            // Swap the buffers
            glfwSwapBuffers(clientWindow.getWindow());

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

}
