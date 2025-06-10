package com.james090500.gui.button;

import com.james090500.utils.FontManager;
import com.james090500.utils.TextureManager;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.nanovg.NVGPaint;

import static org.lwjgl.nanovg.NanoVG.*;

@Getter
public class Button {

    private final String text;
    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final Runnable onclick;

    @Setter
    private boolean enabled = true;

    public Button(String text, float x, float y, float width, float height, Runnable onclick) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.onclick = onclick;
    }

    public static Button create(String text, float x, float y, float width, float height, Runnable onclick) {
        return new Button(text, x, y, width, height, onclick);
    }

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
                    .uiText(text, 20f, x + (width / 2), y + height);
        } else {
            FontManager.create().color(0.5f, 0.5f, 0.5f, 1f)
                    .center()
                    .uiText(text, 20f, x + (width / 2), y + height);
        }
    }
}
