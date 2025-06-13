package com.james090500.gui.component;

import com.james090500.utils.FontManager;
import org.lwjgl.nanovg.NVGColor;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVG.nvgFill;

public class TextComponent extends Component {

    public TextComponent(String text, float x, float y, float width, float height) {
        super(text, x, y, width, height, null);
    }

    @Override
    public void render(long vg, boolean hovered) {
        NVGColor color = NVGColor.calloc(); // or .malloc() if you're managing memory manually
        nvgRGBA((byte) 0, (byte) 0, (byte) 0, (byte) 255, color);

        nvgBeginPath(vg);
        nvgRect(vg, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        nvgFillColor(vg, color);
        nvgFill(vg);

        color.free();

        FontManager.create().color(1f, 1f, 1f, 1f)
                .center()
                .uiText(this.getTypedValue(), 20f, this.getX() + (this.getWidth() / 2), this.getY() + this.getHeight());
    }
}
