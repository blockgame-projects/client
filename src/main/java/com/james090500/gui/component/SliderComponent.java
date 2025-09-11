package com.james090500.gui.component;

import com.james090500.BlockGame;
import com.james090500.Config;
import com.james090500.utils.FontManager;
import org.lwjgl.nanovg.NVGColor;

import static org.lwjgl.nanovg.NanoVG.*;

public class SliderComponent extends Component {

    private Config.SliderOption userOptions;

    public SliderComponent(String text, float x, float y, float width, float height, Config.SliderOption userOptions) {
        super(text, x, y, width, height, ((mouseX, mouseY) -> {
           int range = userOptions.getMax();
           int result = Math.round(mouseX * range);

            userOptions.setValue(userOptions.getMin() + result);

            // Update world
            if(BlockGame.getInstance().getWorld() != null) {
                BlockGame.getInstance().getWorld().setForceUpdate(true);
            }
        }));

        this.userOptions = userOptions;
    }

    @Override
    public void render(long vg, boolean hovered) {
        int renderDistance = BlockGame.getInstance().getConfig().getUserOptions().getRenderDistance().getValue();

        // Text
        NVGColor color = NVGColor.calloc();
        nvgRGBA((byte) 0, (byte) 0, (byte) 0, (byte) 255, color);

        // Border & Selector
        NVGColor borderColor = NVGColor.calloc();
        NVGColor selectorColor = NVGColor.calloc();
        if(!this.isSelected()) {
            nvgRGBA((byte) 92, (byte) 88, (byte) 95, (byte) 255, borderColor);
            nvgRGBA((byte) 92, (byte) 88, (byte) 95, (byte) 255, selectorColor);
        } else {
            nvgRGBA((byte) 255, (byte) 255, (byte) 255, (byte) 255, borderColor);
            nvgRGBA((byte) 70, (byte) 184, (byte) 14, (byte) 255, selectorColor);
        }

        // Border
        nvgBeginPath(vg);
        nvgRect(vg, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        nvgFillColor(vg, borderColor);
        nvgFill(vg);

        // Background
        nvgBeginPath(vg);
        nvgRect(vg, this.getX() + 2, this.getY() + 2, this.getWidth() - 4, this.getHeight() - 4);
        nvgFillColor(vg, color);
        nvgFill(vg);

        // Slider
        float sliderWidth = this.getWidth() / (this.userOptions.getMax());
        float sliderX = (sliderWidth * (this.userOptions.getValue())) - sliderWidth;
        nvgBeginPath(vg);
        nvgRect(vg, this.getX() + sliderX, this.getY(), sliderWidth, this.getHeight());
        nvgFillColor(vg, selectorColor);
        nvgFill(vg);


        color.free();
        borderColor.free();
        selectorColor.free();

        FontManager.create().color(1f, 1f, 1f, 1f)
                .center()
                .text("Render Distance: " + renderDistance, 20f, this.getX() + (this.getWidth() / 2), this.getY() + this.getHeight());
    }
}
