package com.james090500.gui.component;

import com.james090500.utils.FontManager;
import com.james090500.utils.TextureManager;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.nanovg.NVGPaint;

import static org.lwjgl.nanovg.NanoVG.*;

@Getter
public class Component {

    private final String text;
    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final ComponentClick onclick;

    @Setter
    private boolean enabled = true;

    @Setter
    private boolean selected = false;

    @Setter
    private String typedValue = "";
    @Setter
    private int maxValue = 0;

    public Component(String text, float x, float y, float width, float height, ComponentClick onclick) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.onclick = onclick;
    }

    /**
     * Handle typing
     * @param key The key ID
     */
    public void onType(int key) {
        if(key == -1 && !this.typedValue.isEmpty()) {
            this.typedValue = this.typedValue.substring(0, this.typedValue.length() - 1);
        } else if(this.maxValue == 0 || this.maxValue > 0 && this.typedValue.length() < this.maxValue) {
            this.typedValue += (char) key;
        }
    }

    /**
     * Render the component
     * @param vg nanoVG
     * @param hovered If hovered
     */
    public void render(long vg, boolean hovered) {
        NVGPaint paint = NVGPaint.calloc();

        int btnTexture;
        if(!enabled) {
            btnTexture = TextureManager.button_disabled;
        } else if(hovered) {
            btnTexture = TextureManager.button_active;
        } else {
            btnTexture = TextureManager.button;
        }
        nvgImagePattern(vg, x, y, width, height,0f, btnTexture,1f, paint);

        nvgBeginPath(vg);
        nvgRect(vg, x, y, width, height);
        nvgFillPaint(vg, paint);
        nvgFill(vg);

        paint.free();

        if(enabled) {
            FontManager.create().color(1f, 1f, 1f, 1f)
                    .center()
                    .text(text, 20f, x + (width / 2), y + height);
        } else {
            FontManager.create().color(0.5f, 0.5f, 0.5f, 1f)
                    .center()
                    .text(text, 20f, x + (width / 2), y + height);
        }
    }

    @FunctionalInterface
    public interface ComponentClick {
        void onClick(float mouseX, float mouseY);
    }

}
