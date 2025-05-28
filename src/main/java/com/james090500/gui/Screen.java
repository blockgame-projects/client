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

    protected int width;
    protected int height;

    protected long vg;
    private FontManager fontManager;

    private String title;

    @Setter(AccessLevel.NONE)
    private ArrayList<Button> buttons = new ArrayList<>();
    protected record Button(String text, float x, float y, float width, float height, Runnable onclick) {}

    public Screen() {
        this.width = BlockGame.getInstance().getClientWindow().getFramebufferWidth();
        this.height = BlockGame.getInstance().getClientWindow().getFramebufferHeight();

        this.vg = BlockGame.getInstance().getClientWindow().getVg();

        this.fontManager = new FontManager();
    }

    protected void addButton(Button button) {
        buttons.add(button);
    }

    public void click() {
        for(Button button : buttons) {
            boolean hovered = isHovered(button.x, button.y, button.width, button.height);

            if(hovered) {
                button.onclick.run();
                break;
            }
        }
    }

    protected void renderButtons() {
        for(Button button : buttons) {
            boolean hovered = isHovered(button.x, button.y, button.width, button.height);

            NVGPaint paint = NVGPaint.calloc();
            nvgImagePattern(vg, button.x, button.y, button.width, button.height, 0f, hovered ? TextureManager.button_active : TextureManager.button, 1f, paint);

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

    private boolean isHovered(float x, float y, float width, float height) {
        // Get logical mouse position (e.g., scaled down from framebuffer size)
        double mouseX = BlockGame.getInstance().getClientWindow().getMouseX();
        double mouseY = BlockGame.getInstance().getClientWindow().getMouseY();

        // Get window and framebuffer sizes
        int windowWidth = BlockGame.getInstance().getClientWindow().getWindowWidth();
        int windowHeight = BlockGame.getInstance().getClientWindow().getWindowHeight();
        int framebufferWidth = BlockGame.getInstance().getClientWindow().getFramebufferWidth();
        int framebufferHeight = BlockGame.getInstance().getClientWindow().getFramebufferHeight();

        // Calculate scale factor (for high-DPI)
        float scaleX = (float) framebufferWidth / windowWidth;
        float scaleY = (float) framebufferHeight / windowHeight;

        // Scale mouse coordinates back to logical coordinates
        float scaledMouseX = (float) mouseX / scaleX;
        float scaledMouseY = (float) mouseY / scaleY;

        // Flip Y because OpenGL's origin is bottom-left
        float flippedMouseY = windowHeight - scaledMouseY;

        return scaledMouseX >= x && scaledMouseX <= x + width &&
                flippedMouseY >= y && flippedMouseY <= y + height;
    }

    public void render() {
        renderButtons();
        fontManager.color(1f, 1f, 1f, 1f)
                .center()
                .text(title, 20f, width / 2f, 60f);
    }

    public void resize() {
        float oldWidth = this.width;
        float oldHeight = this.height;
        this.width = BlockGame.getInstance().getClientWindow().getFramebufferWidth();
        this.height = BlockGame.getInstance().getClientWindow().getFramebufferHeight();

        float scaleX = (this.width / oldWidth);
        float scaleY = (this.height / oldHeight);

        ArrayList<Button> newButtons = new ArrayList<>();
        for(Button button : this.buttons) {
            System.out.println(scaleX);
            newButtons.add(new Button(
                    button.text,
                    button.x * scaleX,
                    button.y * scaleY,
                    button.width * scaleX,
                    button.height * scaleY,
                    button.onclick
            ));
        }
        this.buttons = newButtons;
    }

    public void close() {
        ScreenManager.remove(this);
    }

}
