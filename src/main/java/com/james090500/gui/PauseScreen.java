package com.james090500.gui;

import com.james090500.BlockGame;
import com.james090500.gui.component.Component;
import com.james090500.gui.options.OptionsScreen;

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
                (mouseX, mouseY) -> {
                    BlockGame.getInstance().unpause();
                    this.close();
                })
        );

        addComponent(new Component(
                "Options",
                this.width / 2 - 150f,
                150f,
                300f,
                40f,
                (mouseX, mouseY) -> {
                    ScreenManager.add(new OptionsScreen(true));
                    this.close();
                })
        );

        addComponent(new Component(
                "Exit to Menu",
                this.width / 2 - 150f,
                400f,
                300f,
                40f,
                (mouseX, mouseY) -> BlockGame.getInstance().exit())
        );
    }
}
