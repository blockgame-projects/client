package com.james090500.gui.singleplayer;

import com.james090500.BlockGame;
import com.james090500.gui.Screen;
import com.james090500.gui.ScreenManager;
import com.james090500.gui.component.StandardButton;
import com.james090500.gui.component.TextComponent;

public class NewWorldScreen extends Screen {
    public NewWorldScreen() {
        setCloseable(false);
        setBackground(true);
        setTitle("New World");

        TextComponent worldName = new TextComponent(
                "Name",
                this.width / 2 - 200F,
                100f,
                400f,
                40f
        );
        addComponent(worldName);

        TextComponent worldSeed = new TextComponent(
                "Seed",
                this.width / 2 - 200F,
                200f,
                400f,
                40f
        );
        addComponent(worldSeed);

        addComponent(
                new StandardButton(
                        "Create World",
                        this.width / 2 - 200F,
                        260,
                        400f,
                        40f,
                        (mouseX, mouseY) -> {
                            BlockGame.getInstance().start(worldName.getTypedValue(), worldSeed.getTypedValue());
                            this.close();
                        }
                )
        );

        addComponent(
                new StandardButton(
                    "Cancel",
                    this.width / 2 - 150F,
                    this.height - 60F,
                    300f,
                    40f,
                    (mouseX, mouseY) -> {
                        ScreenManager.add(new WorldScreen());
                        this.close();
                    }
                )
        );
    }
}
