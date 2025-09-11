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
                new LabelComponent(
                        response,
                        1f,
                        this.width / 2,
                        150f
                )
        );


        addComponent(
                new StandardButton(
                        "Close",
                        this.width / 2 - 150F,
                        this.height - 60F,
                        300f,
                        40f,
                        (mouseX, mouseY) -> {
                            ScreenManager.add(new MultiplayerScreen());
                            this.close();
                        }
                )
        );
    }
}
