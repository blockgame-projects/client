package com.james090500;

import com.james090500.client.Camera;
import com.james090500.client.ClientWindow;
import com.james090500.client.LocalPlayer;
import com.james090500.gui.MainMenu;
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

    @Getter private static BlockGame instance;
    private final Config config = new Config();
    @Getter private static final Logger logger = Logger.getLogger("BlockGame");
    private final ClientWindow clientWindow;

    private LocalPlayer localPlayer;
    private Camera camera;
    private World world;

    public BlockGame() {
        instance = this;

        clientWindow = new ClientWindow();
        clientWindow.create();

        // Start the Menu
        ScreenManager.add(new MainMenu());

        this.loop(clientWindow);

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(clientWindow.getWindow());
        glfwDestroyWindow(clientWindow.getWindow());

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public void unpause() {
        ScreenManager.clear();
        BlockGame.getInstance().getConfig().setPaused(false);
        glfwSetInputMode(clientWindow.getWindow(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    public void pause() {
        ScreenManager.clear();
        ScreenManager.add(new PauseScreen());
        BlockGame.getInstance().getConfig().setPaused(true);
        glfwSetInputMode(clientWindow.getWindow(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }

    public void start() {
        this.unpause();
        this.localPlayer = new LocalPlayer();
        this.world = new World();
        this.camera = new Camera(0, 100, 0);
    }

    public void exit() {
        ScreenManager.clear();
        ScreenManager.add(new MainMenu());
        BlockGame.getInstance().getConfig().setPaused(true);

        ThreadUtil.shutdown();
        RenderManager.clear();

        this.world = null;
        this.camera = null;

        glfwSetInputMode(clientWindow.getWindow(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }

    public void close() {
        ThreadUtil.shutdown();
        glfwSetWindowShouldClose(BlockGame.getInstance().getClientWindow().getWindow(), true);
    }

    private void loop(ClientWindow clientWindow) {
        int fps = 0;
        long start = System.currentTimeMillis();

        // Set the clear color
        glClearColor(0.529f, 0.808f, 0.922f, 1.0f);

        // Run the rendering loop until the user has attempted to close
        double currentFrame = glfwGetTime();
        double lastFrame = currentFrame;
        double deltaTime;
        while ( !glfwWindowShouldClose(clientWindow.getWindow()) ) {
            currentFrame = glfwGetTime();
            deltaTime = currentFrame - lastFrame;
            lastFrame = currentFrame;

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
            clientWindow.poll();

            // Render all pending objects
            RenderManager.render();

            if(!BlockGame.getInstance().getConfig().isPaused()) {
                this.world.render();
                this.localPlayer.render(deltaTime);
            }

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
