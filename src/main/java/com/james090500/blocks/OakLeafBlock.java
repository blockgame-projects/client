package com.james090500.blocks;

public class OakLeafBlock extends Block {
    public OakLeafBlock(byte id) {
        super(id);
        this.name = "Leaf";
        this.sound = "grass";
        this.texture = 6;
        this.transparent = true;
    }
}
