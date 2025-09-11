package com.james090500.gui.multiplayer;

import com.james090500.gui.MainMenu;
import com.james090500.gui.Screen;
import com.james090500.gui.ScreenManager;
import com.james090500.gui.component.StandardButton;
import com.james090500.gui.component.TextComponent;
import com.james090500.network.NettyHandler;

public class MultiplayerScreen extends Screen {
    public MultiplayerScreen() {
        setCloseable(false);
        setBackground(true);
        setTitle("Multiplayer");

        TextComponent serverIp = new TextComponent(
                "Server IP",
                this.width / 2 - 200F,
                100f,
                400f,
                40f
        );
        addComponent(serverIp);

        addComponent(
                new StandardButton(
                        "Connect",
                        this.width / 2 - 200F,
                        260,
                        400f,
                        40f,
                        (mouseX, mouseY) -> {
                            NettyHandler test = new NettyHandler(serverIp.getTypedValue(), 28004);
                            test.run();
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
                        ScreenManager.add(new MainMenu());
                        this.close();
                    }
                )
        );
    }
}
