package com.james090500.blocks;

import com.james090500.blocks.model.VegetationModel;
import com.james090500.textures.TextureLocation;

public class YellowFlowerBlock extends VegetationBlock {

    public YellowFlowerBlock(byte id) {
        super(id);
        this.name = "Yellow Flower";
        this.sound = "grass";
        this.texture = TextureLocation.get("assets/foliage/yellow_flower");
        this.transparent = true;
        this.solid = false;
        this.model = new VegetationModel(this.uv, this.texture);
    }
}
