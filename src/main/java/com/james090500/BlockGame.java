package com.james090500;

import com.james090500.client.Camera;
import com.james090500.client.ClientWindow;
import com.james090500.gui.PauseScreen;
import com.james090500.gui.ScreenManager;
import com.james090500.renderer.RenderManager;
import com.james090500.utils.ThreadUtil;
import com.james090500.world.World;
import lombok.Getter;

import java.util.logging.Logger;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

@Getter
public class BlockGame {

    @Getter
    private static BlockGame instance;
    @Getter
    private final Config config = new Config();
    @Getter
    private static final Logger logger = Logger.getLogger("BlockGame");
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

    public void unpause() {
        ScreenManager.closeAll();
        BlockGame.getInstance().getConfig().setPaused(false);
        glfwSetInputMode(clientWindow.getWindow(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    public void pause() {
        ScreenManager.closeAll();
        ScreenManager.add(new PauseScreen());
        BlockGame.getInstance().getConfig().setPaused(true);
        glfwSetInputMode(clientWindow.getWindow(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
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

            // Blending
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            // 3D Depth
            glEnable(GL_DEPTH_TEST);

            // Disable back faces
            glEnable(GL_CULL_FACE); // Enable face culling
            glCullFace(GL_BACK); // Cull back faces (i.e. only render front faces)

            // Inputs etc
            clientWindow.loop();

            // Render all pending objects
            RenderManager.render();

            //Run a single main thread queue
            ThreadUtil.runMainQueue();

            // Render UI
            ScreenManager.render();

            // Swap the buffers
            glfwSwapBuffers(clientWindow.getWindow());

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
