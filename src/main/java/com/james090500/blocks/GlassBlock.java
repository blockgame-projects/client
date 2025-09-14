package com.james090500.blocks;

public class GlassBlock extends Block {
    public GlassBlock(byte id) {
        super(id);
        this.name = "Glass";
        this.sound = "stone";
        this.texture = 15;
        this.transparent = true;
    }
}
