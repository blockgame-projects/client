package com.james090500.blocks;

import com.james090500.blocks.model.VegetationModel;
import com.james090500.textures.TextureLocation;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.joml.Vector3i;

public class RedFlowerBlock extends VegetationBlock implements IBlockRender {

    public RedFlowerBlock(byte id) {
        super(id);
        this.name = "Red Flower";
        this.sound = "grass";
        this.texture = TextureLocation.get("assets/foliage/red_flower");
        this.transparent = true;
        this.solid = false;
        this.model = new VegetationModel(this.uv);
    }

    @Override
    public void render(ObjectList<Vector3i> position) {
        this.model.render(position);
    }
}
