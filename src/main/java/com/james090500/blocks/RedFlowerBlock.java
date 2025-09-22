package com.james090500.blocks;

import com.james090500.blocks.model.VegetationModel;
import com.james090500.textures.TextureLocation;

public class RedFlowerBlock extends VegetationBlock {

    public RedFlowerBlock(byte id) {
        super(id);
        this.name = "Red Flower";
        this.sound = "grass";
        this.texture = TextureLocation.get("assets/foliage/red_flower");
        this.transparent = true;
        this.solid = false;
        this.model = new VegetationModel(this.uv, this.texture);
    }
}
