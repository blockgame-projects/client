package com.james090500.gui;

import com.james090500.BlockGame;
import com.james090500.utils.FontManager;
import com.james090500.utils.TextureManager;
import lombok.AccessLevel;
import lombok.Setter;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;

import java.util.ArrayList;

import static org.lwjgl.nanovg.NanoVG.*;

@Setter
public class Screen {

    protected final int width;
    protected final int height;

    protected long vg;
    private FontManager fontManager;

    private String title;

    @Setter(AccessLevel.NONE)
    private ArrayList<Button> buttons = new ArrayList<>();
    protected record Button(String text, float x, float y, float width, float height, Runnable onclick) {}

    public Screen() {
        this.width = BlockGame.getInstance().getClientWindow().getWidth();
        this.height = BlockGame.getInstance().getClientWindow().getHeight();

        this.vg = BlockGame.getInstance().getClientWindow().getVg();

        this.fontManager = new FontManager();
    }

    protected void addButton(Button button) {
        buttons.add(button);
    }

    public void click() {
        double mouseX = BlockGame.getInstance().getClientWindow().getMouseX();
        double mouseY = BlockGame.getInstance().getClientWindow().getMouseY();

        for(Button button : buttons) {
            boolean isHovered = mouseX >= button.x && mouseX <= button.x + button.width &&
                    mouseY >= button.y && mouseY <= button.y + button.height;

            if(isHovered) {
                button.onclick.run();
                break;
            }
        }
    }

    protected void renderButtons() {
        double mouseX = BlockGame.getInstance().getClientWindow().getMouseX();
        double mouseY = BlockGame.getInstance().getClientWindow().getMouseY();

        for(Button button : buttons) {
            boolean isHovered = mouseX >= button.x && mouseX <= button.x + button.width &&
                    mouseY >= button.y && mouseY <= button.y + button.height;

            NVGPaint paint = NVGPaint.calloc();
            nvgImagePattern(vg, button.x, button.y, button.width, button.height, 0f, isHovered ? TextureManager.button_active : TextureManager.button, 1f, paint);

            nvgBeginPath(vg);
            nvgRect(vg, button.x, button.y, button.width, button.height);
            nvgFillPaint(vg, paint);
            nvgFill(vg);

            paint.free();

            fontManager.color(1f, 1f, 1f, 1f)
                    .center()
                    .text(button.text, 20f, button.x + (button.width / 2), button.y + button.height);

        }
    }

    protected void renderBackground() {
        NVGPaint paint = NVGPaint.calloc();
        nvgImagePattern(vg, -32, -32, 64, 64, 0f, TextureManager.background, 1f, paint);

        nvgBeginPath(vg);
        nvgRect(vg, 0, 0, width, height);
        nvgFillPaint(vg, paint);
        nvgFill(vg);

        paint.free();
    }

    protected void renderOverlay() {
        try (NVGColor color = NVGColor.calloc()) {
            color.r(0f).g(0f).b(0f).a(0.5f); // black with 50% alpha
            nvgBeginPath(vg);
            nvgRect(vg, 0, 0, width, height);
            nvgFillColor(vg, color);
            nvgFill(vg);
        }
    }

    public void render() {
        renderButtons();
        fontManager.color(1f, 1f, 1f, 1f)
                .center()
                .text(title, 20f, width / 2f, 60f);
    }

    public void close() {
        ScreenManager.remove(this);
    }

}
