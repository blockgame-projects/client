package com.james090500.gui.singleplayer;

import com.james090500.BlockGame;
import com.james090500.gui.MainMenu;
import com.james090500.gui.Screen;
import com.james090500.gui.ScreenManager;
import com.james090500.gui.component.Component;
import com.james090500.gui.component.StandardButton;
import com.james090500.gui.component.WorldComponent;
import com.james090500.io.WorldManager;

import java.util.ArrayList;
import java.util.List;

public class WorldScreen extends Screen {

    private String selectedWorld;
    private List<String> worlds;

    private Component playWorld;
    private Component deleteWorld;
    private List<Component> worldComponents = new ArrayList<>();

    public WorldScreen() {
        setCloseable(false);
        setBackground(true);
        setTitle("Singleplayer");

        worlds = WorldManager.getWorlds();

        init();
    }

    private void init() {
        // List the worlds
        int i = 1;
        for(String world : worlds) {
            Component worldComponent = new WorldComponent(
                    world,
                    this.width / 2 - 305,
                    75 * i,
                    610,
                    72,
                    (mouseX, mouseY) -> selectedWorld = world
            );
            worldComponents.add(worldComponent);
            addComponent(worldComponent);
            i++;
        }

        playWorld = new StandardButton(
                "Play World",
                this.width / 4 - 80F,
                this.height - 110F,
                300f,
                40f,
                (mouseX, mouseY) -> {
                    BlockGame.getInstance().start(selectedWorld, null);
                    this.close();
                }
        );
        playWorld.setEnabled(selectedWorld != null);
        addComponent(playWorld);

        addComponent(
                new StandardButton(
                        "New World",
                        this.width / 4 + 230,
                        this.height - 110F,
                        300f,
                        40f,
                        (mouseX, mouseY) -> {
                            ScreenManager.add(new NewWorldScreen());
                            this.close();
                        }
                )
        );

        deleteWorld = new StandardButton(
                "Delete World",
                this.width / 4 - 80F,
                this.height - 60,
                300f,
                40f,
                (mouseX, mouseY) -> {
                    ScreenManager.add(new DeleteWorldScreen(selectedWorld));
                    this.close();
                }
        );
        deleteWorld.setEnabled(false);
        addComponent(deleteWorld);

        addComponent(
                new StandardButton(
                        "Cancel",
                        this.width / 4 + 230,
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

    @Override
    public void render() {
        boolean anySelected = worldComponents.stream().anyMatch(Component::isSelected);
        playWorld.setEnabled(anySelected);
        deleteWorld.setEnabled(anySelected);

        super.render();
    }
}
