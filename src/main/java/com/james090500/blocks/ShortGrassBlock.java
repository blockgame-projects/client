package com.james090500.blocks;

import com.james090500.blocks.model.VegetationModel;
import com.james090500.textures.TextureLocation;

public class ShortGrassBlock extends VegetationBlock {

    public ShortGrassBlock(byte id) {
        super(id);
        this.name = "Short Grass";
        this.sound = "grass";
        this.texture = TextureLocation.get("assets/foliage/short_grass");
        this.transparent = true;
        this.solid = false;
        this.model = new VegetationModel(this.uv, this.texture);
    }
}
