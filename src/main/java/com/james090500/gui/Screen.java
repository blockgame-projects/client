package com.james090500.gui;

import com.james090500.BlockGame;
import com.james090500.gui.component.Component;
import com.james090500.utils.FontManager;
import com.james090500.utils.SoundManager;
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
    private ArrayList<Component> components = new ArrayList<>();

    public Screen() {

        this.vg = BlockGame.getInstance().getClientWindow().getVg();
    }

    protected void addComponent(Component component) {
        components.add(component);
    }

    public void click() {
        for(Component component : components) {
            boolean hovered = isHovered(component);

            if(hovered && component.isEnabled()) {
                if(component.getOnclick() != null) {
                    SoundManager.play("assets/sound/gui/click");
                    component.getOnclick().onClick(getMouseComponentX(component), getMouseComponentY(component));
                }
                component.setSelected(true);
            } else {
                component.setSelected(false);
            }
        }
    }

    public void type(int key) {
        for(Component component : components) {
            if(component.isSelected()) {
                component.onType(key);
            }
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

    private float getUIMouseX() {
        double mouseX = BlockGame.getInstance().getClientWindow().getMouseX(); // logical (window)

        int windowWidth = BlockGame.getInstance().getClientWindow().getWindowWidth();      // logical
        int framebufferWidth = BlockGame.getInstance().getClientWindow().getFramebufferWidth(); // physical

        float baseWidth = BlockGame.getInstance().getClientWindow().getBaseWidth();   // e.g. 854

        // Convert mouse from logical (window) to physical (framebuffer)
        float dpiScaleX = (float) framebufferWidth / windowWidth;
        float fbMouseX = (float) (mouseX * dpiScaleX);

        // Apply inverse UI scaling
        float scale = (float) framebufferWidth / baseWidth;
        float offsetX = (framebufferWidth - baseWidth * scale) / 2f;

        return (fbMouseX - offsetX) / scale;
    }

    private float getUIMouseY() {
        double mouseY = BlockGame.getInstance().getClientWindow().getMouseY();

        int windowHeight = BlockGame.getInstance().getClientWindow().getWindowHeight();    // logical
        int framebufferHeight = BlockGame.getInstance().getClientWindow().getFramebufferHeight(); // physical

        float baseHeight = BlockGame.getInstance().getClientWindow().getBaseHeight(); // e.g. 480

        // Convert mouse from logical (window) to physical (framebuffer)
        float dpiScaleY = (float) framebufferHeight / windowHeight;
        float fbMouseY = (float) (mouseY * dpiScaleY);

        // Apply inverse UI scaling
        float scale = (float) framebufferHeight / baseHeight;
        float offsetY = (framebufferHeight - baseHeight * scale) / 2f;

        return (fbMouseY - offsetY) / scale;
    }

    private boolean isHovered(Component component) {
        float uiMouseX = getUIMouseX();
        float uiMouseY = getUIMouseY();

        return uiMouseX >= component.getX() && uiMouseX <= component.getX() + component.getWidth() &&
                uiMouseY >= component.getY() && uiMouseY <= component.getY() + component.getHeight();
    }

    private float getMouseComponentX(Component component) {
        float uiMouseX = getUIMouseX();
        return (uiMouseX - component.getX()) / component.getWidth();
    }

    private float getMouseComponentY(Component component) {
        float uiMouseY = getUIMouseY();
        return (uiMouseY - component.getY()) / component.getHeight();
    }

    public void render() {
        for(Component component : components) {
            boolean hovered = isHovered(component);
            component.render(vg, hovered);
        }

        FontManager.create().color(1f, 1f, 1f, 1f)
                .center()
                .text(title, 20f, width / 2f, 60f);
    }

    public void close() {
        ScreenManager.remove(this);
    }

}
