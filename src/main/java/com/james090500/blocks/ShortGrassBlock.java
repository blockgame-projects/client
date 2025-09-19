package com.james090500.blocks;

import com.james090500.blocks.model.VegetationModel;
import com.james090500.textures.TextureLocation;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.joml.Vector3i;

public class ShortGrassBlock extends VegetationBlock implements IBlockRender {

    public ShortGrassBlock(byte id) {
        super(id);
        this.name = "Short Grass";
        this.sound = "grass";
        this.texture = TextureLocation.get("assets/foliage/short_grass");
        this.transparent = true;
        this.solid = false;
        this.model = new VegetationModel(this.uv, this.texture);
    }

    @Override
    public void render(ObjectList<Vector3i> positions) {
        this.model.render(positions);
    }
}
