package com.james090500.blocks;

public class WaterBlock extends Block {
    public WaterBlock(byte id) {
        super(id);
        this.name = "Water";
        this.texture = 205;
        this.transparent = true;
        this.solid = false;
    }
}
