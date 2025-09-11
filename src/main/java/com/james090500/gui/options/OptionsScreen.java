package com.james090500.gui.options;

import com.james090500.BlockGame;
import com.james090500.gui.MainMenu;
import com.james090500.gui.PauseScreen;
import com.james090500.gui.Screen;
import com.james090500.gui.ScreenManager;
import com.james090500.gui.component.SliderComponent;
import com.james090500.gui.component.StandardButton;

public class OptionsScreen extends Screen {

    public OptionsScreen(boolean inGame) {
        setCloseable(false);
        setBackground(!inGame);
        setOverlay(inGame);
        setTitle("Options");

        addComponent(
                new SliderComponent(
                        "Render Distance",
                        this.width / 4 - 80F,
                        100F,
                        300f,
                        40f,
                        BlockGame.getInstance().getConfig().getUserOptions().getRenderDistance()
                )
        );

//        addComponent(
//                new SliderComponent(
//                        "Option 2",
//                        this.width / 4 + 230,
//                        100F,
//                        300f,
//                        40f,
//                        BlockGame.getInstance().getConfig().getUserOptions().getRenderDistance()
//                )
//        );

        addComponent(
                new StandardButton(
                        "Close",
                        this.width / 2 - 150f,
                        400f,
                        300f,
                        40f,
                        (mouseX, mouseY) -> {
                            if(inGame) {
                                ScreenManager.add(new PauseScreen());
                                this.close();
                            } else {
                                ScreenManager.add(new MainMenu());
                                this.close();
                            }
                        }
                )
        );
    }

    @Override
    public void render() {
        super.render();
    }
}
