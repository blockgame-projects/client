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
import com.james090500.textures.TextureManager;
import com.james090500.utils.GameLogger;
import com.james090500.utils.SoundManager;
import com.james090500.utils.ThreadUtil;
import com.james090500.world.World;
import io.netty.channel.Channel;
import lombok.Getter;

import java.util.logging.Logger;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

@Getter
public class BlockGame {

    @Getter private static BlockGame instance;
    @Getter private TextureManager textureManager;
    private final Config config = new Config();
    @Getter private static final Logger logger = GameLogger.get("BlockGame");
    private final ClientWindow clientWindow;

    private LocalPlayer localPlayer;
    private Camera camera;
    private World world;

    private Channel channel;

    public BlockGame() {
        instance = this;

        //spark = new Spark("start open");

        clientWindow = new ClientWindow();
        clientWindow.create();

        // Extract Assets
        AssetManager.extractAssets();
        this.textureManager = new TextureManager();

        // Start the Menu
        ScreenManager.add(new MainMenu());

        // Loop the game
        this.loop(clientWindow);

        //
        // Game Closes
        //
        this.exit();
        ThreadUtil.shutdown();
        SoundManager.destroy();

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

    public void startRemote(Channel channel) {
        this.channel = channel;

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

        if(BlockGame.getInstance().getLocalPlayer() != null) {
            BlockGame.getInstance().getLocalPlayer().savePlayer();
        }

        if(BlockGame.getInstance().getWorld() != null) {
            BlockGame.getInstance().getWorld().exitWorld();
        }

        this.localPlayer = null;
        this.world = null;
        this.camera = null;

        //spark.disable();

        glfwSetInputMode(clientWindow.getWindow(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }

    /**
     * Closes the game so ends all pending tasks
     */
    public void close() {
        glfwSetWindowShouldClose(BlockGame.getInstance().getClientWindow().getWindow(), true);
    }

    /**
     * Starts the game loop
     * @param clientWindow
     */
    private void loop(ClientWindow clientWindow) {
        final double TICKS_PER_SECOND = 20.0;
        final double NS_PER_TICK = 1_000_000_000.0 / TICKS_PER_SECOND; // 50_000_000 ns
        final float FIXED_DELTA_SECONDS = 1.0f / (float)TICKS_PER_SECOND; // 0.05 seconds

        int fps = 0;
        int tps = 0;
        long fpsTimer = System.currentTimeMillis();

        long lastTime = System.nanoTime();
        double accumulator = 0.0;

        double currentFrame = glfwGetTime();
        double lastFrame = currentFrame;
        double deltaTime;

        // Set the clear color once
        glClearColor(0.529f, 0.808f, 0.922f, 1.0f);

        while (!glfwWindowShouldClose(clientWindow.getWindow())) {
            currentFrame = glfwGetTime();
            deltaTime = currentFrame - lastFrame;
            lastFrame = currentFrame;


            long now = System.nanoTime();
            long elapsedNanos = now - lastTime;
            lastTime = now;
            accumulator += (double) elapsedNanos;

            // Process input/events every frame
            glfwPollEvents();          // make sure to poll events each loop

            // Run fixed ticks while we've accumulated one-or-more tick durations
            while (accumulator >= NS_PER_TICK) {
                // --- TICK: update game logic at fixed rate ---
                if (!BlockGame.getInstance().getConfig().isPaused()) {
                    world.update(); // game logic should use FIXED_DELTA_SECONDS where appropriate
                }

                accumulator -= NS_PER_TICK;
                tps++;
            }

            if(!BlockGame.getInstance().getConfig().isPaused()) {
                if (this.localPlayer != null) {
                    this.localPlayer.update(deltaTime);
                }
            }

            ThreadUtil.runMainQueue(); // run main-thread queued tasks on tick

            // Prepare GL state and render
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glEnable(GL_DEPTH_TEST);
            glEnable(GL_CULL_FACE);

            // Inputs, render managers and UI (per-frame)
            clientWindow.poll(); // your code — keep per-frame input/polling
            RenderManager.render();
            if (this.localPlayer != null) this.localPlayer.render(); // rendering uses alpha if desired

            ScreenManager.render();

            glfwSwapBuffers(clientWindow.getWindow());
            fps++;

            // FPS / TPS reporting (once per second)
            if (System.currentTimeMillis() - fpsTimer >= 1000) {
                BlockGame.getInstance().getConfig().setFPS(fps);
                BlockGame.getInstance().getConfig().setTicks(tps);
                fps = 0;
                tps = 0;
                fpsTimer += 1000;
            }
        }
    }

}
