package com.james090500.gui;

import com.james090500.BlockGame;
import com.james090500.gui.button.Button;
import com.james090500.utils.FontManager;
import com.james090500.utils.TextureManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;

import java.util.ArrayList;

import static org.lwjgl.nanovg.NanoVG.*;

@Setter
public class Screen {

    protected float width = BlockGame.getInstance().getClientWindow().getBaseWidth();
    protected float height = BlockGame.getInstance().getClientWindow().getBaseHeight();

    protected long vg;

    @Getter
    private boolean closeable = true;
    @Getter
    private boolean inGame = false;

    private String title;
    @Getter
    private boolean overlay;
    @Getter
    private boolean background;

    @Setter(AccessLevel.NONE)
    private ArrayList<Button> buttons = new ArrayList<>();

    public Screen() {

        this.vg = BlockGame.getInstance().getClientWindow().getVg();
    }

    protected void addButton(Button button) {
        buttons.add(button);
    }

    public void click() {
        for(Button button : buttons) {
            boolean hovered = isHovered(button.getX(), button.getY(), button.getWidth(), button.getHeight());

            if(hovered) {
                button.getOnclick().run();
                break;
            }
        }
    }

    protected void renderButtons() {
        for(Button button : buttons) {
            boolean hovered = isHovered(button.getX(), button.getY(), button.getWidth(), button.getHeight());
            button.render(vg, hovered);
        }
    }

    protected void renderBackground() {
        float scale = (float) BlockGame.getInstance().getClientWindow().getFramebufferWidth() / BlockGame.getInstance().getClientWindow().getWindowWidth();

        NVGPaint paint = NVGPaint.calloc();
        nvgImagePattern(vg, -32 * scale, -32 * scale, 64 * scale, 64 * scale, 0f, TextureManager.background, 1f, paint);

        nvgBeginPath(vg);
        nvgRect(vg, 0, 0, BlockGame.getInstance().getClientWindow().getFramebufferWidth(), BlockGame.getInstance().getClientWindow().getFramebufferHeight());
        nvgFillPaint(vg, paint);
        nvgFill(vg);

        paint.free();
    }

    protected void renderOverlay() {
        try (NVGColor color = NVGColor.calloc()) {
            color.r(0f).g(0f).b(0f).a(0.5f); // black with 50% alpha
            nvgBeginPath(vg);
            nvgRect(vg, 0, 0, BlockGame.getInstance().getClientWindow().getFramebufferWidth(), BlockGame.getInstance().getClientWindow().getFramebufferHeight());
            nvgFillColor(vg, color);
            nvgFill(vg);
        }
    }

    private boolean isHovered(float x, float y, float width, float height) {
        double mouseX = BlockGame.getInstance().getClientWindow().getMouseX(); // logical (window)
        double mouseY = BlockGame.getInstance().getClientWindow().getMouseY();

        int windowWidth = BlockGame.getInstance().getClientWindow().getWindowWidth();      // logical
        int windowHeight = BlockGame.getInstance().getClientWindow().getWindowHeight();    // logical
        int framebufferWidth = BlockGame.getInstance().getClientWindow().getFramebufferWidth(); // physical
        int framebufferHeight = BlockGame.getInstance().getClientWindow().getFramebufferHeight(); // physical

        float baseWidth = BlockGame.getInstance().getClientWindow().getBaseWidth();   // e.g. 854
        float baseHeight = BlockGame.getInstance().getClientWindow().getBaseHeight(); // e.g. 480

        // Convert mouse from logical (window) to physical (framebuffer)
        float dpiScaleX = (float) framebufferWidth / windowWidth;
        float dpiScaleY = (float) framebufferHeight / windowHeight;
        float fbMouseX = (float) (mouseX * dpiScaleX);
        float fbMouseY = (float) (mouseY * dpiScaleY);

        // Apply inverse UI scaling
        float scale = Math.min((float) framebufferWidth / baseWidth, (float) framebufferHeight / baseHeight);
        float offsetX = (framebufferWidth - baseWidth * scale) / 2f;
        float offsetY = (framebufferHeight - baseHeight * scale) / 2f;

        float uiMouseX = (fbMouseX - offsetX) / scale;
        float uiMouseY = (fbMouseY - offsetY) / scale;

        return uiMouseX >= x && uiMouseX <= x + width &&
                uiMouseY >= y && uiMouseY <= y + height;
    }

    public void render() {
        renderButtons();
        FontManager.create().color(1f, 1f, 1f, 1f)
                .center()
                .text(title, 20f, width / 2f, 60f);
    }

    public void close() {
        ScreenManager.remove(this);
    }

}
