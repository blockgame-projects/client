package com.james090500.gui.multiplayer;

import com.james090500.gui.Screen;
import com.james090500.gui.ScreenManager;
import com.james090500.gui.component.LabelComponent;
import com.james090500.gui.component.StandardButton;

public class ConnectingScreen extends Screen {
    public ConnectingScreen(String serverIp) {
        setCloseable(false);
        setBackground(true);
        setTitle("Multiplayer");

        addComponent(
                new LabelComponent(
                        "Connecting to " + serverIp + "....",
                        1f,
                        this.width / 2,
                        150f
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
                            ScreenManager.add(new MultiplayerScreen());
                            this.close();
                        }
                )
        );
    }
}
