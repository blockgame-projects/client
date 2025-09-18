package com.james090500.blocks;

import com.james090500.blocks.model.VegetationModel;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.joml.Vector3i;

public class YellowFlowerBlock extends VegetationBlock implements IBlockRender {

    public YellowFlowerBlock(byte id) {
        super(id);
        this.name = "Yellow Flower";
        this.sound = "grass";
        this.texture = 23;
        this.transparent = true;
        this.solid = false;
        this.model = new VegetationModel(this.getTexture());
    }

    @Override
    public void render(ObjectList<Vector3i> position) {
        this.model.render(position);
    }
}
