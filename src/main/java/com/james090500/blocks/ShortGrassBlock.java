package com.james090500.blocks;

import com.james090500.blocks.model.VegetationModel;
import org.joml.Vector3i;

public class ShortGrassBlock extends VegetationBlock implements IBlockRender {

    public ShortGrassBlock(byte id) {
        super(id);
        this.name = "Short Grass";
        this.sound = "grass";
        this.texture = 2;
        this.transparent = true;
        this.solid = false;
        this.model = new VegetationModel(this.getTexture());
    }

    @Override
    public void render(Vector3i position) {
        this.model.render(position);
    }
}
