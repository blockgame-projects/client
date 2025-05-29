package com.james090500.gui;

import com.james090500.BlockGame;
import com.james090500.gui.button.Button;

public class PauseScreen extends Screen {

    public PauseScreen() {
        setTitle("Paused");
        setOverlay(true);

        addButton(new Button(
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

        addButton(new Button(
                "Exit to Menu",
                this.width / 2 - 150f,
                400f,
                300f,
                40f,
                () -> BlockGame.getInstance().exit())
        );
    }
}
