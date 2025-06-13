package com.james090500.gui;

import com.james090500.BlockGame;
import com.james090500.gui.component.StandardButton;
import com.james090500.gui.component.TextComponent;

public class WorldScreen extends Screen {
    public WorldScreen() {
        setCloseable(false);
        setBackground(true);
        setTitle("Singleplayer");

        TextComponent worldSeed = new TextComponent(
                "Seed",
                this.width / 2 - 200F,
                150f,
                400f,
                40f
        );
        addComponent(worldSeed);

        addComponent(
                StandardButton.create(
                        "Create World",
                        this.width / 2 - 200F,
                        200,
                        400f,
                        40f,
                        () -> {
                            BlockGame.getInstance().start(worldSeed.getTypedValue());
                            this.close();
                        }
                )
        );

        addComponent(
                StandardButton.create(
                    "Cancel",
                    this.width / 2 - 150F,
                    this.height - 60F,
                    300f,
                    40f,
                    () -> {
                        ScreenManager.add(new MainMenu());
                        this.close();
                    }
                )
        );
    }
}
