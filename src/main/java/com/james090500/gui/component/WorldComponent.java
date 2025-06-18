package com.james090500.gui.component;

import com.james090500.utils.FontManager;
import com.james090500.utils.TextureManager;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVG.nvgFill;

public class WorldComponent extends Component {

    public WorldComponent(String text, float x, float y, float width, float height, Runnable onclick) {
        super(text, x, y, width, height, onclick);
    }

    @Override
    public void render(long vg, boolean hovered) {
        //Border
        NVGColor backgroundColor = NVGColor.calloc();
        nvgRGBA((byte) 0, (byte) 0, (byte) 0, (byte) 100, backgroundColor);

        if(this.isSelected()) {
            NVGColor borderColor = NVGColor.calloc();
            nvgRGBA((byte) 255, (byte) 255, (byte) 255, (byte) 255, borderColor);

            // Border
            nvgBeginPath(vg);
            nvgRect(vg, this.getX(), this.getY(), this.getWidth(), this.getHeight());
            nvgStrokeWidth(vg, 6.0f);
            nvgStrokeColor(vg, borderColor);
            nvgStroke(vg);

            borderColor.free();
        }

        // Background
        nvgBeginPath(vg);
        nvgRect(vg, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        nvgFillColor(vg, backgroundColor);
        nvgFill(vg);

        // Icon
        this.addIcon(vg);

        // Label
        FontManager.create().uiText(this.getText(), 20f, this.getX() + 100, this.getY() + 40);

        // Free
        backgroundColor.free();
    }

    private void addIcon(long vg) {
        NVGPaint paint = NVGPaint.calloc();
        nvgImagePattern(vg, this.getX() + 4, this.getY() + 4, 64, 64, 0f, TextureManager.pack, 1f, paint); // alpha = 1.0

        nvgBeginPath(vg);
        nvgRect(vg, this.getX() + 4, this.getY() + 4, 64, 64); // or custom width/height
        nvgFillPaint(vg, paint);
        nvgFill(vg);

        paint.free();
    }
}
