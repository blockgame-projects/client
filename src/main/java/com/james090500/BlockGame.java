package com.james090500;

import com.james090500.client.Camera;
import com.james090500.client.ClientWindow;
import com.james090500.client.LocalPlayer;
import com.james090500.gui.DebugScreen;
import com.james090500.gui.MainMenu;
import com.james090500.gui.PauseScreen;
import com.james090500.gui.ScreenManager;
import com.james090500.io.AssetManager;
import com.james090500.renderer.RenderManager;
import com.james090500.utils.GameLogger;
import com.james090500.utils.SoundManager;
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
    @Getter private static final Logger logger = GameLogger.get("BlockGame");
    private final ClientWindow clientWindow;

    private LocalPlayer localPlayer;
    private Camera camera;
    private World world;

    public BlockGame() {
        instance = this;

        clientWindow = new ClientWindow();
        clientWindow.create();

        // Extract Assets
        AssetManager.extractAssets();

        // Start the Menu
        ScreenManager.add(new MainMenu());

        // Loop the game
        this.loop(clientWindow);

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(clientWindow.getWindow());
        glfwDestroyWindow(clientWindow.getWindow());

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    /**
     * Unpause the game
     * Doesn't remove any screens but locks the mouse
     */
    public void unpause() {
        BlockGame.getInstance().getConfig().setPaused(false);
        glfwSetInputMode(clientWindow.getWindow(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    /**
     * Pauses the game and adds the pause screen
     * Also unlocks the mouse
     */
    public void pause() {
        ScreenManager.clear();
        ScreenManager.add(new PauseScreen());
        BlockGame.getInstance().getConfig().setPaused(true);
        glfwSetInputMode(clientWindow.getWindow(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }

    /**
     * Starting a game so generates the player, world, camera etc
     */
    public void start(String name, String seed) {
        this.camera = new Camera(0, 150, 0);

        this.world = new World(name, seed);

        this.localPlayer = new LocalPlayer();
        this.localPlayer.loadGui();

        this.unpause();

        ScreenManager.add(new DebugScreen());
    }

    public void startRemote() {
        this.camera = new Camera(0, 150, 0);

        this.world = new World();

        this.localPlayer = new LocalPlayer();
        this.localPlayer.loadGui();

        this.unpause();

        ScreenManager.add(new DebugScreen());
    }

    /**
     * Exists a world so stops all pending tasks and opens the main menu
     */
    public void exit() {
        ScreenManager.active().clear();
        ScreenManager.add(new MainMenu());
        BlockGame.getInstance().getConfig().setPaused(true);

        ThreadUtil.shutdown();
        RenderManager.clear();

        BlockGame.getInstance().getLocalPlayer().savePlayer();
        BlockGame.getInstance().getWorld().exitWorld();

        this.localPlayer = null;
        this.world = null;
        this.camera = null;

        glfwSetInputMode(clientWindow.getWindow(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }

    /**
     * Closes the game so ends all pending tasks
     */
    public void close() {
        ThreadUtil.shutdown();
        SoundManager.destroy();
        glfwSetWindowShouldClose(BlockGame.getInstance().getClientWindow().getWindow(), true);
    }

    /**
     * Starts the game loop
     * @param clientWindow
     */
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

            //TODO Temp I think
            if(this.localPlayer != null) {
                this.localPlayer.render();
            }

            if(!BlockGame.getInstance().getConfig().isPaused()) {
                this.world.update();
                this.localPlayer.update(deltaTime);
            }

            //Run a single main thread queue
            ThreadUtil.runMainQueue();

            // Render UI
            ScreenManager.render();

            // Swap the buffers
            glfwSwapBuffers(clientWindow.getWindow());

            // FPS Calculator
            long now = System.currentTimeMillis();
            if (now - start >= 1000) {
                BlockGame.getInstance().getConfig().setFPS(fps);
                start = now;
                fps = 0;
            } else {
                fps++;
            }
        }
    }

}
