package com.james090500.blocks;

public class LeafBlock extends Block {
    public LeafBlock(byte id) {
        super(id);
        this.name = "Leaf";
        this.sound = "grass";
        this.texture = 52;
        this.transparent = true;
    }
}
