package com.james090500.gui.multiplayer;

import com.james090500.gui.Screen;
import com.james090500.gui.ScreenManager;
import com.james090500.gui.component.LabelComponent;
import com.james090500.gui.component.StandardButton;

public class ConnectionScreen extends Screen {
    public ConnectionScreen(String response) {
        setCloseable(false);
        setBackground(true);
        setTitle("Multiplayer");

        addComponent(
                LabelComponent.create(
                        response,
                        this.width / 2,
                        150f,
                        0,
                        0,
                        null
                )
        );


        addComponent(
                StandardButton.create(
                        "Close",
                        this.width / 2 - 150F,
                        this.height - 60F,
                        300f,
                        40f,
                        () -> {
                            ScreenManager.add(new MultiplayerScreen());
                            this.close();
                        }
                )
        );
    }
}
