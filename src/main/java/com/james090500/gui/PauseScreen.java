package com.james090500.gui;

import com.james090500.BlockGame;
import com.james090500.gui.component.Component;

public class PauseScreen extends Screen {

    public PauseScreen() {
        setTitle("Paused");
        setOverlay(true);

        addComponent(new Component(
                "Resume Game",
                this.width / 2 - 150f,
                100f,
                300f,
                40f,
                () -> {
                    BlockGame.getInstance().unpause();
                    this.close();
                })
        );

        addComponent(new Component(
                "Exit to Menu",
                this.width / 2 - 150f,
                400f,
                300f,
                40f,
                () -> BlockGame.getInstance().exit())
        );
    }
}
