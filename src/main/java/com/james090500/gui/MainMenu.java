package com.james090500.gui;

import com.james090500.BlockGame;
import com.james090500.gui.component.StandardButton;
import com.james090500.gui.multiplayer.MultiplayerScreen;
import com.james090500.gui.options.OptionsScreen;
import com.james090500.gui.singleplayer.WorldScreen;
import com.james090500.utils.TextureManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.nanovg.NVGPaint;

import java.nio.IntBuffer;

import static org.lwjgl.nanovg.NanoVG.*;

public class MainMenu extends Screen {
    public MainMenu() {
        setCloseable(false);
        setBackground(true);

        addComponent(
            new StandardButton(
                "Singleplayer",
                this.width / 2 - 200F,
                150f,
                400f,
                40f,
                (mouseX, mouseY) -> {
                    ScreenManager.add(new WorldScreen());
                    this.close();
                }
            )
        );

        addComponent(
            new StandardButton(
                    "Multiplayer",
                    this.width / 2 - 200F,
                    200f,
                    400f,
                    40f,
                    (mouseX, mouseY) -> {
                        ScreenManager.add(new MultiplayerScreen());
                        this.close();
                    }
            )
        );

        addComponent(
                new StandardButton(
                        "Options",
                        this.width / 2 - 200F,
                        250f,
                        400f,
                        40f,
                        (mouseX, mouseY) -> {
                            ScreenManager.add(new OptionsScreen(false));
                            this.close();
                        }
                )
        );

        addComponent(
                new StandardButton(
                    "Quit Game",
                    this.width / 2 - 150F,
                    this.height - 60F,
                    300f,
                    40f,
                    (mouseX, mouseY) -> BlockGame.getInstance().close()
                )
        );
    }

    @Override
    public void render() {
        addLogo();
        super.render();
    }

    private void addLogo() {
        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        nvgImageSize(this.vg, TextureManager.logo, w, h);
        int imgWidth = w.get(0);
        int imgHeight = h.get(0);

        NVGPaint paint = NVGPaint.calloc();
        nvgImagePattern(this.vg, this.width / 2 - (float) imgWidth / 2, 0f, imgWidth, imgHeight, 0f, TextureManager.logo, 1f, paint); // alpha = 1.0

        nvgBeginPath(this.vg);
        nvgRect(this.vg, this.width / 2 - (float) imgWidth / 2, 0f, imgWidth, imgHeight); // or custom width/height
        nvgFillPaint(this.vg, paint);
        nvgFill(this.vg);

        paint.free();
    }
}
