package com.james090500.gui.singleplayer;

import com.james090500.gui.Screen;
import com.james090500.gui.ScreenManager;
import com.james090500.gui.component.LabelComponent;
import com.james090500.gui.component.StandardButton;
import com.james090500.io.WorldManager;

public class DeleteWorldScreen extends Screen {
    public DeleteWorldScreen(String worldName) {
        setCloseable(false);
        setBackground(true);
        setTitle("Delete World?");

        addComponent(
                new LabelComponent(
                    "Are you sure you want to delete \"" + worldName + "\"\n",
                    1f,
                    this.width / 2,
                    150f
                )
        );
        addComponent(
                new LabelComponent(
                        "This action is permanent!",
                        1f,
                        this.width / 2,
                        180f
                )
        );

        addComponent(
                new StandardButton(
                        "Cancel",
                        this.width / 6 - 5,
                        260F,
                        300f,
                        40f,
                        (mouseX, mouseY) -> {
                            ScreenManager.add(new WorldScreen());
                            this.close();
                        }
                )
        );

        addComponent(
                new StandardButton(
                        "Delete",
                        this.width / 6 + 305F,
                        260F,
                        300f,
                        40f,
                        (mouseX, mouseY) -> {
                            WorldManager.deleteWorld(worldName);
                            ScreenManager.add(new WorldScreen());
                            this.close();
                        }
                )
        );
    }
}
