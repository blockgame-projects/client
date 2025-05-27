package com.james090500.gui;

import com.james090500.BlockGame;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

public class PauseScreen extends Screen {

    public PauseScreen() {
        setTitle("Paused");

        addButton(new Button(
                "Resume Game",
                (float) BlockGame.getInstance().getClientWindow().getWidth() / 2 - 150f,
                100f,
                300f,
                40f,
                () -> BlockGame.getInstance().unpause())
        );

        addButton(new Button(
                "Exit Game",
                (float) BlockGame.getInstance().getClientWindow().getWidth() / 2 - 150f,
                400f,
                300f,
                40f,
                () -> BlockGame.getInstance().exit())
        );
    }
}
