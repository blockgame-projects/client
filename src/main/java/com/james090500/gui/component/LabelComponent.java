package com.james090500.gui.component;

import com.james090500.utils.FontManager;

public class LabelComponent extends Component {

    float size;

    public LabelComponent(String text, float size, float x, float y) {
        super(text, x, y, 0, 0, null);
        this.size = size;
    }

    @Override
    public void render(long vg, boolean hovered) {
        // Label
        FontManager.create().text(this.getText(), size, this.getX(), this.getY());

        super.render(vg, hovered);
    }
}
