package com.james090500.blocks;

import com.james090500.blocks.model.VegetationModel;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.joml.Vector3i;

public class RedFlowerBlock extends VegetationBlock implements IBlockRender {

    public RedFlowerBlock(byte id) {
        super(id);
        this.name = "Red Flower";
        this.sound = "grass";
        this.texture = 22;
        this.transparent = true;
        this.solid = false;
        this.model = new VegetationModel(this.getTexture());
    }

    @Override
    public void render(ObjectList<Vector3i> position) {
        this.model.render(position);
    }
}
