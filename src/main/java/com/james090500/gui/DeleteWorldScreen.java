package com.james090500.gui;

import com.james090500.gui.component.LabelComponent;
import com.james090500.gui.component.StandardButton;
import com.james090500.io.WorldManager;

public class DeleteWorldScreen extends Screen {
    public DeleteWorldScreen(String worldName) {
        setCloseable(false);
        setBackground(true);
        setTitle("Delete World?");

        addComponent(
                LabelComponent.create(
                    "Are you sure you want to delete \"" + worldName + "\"\n",
                    this.width / 2,
                    150f,
                    0,
                    0,
                    null
                )
        );
        addComponent(
                LabelComponent.create(
                        "This action is permanent!",
                        this.width / 2,
                        180f,
                        0,
                        0,
                        null
                )
        );

        addComponent(
                StandardButton.create(
                        "Cancel",
                        this.width / 6 - 5,
                        260F,
                        300f,
                        40f,
                        () -> {
                            ScreenManager.add(new WorldScreen());
                            this.close();
                        }
                )
        );

        addComponent(
                StandardButton.create(
                        "Delete",
                        this.width / 6 + 305F,
                        260F,
                        300f,
                        40f,
                        () -> {
                            WorldManager.deleteWorld(worldName);
                            ScreenManager.add(new WorldScreen());
                            this.close();
                        }
                )
        );
    }
}
