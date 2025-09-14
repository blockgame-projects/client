package com.james090500.blocks;

public class SpruceLeafBlock extends Block {
    public SpruceLeafBlock(byte id) {
        super(id);
        this.name = "Leaf";
        this.sound = "grass";
        this.texture = 7;
        this.transparent = true;
    }
}
