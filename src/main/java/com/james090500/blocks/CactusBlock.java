package com.james090500.blocks;

import com.james090500.blocks.model.CactusModel;
import com.james090500.textures.TextureLocation;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.joml.Vector3i;

public class CactusBlock extends Block implements IBlockRender {

    public CactusBlock(byte id) {
        super(id);
        this.name = "Cactus";
        this.sound = "cloth";
        this.texture = TextureLocation.get("assets/blocks/cactus_side");
        this.model = new CactusModel();
    }

    @Override
    public TextureLocation getTexture(String face) {
        if (face.equalsIgnoreCase("top")) {
            return TextureLocation.get("assets/blocks/cactus_side_top");
        } else if (face.equalsIgnoreCase("bottom")) {
            return TextureLocation.get("assets/blocks/cactus_side_bottom");
        } else {
            return this.texture;
        }
    }

    @Override
    public void render(ObjectList<Vector3i> position) {
        this.model.render(position);
    }
}
