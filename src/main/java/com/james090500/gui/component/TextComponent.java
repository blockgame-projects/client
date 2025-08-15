package com.james090500.gui.component;

import com.james090500.utils.FontManager;
import org.lwjgl.nanovg.NVGColor;

import static org.lwjgl.nanovg.NanoVG.*;

public class TextComponent extends Component {

    public TextComponent(String text, float x, float y, float width, float height) {
        super(text, x, y, width, height, null);
    }

    @Override
    public void render(long vg, boolean hovered) {
        // Label
        FontManager.create().uiText(this.getText(), 20f, this.getX(), this.getY());

        // Text
        NVGColor color = NVGColor.calloc();
        nvgRGBA((byte) 0, (byte) 0, (byte) 0, (byte) 255, color);

        // Border
        NVGColor borderColor = NVGColor.calloc();
        if(!this.isSelected()) {
            nvgRGBA((byte) 92, (byte) 88, (byte) 95, (byte) 255, borderColor);
        } else {
            nvgRGBA((byte) 255, (byte) 255, (byte) 255, (byte) 255, borderColor);
        }

        // Border
        nvgBeginPath(vg);
        nvgRect(vg, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        nvgFillColor(vg, borderColor);
        nvgFill(vg);

        // Text
        nvgBeginPath(vg);
        nvgRect(vg, this.getX() + 2, this.getY() + 2, this.getWidth() - 4, this.getHeight() - 4);
        nvgFillColor(vg, color);
        nvgFill(vg);

        color.free();
        borderColor.free();

        FontManager.create().color(1f, 1f, 1f, 1f)
                .center()
                .uiText(this.getTypedValue(), 20f, this.getX() + (this.getWidth() / 2), this.getY() + this.getHeight());
    }
}
